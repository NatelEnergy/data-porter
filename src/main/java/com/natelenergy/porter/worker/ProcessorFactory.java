package com.natelenergy.porter.worker;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.natelenergy.porter.model.FileNameInfo;
import com.natelenergy.porter.model.ValueProcessor;

@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="@type")
public abstract class ProcessorFactory {
  
  public static class PathMatcher {
    public String channel;
    
    public boolean matches(FileNameInfo p) {
      return p.channel.equals(channel);
    }
  }

  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  public String id;
  public PathMatcher match;
  public boolean onlyProcessor = false;

  public final ValueProcessor create(String repo, Path path, FileNameInfo info) {
    if(this.match!= null) {
      if(!this.match.matches(info)) {
        return null;
      }
    }
    return doCreate(repo, path, info);
  }

  protected abstract ValueProcessor doCreate(String repo, Path path, FileNameInfo info);
  
  /**
   * This is the status returned by the API 
   */
  @JsonIgnore
  public Object getStatus(String repo) {
    return this;
  }
  
}
