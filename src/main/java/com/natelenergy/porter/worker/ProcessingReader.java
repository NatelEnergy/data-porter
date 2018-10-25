package com.natelenergy.porter.worker;

import java.nio.file.Path;
import com.natelenergy.porter.processor.ValueProcessor;

public abstract class ProcessingReader {
  
  protected final Path file;
  
  public ProcessingReader(Path file) {
    this.file = file;
  }
  
  // This *can* get called multple times, so keep track of state outside
  public abstract long process(FileWorkerStatus status, ValueProcessor processor) throws Exception;
}
