package com.natelenergy.porter.api.v0;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.natelenergy.porter.views.FileView;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

import java.io.*;
import java.lang.invoke.MethodHandles;

import io.swagger.annotations.*;

@Path("/upload")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/upload", tags="File Upload")
public class UploadResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ObjectMapper mapper;
  
  public UploadResource() {
    this.mapper = new ObjectMapper();
    this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
  }
  
  @GET
  @Path("formx")
  @Produces(MediaType.TEXT_HTML)
  public FileView getForm() {
    return new FileView();
  }
  
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("file")
  public Response uploadFile(
          @FormDataParam("file") InputStream data,
          @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
    
      LOGGER.info( "UPLOAD: " + mapper.writeValueAsString(fileDetail) );

      this.process("XXX", data);
      
      return Response.ok( "done" ).build();
  }
  
  @POST
  @Path("stream")
  public Response uploadFile(InputStream data) throws IOException {
    
    this.process("XXX", data);
    
    return Response.ok( "done" ).build();
  }
  

  void process(String path, InputStream stream) throws IOException
  {
    System.out.println("STREAM: "+path);
    String v = IOUtils.toString( stream, Charsets.UTF_8 );
    System.out.println(">>> " + v);
  }
}
