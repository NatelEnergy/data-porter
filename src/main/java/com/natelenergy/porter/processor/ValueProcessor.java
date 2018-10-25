package com.natelenergy.porter.processor;

import java.util.Map;

public interface ValueProcessor {
  public void write(long time, String key, Object value);
  public void write(long time, Map<String, Object> record);
  public void flush();
}
