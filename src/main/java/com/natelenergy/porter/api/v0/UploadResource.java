package com.natelenergy.porter.api.v0;

import org.glassfish.jersey.media.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natelenergy.porter.views.FileView;

import java.io.*;

import javax.ws.rs.*;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import java.lang.invoke.MethodHandles;

import io.swagger.annotations.*;

@Path("/upload")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/upload", tags="File Upload")
public class UploadResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Produces(MediaType.TEXT_HTML)
  @GET
  @Path("form")
  public FileView getForm() {
    return new FileView();
  }
  
  
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("file")
  public Response uploadFile(
          @FormDataParam("file") InputStream uploadedInputStream,
          @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
    
      // TODO: uploadFileLocation should come from config.yml
      String uploadedFileLocation = "C:/tmp/" + fileDetail.getFileName();
      LOGGER.info(uploadedFileLocation);
      // save it
      writeToFile(uploadedInputStream, uploadedFileLocation);
      String output = "File uploaded to : " + uploadedFileLocation;
      return Response.ok(output).build();
  }

  // save uploaded file to new location
  private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) throws IOException {
      int read;
      final int BUFFER_LENGTH = 1024;
      final byte[] buffer = new byte[BUFFER_LENGTH];
      OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
      while ((read = uploadedInputStream.read(buffer)) != -1) {
          out.write(buffer, 0, read);
      }
      out.flush();
      out.close();
  }
}
