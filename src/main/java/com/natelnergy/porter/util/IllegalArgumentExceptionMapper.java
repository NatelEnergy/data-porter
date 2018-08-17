package com.natelnergy.porter.util;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.json.JSONException;
import org.json.JSONStringer;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
  @Override
  public Response toResponse(final IllegalArgumentException ex) {
    try {
      JSONStringer json = new JSONStringer();
      json.object();
      json.key("code").value(400);
      json.key("message").value(ex.getMessage());
      json.endObject();
      
      return Response
        .status(400)
        .entity(json.toString())
        .header("Content-Type", "application/json")
        .build();
    }
    catch(JSONException jex)
    {
      throw ex;
    }
  }
}