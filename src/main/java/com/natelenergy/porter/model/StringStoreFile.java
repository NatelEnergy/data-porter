package com.natelenergy.porter.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class StringStoreFile implements StringStore {
  final File root;
  
  public StringStoreFile(File f) {
    this.root = f;
    f.mkdirs();
  }

  @Override
  public String[] list() {
    List<String> names = new ArrayList<>();
    for(String n : this.root.list()) {
      if(n.endsWith(".json")) {
        names.add(n.substring(0, n.lastIndexOf('.')));
      }
    }
    return names.toArray(new String[names.size()]);
  }

  @Override
  public String read(String name, AtomicLong modified) throws IOException {
    File f = new File(this.root, name + ".json");
    if(modified!=null) {
      modified.set(f.lastModified());
    }
    return Files.asCharSource(f, Charsets.UTF_8).read();
  }

  @Override
  public void write(String name, String val) throws IOException {
    File f = new File(this.root, name + ".json");
    Files.asCharSink(f, Charsets.UTF_8 ).write(val);
  }
}

