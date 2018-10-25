package com.natelenergy.porter.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistryConfig {
  @JsonProperty
  public String store = "data/store";

  @JsonProperty
  public String meta = "data/meta";

  @JsonProperty
  public List<String> init = new ArrayList<String>();
}
