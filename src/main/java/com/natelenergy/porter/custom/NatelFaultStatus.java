package com.natelenergy.porter.custom;

import java.util.LinkedList;
import java.util.Map;

public class NatelFaultStatus {
  public int count = 0;
  public int errors = 0;
  public LinkedList<Map<String,Object>> history = new LinkedList<>();
}
