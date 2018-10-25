package com.natelenergy.porter.worker;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natelenergy.porter.processor.ValueProcessor;
import com.opencsv.CSVReader;

public class ReaderCSV extends ProcessingReader {
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  long lastSize = 0;
  
  public ReaderCSV(Path file, Supplier<ValueProcessor> processor) {
    super(file, processor);
  }
  
  public static Object parse(String v, String type) {
    switch(type) {
    case "BOOL": return Boolean.parseBoolean(v);
    case "REAL": return Float.parseFloat(v);

    case "LINT":
    case "LONG": return Long.parseLong(v);
    
    case "USINT":
    case "INT": return Integer.parseInt(v);
    }
    return v;
  }

  @Override
  public long process(FileWorkerStatus status) throws Exception {
    if(!Files.exists(this.file)) {
      return 0;
    }
    
    long count = 0;
    BasicFileAttributes attrs = Files.readAttributes(this.file, BasicFileAttributes.class);
    if(attrs.isRegularFile() && attrs.size() > lastSize) {
      ValueProcessor processor = supplier.get();
      if(processor == null) {
        throw new Exception("Missing processor!");
      }
      
      status.cursor = lastSize = attrs.size();
      try (CSVReader reader = new CSVReader(Files.newBufferedReader(this.file, Charsets.UTF_8)))
      {
        String[] line = reader.readNext();
        while(line != null) {
          if(line.length>0 && !line[0].startsWith("#")) {
            if(line.length==4) {
              long when = Long.parseLong(line[0]);
              processor.write(when, line[1], parse(line[2], line[3]));
            }
            else {
              LOGGER.warn("SKIP: "+Arrays.asList(line));
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
