package com.natelenergy.porter.model;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.SourceVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.natelenergy.porter.model.StringBacked.StringBackedConfigSupplier;
import com.natelenergy.porter.worker.ProcessingReader;
import com.natelenergy.porter.worker.ReaderAvro;
import com.natelenergy.porter.worker.ReaderCSV;

public class SignalRepo implements StringBackedConfigSupplier {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public final String id;

  public final ObjectMapper mapper;
  
  public final JsonDB json;
  public final LastValueDB last;
  public final Path store;
  
  private final StringStore strings;
  public final SignalRepoConfig config;
  
  public SignalRepo(String id, Path store, Path strings)
  {
    if(!SourceVersion.isName(id)) {
      throw new IllegalArgumentException("Invalid id: "+id);
    }
    this.id = id;
    this.store = resolveAndCreate( store, id );
    this.strings = new StringStoreFile(resolveAndCreate( strings, id ).toFile());

    this.mapper = new ObjectMapper();
    this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    
    SignalRepoConfig cfg = null;
    try {
      String json = this.strings.read("config", null);
      if(json!=null) {
        cfg = mapper.readValue(json, SignalRepoConfig.class);
      }
    }
    catch(Exception ex) {
      LOGGER.warn("Error loading config" );
    }
    finally {
      boolean save = false;
      if(cfg == null) {
        cfg = new SignalRepoConfig();
        save = true;
      }
      this.config = cfg.validate();
      if(save) {
        this.saveConfig();
      }
    }
    
    this.json = new JsonDB("json", this.strings, this);
    this.last = new LastValueDB( "last", this.strings, this);
  }
  
  private void saveConfig() {
    try {
      this.strings.write("config", mapper.writeValueAsString(config));
    }
    catch(Exception ex) {
      LOGGER.error("Error saving config", ex);
    }
  }
  
  private static Path resolveAndCreate(Path p, String name) {
    Path root = p.resolve(name);
    if(!Files.isDirectory(p)) {
      if(Files.exists(root)) {
        throw new IllegalStateException("File should be directory: "+root );
      }
      try {
        Files.createDirectories(root);
      }
      catch(Exception ex) {
        LOGGER.warn("Unable to create root directory: "+root, ex);
      } 
    }
    return root.toAbsolutePath();
  }

  @Override
  public int getSaveInterval() {
    return config.saveInterval;
  }

  @Override
  public ObjectMapper getMapper() {
    return this.mapper;
  }
  
  public ProcessingReader getReader(Path path, FileNameInfo info) {
    String name = path.getFileName().toString();
    if(name.endsWith(".avro")) {
      return new ReaderAvro(path);
    }
    if(name.endsWith(".csv")) {
      return new ReaderCSV(path);
    }
    return null;
  }

  public ValueProcessor getProcessors(Path path, FileNameInfo name) {
    List<ValueProcessor> list = new ArrayList<>();
    list.add(last);
    for(ProcessorFactory f : this.config.processors) {
      ValueProcessor p = f.create(this.id, path, name);
      if(p!=null) {
        list.add(p);
      }
    }
    if(list.size()==1) {
      return list.get(0);
    }
    return new ChainedProcessors(list);
  }
}
