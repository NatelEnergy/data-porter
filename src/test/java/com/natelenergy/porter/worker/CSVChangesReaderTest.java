package com.natelenergy.porter.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.natelenergy.porter.model.LastValueDB;
import com.natelenergy.porter.util.JSONHelper;

/**
 * Unit tests for {@link PeopleResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CSVChangesReaderTest {
  
  public Path getTestFile(String name) {
    Path p = Paths.get("src","test","resources", "data", name);
    if(!Files.exists(p)) {
      System.out.println( "PATH: "+p.toAbsolutePath().toString() );
      System.out.println( " CWD: "+Paths.get(".").toAbsolutePath() );
      Assertions.fail( "Path should exist: "+p );
    }
    return p;
  }
  
  @Test
  public void readSystemChanges() throws Exception {
    Path p = getTestFile("system_changes.csv");
    
    LastValueDB last = new LastValueDB(null,null,null);
    FileWorkerStatus status = new FileWorkerStatus(null, "test");
    ReaderCSV reader = new ReaderCSV(p);
    reader.process(status, last);

    System.out.println( "AFTER: "+ JSONHelper.toJSON(last.getDB(null)));
    
    assertThat( last.get("ControllerPLC_Connected").getValue() ).isEqualTo( "Connected" );
//    assertThat( last.get("fLONG").getValue() ).isEqualTo( (long)10 );
//    assertThat( last.get("fFLOAT").getValue() ).isEqualTo( (float)10 );
//    assertThat( last.get("fDOUBLE").getValue() ).isEqualTo( (double)10 );
//
//    assertThat( last.get("fUINT64").getValue() ).isEqualTo( UnsignedLong.valueOf(10) );
//    assertThat( last.get("fUINT32").getValue() ).isEqualTo( UnsignedInteger.valueOf(10) );
    
  }
  

  @Test
  public void readFaultEvents() throws Exception {
    Path p = getTestFile("test-fault_events.csv");
    
    LastValueDB last = new LastValueDB(null,null,null);
    FileWorkerStatus status = new FileWorkerStatus(null, "test");
    ReaderCSV reader = new ReaderCSV(p);
    reader.process(status, last);

    System.out.println( "AFTER: "+ JSONHelper.toJSON(last.getDB(null)));
    
  //  assertThat( last.get("ControllerPLC_Connected").getValue() ).isEqualTo( "Connected" );
//    assertThat( last.get("fLONG").getValue() ).isEqualTo( (long)10 );
//    assertThat( last.get("fFLOAT").getValue() ).isEqualTo( (float)10 );
//    assertThat( last.get("fDOUBLE").getValue() ).isEqualTo( (double)10 );
//
//    assertThat( last.get("fUINT64").getValue() ).isEqualTo( UnsignedLong.valueOf(10) );
//    assertThat( last.get("fUINT32").getValue() ).isEqualTo( UnsignedInteger.valueOf(10) );
    
  }
}
