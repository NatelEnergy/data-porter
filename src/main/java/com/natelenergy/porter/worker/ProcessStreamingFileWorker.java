package com.natelenergy.porter.worker;
import com.natelenergy.porter.worker.FileWorkerStatus.State;

public class ProcessStreamingFileWorker extends ProcessFileWorker {
  
  protected FileWorkerStatus.State nudgedState = null;
  
  public ProcessStreamingFileWorker(String path, FileIndexer indexer) {
    super(path,indexer);
  }

  @Override
  public void nudge(FileWorkerStatus.State state) {
    this.nudgedState = state;
  }

  @Override
  public void doRun() throws Exception {
    while(true) {
      super.doRun();
      
      LOGGER.info("still running... so sleep and try again: "+status.path);
      Thread.sleep(2000);
      if(nudgedState == State.FINISHED || nudgedState == State.FAILED) {
        return;
      }
    }
  }
}
