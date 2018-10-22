package com.natelenergy.porter.worker;

public class FileNameInfo {
  public final String db;
  public final String table;
  public final String date;
  
  public FileNameInfo(String d, String t, String table) {
    this.db = d;
    this.date = t;
    this.table = table;
  }
  
  public static FileNameInfo parse(String name)
  {
    int idx = name.lastIndexOf('/');
    if(idx>0){
      name = name.substring(idx+1);
    }
    idx = name.indexOf('.');
    if(idx>0){
      name = name.substring(0, idx);
    }
    String[] v = name.split("-");
    if(v.length==3) {
      return new FileNameInfo(v[0], v[1],v[2]);
    }
    return null;
  }
}
