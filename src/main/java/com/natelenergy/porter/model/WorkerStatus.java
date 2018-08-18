package com.natelenergy.porter.model;

public class WorkerStatus {
  public static enum State {
    RUNNING,
    FINISHED,
    FAILED,
  }
  
  public WorkerStatus state = null;
  public long total = -1;
  public long progress = -1;
}
