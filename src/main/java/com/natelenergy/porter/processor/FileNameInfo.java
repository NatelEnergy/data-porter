package com.natelenergy.porter.processor;

public class FileNameInfo {
  public final String site;
  public final String channel;
  public final String date;
  
  public FileNameInfo(String d, String t, String table) {
    this.site = d;
    this.date = t;
    this.channel = table;
  }
}
