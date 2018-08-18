package com.natelenergy.porter.playground;

import java.io.File;

import org.apache.avro.io.*;
import org.apache.avro.generic.*;
import org.apache.avro.file.*;


public class KeepReadingFile 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Hello World!" );
        
        int sleeps = 0;
        File dir = new File( "C:\\natel\\data\\big-loop-agent\\channel\\2018\\08\\14");
        for(File f : dir.listFiles()) {
          if(f.getName().endsWith(".avro")) {
            System.out.println( "Open: "+f.getName() );
            
            
            DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
            
            GenericRecord record = null;
            
            long lastSync = -1;
            while(lastSync < f.length()) {

              int count = 0;
              SeekableFileInput sin = new SeekableFileInput(f);
              DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(sin, datumReader);
              //Schema schema = dataFileReader.getSchema();
              System.out.println("open: "+f.getName());
              if(lastSync>0) {
                System.out.println("sync: "+lastSync);
                dataFileReader.seek(lastSync);
                dataFileReader.previousSync();
              }
              
              while (dataFileReader.hasNext()) {
                  record = dataFileReader.next(record);
                  long when = (long)record.get("epoch");
                  System.out.println( sleeps + " // " + (count++) + " // " +when);
              }
              
              if(count<1) {
                break;
              }
              
              lastSync = dataFileReader.previousSync();
              sleeps++;
              System.out.println( "sleeping 1s.... " + lastSync + " //" + f.getName() );
              Thread.sleep(2500);
            }
          }
        }

        System.out.println( "done done" );
    }
}
