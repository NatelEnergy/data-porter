package com.natelenergy.porter.worker;
import java.nio.file.Path;
import java.util.Random;

public class ProcessFileWorker extends FileWorker {
  
  protected final Path f;
  protected final FileWorkerStatus status;
  
  public ProcessFileWorker(String path, Path dest) {
    status = new FileWorkerStatus(this, path);
    this.f = dest;
  }

  @Override
  public FileWorkerStatus getStatus() {
    return status;
  }


  @Override
  public void doRun() throws Exception {
    LOGGER.info("TODO, process: "+ f );
    Thread.sleep(2000 + new Random().nextInt(2000));
  }
}
