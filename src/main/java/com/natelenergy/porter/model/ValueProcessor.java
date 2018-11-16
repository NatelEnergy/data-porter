package com.natelenergy.porter.model;

import java.io.Closeable;
import java.util.Map;

public interface ValueProcessor extends Closeable {
  public void write(long time, String key, Object value);
  public void write(long time, Map<String, Object> record);
  public void flush();
}
