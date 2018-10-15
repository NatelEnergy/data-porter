package com.natelenergy.porter.worker;
import java.io.IOException;
import java.nio.file.Path;

public class ProcessStreamingFileWorker extends FileWorker {
  
  protected final Path f;
  protected final FileWorkerStatus status;
  protected final boolean waitForNudge;
 
  protected FileWorkerStatus.State nudgedState = null;
  protected boolean nudged = false;
  
  public ProcessStreamingFileWorker(String path, Path dest, boolean waitForNudge) {
    status = new FileWorkerStatus(this, path);
    this.f = dest;
    this.waitForNudge = waitForNudge;
  }

  @Override
  public FileWorkerStatus getStatus() {
    return status;
  }

  @Override
  public void nudge(FileWorkerStatus.State state) {
    this.nudgedState = state;
    nudged = true;
  }

  @Override
  public void doRun() throws IOException {
    
  }
}
