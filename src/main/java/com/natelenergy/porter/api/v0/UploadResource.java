package com.natelenergy.porter.api.v0;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.natelenergy.porter.views.FileView;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
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
  @Path("form")
  @Produces(MediaType.TEXT_HTML)
  public FileView getForm() {
    return new FileView();
  }
//  
//  @POST
//  @Consumes(MediaType.MULTIPART_FORM_DATA)
//  @Path("file")
//  public Response uploadFile(
//          @FormDataParam("file") InputStream uploadedInputStream,
//          @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
//    
//      LOGGER.info( "UPLOAD: " + mapper.writeValueAsString(fileDetail) );
//      
//      int count = 0;
//      int read = 0;
//      final int BUFFER_LENGTH = 1024;
//      final byte[] buffer = new byte[BUFFER_LENGTH];
//      while ((read = uploadedInputStream.read(buffer)) != -1) {
//          System.out.println( "BUFFER: "+read + " // " + (count++));
//      }
//      
//      return Response.ok( "done" ).build();
//  }
}
