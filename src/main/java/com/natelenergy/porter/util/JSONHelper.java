package com.natelenergy.porter.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONHelper {
  public static final ObjectMapper mapper;
  static {
    mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
  }
  
  public static String toJSON(Object v) {
    try {
      return mapper.writeValueAsString(v);
    } 
    catch (JsonProcessingException e) {
      return v.toString();
    }
  }
}
