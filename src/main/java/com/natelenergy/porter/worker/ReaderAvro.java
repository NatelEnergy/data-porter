package com.natelenergy.porter.worker;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableFileInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.natelenergy.porter.processor.ValueProcessor;

public class ReaderAvro extends ProcessingReader {
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
  long lastSync = 0;
  
  public ReaderAvro(Path file, Supplier<ValueProcessor> processor) {
    super(file, processor);
  }
  
  public static class FConvert {
    public String name;
    public int index;
    public Function<Object, Object> norm;
    
    public FConvert(Field f) {
      this.name = f.name();
      this.index = f.pos();

      Schema s = f.schema();
      Schema.Type t = s.getType();
      if(t == Type.DOUBLE || t == Type.FLOAT || t == Type.INT ||  t == Type.LONG || t == Type.STRING || t == Type.BOOLEAN ) {
        norm = Functions.identity();
      }
      else if(t== Type.FIXED) {
        // TODO!
      }
    }
  }
  

  @Override
  public long process(FileWorkerStatus status) throws Exception {
    if(!Files.exists(this.file)) {
      return 0;
    }

    long count = 0;
    BasicFileAttributes attrs = Files.readAttributes(this.file, BasicFileAttributes.class);
    if(attrs.isRegularFile() && attrs.size() > lastSync) { // Must have a file size
      long total = 0;
      if(status.count!=null) {
        total = status.count;
      }
      
      ValueProcessor processor = supplier.get();
      if(processor == null) {
        throw new Exception("Missing processor!");
      }
      
      GenericRecord record = null;
      try( 
        SeekableFileInput sin = new SeekableFileInput(this.file.toFile());
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(sin, datumReader) 
      ){
        // Schema schema = dataFileReader.getSchema();
        if(lastSync>0) {
          dataFileReader.seek(lastSync);
          dataFileReader.previousSync();
        }
        
        FConvert epoch = null;
        Schema schema = null;
        List<FConvert> fields = new ArrayList<>();
        Map<String, Object> values = new HashMap<>();
        
        while (dataFileReader.hasNext()) {
          record = dataFileReader.next(record);

          if(schema != record.getSchema()) {
            fields.clear();
            schema = record.getSchema();
            for(Field f : schema.getFields()) {
              FConvert c = new FConvert(f);
              if(c.norm!=null) {
                if("epoch".equals(c.name)) {
                  epoch = c;
                }
                else {
                  fields.add(c);
                }
              }
              else {
                LOGGER.warn("Skipping field: "+f );
              }
            }
          }

          values.clear();
          for(FConvert s : fields) {
            values.put(s.name, s.norm.apply( record.get(s.index)) );
          }

          long when = (long)record.get(epoch.index);
          processor.write(when, values);
          
          count++;
          status.count = total++;
          status.time = when;
          if(total % 1000 == 0) {
            LOGGER.info("indexing: "+this.file + " // Count:"+status.count + " // time:"+when );
          }
        }
        lastSync = dataFileReader.previousSync();
      }
      finally {
        processor.flush();
      }
    }
    return count;
  }
}
