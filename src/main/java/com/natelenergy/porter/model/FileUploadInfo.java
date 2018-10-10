package com.natelenergy.porter.model;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

/**
 * This vaguely matches the NGINX json output
 */
@JsonInclude(Include.NON_NULL)
public class FileUploadInfo {
  public String name;
  public String path;
  public String type;
  public Boolean exists;
  public long size;
  public long mtime;
  public String md5;
  
  public static FileUploadInfo make(File f, String root, boolean hash, boolean showPath) throws IOException
  {
    FileUploadInfo info = new FileUploadInfo();
    info.size = f.length();
    
    if(showPath) {
      String abs = f.getAbsolutePath();
      if(abs.length()>root.length()) {
        info.path = f.getAbsolutePath().substring(root.length()+1).replace('\\', '/');
      }
    }
    else {
      info.name = f.getName();
    }
    
    if(f.exists()) {
      info.mtime = f.lastModified();
      if(f.isDirectory()) {
        info.type = "directory";
      }
      else if(hash) {
        HashCode md5 = Files.asByteSource(f).hash(Hashing.md5());
        info.md5 = md5.toString();
      }
    }
    else {
      info.exists = false;
    }
    return info;
  }

  public static FileUploadInfo[] list(File dir) throws IOException {
    File[] files = dir.listFiles();
    FileUploadInfo[] info = new FileUploadInfo[files.length];
    for(int i=0; i<files.length; i++) {
      info[i] = make(files[i], "", false, false);
    }
    return info;
  }
}
