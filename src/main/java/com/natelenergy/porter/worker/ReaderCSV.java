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
import com.google.common.primitives.UnsignedLong;
import com.natelenergy.porter.model.ValueProcessor;
import com.opencsv.CSVReader;

public class ReaderCSV extends ProcessingReader {
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  long lastSize = 0;
  
  public ReaderCSV(Path file) {
    super(file);
  }
  
  public static Object parse(String v, String type) {
    if(type==null) {
      return v;
    }
    
    try {
      switch(type) {
        case "BOOL": return Boolean.parseBoolean(v);
    
        case "DOUBLE":
        case "LREAL":
        case "REAL": return Float.parseFloat(v);

        case "millis":
        case "MILLIS":
        case "LINT":
        case "LONG": return Long.parseLong(v);
        
        case "USINT": 
          return UnsignedLong.valueOf(v);
          
          
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
    
    boolean changesFormat = false;
    String[] names = null;
    String[] formats = null;
    int timeIndex = -1;
    Map<String,Object> values = null;
    
    long count = 0;
    BasicFileAttributes attrs = Files.readAttributes(this.file, BasicFileAttributes.class);
    if(attrs.isRegularFile() && attrs.size() > lastSize) {   
      status.cursor = lastSize = attrs.size();
      Map<String,String> fieldToType = new HashMap<>();
      try (CSVReader reader = new CSVReader(Files.newBufferedReader(this.file, Charsets.UTF_8)))
      {
        String[] line = reader.readNext();
        for(int lineNumber=1; line != null; lineNumber++) {
          if(line.length>0) {
            if( line[0].startsWith("#") ) {
              if(lineNumber == 1) {
                if(line[0].equalsIgnoreCase("#Changes")) {
                  changesFormat = true;
                }
                else {
                  changesFormat = false;
                  names = line;
                  names[0] = line[0].substring(1).trim();
                  values = new HashMap<>();
                }
              }
              else if(lineNumber == 2) {
                formats = line;
                formats[0] = line[0].substring(1).trim();
                for(int i=0; i<formats.length; i++) {
                  if(formats[i].endsWith(":time")) {
                    timeIndex = i;
                    formats[i] = formats[i].substring(0,  formats[i].indexOf(':'));
                    break;
                  }
                }
              }
            }
            else {
              if(changesFormat) {
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
              else if(names != null) {
                if( names.length == line.length) {
                  if(formats==null) {
                    formats = new String[names.length];
                  }
                  long when = 0;
                  values.clear();
                  for(int i=0; i<names.length; i++) {
                    String v = line[i];
                    if(!Strings.isNullOrEmpty(v)) {
                      Object val = parse(v, formats[i]);
                      values.put(names[i], val);
                      if(i==timeIndex) {
                        when = (long)val;
                      }
                    }
                  }
                  processor.write(when, values);
                }
                else {
                  LOGGER.warn("SKIP: "+Arrays.asList(line) + " ("+line.length + " != names length: "+names.length+")");
                }
              }
              else {
                LOGGER.warn("SKIP: "+Arrays.asList(line) + " ("+line.length + " != 4)");
              }
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
