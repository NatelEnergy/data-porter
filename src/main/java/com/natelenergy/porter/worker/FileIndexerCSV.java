package com.natelenergy.porter.worker;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileIndexerCSV extends FileIndexer {
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  long lastLine = -1;
  
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
    if(attrs.isRegularFile() && attrs.size() > lastLine) {
      Thread.sleep(5000);      
      LOGGER.info("TODO, read CSV! "+this.file );
    }
    return count;
  }
}
