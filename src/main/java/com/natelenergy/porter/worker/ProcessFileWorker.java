package com.natelenergy.porter.worker;

public class ProcessFileWorker extends FileWorker {
  
  protected final FileWorkerStatus status;
  protected final FileIndexer indexer;
  
  public ProcessFileWorker(String path, FileIndexer indexer) {
    status = new FileWorkerStatus(this, path);
    this.indexer = indexer;
  }

  @Override
  public FileWorkerStatus getStatus() {
    return status;
  }

  @Override
  public long doRun() throws Exception {
    return this.indexer.process(this.status);
  }
}
