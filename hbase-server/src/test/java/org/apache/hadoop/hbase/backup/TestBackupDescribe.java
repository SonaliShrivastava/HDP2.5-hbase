/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.backup;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.backup.BackupInfo.BackupState;
import org.apache.hadoop.hbase.backup.impl.BackupSystemTable;
import org.apache.hadoop.hbase.testclassification.LargeTests;
import org.apache.hadoop.util.ToolRunner;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.Lists;

@Category(LargeTests.class)
public class TestBackupDescribe extends TestBackupBase {

  private static final Log LOG = LogFactory.getLog(TestBackupDescribe.class);

  /**
   * Verify that full backup is created on a single table with data correctly. Verify that describe
   * works as expected
   * @throws Exception
   */
  @Test
  public void testBackupDescribe() throws Exception {

    LOG.info("test backup describe on a single table with data");
    
    List<TableName> tableList = Lists.newArrayList(table1);
    String backupId = fullTableBackup(tableList);
    
    LOG.info("backup complete");
    assertTrue(checkSucceeded(backupId));


    BackupInfo info = getBackupAdmin().getBackupInfo(backupId);
    assertTrue(info.getState() == BackupState.COMPLETE);

  }

  @Test
  public void testBackupSetCommandWithNonExistentTable() throws Exception {
    String[] args = new String[]{"set", "add", "some_set", "table" }; 
    // Run backup
    int ret = ToolRunner.run(conf1, new BackupDriver(), args);
    assertNotEquals(ret, 0);
  }

  @Test
  public void testBackupDescribeCommand() throws Exception {

    LOG.info("test backup describe on a single table with data: command-line");
    
    List<TableName> tableList = Lists.newArrayList(table1);
    String backupId = fullTableBackup(tableList);
    
    LOG.info("backup complete");
    assertTrue(checkSucceeded(backupId));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    String[] args = new String[]{"describe",  backupId }; 
    // Run backup
    int ret = ToolRunner.run(conf1, new BackupDriver(), args);
    assertTrue(ret == 0);    
    String response = baos.toString();
    assertTrue(response.indexOf(backupId) > 0);
    assertTrue(response.indexOf("COMPLETE") > 0);

    BackupSystemTable table = new BackupSystemTable(TEST_UTIL.getConnection());
    BackupInfo status = table.readBackupInfo(backupId);
    String desc = status.getShortDescription();
    table.close();
    assertTrue(response.indexOf(desc) >= 0);

  }
  
  
}
