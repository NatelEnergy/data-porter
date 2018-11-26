package com.natelenergy.porter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.natelenergy.porter.model.RegistryConfig;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class SignalServerConfiguration extends Configuration {
  
  @JsonProperty("swagger")
  public SwaggerBundleConfiguration swaggerBundleConfiguration;

  @JsonProperty
  public RegistryConfig repos;
}
