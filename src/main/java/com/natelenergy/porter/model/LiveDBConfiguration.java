package com.natelenergy.porter.model;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveDBConfiguration {
  
  @JsonProperty
  public int saveInterval = 6000; // 1 min

  @JsonProperty
  public String store = "file";

  @JsonProperty
  public String path = "liveDB";
  
  
  public StringStore create()
  {
    File dir = new File(this.path);
    return new StringStoreFile(dir);
  }
}
