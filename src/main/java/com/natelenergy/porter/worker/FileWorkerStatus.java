package com.natelenergy.porter.worker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class FileWorkerStatus {
  public static enum MessageType {
    INFO,
    WARNING,
    ERROR,
  }

  @JsonInclude(Include.NON_NULL)
  public static class Message {
    public MessageType type;
    public String text;
    public String details;
  }
  
  public static enum State {
    QUEUED,
    RUNNING,
    FINISHED,
    FAILED,
  }
  
  public final String worker;
  public final String path;
  public State state;
  
  // File Size stuff
  public Long size;
  public Long cursor;
  
  // Data info
  public Long count;
  public Long time;

  // Timing Info
  public final Long queued;
  public Long started;
  public Long finished;
  
  public List<Message> messages;
  
  
  public FileWorkerStatus(FileWorker worker, String path) {
    this.worker = (worker==null)? "<none>" : worker.getClass().getSimpleName();
    this.path = path.replace('\\', '/');
    this.queued = System.currentTimeMillis();
    this.state = State.QUEUED;
  }
  
  public void addMessage(Message msg) {
    if(this.messages == null) {
      this.messages = new ArrayList<>();
    }
    this.messages.add(msg);
  }
  
  public void addError(Exception ex) {
    Message m = new Message();
    m.text = "Error: "+ex.toString();
    
    StringWriter errors = new StringWriter();
    ex.printStackTrace(new PrintWriter(errors));
    m.details = errors.toString();
    addMessage(m);
  }
}
