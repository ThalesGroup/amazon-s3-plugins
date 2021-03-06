/*
 * Copyright © 2015, 2016 Cask Data, Inc.
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

package co.cask.hydrator.plugin.sink;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.cdap.api.data.batch.OutputFormatProvider;
import co.cask.cdap.api.data.format.StructuredRecord;
import co.cask.cdap.api.dataset.lib.KeyValue;
import co.cask.cdap.etl.api.Emitter;
import co.cask.cdap.etl.api.batch.BatchRuntimeContext;
import co.cask.cdap.etl.api.batch.BatchSinkContext;
import com.google.common.collect.Maps;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import parquet.avro.AvroParquetOutputFormat;

import java.text.SimpleDateFormat;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * {@link S3ParquetBatchSink} that stores data in parquet format to S3.
 */
@Plugin(type = "batchsink")
@Name("S3Parquet")
@Description("Batch sink to write to Amazon S3 in Parquet format.")
public class S3ParquetBatchSink extends S3BatchSink<Void, GenericRecord> {

  private StructuredToAvroTransformer recordTransformer;
  private final S3ParquetSinkConfig config;
  private static final String PARQUET_AVRO_SCHEMA = "parquet.avro.schema";

  public S3ParquetBatchSink(S3ParquetSinkConfig config) {
    super(config);
    this.config = config;
  }

  @Override
  public void initialize(BatchRuntimeContext context) throws Exception {
    super.initialize(context);
    recordTransformer = new StructuredToAvroTransformer(config.schema);
  }

  @Override
  protected OutputFormatProvider createOutputFormatProvider(BatchSinkContext context) {
    return new S3ParquetOutputFormatProvider(config, context);
  }

  @Override
  public void transform(StructuredRecord input,
                        Emitter<KeyValue<Void, GenericRecord>> emitter) throws Exception {
    emitter.emit(new KeyValue<Void, GenericRecord>(null, recordTransformer.transform(input)));
  }

  /**
   * Configuration for the S3ParquetConfig.
   */
  public static class S3ParquetSinkConfig extends S3BatchSinkConfig {

    @Nullable
    @Description("Used to specify the compression codec to be used for the final dataset.")
    private String compressionCodec;

    @SuppressWarnings("unused")
    public S3ParquetSinkConfig() {
      super();
    }

    @SuppressWarnings("unused")
    public S3ParquetSinkConfig(String referenceName, String basePath, String accessID, String accessKey,
                               String pathFormat, String fileSystemProperties, String compressionCodec,
                               String authenticationMethod, String enableEncryption) {
      super(referenceName, basePath, accessID, accessKey, pathFormat, fileSystemProperties, authenticationMethod,
            enableEncryption);
      this.compressionCodec = compressionCodec;
    }
  }

  /**
   * Output format provider that sets parquet output format to be use in MapReduce.
   */
  public static class S3ParquetOutputFormatProvider implements OutputFormatProvider {

    private final Map<String, String> conf;

    public S3ParquetOutputFormatProvider(S3ParquetSinkConfig config, BatchSinkContext context) {
      @SuppressWarnings("ConstantConditions")
      SimpleDateFormat format = new SimpleDateFormat(config.pathFormat);

      conf = Maps.newHashMap();
      conf.put(PARQUET_AVRO_SCHEMA, config.schema);
      conf.putAll(FileSetUtil.getParquetCompressionConfiguration(config.compressionCodec, config.schema, true));
      conf.put(FileOutputFormat.OUTDIR,
               String.format("%s/%s", config.basePath, format.format(context.getLogicalStartTime())));
    }

    @Override
    public String getOutputFormatClassName() {
      return AvroParquetOutputFormat.class.getName();
    }

    @Override
    public Map<String, String> getOutputFormatConfiguration() {
      return conf;
    }
  }
}
