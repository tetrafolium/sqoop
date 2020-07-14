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

package org.apache.sqoop.manager.postgresql;

import static org.apache.sqoop.manager.postgresql.PostgresqlTestUtil.CONNECT_STRING;
import static org.apache.sqoop.manager.postgresql.PostgresqlTestUtil.DATABASE_USER;
import static org.apache.sqoop.manager.postgresql.PostgresqlTestUtil.PASSWORD;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.JobConf;
import org.apache.sqoop.TestExport;
import org.apache.sqoop.mapreduce.db.DBConfiguration;
import org.apache.sqoop.testcategories.sqooptest.ManualTest;
import org.apache.sqoop.testcategories.thirdpartytest.PostgresqlTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test the DirectPostgresqlManager implementations.
 * DirectPostgresqlManager uses JDBC driver to facilitate it.
 *
 * Since this requires a Postgresql installation on your local machine to use,
 * this class is named in such a way that Hadoop's default QA process does not
 * run it.
 *
 * You need to run this manually with
 * -Dtestcase=DirectPostgreSQLExportManualTest.
 *
 * You need to put Postgresql's JDBC driver library into lib dir.
 *
 * You need to create a sqooptest superuser and database and tablespace,
 *
 * $ sudo -u postgres createuser -U postgres -s sqooptest
 * $ sudo -u postgres createdb -U sqooptest sqooptest
 * $ psql -U sqooptest sqooptest
 *
 */
@Category({ManualTest.class, PostgresqlTest.class})
public class DirectPostgreSQLExportManualTest extends TestExport {

public static final Log LOG =
	LogFactory.getLog(DirectPostgreSQLExportManualTest.class.getName());
private DBConfiguration dbConf;

public DirectPostgreSQLExportManualTest() {
	JobConf conf = new JobConf(getConf());
	DBConfiguration.configureDB(conf, "org.postgresql.Driver",
	                            getConnectString(), getUserName(), PASSWORD,
	                            (Integer)null);
	dbConf = new DBConfiguration(conf);
}

@Override
protected boolean useHsqldbTestServer() {
	return false;
}

@Override
protected String getConnectString() {
	return CONNECT_STRING;
}

protected String getUserName() {
	return DATABASE_USER;
}

@Override
protected String getTablePrefix() {
	return super.getTablePrefix().toLowerCase();
}

@Override
protected String getTableName() {
	return super.getTableName().toLowerCase();
}

@Override
public String getStagingTableName() {
	return super.getStagingTableName().toLowerCase();
}

@Override
protected Connection getConnection() {
	try {
		Connection conn = dbConf.getConnection();
		conn.setAutoCommit(false);
		PreparedStatement stmt =
			conn.prepareStatement("SET extra_float_digits TO 0");
		stmt.executeUpdate();
		conn.commit();
		return conn;
	} catch (SQLException sqlE) {
		LOG.error("Could not get connection to test server: " + sqlE);
		return null;
	} catch (ClassNotFoundException cnfE) {
		LOG.error("Could not find driver class: " + cnfE);
		return null;
	}
}

@Override
protected String getDropTableStatement(String tableName) {
	return "DROP TABLE IF EXISTS " + tableName;
}

@Override
protected String[] getArgv(boolean includeHadoopFlags, int rowsPerStatement,
                           int statementsPerTx, String... additionalArgv) {
	ArrayList<String> args =
		new ArrayList<String>(Arrays.asList(additionalArgv));
	args.add("--username");
	args.add(getUserName());
	args.add("--password");
	args.add(PASSWORD);
	args.add("--direct");
	return super.getArgv(includeHadoopFlags, rowsPerStatement, statementsPerTx,
	                     args.toArray(new String[0]));
}

@Override
protected String[] getCodeGenArgv(String... extraArgs) {
	ArrayList<String> args = new ArrayList<String>(Arrays.asList(extraArgs));
	args.add("--username");
	args.add(getUserName());
	args.add("--password");
	args.add(PASSWORD);
	return super.getCodeGenArgv(args.toArray(new String[0]));
}

@Ignore(
	"Ignoring this test case as direct export does not support --columns option.")
@Override
@Test
public void
testColumnsExport() throws IOException, SQLException {
}

@Ignore(
	"Ignoring this test case as the scenario is not supported with direct export.")
@Override
@Test
public void
testLessColumnsInFileThanInTable() throws IOException, SQLException {
}

@Ignore(
	"Ignoring this test case as the scenario is not supported with direct export.")
@Override
@Test
public void
testLessColumnsInFileThanInTableInputNullIntPassed()
throws IOException, SQLException {
}

@Ignore(
	"Ignoring this test case as the scenario is not supported with direct export.")
@Override
@Test
public void
testLessColumnsInFileThanInTableInputNullStringPassed()
throws IOException, SQLException {
}
}
