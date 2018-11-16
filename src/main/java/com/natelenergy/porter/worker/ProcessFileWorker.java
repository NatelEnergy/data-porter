package com.natelenergy.porter.worker;

import java.util.function.Supplier;

import com.natelenergy.porter.model.ValueProcessor;

public class ProcessFileWorker extends FileWorker {
  protected final FileWorkerStatus status;
  protected final ProcessingReader reader;
  protected final Supplier<ValueProcessor> supplier;
  
  public ProcessFileWorker(String path, ProcessingReader reader, Supplier<ValueProcessor> supplier) {
    status = new FileWorkerStatus(this, path);
    this.reader = reader;
    this.supplier = supplier;
  }

  @Override
  public FileWorkerStatus getStatus() {
    return status;
  }

  @Override
  public long doRun() throws Exception {
    try(ValueProcessor p = supplier.get()) {
      return this.reader.process(this.status, p);
    }
  }
}
