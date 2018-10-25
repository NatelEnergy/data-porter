package com.natelenergy.porter.model;

import java.io.StringReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.natelenergy.porter.model.LastValueDB;
import com.natelenergy.porter.model.LastValueDB.LastValue;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PeopleResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LastValueTest {
  @Test
  public void checkLastDB() throws Exception {
    
    LastValueDB db = new LastValueDB("test", null, null);
    db.write(1, "A", 100);
    db.write(2, "B", 200);
    db.write(3, "A", 300);
    db.write(2, "A", 400);
    
    assertThat(db.get("A").getValue()).isEqualTo(300);
    
    ObjectMapper mapper = new ObjectMapper();
    
    LastValue last = db.get("A");
    String json = mapper.writeValueAsString(db.get("A"));
    LastValue out = mapper.readValue(json, LastValue.class);
    assertThat(out.time).isEqualTo(last.time);
    assertThat(out.value).isEqualTo(last.value);
    
    System.out.println("VALUE:" + json );
    json = mapper.writeValueAsString(db.getDB(null));
    System.out.println("DB:" + json );
    
    LastValueDB copy = new LastValueDB("copy", null, null);
    copy.load( mapper, new StringReader(json) );
    String json2 = mapper.writeValueAsString(db.getDB(null));
    assertThat(json2).isEqualTo(json);
  }
}
