package com.natelenergy.porter.api.v0;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.natelenergy.porter.model.SignalRepo;
import com.natelenergy.porter.model.FileUploadInfo;
import com.natelenergy.porter.model.Registry;
import com.natelenergy.porter.model.LastValueDB.LastValue;
import com.natelenergy.porter.worker.FileWorker;
import com.natelenergy.porter.worker.FileWorkerStatus.State;
import com.natelenergy.porter.worker.ProcessFileWorker;
import com.natelenergy.porter.worker.ProcessFileWorkerStreaming;
import com.natelenergy.porter.worker.ProcessingReader;
import com.natelenergy.porter.worker.WriteStreamWorker;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/repo")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/repo", tags="Manage Files")
public class RepoResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static enum Show {
    nav,
    info,
    debug
  }
  
  private final Registry registry;
  
  public RepoResource(Registry registry) {
    this.registry = registry;
  }

  @GET
  @Path("{instance}")
  public Response getRepoInfo(
      @PathParam("instance") 
      String instance) throws IOException {
    
    SignalRepo repo = registry.repos.get(instance);
    if(repo==null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    Map<String,Object> info = new HashMap<>();
    info.put("id", repo.id);
    info.put("root", repo.store);
    info.put("last", repo.last.size());
    return Response.ok(info).build();
  }

  @GET
  @Path("{instance}/config.json")
  public Response getRepoConfig(
      @PathParam("instance") 
      String instance) throws IOException {
    
    SignalRepo repo = registry.repos.get(instance);
    if(repo==null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok(repo.config).build();
  }
  
  @GET
  @Path("{instance}/store/{path : (.+)?}")
  public Response uploadFile(
      @PathParam("instance") 
      String instance,
      
      @PathParam("path") 
      String path,
      
      @DefaultValue("nav")
      @QueryParam("show")
      Show show) throws IOException {
    
    boolean debug = (show==Show.debug);
    Registry.PathInfo info = registry.get(instance, path, debug);
    if(info==null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    if(debug) {
      return Response.ok(info).build();
    }

    if(Files.isDirectory(info.path)) {
      return Response.ok( FileUploadInfo.list(info.path) ).build();
    }
    
    if(Files.exists(info.path) && (show==Show.nav)) {
      String name = info.path.getFileName().toString();
      if(name.endsWith(".csv")) {
        return Response.ok(info.path.toFile(), "text/csv")
            .build();
      }
      return Response.ok(info.path.toFile(), MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", 
            "attachment; filename=\"" + name + "\"" ) 
        .build();
    }
    return Response.ok(FileUploadInfo.make(info.path, info.repo.store, true)).build();
  }

  public Object queueFile(String instance, String path) throws IOException {
    Registry.PathInfo info = registry.get(instance, path, true);
    if(info==null || !Files.exists(info.path)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    
    if(Files.isDirectory(info.path)) {
      // Queue all files in the directory
      final java.nio.file.Path root = info.repo.store;
      Files.walk(info.path, FileVisitOption.FOLLOW_LINKS ).forEach( s -> {
        String rel = root.relativize(s).toString().replace('\\', '/');
        Registry.PathInfo sub = registry.get(instance, rel, true);
        ProcessingReader r = sub.repo.getReader(sub.path, sub.name);
        if(r != null) {
          registry.workers.queue(new ProcessFileWorker(rel, r, sub));
        }
      });
    }
    else if(info.reader != null){
      registry.workers.queue(new ProcessFileWorker(path, info.reader, info));
    }
    try {
      Thread.sleep(25);
    } 
    catch (InterruptedException e) {}
    return registry.workers.getStatus();
  }
  
  @POST
  @Path("{instance}/store/{path : (.+)?}")
  public Object uploadFile(
      @PathParam("instance") 
      String instance,
      
      @PathParam("path") 
      String path,
      
      @DefaultValue("false")
      @QueryParam("queue")
      boolean queue,

      @Context
      HttpHeaders headers,
      
      InputStream data) throws IOException 
  {
    if(queue) {
      return queueFile(instance, path);
    }
    
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
    return doUploadFile(instance, path, stream, length, data);
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
    return doUploadFile("xxx", p, false, size, data);
  }
  
  FileUploadInfo doUploadFile(
      String instance,
      String path,
      boolean stream,
      Long length,
      InputStream data) throws IOException 
  {
    Registry.PathInfo info = registry.get(instance, path, true); // TODO, use stream!
    if(info==null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    WriteStreamWorker w = new WriteStreamWorker(path, info.path, data, length);
    FileWorker fp = null;
    if(info.reader != null) {
      if(stream) {
        fp = new ProcessFileWorkerStreaming(path, info.reader, info);
      }
      else {
        fp = new ProcessFileWorker(path, info.reader, info);
      }
    }
    
    if(stream) {
      LOGGER.info("STREAM: "+path);
      if(fp!=null) {
        w.child = fp;
        registry.workers.start(w.child);
      }
      registry.workers.run(w);
    }
    else {
      LOGGER.info("UPLOAD: "+path + " (Length: "+length+")");
      registry.workers.run(w);

      // If it uploaded OK, then queue processor
      if(fp != null && w.is( State.FINISHED) ) {
        registry.workers.queue(fp);
      }
    }
    return FileUploadInfo.make(info.path, info.repo.store, true);
  } 

  @GET
  @Path("{instance}/last/{path : (.+)?}")
  @ApiOperation( value="get value", notes="hello notes!!!" )
  @Produces(MediaType.APPLICATION_JSON)
  public Object getLAST(
      @PathParam("instance") 
      String instance,

      @PathParam("path") 
      String path
      ) throws Exception {
    
    SignalRepo porter = registry.repos.get(instance);
    if(porter==null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    
    if(Strings.isNullOrEmpty(path)) {
      return porter.last.getDB(null);
    }
    else if(path.endsWith("*")) {
      return porter.last.getDB( new Predicate<String>() {
        final String pfix = path.substring(0, path.length()-1);

        @Override
        public boolean test(String t) {
          return t.startsWith(pfix);
        }
      });
    }
    
    LastValue v = porter.last.get(path);
    if(v==null) {
      return Response.noContent().build();
    }
    return v;
  }
  

  @GET
  @Path("{instance}/json/{path : (.+)?}")
  @ApiOperation( value="get value", notes="hello notes!!!" )
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(
      @PathParam("instance") 
      String instance,

      @PathParam("path") 
      String path,

      @DefaultValue("false")
      @QueryParam("last") 
      boolean andLast
      ) throws Exception {
    
    SignalRepo porter = registry.repos.get(instance);
    if(porter==null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    
    Object rsp = porter.json.get(path);
    if(rsp==null) {
      return Response.noContent().build();
    }
    
    // Also get the last values!
    if(andLast) {
      Map<String, Object> res = new HashMap<String,Object>();
      res.put("json", rsp);
      res.put("last", porter.last.getDB(null));
      rsp = res;
    }
    return Response.ok(rsp).build();
  }

  @POST
  @Path("{instance}/json/{path : (.+)?}")
  @ApiOperation( value="set value", notes="hello notes!!!" )
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response setJSON(
      @PathParam("instance") 
      @ApiParam(example="myDB")
      String instance,

      @ApiParam(example="users/info/ryan")
      @PathParam("path") 
      String path,
      
      // Parsed by jersey!
      @ApiParam(example="{ \"first\": \"Ryan\" }")
      Map<String,Object> body
      ) throws Exception {

    SignalRepo porter = registry.repos.get(instance);
    if(porter ==null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    // Update the value
    porter.json.set(path, body);
    
    Map<String,Object> rsp = new HashMap<>();
    rsp.put("modified", porter.json.getLastModified());
    return Response.ok(rsp).build();
  }
  
  @GET
  @ApiOperation( value="get all names", notes="hello notes!!!" )
  @Produces(MediaType.APPLICATION_JSON)
  public Response getNames() throws Exception {
    return Response.ok(registry.repos.keySet()).build();
  }
}
