package com.natelenergy.porter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.natelenergy.porter.processor.FileNameInfo;

@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="@type")
public abstract class NamingConvention {
  public abstract FileNameInfo read(String path);
  
  public static class Standard extends NamingConvention {
    @Override
    public FileNameInfo read(String path) {
      int idx = path.lastIndexOf('/');
      if(idx>0){
        path = path.substring(idx+1);
      }
      idx = path.indexOf('.');
      if(idx>0){
        path = path.substring(0, idx);
      }
      String[] v = path.split("-");
      if(v.length==3) {
        return new FileNameInfo(v[0], v[1],v[2]);
      }
      return null;
    }
  }
}
