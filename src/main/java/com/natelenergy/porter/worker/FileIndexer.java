package com.natelenergy.porter.worker;

import java.nio.file.Path;

public abstract class FileIndexer {
  
  protected final Path file;
  
  public FileIndexer(Path file) {
    this.file = file;
  }
  
  // This *can* get called multple times, so keep track of state outside
  public abstract long process(FileWorkerStatus status) throws Exception;
}
