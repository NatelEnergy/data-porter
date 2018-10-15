package com.natelenergy.porter.api.v0;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natelenergy.porter.worker.WorkerRegistry;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.invoke.MethodHandles;
import io.swagger.annotations.*;

@Path("/workers")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="/workers", tags="Manage Workers")
public class WorkersResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final WorkerRegistry workers;
  
  public WorkersResource(WorkerRegistry workers) {
    this.workers = workers;
  }

  @GET
  @Path("status")
  public Object status()
  {
    return workers.getStatus();
  }
}
