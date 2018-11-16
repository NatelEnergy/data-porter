package com.natelenergy.porter.custom;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.natelenergy.porter.model.FileNameInfo;
import com.natelenergy.porter.model.ProcessorFactory;
import com.natelenergy.porter.model.ValueProcessor;

public class NatelFaultEventProcessorFactory extends ProcessorFactory
{
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  public String driver = "";
  public String connection = "jdbc:postgresql://host:5432/plant_$REPO?stringtype=unspecified";
  public String username = "postgres";
  public String password = "???";
  public String table = "fault_events";
  
  private NatelFaultStatus status = new NatelFaultStatus();
  
  @Override
  @JsonIgnore
  public Object getStatus() {
    return status;
  }
  
  public static Timestamp getTS(Object v) {
    if(v instanceof Long) {
      return new Timestamp((long)v);
    }
    return null;
  }
  
  @Override
  public ValueProcessor doCreate(String repo, Path path, FileNameInfo info) {
    if(info==null || com.google.common.base.Strings.isNullOrEmpty(info.channel)) {
      return null;
    }
    
    try {
      String connection = this.connection.replace("$REPO", repo);
      String table = this.table.replace("$REPO", repo);
      
      return new ValueProcessor() {
        // New Connection every time we read the file
        Connection conn = DriverManager.getConnection(connection, username, password);
        PreparedStatement del = conn.prepareStatement("DELETE FROM "+table+" WHERE id=?");
        PreparedStatement ins = conn.prepareStatement("INSERT INTO "+table
            +" (id,manager,fault,endpoint,conditionHitTime,faultedTime,releaseTime,ackTime,value) "
            +" VALUES(?,?,?,?,?,?,?,?,?)");
        
        
        @Override
        public void write(long time, Map<String, Object> record) {
          try {
            
            int i=1;
            long id = (long)record.get("id");
            del.setLong(1, id);
            ins.setLong(i++, id);
            ins.setString(i++, (String)record.get("manager"));
            ins.setString(i++, (String)record.get("fault"));
            ins.setString(i++, (String)record.get("endpoint"));

            ins.setTimestamp(i++, getTS(record.get("conditionHitTime")));
            ins.setTimestamp(i++, getTS(record.get("faultedTime")));
            ins.setTimestamp(i++, getTS(record.get("releaseTime")));
            ins.setTimestamp(i++, getTS(record.get("ackTime")));
            ins.setFloat(i++, (float)record.get("value"));
            
            // Delete then insert
            conn.setAutoCommit(false);
            del.executeUpdate();
            ins.executeUpdate();
            conn.commit();
            status.count++;
          }
          catch(Exception ex) {
            LOGGER.warn("error writng fault", ex);
            status.errors++;
          }
          
          HashMap<String, Object> copy = new HashMap<>(record);
          status.history.push(copy);
          if(status.history.size()>20) {
            status.history.removeLast();
          }
          System.out.println( "Write to SQL: "+record );
        }
        
        @Override
        public void write(long time, String key, Object value) {
          throw new UnsupportedOperationException();
        }
        
        @Override
        public void flush() {
          
        }

        @Override
        public void close() throws IOException {
          if (conn != null) {
            try {
              conn.close();
            } 
            catch (SQLException e) {
              throw new IOException(e);
            }
          }
        }
      };
    }
    catch(Exception ex) {
      LOGGER.warn("error creating fault processor", ex);
    }
    return null;
  }
}