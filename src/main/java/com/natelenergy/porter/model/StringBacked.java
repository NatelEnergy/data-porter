package com.natelenergy.porter.model;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

public abstract class StringBacked {
  public static interface StringBackedConfigSupplier {
    public int getSaveInterval();
    public ObjectMapper getMapper();
  }
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  
  private final String name;
  private final StringStore file;
  
  private long updated;
  private long saved = -1;
  private ScheduledFuture<?> saver = null;
  protected final StringBackedConfigSupplier config;
  
  public StringBacked(String name, StringStore file, StringBackedConfigSupplier cfg) {
    this.config = cfg;
    this.file = file;
    this.name = name;
  }
  
  protected abstract void load(String string) throws Exception;
  protected abstract String getSaveString() throws Exception;
  
  public void load() {
    if(this.file != null) {
      try {
        AtomicLong changed = new AtomicLong();
        String v = this.file.read(name, changed);
        if(!Strings.isNullOrEmpty(v)) {
          this.load(v);
          this.updated = changed.get();
          return;
        }
      }
      catch(Exception ex) {
        LOGGER.warn("Error reading FileDB: "+name, ex);
      }
      try {
        this.load(null);
      }
      catch(Exception ex) {}
    }
  }
  
  protected void updated() {
    this.updated = System.currentTimeMillis();
    
    // Check if we should try to save the contents
    if(this.file != null && saver == null) {
      synchronized(this) {
        long delay = Math.max( 2, config.getSaveInterval() - (System.currentTimeMillis() - saved));
        if(saver == null) {
          saver = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
              StringBacked.this.saver = null;
              try {
                file.write(name, getSaveString());
              }
              catch(Exception ex) {
                LOGGER.warn("Error saving: "+name, ex);
              }
            }}, delay, TimeUnit.MILLISECONDS);
        }
      }
    }
  }

  public long getLastModified() {
    return updated;
  }
}
