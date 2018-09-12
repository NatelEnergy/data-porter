package com.natelenergy.porter.api.v0;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.natelenergy.porter.model.LiveDB;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/info")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/info", tags="System Info")
public class InfoResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private final ObjectMapper mapper;
  
  public InfoResource(ObjectMapper mapper) {
    this.mapper = mapper;
  }
  
  public Map<String,Object> loadGit() throws Exception {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream stream = classLoader.getResourceAsStream("git.json");
    return (Map<String,Object>)mapper.readValue(stream, HashMap.class);
  }
  
  @GET
  @Path("git")
  @ApiOperation( value="get git info", notes="hello notes!!!" )
  @Produces(MediaType.APPLICATION_JSON)
  public Response git() throws Exception {
    return Response.ok(loadGit()).build();
  }

  public String getGitDescription() {
    try {
      Map<String,Object> git = loadGit();
      String time = (String)git.get("git.build.time");
      String ref = (String)git.get("git.commit.id.abbrev");
      String ver = (String)git.get("git.build.version");
      return ver + " (" + ref + ") " + time;
    }
    catch(Exception ex) {}
    return "<unknown>";
  }
}
