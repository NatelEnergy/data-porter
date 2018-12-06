package com.natelenergy.porter.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Strings;
import com.natelenergy.porter.worker.ProcessorFactory;


public class SignalRepoConfig {
  public int saveInterval = 30000; // Every 30 seconds
  
  public NamingConvention name;
  
  public List<ProcessorFactory> processors;
  
  public SignalRepoConfig validate() {
    if(name==null) {
      name = new NamingConvention.Standard();
    }
    if(processors==null) {
      processors = new ArrayList<>();
    }
    
    // Add a unique ID to each processor if missing
    HashSet<String> ids = new HashSet<>();
    for(ProcessorFactory f : processors) {
      if(Strings.isNullOrEmpty(f.id)) {
        f.id = UUID.randomUUID().toString();
      }
      if(ids.contains(f.id)) {
        throw new IllegalArgumentException("Processor has duplicate ID: "+f.id);
      }
    }
    return this;
  }
}
