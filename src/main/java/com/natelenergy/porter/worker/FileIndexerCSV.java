package com.natelenergy.porter.worker;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

public class FileIndexerCSV extends FileIndexer {
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  long lastSize = 0;
  
  public FileIndexerCSV(Path file) {
    super(file);
  }

  @Override
  public long process(FileWorkerStatus status) throws Exception {
    if(!Files.exists(this.file)) {
      return 0;
    }
    
    long count = 0;
    BasicFileAttributes attrs = Files.readAttributes(this.file, BasicFileAttributes.class);
    if(attrs.isRegularFile() && attrs.size() > lastSize) {
      status.cursor = lastSize = attrs.size();
      try (CSVReader reader = new CSVReader(Files.newBufferedReader(this.file, Charsets.UTF_8)))
      {
        String[] line = reader.readNext();
        while(line != null) {
          
          line = reader.readNext();
        }
        attrs = Files.readAttributes(this.file, BasicFileAttributes.class);
        status.cursor = lastSize = attrs.size();
      }
    }
    return count;
  }
}
