package com.natelenergy.porter.worker;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class WriteStreamWorker extends FileWorker {
  
  final Path f;
  final InputStream stream;
  final FileWorkerStatus status;
  
  public WriteStreamWorker(String path, Path dest, InputStream stream, Long length) {
    status = new FileWorkerStatus(this, path);
    status.size = length;
    this.f = dest;
    this.stream = stream;
  }

  @Override
  public FileWorkerStatus getStatus() {
    return status;
  }

  @Override
  public long doRun() throws IOException {
    Path dir = f.getParent();
    if( !Files.exists(dir) ) {
      Files.createDirectories(dir);
    }
    
    // Replace the existing file
    Files.deleteIfExists(f);
    
    long cursor = 0;
    long ts = System.currentTimeMillis();
    try( OutputStream outStream = Files.newOutputStream(f, 
        StandardOpenOption.CREATE, 
        StandardOpenOption.WRITE ))
    {
      byte[] buffer = new byte[8 * 1024];
      int bytesRead;
      while ((bytesRead = stream.read(buffer)) != -1) {
        outStream.write(buffer, 0, bytesRead);
        cursor += bytesRead;
        status.cursor = cursor;
        
        long now = System.currentTimeMillis();
        long elapsed = now-ts;
        if (elapsed > 1500) {
          outStream.flush();
          ts = now;
          if(child != null) {
            child.nudge(FileWorkerStatus.State.RUNNING);
          }
        }
      }
    }
    finally {
      // Remove empty files
      if(cursor<1) {
        Files.delete(f);
      }
    }
    return cursor;
  }
}
