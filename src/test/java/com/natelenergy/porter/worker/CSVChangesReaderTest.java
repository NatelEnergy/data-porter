package com.natelenergy.porter.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.natelenergy.porter.model.LastValueDB;
import com.natelenergy.porter.util.JSONHelper;

/**
 * Unit tests for {@link PeopleResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CSVChangesReaderTest {
  @Test
  public void readChanges() throws Exception {
    Path p = Paths.get("src","test","resources", "data", "system_changes.csv");
    if(!Files.exists(p)) {
      System.out.println( "PATH: "+p.toAbsolutePath().toString() );
      System.out.println( " CWD: "+Paths.get(".").toAbsolutePath() );
      Assertions.fail( "Path should exist: "+p );
    }
    
    LastValueDB last = new LastValueDB(null,null,null);
    FileWorkerStatus status = new FileWorkerStatus(null, "test");
    ReaderCSV reader = new ReaderCSV(p);
    reader.process(status, last);

    System.out.println( "AFTER: "+ JSONHelper.toJSON(last.getDB(null)));
    

//    assertThat( last.get("fINT").getValue() ).isEqualTo( 10 );
//    assertThat( last.get("fLONG").getValue() ).isEqualTo( (long)10 );
//    assertThat( last.get("fFLOAT").getValue() ).isEqualTo( (float)10 );
//    assertThat( last.get("fDOUBLE").getValue() ).isEqualTo( (double)10 );
//
//    assertThat( last.get("fUINT64").getValue() ).isEqualTo( UnsignedLong.valueOf(10) );
//    assertThat( last.get("fUINT32").getValue() ).isEqualTo( UnsignedInteger.valueOf(10) );
    
  }
}
