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
import com.natelenergy.porter.worker.WorkerRegistry;
import com.natelenergy.porter.worker.WriteStreamWorker;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import io.swagger.annotations.*;

@Path("/files")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/files", tags="Manage Files")
public class FilesResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ObjectMapper mapper;
  private final java.nio.file.Path root;
  
  private final WorkerRegistry workers;
  
  public FilesResource(WorkerRegistry workers, java.nio.file.Path root) {
    this.mapper = new ObjectMapper();
    this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    
    try {
      Files.createDirectories(root);
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
  @Path("stream/{path : (.+)?}")
  public FileUploadInfo streamFile(
      @PathParam("path") 
      String path,
      
      InputStream data) throws IOException 
  {
    java.nio.file.Path p = root.resolve(path);
    
    WriteStreamWorker w = new WriteStreamWorker(path, p, data, null);
    w.child = createFileProcessor(path, p, true);
    workers.start(w.child);
    workers.run(w);
    
    return FileUploadInfo.make(p, root, true);
  }
  
  @POST
  @Path("upload/{path : (.+)?}")
  public FileUploadInfo uploadFile(
      @PathParam("path") 
      String path,
      
      InputStream data) throws IOException 
  {
    java.nio.file.Path p = root.resolve(path);
    return doUpload(p, data, null);
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("upload")
  public FileUploadInfo uploadFile(
      @FormParam("folder") 
      String folder,
      
      @FormDataParam("file") InputStream data,
      @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
    
    java.nio.file.Path p = root.resolve(folder);
    p = p.resolve(fileDetail.getFileName());
    return doUpload(p, data, null);
  }
  
  private FileUploadInfo doUpload(java.nio.file.Path path, InputStream data, Long length) throws IOException {
    String rel = root.relativize(path).toString();
    WriteStreamWorker w = new WriteStreamWorker(
        rel, path, data, length);
    workers.run(w);

    // If it uploaded OK, then queue processor
    if(w.is( State.FINISHED) ) {
      workers.queue(createFileProcessor(rel, path, false));
    }
    return FileUploadInfo.make(path, root, true);
  }
  
  // TODO??? This should select the smarts on what to do
  public FileWorker createFileProcessor(String path, java.nio.file.Path p, boolean stream) {
    return new ProcessStreamingFileWorker(path, p, stream);
  }
}
