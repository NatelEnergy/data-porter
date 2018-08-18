package com.natelenergy.porter.api.v0;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.lang.invoke.MethodHandles;
import io.swagger.annotations.*;

@Path("/upload")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/upload", tags="File Upload")
public class UploadResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  public UploadResource() {
    
  }

  @GET
  @Path("/hello")
  @ApiOperation( value="hello", notes="hello notes!!!" )
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSurfacePlot() throws Exception {
    JSONObject obj = new JSONObject();
    obj.put("hello", "world");
    
    return Response.ok(obj).build();
  }
  
  @GET
  @Path("/dump")
  @ApiOperation( value="dump", hidden=true )
  @Produces(MediaType.TEXT_PLAIN)
  public String dumpSheet(@QueryParam("path") String path, @QueryParam("path") String range) {
    StringBuilder str = new StringBuilder();
    str.append("TODO: "+ path);
    return str.toString();
  }
}
