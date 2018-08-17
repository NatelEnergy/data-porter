package com.natelenergy.porter.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link PeopleResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PeopleResourceTest {
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
    @Test
    public void listPeople() throws Exception {
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
