package com.natelenergy.porter.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="@type")
public class DataRepoConfig {
  public int saveInterval = 30000; // Every 30 seconds
  
  public NamingConvention name;
  
  public List<ProcessorFactory> processors;
  
  public DataRepoConfig validate() {
    if(name==null) {
      name = new NamingConvention.Standard();
    }
    if(processors==null) {
      processors = new ArrayList<>();
    }
    return this;
  }
}
