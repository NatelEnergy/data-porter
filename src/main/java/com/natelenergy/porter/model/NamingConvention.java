package com.natelenergy.porter.model;

import com.natelenergy.porter.processor.FileNameInfo;

public interface NamingConvention {
  public FileNameInfo read(String path);
  
  public static class Standard implements NamingConvention {
    public String pattern = "{site}-{date}-{channel}";
    
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
