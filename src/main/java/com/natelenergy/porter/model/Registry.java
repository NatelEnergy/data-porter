package com.natelenergy.porter.model;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.lang.model.SourceVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.natelenergy.porter.processor.FileNameInfo;
import com.natelenergy.porter.processor.ValueProcessor;
import com.natelenergy.porter.worker.ProcessingReader;
import com.natelenergy.porter.worker.WorkerRegistry;

public class Registry {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  public static class PathInfo implements Supplier<ValueProcessor> {
    @JsonIgnore
    public DataRepo repo;
    public Path path;
    public FileNameInfo name;
    
    public ProcessingReader reader;
    public ValueProcessor processor;

    @Override
    public ValueProcessor get() {
      return processor;
    }
  }
  
  public final Map<String,DataRepo> repos = new ConcurrentHashMap<>();
  public final WorkerRegistry workers = new WorkerRegistry();
 
  private final Path store;
  private final Path meta;
  
  public Registry(RegistryConfig config) {
    this.store = new File(config.store).toPath();
    this.meta = new File(config.meta).toPath();
    
    // Load everything in the folder
    try( DirectoryStream<Path> dirs = Files.newDirectoryStream(this.meta) ) {
      for(Path dir : dirs) {
        this.create(dir.getFileName().toString());
      }
    }
    catch(Exception ex) {};
    if(config.init == null || config.init.isEmpty()) {
      config.init = new ArrayList<>();
      config.init.add("test");
    }
    
    // Make sure a few repos exist
    for(String r : config.init) {
      if(!repos.containsKey(r)) {
        this.create(r);
      }
    }
  }
  
  public DataRepo create(String name) {
    if(!SourceVersion.isName(name)) {
      throw new IllegalArgumentException("must be ok variable name");
    }
    if(repos.containsKey(name)) {
      throw new IllegalArgumentException("Repo already exists");
    }
    
    try {
      DataRepo p = new DataRepo(name, store, meta);
      repos.put(p.id, p);
      return p;
    }
    catch(Exception ex) {
      LOGGER.error("Error creating porter: "+name, ex);
    }
    return null;
  }

  public PathInfo get(String instance, String path, boolean complete) {
    DataRepo p = repos.get(instance);
    if(p==null) {
      return null;
    }
    
    PathInfo info = new PathInfo();
    info.repo = p;
    info.path = p.store.resolve(path);
    info.name = p.config.name.read(path);
    if(complete) {
      info.reader = p.getReader(info.path, info.name);
      if(info.reader!=null) {
        info.processor = p.getProcessors(info.path, info.name);
        if(info.processor == null) {
          info.reader = null;
        }
      }
    }
    return info;
  }
}
