package com.natelenergy.porter.resources;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.natelenergy.porter.model.JsonDB;
import com.natelenergy.porter.model.StringBacked.StringBackedConfigSupplier;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PeopleResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LiveDBTest {
//    private static final PersonDAO PERSON_DAO = mock(PersonDAO.class);
//    @ClassRule
//    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
//            .addResource(new PeopleResource(PERSON_DAO))
//            .build();
//    @Captor
//    private ArgumentCaptor<Person> personCaptor;
//    private Person person;
//
//    @Before
//    public void setUp() {
//        person = new Person();
//        person.setFullName("Full Name");
//        person.setJobTitle("Job Title");
//    }
//
//    @After
//    public void tearDown() {
//        reset(PERSON_DAO);
//    }
//
//    @Test
//    public void createPerson() throws JsonProcessingException {
//        when(PERSON_DAO.create(any(Person.class))).thenReturn(person);
//        final Response response = RESOURCES.target("/people")
//                .request(MediaType.APPLICATION_JSON_TYPE)
//                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE));
//
//        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
//        verify(PERSON_DAO).create(personCaptor.capture());
//        assertThat(personCaptor.getValue()).isEqualTo(person);
//    }
//
  
    public final ObjectMapper mapper = new ObjectMapper();
    public final StringBackedConfigSupplier cfg = new StringBackedConfigSupplier() {

      @Override
      public int getSaveInterval() {
        return 1000;
      }

      @Override
      public ObjectMapper getMapper() {
        return mapper;
      }
    };
  
    @Test
    public void checkDBManipulation() throws Exception {
      JsonDB db = new JsonDB("test", null, cfg);
      
      Map<String,Object> v = new HashMap<>();
      v.put("one", 1);
      v.put("two", 2.2);
      v.put("str", "string");
      db.set( "path/to/value", v );
      
      Map<String,Object> out = db.get("path/to/value");

      assertThat( out.get("one") ).isEqualTo( 1 );
      assertThat( out.get("str") ).isEqualTo( "string" );
      
//      Map<String,Object> sub = new HashMap<>();
//      sub.put("str", v);
//      db.patch( sub );
//      
//      assertThat( db.find("str.one") ).isEqualTo( 1 );
//      assertThat( db.find("str.str") ).isEqualTo( "string" );
//      
   //   System.out.println("GOT" + out );
      
      
//        final ImmutableList<Person> people = ImmutableList.of(person);
//        when(PERSON_DAO.findAll()).thenReturn(people);
//
//        final List<Person> response = RESOURCES.target("/people")
//            .request().get(new GenericType<List<Person>>() {
//            });
//
//        verify(PERSON_DAO).findAll();
//        assertThat(response).containsAll(people);
    }

}
