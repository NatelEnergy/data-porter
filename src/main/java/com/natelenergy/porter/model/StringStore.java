package com.natelenergy.porter.model;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public interface StringStore {
  public String[] list();
  public String read(String name, AtomicLong modified) throws IOException;
  public void write(String name, String val) throws IOException;
}
