package com.natelenergy.porter.model;

import java.nio.file.Path;

import org.apache.parquet.Strings;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.natelenergy.porter.processor.FileNameInfo;
import com.natelenergy.porter.processor.ValueProcessor;

@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="@type")
public abstract class ProcessorFactory {
  
  public String id;
  
  public abstract ValueProcessor create(Path path, FileNameInfo info);
  
  
  public static class InfluxFactory extends ProcessorFactory
  {
    public String url = "http://localhost:8086/";
    public String username;
    public String password;
    public String database = "xxx";
    
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
      }
      
      return new InfluxWriter(influx, info.channel);
    }
  }
}
