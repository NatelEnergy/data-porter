package com.natelenergy.porter.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkerRegistry {

  private final BlockingQueue<Runnable> buffer;
  private final ExecutorService fixed;
  private final ExecutorService cached;
  
  private Map<FileWorker,FileWorker> active = new ConcurrentHashMap<>();
  
  private FileWorkerStatus[] history = new FileWorkerStatus[20];
  private int historyIndex = 0;
  
  private class RunWrapper implements Runnable {
    final FileWorker worker;
    RunWrapper(FileWorker w) {
      this.worker = w;
      active.put(worker, worker);
    }
    
    @Override
    public void run() {
      try {
        worker.run();
      }
      finally {
        active.remove(worker);
        
        // Add to the history
        synchronized(history) {
          history[historyIndex] = worker.getStatus();
          if(++historyIndex > history.length) {
            historyIndex = 0;
          }
        }
      }
    }
  }
  
  public WorkerRegistry() {
    this.cached = Executors.newCachedThreadPool();
    this.buffer = new LinkedBlockingQueue<>();
    
    int nThreads = 2;
    this.fixed = new ThreadPoolExecutor(nThreads, nThreads,
      1L, TimeUnit.SECONDS, // keep alive time
      this.buffer);
  }
  
  //----------------------------------------------------------------------------
  //----------------------------------------------------------------------------
  
  public static class RegistryStatus {
    public int queued = 0;
    public List<FileWorkerStatus> active;
    public List<FileWorkerStatus> history;
  }
  
  public RegistryStatus getStatus()
  {
    RegistryStatus s = new RegistryStatus();
    s.active = new ArrayList<>();
    for(FileWorker w : active.values()) {
      s.active.add( w.getStatus() );
    }
    
    s.history = new ArrayList<>(this.history.length);
    for(int i=0; i<this.history.length; i++) {
      if(this.history[i] != null) {
        s.history.add(this.history[i]);
      }
    }
    s.queued = buffer.size();
    return s;
  }
  
  //----------------------------------------------------------------------------
  //----------------------------------------------------------------------------
  
  
  /**
   * Run the worker in the current thread
   */
  public void run(FileWorker worker)
  {
    new RunWrapper(worker).run();
  }
  
  /**
   * Start the worker in a background thread
   */
  public Future<?> start(FileWorker worker) {
    return cached.submit(new RunWrapper(worker));
  }

  /**
   * Queue the worker in a background thread
   */
  public Future<?> queue(FileWorker worker) {
    return fixed.submit(new RunWrapper(worker));
  }
}
