package com.natelenergy.porter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.natelenergy.porter.model.LiveDBConfiguration;
import com.natelenergy.porter.processor.ProcessingConfig;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class PorterServerConfiguration extends Configuration {
  
  @JsonProperty("swagger")
  public SwaggerBundleConfiguration swaggerBundleConfiguration;

  @JsonProperty
  public LiveDBConfiguration liveDB = new LiveDBConfiguration();

  @JsonProperty
  public List<ProcessingConfig> processing = new ArrayList<>();
}
