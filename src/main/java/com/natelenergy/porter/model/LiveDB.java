package com.natelenergy.porter.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class LiveDB extends StringBacked {  
  private Map<String,Object> root;
  
  public LiveDB(String name, StringStore store, StringBackedConfigSupplier cfg) {
    super(name, store, cfg );
    this.root = new ConcurrentHashMap<>();
    this.load();
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
          Map<?,?> m = (Map)v;
          v = new ConcurrentHashMap<>(m.size()+10);
          for (Map.Entry<?,?> e : m.entrySet()) {
            if(e.getKey()==null) {
              LOGGER.warn("Ignore null key: "+v);
            }
            else if(e.getValue()==null) {
              LOGGER.warn("Ignore null value: "+e.getKey() + " // " + data);
            }
            else {
              ((Map) v).put(e.getKey(), e.getValue());
            }
          }
        }
        else if(v instanceof Collection) {
          v = new CopyOnWriteArrayList<>((Collection)v);
        }
        Object old = root.get(entry.getKey());
        if(!Objects.equal(old, v)) {
          changed = true;
        }
        root.put(entry.getKey(), v);
      }
    }
    finally {
      if(changed) {
        super.updatedX();
      }
    }
  }

  @Override
  protected void load(String str) throws Exception {
    if(Strings.isNullOrEmpty(str)) {
      root.clear();
    }
    else {
      Map<String,Object> vals = config.getMapper().readValue(str, Map.class);
      if(vals != null) {
        this.root = new ConcurrentHashMap<>( vals );
      }  
    }
  }

  @Override
  protected String getSaveString() throws JsonProcessingException {
    return config.getMapper().writeValueAsString(root);
  }
}
