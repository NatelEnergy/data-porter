package com.natelenergy.porter.api.v0;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.lang.model.SourceVersion;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natelenergy.porter.model.LiveDB;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/json")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/json", tags="JSON Database")
public class JSONResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  // Share this across all instances
  private static final ConcurrentHashMap<String, LiveDB> dbs = new ConcurrentHashMap<>();
  
  public JSONResource() {
    
  }

  @GET
  @Path("{db}/{path : (.+)?}")
  @ApiOperation( value="get value", notes="hello notes!!!" )
  @Produces(MediaType.APPLICATION_JSON)
  public Response getData(
      @PathParam("db") 
      String name,

      @PathParam("path") 
      String path
      ) throws Exception {
    
    LiveDB db = dbs.get(name);
    if(db==null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    Map<String, Object> rsp = new HashMap<>();
    rsp.put("db", name);
    rsp.put("path", path);
    rsp.put("value", db.get(path));
    rsp.put("modified", db.getLastModified());
    return Response.ok(rsp).build();
  }
  
  @POST
  @Path("{db}/{path : (.+)?}")
  @ApiOperation( value="set value", notes="hello notes!!!" )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response setData(
      @PathParam("db") 
      @ApiParam(example="myDB")
      String name,

      @ApiParam(example="users/info/ryan")
      @PathParam("path") 
      String path,
      
      // Parsed by jersey!
      @ApiParam(example="{ \"first\": \"Ryan\" }")
      Map<String,Object> body
      ) throws Exception {
    
    LiveDB db = dbs.get(name);
    if(db==null) {
      if(!SourceVersion.isIdentifier(name)) {
        throw new IllegalArgumentException("Invalid DB name");
      }
      db = new LiveDB();
      dbs.put(name, db);
    }
    
    // Update the value
    db.set(path, body);
    
    Map<String, Object> rsp = new HashMap<>();
    rsp.put("db", name);
    rsp.put("path", path);
    rsp.put("modified", db.getLastModified());
    return Response.ok(rsp).build();
  }
  

  
  @GET
  @Path("/dump")
  @ApiOperation( value="dump", hidden=true )
  @Produces(MediaType.TEXT_PLAIN)
  public String dumpSheet(@QueryParam("path") String path, @QueryParam("path") String range) {
    StringBuilder str = new StringBuilder();
    str.append("KEYS: "+ dbs.keys() );
    return str.toString();
  }
}
