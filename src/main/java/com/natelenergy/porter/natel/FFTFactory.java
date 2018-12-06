package com.natelenergy.porter.natel;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
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

  public static String toBase64FFT(List<Float> spectrum)
  {
    ByteBuffer buf = ByteBuffer.allocate(Float.SIZE / Byte.SIZE * spectrum.size());
    for(Float f : spectrum) {
      buf.putFloat(f.floatValue());
    }
    return Base64.getEncoder().encodeToString(buf.array());
  }
  
  @Override
  protected InfluxWriter makeWriter(InfluxDB influx, String measurment) {
    return new InfluxWriter(influx, measurment) {

      @Override
      public void write(long time, Map<String, Object> record) {
        String name = (String)record.get("name");
        initalized.add(name);
        sb.append(name).append(' ');
        
        double resolution = (double)record.get("resolution");
        List<Float> spectrum = (List<Float>)record.get("spectrum");
        List<Integer> dominant = (List<Integer>)record.get("dominant");
            
        sb.append("r=").append(NUMBER_FORMATTER.get().format(resolution));
        sb.append(",t=").append(record.get("sampleMS")+"i");
        sb.append(",dr=").append(NUMBER_FORMATTER.get().format(record.get("drivingFrequency")));
        
        for(int i=0; i<dominant.size(); i++) {
          int idx = dominant.get(i);
          if(idx < spectrum.size()) {
            double f = idx * resolution;
            Float mag = spectrum.get(idx);
            sb.append(",d"+i+"F=").append(NUMBER_FORMATTER.get().format(f));
            sb.append(",d"+i+"M=").append(NUMBER_FORMATTER.get().format(mag));
          }
        }
        
        sb.append(",fft=\"").append(toBase64FFT(spectrum)).append( "\" ");
        
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