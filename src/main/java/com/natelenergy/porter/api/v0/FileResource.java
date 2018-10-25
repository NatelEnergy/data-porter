package com.natelenergy.porter.api.v0;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.natelenergy.porter.model.FileUploadInfo;
import com.natelenergy.porter.processor.LastValueDB;
import com.natelenergy.porter.processor.LastValueDB.LastValue;
import com.natelenergy.porter.processor.ProcessingInfo;
import com.natelenergy.porter.processor.Processors;
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
import java.util.function.Predicate;

import io.swagger.annotations.*;

@Path("/file")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/file", tags="Manage Files")
public class FileResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ObjectMapper mapper;
  
  private final WorkerRegistry workers;
  private final Processors processors;
  
  public FileResource(WorkerRegistry workers, Processors processors) {
    this.mapper = new ObjectMapper();
    this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    this.workers = workers;
    this.processors = processors;
  }
  
  @GET
  @Path("form")
  @Produces(MediaType.TEXT_HTML)
  public FileView getForm() {
    return new FileView();
  }

  @GET
  @Path("last")
  public Response getLastDBs() throws IOException {
    return Response.ok( processors.getLastDBs() ).build();
  }

  @GET
  @Path("last/{key}")
  public Response getLastValues(
      @PathParam("key") 
      String key,
      
      @QueryParam("field")
      final String field
      ) throws IOException {
    
    LastValueDB db = processors.getLastDB(key);
    if(db==null) {
      return Response.noContent().build();
    }
    
    if(Strings.isNullOrEmpty(field)) {
      return Response.ok( db.getDB(null) ).build();
    }
    else if(field.endsWith("*")) {
      return Response.ok( db.getDB( new Predicate<String>() {
        final String pfix = field.substring(0, field.length()-1);

        @Override
        public boolean test(String t) {
          return t.startsWith(pfix);
        }
      })).build();
    }
    
    LastValue val = db.get(field);
    if(val == null) {
      return Response.noContent().build();
    }
    return Response.ok( val ).build();
  }

  @GET
  @Path("browse/{path : (.+)?}")
  public Response uploadFile(
      @PathParam("path") 
      String path) throws IOException {

    java.nio.file.Path p = processors.resolve(path);
    if(Files.isDirectory(p)) {
      return Response.ok( FileUploadInfo.list(p) ).build();
    }
    return Response.ok(FileUploadInfo.make(p, processors.getRoot(), true)).build();
  }

  @POST
  @Path("queue/{path : (.+)?}")
  public Object queueFile(
      @PathParam("path") 
      String path) throws IOException {

    java.nio.file.Path p = processors.resolve(path);
    if(!Files.exists(p)) {
      return Response.noContent().build();
    }
    
    if(Files.isDirectory(p)) {
      // Queue all files in the directory
      Files.walk(p, FileVisitOption.FOLLOW_LINKS ).forEach( s -> {
        String rel = processors.getRoot().relativize(s).toString().replace('\\', '/');
        ProcessingInfo x = processors.get(rel, false);
        if(x.processor != null) {
          workers.queue(new ProcessFileWorker(rel, x.reader));
        }
      });
    }
    else {
      ProcessingInfo x = processors.get(path, false);
      if(x.processor != null) {
        workers.queue(new ProcessFileWorker(path, x.reader));
      }
    }
    
    try {
      Thread.sleep(25);
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
  

  @GET
  @Path("info/{path : (.+)?}")
  public Response getInfo(
      @PathParam("path") 
      String path) throws IOException {

    ProcessingInfo ppp = processors.get(path, false);
    return Response.ok(ppp).build();
  }
  
  FileUploadInfo doUploadFile(
      String path,
      boolean stream,
      Long length,
      InputStream data) throws IOException 
  {
    ProcessingInfo info = processors.get(path, stream);
    WriteStreamWorker w = new WriteStreamWorker(path, info.file, data, length);
    FileWorker fp = null;
    if(info.reader != null) {
      if(stream) {
        fp = new ProcessStreamingFileWorker(path, info.reader);
      }
      else {
        fp = new ProcessFileWorker(path, info.reader);
      }
    }
    
    if(stream) {
      LOGGER.info("STREAM: "+path);
      if(fp!=null) {
        w.child = fp;
        workers.start(w.child);
      }
      workers.run(w);
    }
    else {
      LOGGER.info("UPLOAD: "+path + " (Length: "+length+")");
      workers.run(w);

      // If it uploaded OK, then queue processor
      if(fp != null && w.is( State.FINISHED) ) {
        workers.queue(fp);
      }
    }
    return FileUploadInfo.make(info.file, processors.getRoot(), true);
  }
}
