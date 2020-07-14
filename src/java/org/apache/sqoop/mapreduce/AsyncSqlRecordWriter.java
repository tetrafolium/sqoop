/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sqoop.mapreduce;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.sqoop.lib.SqoopRecord;
import org.apache.sqoop.mapreduce.db.DBConfiguration;
import org.apache.sqoop.util.LoggingUtils;

/**
 * Abstract RecordWriter base class that buffers SqoopRecords to be injected
 * into JDBC SQL PreparedStatements to be executed by the
 * AsyncSqlOutputFormat's background thread.
 *
 * Record objects are buffered before actually performing the INSERT
 * statements; this requires that the key implement the SqoopRecord interface.
 *
 * Uses DBOutputFormat/DBConfiguration for configuring the output.
 */
public abstract class AsyncSqlRecordWriter<K extends SqoopRecord, V>
    extends RecordWriter<K, V> {

  private static final Log LOG = LogFactory.getLog(AsyncSqlRecordWriter.class);

  private Connection connection;

  private Configuration conf;

  protected final int rowsPerStmt; // rows to insert per statement.

  // Buffer for records to be put into export SQL statements.
  private List<SqoopRecord> records;

  // Background thread to actually perform the updates.
  private AsyncSqlOutputFormat.AsyncSqlExecThread execThread;
  private boolean startedExecThread;

  private boolean closed;

  public AsyncSqlRecordWriter(TaskAttemptContext context)
      throws ClassNotFoundException, SQLException {
    this.conf = context.getConfiguration();

    this.rowsPerStmt =
        conf.getInt(AsyncSqlOutputFormat.RECORDS_PER_STATEMENT_KEY,
                    AsyncSqlOutputFormat.DEFAULT_RECORDS_PER_STATEMENT);
    int stmtsPerTx =
        conf.getInt(AsyncSqlOutputFormat.STATEMENTS_PER_TRANSACTION_KEY,
                    AsyncSqlOutputFormat.DEFAULT_STATEMENTS_PER_TRANSACTION);

    DBConfiguration dbConf = new DBConfiguration(conf);
    this.connection = dbConf.getConnection();
    this.connection.setAutoCommit(false);

    this.records = new ArrayList<SqoopRecord>(this.rowsPerStmt);

    this.execThread =
        new AsyncSqlOutputFormat.AsyncSqlExecThread(connection, stmtsPerTx);
    this.execThread.setDaemon(true);
    this.startedExecThread = false;

    this.closed = false;
  }

  /**
   * Allow subclasses access to the Connection instance we hold.
   * This Connection is shared with the asynchronous SQL exec thread.
   * Any uses of the Connection must be synchronized on it.
   * @return the Connection object used for this SQL transaction.
   */
  protected final Connection getConnection() { return this.connection; }

  /**
   * Allow subclasses access to the Configuration.
   * @return the Configuration for this MapReduc task.
   */
  protected final Configuration getConf() { return this.conf; }

  /**
   * Should return 'true' if the PreparedStatements generated by the
   * RecordWriter are intended to be executed in "batch" mode, or false
   * if it's just one big statement.
   */
  protected boolean isBatchExec() { return false; }

  /**
   * Generate the PreparedStatement object that will be fed into the execution
   * thread. All parameterized fields of the PreparedStatement must be set in
   * this method as well; this is usually based on the records collected from
   * the user in the userRecords list.
   *
   * Note that any uses of the Connection object here must be synchronized on
   * the Connection.
   *
   * @param userRecords a list of records that should be injected into SQL
   * statements.
   * @return a PreparedStatement to be populated with rows
   * from the collected record list.
   */
  protected abstract PreparedStatement
  getPreparedStatement(List<SqoopRecord> userRecords) throws SQLException;

  /**
   * Takes the current contents of 'records' and formats and executes the
   * INSERT statement.
   * @param closeConn if true, commits the transaction and closes the
   * connection.
   */
  private void execUpdate(boolean commit, boolean stopThread)
      throws InterruptedException, SQLException {

    if (!startedExecThread) {
      this.execThread.start();
      this.startedExecThread = true;
    }

    PreparedStatement stmt = null;
    boolean successfulPut = false;
    try {
      if (records.size() > 0) {
        stmt = getPreparedStatement(records);
        this.records.clear();
      }

      // Pass this operation off to the update thread. This will block if
      // the update thread is already performing an update.
      AsyncSqlOutputFormat.AsyncDBOperation op =
          new AsyncSqlOutputFormat.AsyncDBOperation(stmt, isBatchExec(), commit,
                                                    stopThread);
      execThread.put(op);
      successfulPut = true; // op has been posted to the other thread.
    } finally {
      if (!successfulPut && null != stmt) {
        // We created a statement but failed to enqueue it. Close it.
        stmt.close();
      }
    }

    // Check for any previous SQLException. If one happened, rethrow it here.
    SQLException lastException = execThread.getLastError();
    if (null != lastException) {
      LoggingUtils.logAll(LOG, lastException);
      throw lastException;
    }
  }

  @Override
  /** {@inheritDoc} */
  public void close(TaskAttemptContext context)
      throws IOException, InterruptedException {
    // If any exception is thrown out in this method, mapreduce framework
    // catches the exception and calls this method again in case the recorder
    // hasn't bee closed properly. Without the protection below, it can make the
    // main thread stuck in execThread.put since there is no receiver for the
    // synchronous queue any more.
    if (closed) {
      return;
    }
    closed = true;

    try {
      try {
        execUpdate(true, true);
        execThread.join();
      } catch (SQLException sqle) {
        throw new IOException(sqle);
      }

      // If we're not leaving on an error return path already,
      // now that execThread is definitely stopped, check that the
      // error slot remains empty.
      SQLException lastErr = execThread.getLastError();
      if (null != lastErr) {
        throw new IOException(lastErr);
      }
    } finally {
      try {
        closeConnection(context);
      } catch (SQLException sqle) {
        throw new IOException(sqle);
      }
    }
  }

  public void closeConnection(TaskAttemptContext context) throws SQLException {
    this.connection.close();
  }

  @Override
  /** {@inheritDoc} */
  public void write(K key, V value) throws InterruptedException, IOException {
    try {
      records.add((SqoopRecord)key.clone());
      if (records.size() >= this.rowsPerStmt) {
        execUpdate(false, false);
      }
    } catch (CloneNotSupportedException cnse) {
      throw new IOException("Could not buffer record", cnse);
    } catch (SQLException sqlException) {
      throw new IOException(sqlException);
    }
  }
}
