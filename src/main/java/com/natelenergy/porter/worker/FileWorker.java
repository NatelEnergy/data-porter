package com.natelenergy.porter.worker;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natelenergy.porter.worker.FileWorkerStatus.State;

public abstract class FileWorker implements Runnable {
  protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public abstract FileWorkerStatus getStatus();
  public abstract long doRun() throws Exception;

  public FileWorker child;
  
  /**
   * Some workers are waiting on parent events
   */
  public void nudge(FileWorkerStatus.State state) {
    // Default EMPTY
  }
  
  public boolean is(FileWorkerStatus.State state) {
    return state == getStatus().state;
  }
  
  @Override
  public void run() {
    FileWorkerStatus s = getStatus();
    try {
      s.state = State.RUNNING;
      s.started = System.currentTimeMillis();
      this.doRun();
    }
    catch(Exception ex) {
      s.addError(ex);
      s.state = State.FAILED;
      LOGGER.info("Failed: "+s.path, ex);
    }
    finally {
      if(child!=null) {
        try {
          child.nudge(State.FINISHED);
        }
        catch(Exception ex) {
          LOGGER.warn("Nudge failed! "+s.path, ex);
        }
      }
      
      if(s.state == State.RUNNING) {
        s.state = State.FINISHED;
      }
      s.finished = System.currentTimeMillis();
    }
  }
}
