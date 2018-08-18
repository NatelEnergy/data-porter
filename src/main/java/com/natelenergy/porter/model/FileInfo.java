package com.natelenergy.porter.model;

public class FileInfo {
  public String source; // fs, s3, upload, etc..
  public String path;
  public Long length;
  public String checksum;
}
