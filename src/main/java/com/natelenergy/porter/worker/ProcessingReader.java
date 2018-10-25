package com.natelenergy.porter.worker;

import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.natelenergy.porter.processor.ValueProcessor;

@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="@type")
public abstract class ProcessingReader {
  
  protected final Path file;
  
  public ProcessingReader(Path file) {
    this.file = file;
  }
  
  // This *can* get called multple times, so keep track of state outside
  public abstract long process(FileWorkerStatus status, ValueProcessor processor) throws Exception;
}
