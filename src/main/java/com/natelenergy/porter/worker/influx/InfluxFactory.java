package com.natelenergy.porter.worker.influx;


import java.nio.file.Path;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import com.google.common.base.Strings;
import com.natelenergy.porter.model.*;
import com.natelenergy.porter.worker.*;

public class InfluxFactory extends ProcessorFactory
{
  public String url = "http://localhost:8086/";
  public String username;
  public String password;
  public String database = "db_$REPO";
  public String retention = "raw";
  public String duration = "INF";
  
  public String[] rollups = new String[] { "10s", "1m", "5m" };
  
  private InfluxDB influx;

  
  @Override
  public ValueProcessor doCreate(String repo, Path path, FileNameInfo info) {
    if(info==null || com.google.common.base.Strings.isNullOrEmpty(info.channel)) {
      return null;
    }
    
    if(influx==null) {
      String url = this.url.replace("$REPO", repo);
      String database = this.database.replace("$REPO", repo);
      
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
    
    return makeWriter(influx, info.channel);
  }
  
  protected InfluxWriter makeWriter(InfluxDB influx, String measurment) {
    return new InfluxWriter(influx, measurment); 
  }
}