package com.natelenergy.porter.model;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

import org.apache.parquet.Strings;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="@type")
public abstract class ProcessorFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  
  public String id;
  
  public abstract ValueProcessor create(Path path, FileNameInfo info);
  
  
  public static class InfluxFactory extends ProcessorFactory
  {
    public String url = "http://localhost:8086/";
    public String username;
    public String password;
    public String database = "signals";
    public String retention = "raw";
    public String duration = "INF";
    
    public String[] rollups = new String[] { "10s", "1m", "5m" };
    
    private InfluxDB influx;

    
    
    
    @Override
    public ValueProcessor create(Path path, FileNameInfo info) {
      if(info==null || com.google.common.base.Strings.isNullOrEmpty(info.channel)) {
        return null;
      }
      
      if(influx==null) {
        if(Strings.isNullOrEmpty(username)) {
          influx = InfluxDBFactory.connect(url);
        }
        else {
          influx = InfluxDBFactory.connect(url, username, password);
        }
        influx.setDatabase(database);
        influx.query(new Query("CREATE DATABASE "+database, database, true));
        
        if(!Strings.isNullOrEmpty(this.retention)) {
          if(Strings.isNullOrEmpty(duration)) {
            duration = "INF";
          }
          
          String cmd = "CREATE RETENTION POLICY \""+retention+"\" ON \""+database
            +"\" DURATION "+duration+" REPLICATION 1 DEFAULT";
          try { 
            QueryResult res = influx.query(new Query(cmd, database, true ));
            LOGGER.info("Query: " + res);
          }
          catch(Exception ex) {
            LOGGER.warn("Error creating retention policy: "+cmd, ex);
          }
          influx.setRetentionPolicy(retention);
        }
      }
      
      return new InfluxWriter(influx, info.channel);
    }
  }
}
