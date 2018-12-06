package com.natelenergy.porter.natel;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.natelenergy.porter.model.*;
import com.natelenergy.porter.worker.*;

import liquibase.*;
import liquibase.database.*;
import liquibase.database.jvm.*;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class FaultEventFactory extends ProcessorFactory
{
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  public String driver = "";
  public String connection = "jdbc:postgresql://host:5432/plant_$REPO?stringtype=unspecified";
  public String username = "postgres";
  public String password = "???";
  public String table = "fault_events";

  private final Set<String> initalized = new HashSet<>();
  private FaultEventStatus status = new FaultEventStatus();
  
  @Override
  @JsonIgnore
  public Object getStatus(String repo) {
    try( FaultProcessor p = new FaultProcessor(repo) ) {
      LOGGER.info("status: "+repo );
    }
    catch(Exception ex) {
      LOGGER.warn("error: "+repo, ex );
    }
    return status;
  }
  
  public static Timestamp getTS(Object v) {
    if(v instanceof Long) {
      return new Timestamp((long)v);
    }
    return null;
  }
  
  private class FaultProcessor implements ValueProcessor {

    // New Connection every time we read the file
    Connection conn;
    PreparedStatement del;
    PreparedStatement ins;
    
    final String connection;
    final String table;
    
    public FaultProcessor(String repo) {
      connection = FaultEventFactory.this.connection.replace("$REPO", repo);
      table = FaultEventFactory.this.table.replace("$REPO", repo);
      
      try {
        // The first time this runs, make sure the tables have migrated OK
        String key = connection + "//" + table;
        if(!initalized.contains(key)) {
          this.doLiquidbase();
          initalized.add(key);
        }
      }
      catch(SQLException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    private void checkConnection() throws SQLException {
      if(conn==null || conn.isClosed()) {
        conn = DriverManager.getConnection(connection, username, password);
        del = conn.prepareStatement("DELETE FROM "+table+" WHERE id=?");
        ins = conn.prepareStatement("INSERT INTO "+table
            +" (id,root,is_root,manager,fault,endpoint,condition_hit_time,faulted_time,ok_time,release_time,ack_time,value) "
            +" VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
      }
    }
    
    private void doLiquidbase() throws SQLException {
      LOGGER.info("Checking Table Configuration: "+connection);
      synchronized(FaultEventFactory.this) {
        Liquibase liquibase = null;
        try( Connection c = DriverManager.getConnection(connection, username, password)) {
          Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
          liquibase = new Liquibase("agent.sql", 
              new ClassLoaderResourceAccessor(), database);
          liquibase.update("agent");
        } 
        catch (LiquibaseException e) {
          throw new RuntimeException(e);
        }
      }
    }
    
    @Override
    public void write(long time, Map<String, Object> record) {
      try {
        checkConnection();
        
        int i=1;
        long id = (long)record.get("id");
        long root = (long)record.get("root");
        del.setLong(1, id);
        ins.setLong(i++, id);
        ins.setLong(i++, root);
        ins.setBoolean(i++, (id==root));
        ins.setString(i++, (String)record.get("manager"));
        ins.setString(i++, (String)record.get("fault"));
        ins.setString(i++, (String)record.get("endpoint"));

        ins.setTimestamp(i++, getTS(record.get("conditionHitTime")));
        ins.setTimestamp(i++, getTS(record.get("faultedTime")));
        ins.setTimestamp(i++, getTS(record.get("okTime")));
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
        LOGGER.warn("error writing fault", ex);
        status.errors++;
      }
      
      status.history.push(new HashMap<>(record));
      if(status.history.size()>20) {
        status.history.removeLast();
      }
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
          LOGGER.info("Closing connection: "+table );
          conn.close();
        } 
        catch (SQLException e) {
          throw new IOException(e);
        }
        finally {
          conn = null;
        }
      }
    }
  }
  
  @Override
  public ValueProcessor doCreate(String repo, Path path, FileNameInfo info) {
    if(info==null || com.google.common.base.Strings.isNullOrEmpty(info.channel)) {
      return null;
    }
    return new FaultProcessor(repo);
  }
}