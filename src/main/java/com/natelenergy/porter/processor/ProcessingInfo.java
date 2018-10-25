package com.natelenergy.porter.processor;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.influxdb.InfluxDB;

import com.natelenergy.porter.worker.ProcessingReader;

public class ProcessingInfo implements Supplier<ValueProcessor> {
  public String path;
  public Path file;
  public String siteKey; 
  
  public InfluxDB influx;
  public LastValueDB last;
  
  public boolean streaming;
  public FileNameInfo info;
  
  public ProcessingReader reader;
  public ValueProcessor processor;
  
  @Override
  public ValueProcessor get() {
    return processor;
  }
}
