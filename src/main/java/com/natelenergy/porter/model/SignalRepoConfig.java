package com.natelenergy.porter.model;

import java.util.ArrayList;
import java.util.List;


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
    return this;
  }
}
