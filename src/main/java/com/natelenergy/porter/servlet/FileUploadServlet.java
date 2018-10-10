package com.natelenergy.porter.servlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.util.*;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

import org.apache.commons.fileupload.servlet.*;

public class FileUploadServlet extends HttpServlet {
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }
    
    public void process(String path, InputStream stream) throws IOException
    {
      System.out.println("STREAM: "+path);
      String v = IOUtils.toString( stream, Charsets.UTF_8 );
      System.out.println(">>> " + v);
    }
 
    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
      
      if("POST".equals(request.getMethod())) {
        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        try {
          if(isMultipart) {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    System.out.println("Form field " + name + " with value " + Streams.asString(stream) + " detected.");
                } else {
                  this.process("XXXXX", stream);
                }
            }
          }
          else {
            this.process("XXXXX", request.getInputStream());
          }
        }
        catch(Exception ex) {
          ex.printStackTrace();
        }
        // XXX
        PrintWriter writer = response.getWriter();
        writer.println( "done!" );
      }
    }
}