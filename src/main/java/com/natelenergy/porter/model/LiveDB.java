package com.natelenergy.porter.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.lang.model.SourceVersion;
import javax.ws.rs.NotFoundException;

import com.google.common.base.Strings;

public class LiveDB {
  private long created;
  private long updated;
  private Map<String,Object> root;
  
  public LiveDB() {
    created = updated = System.currentTimeMillis();
    this.root = new ConcurrentHashMap<>();
  }
  
  public Map<String,Object> get(String path) {
    Map<String,Object> res = root;
    if(!Strings.isNullOrEmpty(path)) {
      for(String p : path.split("/")) {
        Object v = res.get(p);
        if(v == null) {
          throw new NotFoundException();
        }
        if(v instanceof Map) {
          res = (Map<String,Object>)v;
        }
        else {
          throw new IllegalStateException("can not select a value diretly: "+p);
        }
      }
    }
    return res;
  }
  
  // Use a key with dots to set deeper elements
  public void set(String path, Map<String,Object> data) {
    boolean changed = false;
    try {
      Map<String,Object> root = this.root;
      if(!Strings.isNullOrEmpty(path)) {
        for(String p : path.split("/")) {
          Object v = root.get(p);
          if(v instanceof Map) {
            root = (Map<String,Object>)v;
          }
          else {
            // Replace everything tha tis not a map
            Map<String,Object> tmp = new ConcurrentHashMap<>();
            root.put(p, tmp);
            root = tmp;
          }
        }
      }
      
      for(Map.Entry<String, Object> entry : data.entrySet()) {
        if( !SourceVersion.isIdentifier(entry.getKey()) ) {
          throw new IllegalArgumentException( "Invalid field name" );
        }
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
