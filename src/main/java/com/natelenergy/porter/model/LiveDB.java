package com.natelenergy.porter.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.base.Strings;

public class LiveDB {
  private long created;
  private long updated;
  private Map<String,Object> root;
  
  public LiveDB() {
    created = updated = System.currentTimeMillis();
    this.root = new ConcurrentHashMap<>();
  }
  
  public <T> T get(String path) {
    Object res = root;
    if(!Strings.isNullOrEmpty(path)) {
      for(String p : path.split("/")) {
        String key = p.trim();
        if(key.length()>0) {
          if(res instanceof Map) {
            res = ((Map)res).get(key);
          }
          else {
            throw new IllegalArgumentException("Not Found: "+key + " // " + path);
          }
        }
      }
    }
    return (T)res;
  }
  
  // Use a key with dots to set deeper elements
  public void set(String path, Map<String,Object> data) {
    boolean changed = false;
    try {
      // 1. Find the root structure
      Map<String,Object> root = this.root;
      if(!Strings.isNullOrEmpty(path)) {
        for(String p : path.split("/")) {
          String norm = p.trim();
          if(!Strings.isNullOrEmpty(norm)) {
            Object v = root.get(norm);
            if(v instanceof Map) {
              root = (Map<String,Object>)v;
            }
            else {
              // Replace everything that is not a map
              Map<String,Object> tmp = new ConcurrentHashMap<>();
              root.put(norm, tmp);
              root = tmp;
            }
          }
        }
      }
      
      // 2. Set each value at that level
      for(Map.Entry<String, Object> entry : data.entrySet()) {
        Object v = entry.getValue();
        if(v instanceof Map) {
          v = new ConcurrentHashMap<>((Map)v);
        }
        else if(v instanceof Collection) {
          v = new CopyOnWriteArrayList<>((Collection)v);
        }
        root.put(entry.getKey(), v);
      }
    }
    finally {
      if(changed) {
        this.updated = System.currentTimeMillis();
      }
    }
  }

  public long getLastModified() {
    return updated;
  }
}
