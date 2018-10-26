package com.natelenergy.porter.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

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
public class AvroReaderTest {
  @Test
  public void readUnsignedAvro() throws Exception {
    
    Path p = Paths.get(ClassLoader.getSystemResource("data/with-uint64-uint32.avro").toURI());
    System.out.println( "PATH: "+p);
    
    LastValueDB last = new LastValueDB(null,null,null);
    FileWorkerStatus status = new FileWorkerStatus(null, "test");
    ReaderAvro reader = new ReaderAvro(p);
    reader.process(status, last);

    Object u64 = last.get("fUINT64").getValue();
    Object u32 = last.get("fUINT32").getValue();

    System.out.println( "U64: "+ u64.getClass() + " // " + u64.toString());
    System.out.println( "U32: "+ u32.getClass() + " // " + u32.toString());
    
    System.out.println( "AFTER: "+ JSONHelper.toJSON(last.getDB(null)));
    

    assertThat( last.get("fINT").getValue() ).isEqualTo( 10 );
    assertThat( last.get("fLONG").getValue() ).isEqualTo( (long)10 );
    assertThat( last.get("fFLOAT").getValue() ).isEqualTo( (float)10 );
    assertThat( last.get("fDOUBLE").getValue() ).isEqualTo( (double)10 );

    assertThat( last.get("fUINT64").getValue() ).isEqualTo( UnsignedLong.valueOf(10) );
    assertThat( last.get("fUINT32").getValue() ).isEqualTo( UnsignedInteger.valueOf(10) );
    
  }
}
