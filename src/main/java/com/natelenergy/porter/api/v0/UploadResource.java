package com.natelenergy.porter.api.v0;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.natelenergy.porter.model.FileUploadInfo;
import com.natelenergy.porter.views.FileView;

import javax.ws.rs.*;
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
  private final File root;
  private final String rootPath;
  
  public UploadResource(File temp) {
    this.mapper = new ObjectMapper();
    this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    temp.mkdirs();
    this.root = temp;
    this.rootPath = temp.getAbsolutePath();
  }
  
  @GET
  @Path("form")
  @Produces(MediaType.TEXT_HTML)
  public FileView getForm() {
    return new FileView();
  }

  @GET
  @Path("info/{path : (.+)?}")
  public Response uploadFile(
      @PathParam("path") 
      String path) throws IOException {

    File f = Strings.isNullOrEmpty(path) ? root : new File(root, path);
    if(f.isDirectory()) {
      return Response.ok( FileUploadInfo.list(f) ).build();
    }
    return Response.ok(FileUploadInfo.make(f, rootPath, true, true)).build();
  }
  
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("file/{path : (.+)?}")
  public FileUploadInfo uploadFile(
      @PathParam("path") 
      String path,
      
      @FormDataParam("file") InputStream data,
      @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
    
    String name = fileDetail.getFileName();
    return this.process( path + "/" + name, data, true);
  }
  
  @POST
  @Path("stream/{path : (.+)?}")
  public FileUploadInfo uploadFile(
      @PathParam("path") 
      String path,
      
      InputStream data) throws IOException 
  {
    return this.process(path, data, true);
  }
  
  
  FileUploadInfo process(String path, InputStream stream, boolean notify) throws IOException
  {
    File f = new File(root, path);
    File tmp = File.createTempFile(f.getName(), "_upload", f.getParentFile());
    
    try( OutputStream outStream = new FileOutputStream(tmp) ) {
      byte[] buffer = new byte[8 * 1024];
      int bytesRead;
      while ((bytesRead = stream.read(buffer)) != -1) {
        outStream.write(buffer, 0, bytesRead);
        if(notify) {
          System.out.println( "TODO notify! "+f.getName() );
        }
      }
    }
    
    f.delete();
    tmp.renameTo(f);
    
    return FileUploadInfo.make(f,rootPath, true, false);
  }
}
