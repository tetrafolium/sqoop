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

package org.apache.sqoop.hbase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.sqoop.testcategories.sqooptest.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * This tests to verify that HBase is present (default when running test cases)
 * and that when in fake not present mode, the method return false.
 */
@Category(UnitTest.class)
public class HBaseUtilTest {

@Test
public void testHBasePresent() {
	assertTrue(HBaseUtil.isHBaseJarPresent());
}

@Test
public void testHBaseNotPresent() {
	HBaseUtil.setAlwaysNoHBaseJarMode(true);
	boolean present = HBaseUtil.isHBaseJarPresent();
	HBaseUtil.setAlwaysNoHBaseJarMode(false);
	assertFalse(present);
}
}
