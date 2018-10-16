package com.natelenergy.porter.worker;
import java.nio.file.Path;
import java.util.Random;

import com.natelenergy.porter.worker.FileWorkerStatus.State;

public class ProcessStreamingFileWorker extends FileWorker {
  
  protected final Path f;
  protected final FileWorkerStatus status;
 
  protected FileWorkerStatus.State nudgedState = null;
  
  public ProcessStreamingFileWorker(String path, Path dest) {
    status = new FileWorkerStatus(this, path);
    this.f = dest;
  }

  @Override
  public FileWorkerStatus getStatus() {
    return status;
  }

  @Override
  public void nudge(FileWorkerStatus.State state) {
    this.nudgedState = state;
  }

  @Override
  public void doRun() throws Exception {
    while(true) {
      LOGGER.info("TODO, read: "+f + " // " + this.nudgedState);
      Thread.sleep(2000 + new Random().nextInt(2000));
      if(nudgedState == State.FINISHED) {
        return;
      }
    }
  }
}
