package com.natelenergy.porter.model;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * General interface to save a set of strings.
 * 
 * Initially this is to save string (JSON) on File, but eventually S3 or Google Cloud Storage
 */
public interface StringStore {
  public String[] list();
  public String read(String name, AtomicLong modified) throws IOException;
  public void write(String name, String val) throws IOException;
}
