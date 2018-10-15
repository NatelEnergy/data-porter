package com.natelenergy.porter.model;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.MoreFiles;

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
  
  public static FileUploadInfo make(Path f, Path root, boolean hash) throws IOException
  {
    FileUploadInfo info = new FileUploadInfo();

    if(root != null) {
      info.path = root.relativize(f).toString().replace('\\', '/');
    }
    else {
      info.name = f.getFileName().toString();
    }
    
    if(Files.exists(f)) {
      BasicFileAttributes attrs = Files.readAttributes(f, BasicFileAttributes.class);
      info.size = attrs.size();
      info.mtime = attrs.lastModifiedTime().toMillis();
      
      if(attrs.isDirectory()) {
        info.type = "directory";
      }
      else if(hash) {
        HashCode md5 = MoreFiles.asByteSource(f).hash(Hashing.md5());
        info.md5 = md5.toString();
      }
    }
    else {
      info.exists = false;
    }
    return info;
  }

  public static List<FileUploadInfo> list(Path dir) throws IOException {
    List<FileUploadInfo> infos = new ArrayList<>();
    try (DirectoryStream<Path> dirs = Files.newDirectoryStream(dir)) {
      for (Path sub : dirs) {
        infos.add(make(sub, null, false));
      }
    } catch (IOException ex) {}
    return infos;
  }
}
