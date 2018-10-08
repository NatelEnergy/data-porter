package com.natelenergy.porter.playground;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.opencsv.CSVReader;


public class CSVToJSON 
{
  public static void main( String[] args ) throws Exception
  {
    System.out.println( "Hello World!" );
    
    FileReader reader = new FileReader( new File( "C:\\Users\\ryan\\Downloads\\sc.csv"));
    CSVReader csv = new CSVReader(reader);
    
    List<List<Number>> values = new ArrayList<>();
    Map<String, List<Number>> cols = new LinkedHashMap<>();
    for(String[] row : csv ) {
      if(row.length > 2 && !Strings.isNullOrEmpty(row[0])) {
        if(cols.isEmpty()) {
          for(String c : row) {
            List v = new ArrayList<>();
            values.add(v);
            cols.put(c,v);
          }
        }
        else if(row.length == cols.size() ) {
          for(int i=0; i<row.length; i++) {
            values.get(i).add(toNumber(row[i]));
          }
        }
        else {
          System.err.println( "BAD Line: "+row.length );
        }
      }
    }
    
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    
    System.out.println( mapper.writeValueAsString(cols));
    
    System.out.println( "done done" );
  }

  private static Number toNumber(String v) {
    v = v.replace("\"", "").replace("$", "").replace(",", "");;
    if(v.indexOf('.') >= 0) {
      return Double.parseDouble(v);
    }
    return Integer.parseInt(v);
  }
}
