package com.natelenergy.porter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.natelenergy.porter.model.LiveDBConfiguration;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class PorterServerConfiguration extends Configuration {
  
  @JsonProperty("swagger")
  public SwaggerBundleConfiguration swaggerBundleConfiguration;

  @JsonProperty
  public LiveDBConfiguration liveDB = new LiveDBConfiguration();
}
