package com.natelenergy.porter.worker;
import java.util.function.Supplier;

import com.natelenergy.porter.processor.ValueProcessor;
import com.natelenergy.porter.worker.FileWorkerStatus.State;

public class ProcessFileWorkerStreaming extends ProcessFileWorker {
  
  protected FileWorkerStatus.State nudgedState = null;
  
  public ProcessFileWorkerStreaming(String path, ProcessingReader reader, Supplier<ValueProcessor> supplier) {
    super(path,reader,supplier);
  }

  @Override
  public void nudge(FileWorkerStatus.State state) {
    this.nudgedState = state;
  }

  @Override
  public long doRun() throws Exception {
    int loops = 0;
    long count = 0;
    
    while(true) {
      long t = 0;
      try {
        t = super.doRun();
      }
      catch(Exception ex) {
        if(t>0 || count > 0) {
          throw ex;
        }
        // Ignore errors before we have read anything
      }
      
      if(t > 0) {
        count += t;
        loops = 0;
        status.loops = null;
      }
      else {
        status.loops = ++loops;
      }
      
      Thread.sleep(2500);
      if(nudgedState == State.FINISHED || nudgedState == State.FAILED) {
        return count;
      }
    }
  }
}
