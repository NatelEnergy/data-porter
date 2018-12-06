package com.natelenergy.porter.natel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.natelenergy.porter.worker.influx.InfluxFactory;
import com.natelenergy.porter.worker.influx.InfluxWriter;

public class FFTFactory extends InfluxFactory
{
  private final Set<String> initalized = new HashSet<>();
  
  @Override
  @JsonIgnore
  public Object getStatus(String repo) {
    return initalized;
  }

  @Override
  protected InfluxWriter makeWriter(InfluxDB influx, String measurment) {
    return new InfluxWriter(influx, measurment) {

      @Override
      public void write(long time, Map<String, Object> record) {
        String name = (String)record.get("name");
        initalized.add(name);
        sb.append(name).append(' ');
        
        sb.append("xxx=1.23");
        
        sb.append(' ');
        sb.append(TimeUnit.MILLISECONDS.toNanos(time));
        sb.append('\n');
        
        count++;
        if(sb.length() > maxBuffer) {
          this.flush();
        }
      }
    };
  }
}