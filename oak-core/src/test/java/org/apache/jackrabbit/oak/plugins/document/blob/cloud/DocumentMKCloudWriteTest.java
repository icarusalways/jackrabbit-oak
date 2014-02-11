/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.document.blob.cloud;

import org.apache.jackrabbit.oak.plugins.blob.cloud.CloudBlobStore;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.MongoUtils;
import org.apache.jackrabbit.oak.plugins.document.blob.DocumentMKWriteTest;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Tests for {@code DocumentMK#write(java.io.InputStream)} with
 * {@link CloudBlobStore}
 */
public class DocumentMKCloudWriteTest extends DocumentMKWriteTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        try {
            Assume.assumeNotNull(CloudStoreUtils.getBlobStore());
        } catch (Exception e) {
            Assume.assumeNoException(e);
        }
    }

    @Override
    @Before
    public void setUpConnection() throws Exception {
        mongoConnection = MongoUtils.getConnection();
        MongoUtils.dropCollections(mongoConnection.getDB());
        mk = new DocumentMK.Builder().setMongoDB(mongoConnection.getDB())
                .setBlobStore(CloudStoreUtils.getBlobStore()).open();
    }

    @Override
    @After
    public void tearDownConnection() throws Exception {
        ((CloudBlobStore) mk.getNodeStore().getBlobStore()).deleteBucket();
        mk.dispose();
        // the db might already be closed
        mongoConnection.close();
        mongoConnection = MongoUtils.getConnection();
        MongoUtils.dropCollections(mongoConnection.getDB());
        mongoConnection.close();
    }
}