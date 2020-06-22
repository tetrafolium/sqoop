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

package org.apache.sqoop.manager.oracle;

import static org.junit.Assert.assertEquals;

import org.apache.hadoop.conf.Configuration;
import org.apache.sqoop.manager.oracle.OraOopConstants.OraOopOracleDataChunkMethod;
import org.apache.sqoop.manager.oracle.util.OracleUtils;
import org.apache.sqoop.testcategories.thirdpartytest.OracleEeTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test import data from Oracle.
 */
@Category(OracleEeTest.class)
public class ImportTest extends OraOopTestCase {

private static final boolean DISABLE_ORACLE_ESCAPING_FLAG = false;

@Test
public void testProductImport() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product");
	createTable("table_tst_product.xml");

	try {
		int retCode = runImport("TST_PRODUCT", getSqoopConf(), false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductWithWhiteSpaceImport() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product");
	createTable("table_tst_product_with_white_space.xml");

	try {
		int retCode = runImport("TST_Pr OdUCT", getSqoopConf(), false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductPartImport() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product_part");
	createTable("table_tst_product_part.xml");

	try {
		int retCode = runImport("TST_PRODUCT_PART", getSqoopConf(), false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductPartImportPartitionChunk() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product_part");
	createTable("table_tst_product_part.xml");

	Configuration sqoopConf = getSqoopConf();
	sqoopConf.set(
		OraOopConstants.ORAOOP_ORACLE_DATA_CHUNK_METHOD,
		OraOopConstants.OraOopOracleDataChunkMethod.PARTITION.toString());

	try {
		int retCode = runImport("TST_PRODUCT_PART", sqoopConf, false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductPartImportSubset() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product_part");
	createTable("table_tst_product_part.xml");

	Configuration sqoopConf = getSqoopConf();
	sqoopConf.set(OraOopConstants.ORAOOP_ORACLE_DATA_CHUNK_METHOD,
	              OraOopOracleDataChunkMethod.ROWID.toString());
	sqoopConf.set(
		OraOopConstants.ORAOOP_IMPORT_PARTITION_LIST,
		"tst_product_part_1,tst_product_part_2,\"tst_product_pa#rt_6\"");

	try {
		int retCode = runImport("TST_PRODUCT_PART", sqoopConf, false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductPartImportSubsetPartitionChunk() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product_part");
	createTable("table_tst_product_part.xml");

	Configuration sqoopConf = getSqoopConf();
	sqoopConf.set(OraOopConstants.ORAOOP_ORACLE_DATA_CHUNK_METHOD,
	              OraOopOracleDataChunkMethod.PARTITION.toString());
	sqoopConf.set(OraOopConstants.ORAOOP_IMPORT_PARTITION_LIST,
	              "tst_product_part_1,tst_product_part_2,"
	              + "tst_product_part_3,\"tst_product_pa#rt_6\"");

	try {
		int retCode = runImport("TST_PRODUCT_PART", sqoopConf, false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductSubPartImport() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product_subpart");
	createTable("table_tst_product_subpart.xml");

	try {
		int retCode = runImport("TST_PRODUCT_SUBPART", getSqoopConf(), false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductSubPartImportPartitionChunk() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product_subpart");
	createTable("table_tst_product_subpart.xml");

	Configuration sqoopConf = getSqoopConf();
	sqoopConf.set(
		OraOopConstants.ORAOOP_ORACLE_DATA_CHUNK_METHOD,
		OraOopConstants.OraOopOracleDataChunkMethod.PARTITION.toString());

	try {
		int retCode = runImport("TST_PRODUCT_SUBPART", sqoopConf, false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductSubPartImportSubset() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product_subpart");
	createTable("table_tst_product_subpart.xml");

	Configuration sqoopConf = getSqoopConf();
	sqoopConf.set(OraOopConstants.ORAOOP_ORACLE_DATA_CHUNK_METHOD,
	              OraOopOracleDataChunkMethod.ROWID.toString());
	sqoopConf.set(OraOopConstants.ORAOOP_IMPORT_PARTITION_LIST,
	              "TST_PRODUCT_PART_1,TST_PRODUCT_PART_2,"
	              + "TST_PRODUCT_PART_3,TST_PRODUCT_PART_4");

	try {
		int retCode = runImport("TST_PRODUCT_SUBPART", sqoopConf, false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductSubPartImportSubsetPartitionChunk() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product_subpart");
	createTable("table_tst_product_subpart.xml");

	Configuration sqoopConf = getSqoopConf();
	sqoopConf.set(
		OraOopConstants.ORAOOP_ORACLE_DATA_CHUNK_METHOD,
		OraOopConstants.OraOopOracleDataChunkMethod.PARTITION.toString());
	sqoopConf.set(OraOopConstants.ORAOOP_IMPORT_PARTITION_LIST,
	              "TST_PRODUCT_PART_1,TST_PRODUCT_PART_2,TST_PRODUCT_PART_3");

	try {
		int retCode = runImport("TST_PRODUCT_SUBPART", sqoopConf, false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductImportConsistentRead() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product");
	createTable("table_tst_product.xml");

	// Make sure Oracle SCN has updated since creating table
	Thread.sleep(10000);

	Configuration sqoopConf = getSqoopConf();
	sqoopConf.setBoolean(OraOopConstants.ORAOOP_IMPORT_CONSISTENT_READ, true);

	try {
		int retCode = runImport("TST_PRODUCT", sqoopConf, false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductImportMixedCaseTableName() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "T5+_Pr#duct");
	createTable("table_tst_product_special_chars.xml");

	try {
		int retCode = runImport("\"\"T5+_Pr#duct\"\"", getSqoopConf(), false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

@Test
public void testProductPartIotImport() throws Exception {
	setSqoopTargetDirectory(getSqoopTargetDirectory() + "tst_product_part");
	createTable("table_tst_product_part_iot.xml");

	Configuration sqoopConf = getSqoopConf();
	sqoopConf.set(
		OraOopConstants.ORAOOP_ORACLE_DATA_CHUNK_METHOD,
		OraOopConstants.OraOopOracleDataChunkMethod.PARTITION.toString());

	try {
		int retCode = runImport("TST_PRODUCT_PART_IOT", sqoopConf, false,
		                        DISABLE_ORACLE_ESCAPING_FLAG);
		assertEquals("Return code should be 0", 0, retCode);

	} finally {
		cleanupFolders();
		closeTestEnvConnection();
	}
}

protected String getConnectString() {
	return OracleUtils.EE_CONNECT_STRING;
}

protected String getUsername() {
	return OracleUtils.ORACLE_EE_USER_NAME;
}

protected String getPassword() {
	return OracleUtils.ORACLE_EE_USER_PASS;
}
}
