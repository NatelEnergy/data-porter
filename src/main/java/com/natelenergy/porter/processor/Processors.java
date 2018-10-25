package com.natelenergy.porter.processor;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.parquet.Strings;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natelenergy.porter.model.InfluxWriter;
import com.natelenergy.porter.worker.ProcessingReader;
import com.natelenergy.porter.worker.ReaderAvro;
import com.natelenergy.porter.worker.ReaderCSV;

public class Processors {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Path root;
  private final List<ProcessingConfig> configs;

  final Map<String,InfluxDB> cache = new ConcurrentHashMap<>();
  final Map<String,LastValueDB> last = new ConcurrentHashMap<>();
  
  public Processors(Path root, List<ProcessingConfig> config) {
    try {
      if(!Files.exists(root)) {
        Files.createDirectories(root);
      }
    }
    catch(Exception ex) {
      LOGGER.warn("Unable to create root directory: "+root, ex);
    }
    this.root = root.toAbsolutePath();
    this.configs = config;
  }

  public Path getRoot() {
    return root;
  }
  
  public Path resolve(String path) {
    return root.resolve(path);
  }
  
  public Collection<String> getLastDBs() {
    return last.keySet();
  }
  
  public LastValueDB getLastDB(String key) {
    return last.get(key);
  }
  
  private InfluxDB getInflux(ProcessingConfig cfg) {
    try {
      String key = cfg.influx + "@" + cfg.database;
      InfluxDB influx = cache.get(key);
      if(influx == null) {
        if(Strings.isNullOrEmpty(cfg.username)) {
          influx = InfluxDBFactory.connect(cfg.influx);
        }
        else {
          influx = InfluxDBFactory.connect(cfg.influx, cfg.username, cfg.password);
        }
        influx.setDatabase(cfg.database);
        influx.query(new Query("CREATE DATABASE "+cfg.database, cfg.database, true));
        cache.put(key, influx);
      }
      return influx;
    }
    catch(Exception ex) {
      LOGGER.warn("unable to get influx:", ex);
    }
    return null;
  }
  
  public ProcessingInfo get(String path, boolean streaming) {
    ProcessingInfo info = new ProcessingInfo();
    info.path = path;
    info.file = root.resolve(path);
    info.streaming = streaming;
    info.info = FileNameInfo.parse(path);
    
    for(ProcessingConfig cfg : this.configs) {
      if(path.startsWith(cfg.path)) {
        info.siteKey = cfg.siteKey;
     //   info.influx = getInflux(cfg);
        if(info.siteKey != null) {
          info.last = last.get(info.siteKey);
          if(info.last==null) {
            info.last = new LastValueDB(info.siteKey);
            last.put(info.siteKey, info.last);
          }
        }
        
        ProcessingReader indexer = null;
        if(path.endsWith(".avro")) {
          indexer = new ReaderAvro(info.file, info);
        }
        else if(path.endsWith(".csv")) {
          indexer = new ReaderCSV(info.file, info);
        }
        
        if(indexer != null) {
          if(info.influx!=null && info.info.channel != null) {
            InfluxWriter w = new InfluxWriter(info.influx, info.info.channel);
            if(info.last != null) {
              info.processor = new ChainedProcessors(info.last, w);
            }
            else {
              info.processor = w;
            }
          }
          else if(info.last != null) {
            info.processor = info.last;
          }
          
          // Make sure we got an indexer
          if(info.processor!=null) {
            info.reader = indexer;
          }
        }
        return info;
      }
    }
    return info;
  }
}
