/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.hydrator.plugin.source;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Tests for {@link S3BatchSource} configuration.
 */
public class S3BatchSourceConfigTest {
  private static final Gson GSON = new Gson();
  private static final Type MAP_STRING_STRING_TYPE = new TypeToken<Map<String, String>>() {
  }.getType();

  @Test
  public void testFileSystemProperties() {
    String accessID = "accessID";
    String accessKey = "accessKey";
    String authenticationMethod = "Access Credentials";
    String path = "s3a://logs/";
    // Test default properties
    S3BatchSource.S3BatchConfig s3BatchConfig = new S3BatchSource.S3BatchConfig(path, accessID, accessKey,
                                                                                authenticationMethod);
    Map<String, String> fsProperties = s3BatchConfig.getFileSystemProperties();
    Assert.assertNotNull(fsProperties);
    Assert.assertEquals(2, fsProperties.size());
    Assert.assertEquals(accessID, fsProperties.get("fs.s3a.access.key"));
    Assert.assertEquals(accessKey, fsProperties.get("fs.s3a.secret.key"));


    // Test extra properties
    s3BatchConfig = new S3BatchSource.S3BatchConfig(path, accessID, accessKey, authenticationMethod,
                                                    ImmutableMap.of("s3.compression", "gzip"));
    fsProperties = s3BatchConfig.getFileSystemProperties();
    Assert.assertNotNull(fsProperties);
    Assert.assertEquals(3, fsProperties.size());
    Assert.assertEquals(accessID, fsProperties.get("fs.s3a.access.key"));
    Assert.assertEquals(accessKey, fsProperties.get("fs.s3a.secret.key"));
    Assert.assertEquals("gzip", fsProperties.get("s3.compression"));
  }

  @Test
  public void testFileSystemPropertiesS3N() {
    String accessID = "accessID";
    String accessKey = "accessKey";
    String authenticationMethod = "Access Credentials";
    String path = "s3n://logs/";
    // Test default properties
    S3BatchSource.S3BatchConfig s3BatchConfig = new S3BatchSource.S3BatchConfig(path, accessID, accessKey,
                                                                                authenticationMethod);
    Map<String, String> fsProperties = s3BatchConfig.getFileSystemProperties();
    Assert.assertNotNull(fsProperties);
    Assert.assertEquals(2, fsProperties.size());
    Assert.assertEquals(accessID, fsProperties.get("fs.s3n.awsAccessKeyId"));
    Assert.assertEquals(accessKey, fsProperties.get("fs.s3n.awsSecretAccessKey"));


    // Test extra properties
    s3BatchConfig = new S3BatchSource.S3BatchConfig(path, accessID, accessKey, authenticationMethod,
                                                    ImmutableMap.of("s3.compression", "gzip"));
    fsProperties = s3BatchConfig.getFileSystemProperties();
    Assert.assertNotNull(fsProperties);
    Assert.assertEquals(3, fsProperties.size());
    Assert.assertEquals(accessID, fsProperties.get("fs.s3n.awsAccessKeyId"));
    Assert.assertEquals(accessKey, fsProperties.get("fs.s3n.awsSecretAccessKey"));
    Assert.assertEquals("gzip", fsProperties.get("s3.compression"));
  }

  @Test
  public void testFileSystemPropertiesMacro() {
    String accessID = "accessID";
    String accessKey = "accessKey";
    String authenticationMethod = "Access Credentials";
    String path = "${my-macro}";
    // Test default properties
    S3BatchSource.S3BatchConfig s3BatchConfig = new S3BatchSource.S3BatchConfig(path, accessID, accessKey,
                                                                                authenticationMethod);
    Map<String, String> fsProperties = s3BatchConfig.getFileSystemProperties();
    Assert.assertNotNull(fsProperties);
    Assert.assertEquals(0, fsProperties.size());
  }

  @Test
  public void testFileSystemPropertiesForIAM() {
    String authenticationMethod = "IAM";
    S3BatchSource.S3BatchConfig s3BatchConfig = new S3BatchSource.S3BatchConfig("", null, null,
                                                                                authenticationMethod);
    Map<String, String> fsProperties = s3BatchConfig.getFileSystemProperties();
    Assert.assertEquals(0, fsProperties.size());
  }
}
