package com.natelenergy.porter.worker;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableFileInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileIndexerAvro extends FileIndexer {
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
  long lastSync = 0;
  
  public FileIndexerAvro(Path file) {
    super(file);

  }

  @Override
  public long process(FileWorkerStatus status) throws IOException {
    if(!Files.exists(this.file)) {
      return 0;
    }

    long count = 0;
    BasicFileAttributes attrs = Files.readAttributes(this.file, BasicFileAttributes.class);
    if(attrs.isRegularFile() && attrs.size() > lastSync) { // Must have a file size
      long total = 0;
      if(status.count!=null) {
        total = status.count;
      }
      
      GenericRecord record = null;
      try( 
        SeekableFileInput sin = new SeekableFileInput(this.file.toFile());
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(sin, datumReader) 
      ){
        // Schema schema = dataFileReader.getSchema();
        if(lastSync>0) {
          dataFileReader.seek(lastSync);
          dataFileReader.previousSync();
        }
        
        while (dataFileReader.hasNext()) {
          record = dataFileReader.next(record);
          long when = (long)record.get("epoch");
          count++;
          status.count = total++;
          status.time = when;
          if(total % 1000 == 0) {
            LOGGER.info("indexing: "+this.file + " // Count:"+status.count + " // time:"+when );
          }
        }
        lastSync = dataFileReader.previousSync();
      }
    }
    return count;
  }
}
