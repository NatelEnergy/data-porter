package com.natelenergy.porter.worker;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.natelenergy.porter.model.ValueProcessor;
import com.opencsv.CSVReader;

public class ReaderCSV extends ProcessingReader {
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  long lastSize = 0;
  
  public ReaderCSV(Path file) {
    super(file);
  }
  
  public static Object parse(String v, String type) {
    try {
      switch(type) {
        case "BOOL": return Boolean.parseBoolean(v);
    
        case "DOUBLE":
        case "LREAL":
        case "REAL": return Float.parseFloat(v);
    
        case "LINT":
        case "LONG": return Long.parseLong(v);
        
        case "USINT": 
        case "INT": { 
          try {
            return Integer.parseInt(v);
          }
          catch(NumberFormatException ex) {
            return Long.parseLong(v);
          }
        }
      }
    }
    catch(NumberFormatException ex) {
      LOGGER.warn("Error parsing: "+type + "/"+v + " :: " + ex.getMessage() );
    }
    return v;
  }

  @Override
  public long process(FileWorkerStatus status, ValueProcessor processor) throws Exception {
    if(!Files.exists(this.file)) {
      return 0;
    }
    if(processor == null) {
      throw new Exception("Missing processor!");
    }
   
    
    long count = 0;
    BasicFileAttributes attrs = Files.readAttributes(this.file, BasicFileAttributes.class);
    if(attrs.isRegularFile() && attrs.size() > lastSize) {   
      status.cursor = lastSize = attrs.size();
      Map<String,String> fieldToType = new HashMap<>();
      try (CSVReader reader = new CSVReader(Files.newBufferedReader(this.file, Charsets.UTF_8)))
      {
        String[] line = reader.readNext();
        while(line != null) {
          if(line.length>0 && !line[0].startsWith("#")) {
            if(line.length >=3) {
              long when = Long.parseLong(line[0]);
              String field = line[1];
              String type = null;
              if(line.length>=4) {
                type = line[3];
                fieldToType.put(field, type);
              }
              else {
                type = fieldToType.get(field);
              }
              
              if(Strings.isNullOrEmpty(type)) {
                LOGGER.error("SKIP: "+Arrays.asList(line) + " (Unknown Type!)");
              }
              else {
                processor.write(when, field, parse(line[2], type));
              }
            }
            else {
              LOGGER.warn("SKIP: "+Arrays.asList(line) + " ("+line.length + " != 4)");
            }
          }
          
          line = reader.readNext();
        }
        attrs = Files.readAttributes(this.file, BasicFileAttributes.class);
        status.cursor = lastSize = attrs.size();
      }
      finally {
        processor.flush();
      }
    }
    return count;
  }
}
