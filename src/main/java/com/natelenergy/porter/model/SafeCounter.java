package com.natelenergy.porter.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SafeCounter {
  public final Map<String, Map<String,AtomicLong>> counter
     = new ConcurrentHashMap<>();
  
  public void increment(String what, String value) {
    Map<String,AtomicLong> c = counter.get(what);
    if(c == null) {
      c = new ConcurrentHashMap<>();
      counter.put(what, c);
    }
    
    AtomicLong v = c.get(value);
    if(v==null) {
      c.put(value, new AtomicLong(1));
    }
    else {
      v.incrementAndGet();
    }    
  }
}
