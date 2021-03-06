package com.natelenergy.porter.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.natelenergy.porter.util.JSONHelper;
import com.natelenergy.porter.worker.influx.InfluxFactory;
/**
 * Unit tests for {@link PeopleResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RepoConfigs {
  @Test
  public void checkRepoConfigs() throws Exception {
    
    SignalRepoConfig cfg = new SignalRepoConfig();
    cfg.validate();
    
    InfluxFactory f = new InfluxFactory();
    cfg.processors.add(f);
    
    System.out.println( "X:" + JSONHelper.toJSON(cfg) );
  }
}
