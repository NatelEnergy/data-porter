package com.natelenergy.porter.api.v0;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.natelenergy.porter.model.FileUploadInfo;
import com.natelenergy.porter.views.FileView;
import com.natelenergy.porter.worker.FileWorker;
import com.natelenergy.porter.worker.ProcessStreamingFileWorker;
import com.natelenergy.porter.worker.FileWorkerStatus.State;
import com.natelenergy.porter.worker.ProcessFileWorker;
import com.natelenergy.porter.worker.WorkerRegistry;
import com.natelenergy.porter.worker.WriteStreamWorker;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.List;

import io.swagger.annotations.*;

@Path("/file")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/file", tags="Manage Files")
public class FileResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ObjectMapper mapper;
  private final java.nio.file.Path root;
  
  private final WorkerRegistry workers;
  
  public FileResource(WorkerRegistry workers, java.nio.file.Path root) {
    this.mapper = new ObjectMapper();
    this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    
    try {
      if(!Files.exists(root)) {
        Files.createDirectories(root);
      }
    }
    catch(Exception ex) {
      LOGGER.warn("Unable to create root directory: "+root, ex);
    }
    this.root = root;
    this.workers = workers;
  }
  
  @GET
  @Path("form")
  @Produces(MediaType.TEXT_HTML)
  public FileView getForm() {
    return new FileView();
  }

  @GET
  @Path("browse/{path : (.+)?}")
  public Response uploadFile(
      @PathParam("path") 
      String path) throws IOException {

    java.nio.file.Path p = root.resolve(path);
    if(Files.isDirectory(p)) {
      return Response.ok( FileUploadInfo.list(p) ).build();
    }
    return Response.ok(FileUploadInfo.make(p, root, true)).build();
  }

  @POST
  @Path("queue/{path : (.+)?}")
  public Object queueFile(
      @PathParam("path") 
      String path) throws IOException {

    java.nio.file.Path p = root.resolve(path);
    if(!Files.exists(p)) {
      return Response.noContent().build();
    }
    
    
    if(Files.isDirectory(p)) {
      // Queue all files in the directory
      Files.walk(p, FileVisitOption.FOLLOW_LINKS ).forEach( s -> {
        String rel = root.relativize(s).toString();
        workers.queue(createFileProcessor(rel, s, false));
      });
    }
    else {
      workers.queue(createFileProcessor(path, p, false));
    }
    
    try {
      Thread.sleep(10);
    } 
    catch (InterruptedException e) {}
    return workers.getStatus();
  }
  
  @POST
  @Path("upload/{path : (.+)?}")
  public FileUploadInfo uploadFile(
      @PathParam("path") 
      String path,

      @Context
      HttpHeaders headers,
      
      InputStream data) throws IOException 
  {
    Long length = null;
    boolean stream = false;
    
    if(headers != null) {
      if(headers.getLength()> 1) {
        length = new Long( headers.getLength() );
      }
      
      // Check for streaming
      List<String> enc = headers.getRequestHeader("Transfer-Encoding");
      if(enc!=null) {
        for(String s :  enc ) {
          if("chunked".equals(s)) {
            stream = true;
          }
        }
      }
      LOGGER.info("HEADERS: "+headers.getRequestHeaders());
    }
    return doUploadFile(path, stream, length, data);
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("upload")
  public FileUploadInfo multipartUpload(
      @FormDataParam("file") InputStream data,
      @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException 
  {
    Long size = null;
    if(fileDetail.getSize() > 1) {
      size = fileDetail.getSize();
    }
    String p = "aaaaaa/" +fileDetail.getFileName();
    return doUploadFile(p, false, size, data);
  }
  
  FileUploadInfo doUploadFile(
      String path,
      boolean stream,
      Long length,
      InputStream data) throws IOException 
  {
    final java.nio.file.Path p = root.resolve(path);
    WriteStreamWorker w = new WriteStreamWorker(path, p, data, length);
    FileWorker fp = createFileProcessor(path, p, stream);
    if(stream) {
      LOGGER.info("STREAM: "+path);
      w.child = fp;
      workers.start(w.child);
      workers.run(w);
    }
    else {
      LOGGER.info("UPLOAD: "+path + " (Length: "+length+")");
      workers.run(w);

      // If it uploaded OK, then queue processor
      if(w.is( State.FINISHED) ) {
        workers.queue(fp);
      }
    }
    return FileUploadInfo.make(p, root, true);
  }
  
  
  // TODO??? This should select the smarts on what to do
  public FileWorker createFileProcessor(String path, java.nio.file.Path p, boolean stream) {
    if(stream) {
      return new ProcessStreamingFileWorker(path, p);
    }
    return new ProcessFileWorker(path, p);
    
  }
}
