package com.natelenergy.porter.model;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;

public class LiveDB {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  
  private final String name;
  private final StringStore file;
  
  private long updated;

  private long saved = -1;
  private int saveInterval = 60000; // 1min
  
  private Map<String,Object> root;
  private ObjectMapper mapper = null;
  private ScheduledFuture<?> saver = null;
  
  public LiveDB(String name, StringStore file, int saveInterval) {
    updated = System.currentTimeMillis();
    this.root = new ConcurrentHashMap<>();
    this.file = file;
    this.name = name;
    this.load();
  }
  
  private ObjectMapper getObjectMapper() {
    if(mapper == null) {
      mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    return mapper;
  }
  
  public void load() {
    if(this.file != null) {
      try {
        AtomicLong changed = new AtomicLong();
        String v = this.file.read(name, changed);
        if(!Strings.isNullOrEmpty(v)) {
          Map<String,Object> vals = getObjectMapper().readValue(v, Map.class);
          if(vals != null) {
            this.root = new ConcurrentHashMap<>( vals );
            this.updated = changed.get();
            return;
          }
        }
      }
      catch(Exception ex) {
        LOGGER.warn("Error reading FileDB: "+name, ex);
      }
    }
    // If we did not find anything, we should clear the values
    this.root.clear();
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
        
        // Check if we should try to save the contents
        synchronized(this) {
          if(this.file != null && saver != null) {
            long delay = Math.max( 2, saveInterval - (System.currentTimeMillis() - saved));
            this.saver = scheduler.schedule(new Runnable() {
              @Override
              public void run() {
                LiveDB.this.saver = null;
                try {
                  file.write(name, getObjectMapper().writeValueAsString(root));
                }
                catch(Exception ex) {
                  LOGGER.warn("Error saving: "+name, ex);
                }
              }}, delay, TimeUnit.MILLISECONDS);
          }
        }
      }
    }
  }

  public long getLastModified() {
    return updated;
  }
}
