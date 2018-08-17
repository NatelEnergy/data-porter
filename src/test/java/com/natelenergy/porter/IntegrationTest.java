package com.natelenergy.porter;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

import org.apache.poi.hssf.util.CellReference;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.natelnergy.porter.PorterServerApplication;
import com.natelnergy.porter.PorterServerConfiguration;
import com.natelnergy.porter.api.v0.UploadResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTest {

  private static final String TMP_FILE = createTempFile();
  private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");

  @ClassRule
  public static final DropwizardAppRule<PorterServerConfiguration> RULE = 
    new DropwizardAppRule<>(
        PorterServerApplication.class, CONFIG_PATH
       // ConfigOverride.config("database.url", "jdbc:h2:" + TMP_FILE)
    );
  
  private static UploadResource EXAMPLES;

  @BeforeClass
  public static void migrateDb() throws Exception {
    // RULE.getApplication().run("db", "migrate", CONFIG_PATH);
    //LOCATOR = new WorkbookLocator(Files.createTempDirectory("xls-test").toFile(), RULE.getObjectMapper() );
    EXAMPLES = new UploadResource();
  }

  private static String createTempFile() {
    try {
      return File.createTempFile("test-example", null).getAbsolutePath();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void testEvaluationRuns() throws Exception {
    CellReference ref = new CellReference("sheet!B12");
    assertThat(ref.getSheetName()).isEqualTo("sheet");
    assertThat(ref.getRow()).isEqualTo(11);
    assertThat(ref.getCol()).isEqualTo((short)1);
    
//    final EvaluateRequest req = EXAMPLES.getEvaluateRequest(); //.loadAssetRequestExample("hydro/slh_01.json");
//    System.out.println( "REQ: " + RULE.getObjectMapper().writeValueAsString(req));
//    
//    final EvaluateResponse rsp = postRequest(req);
//    
//    System.out.println( "RSP: "+ RULE.getObjectMapper().writeValueAsString(rsp));
      
//    final Person person = new Person("Dr. IntegrationTest", "Chief Wizard");
//    final Person newPerson = postPerson(person);
//    final String url = "http://localhost:" + RULE.getLocalPort() + "/people/" + newPerson.getId() + "/" + viewName;
//    Response response = RULE.client().target(url).request().get();
//    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
  }

//  private EvaluateResponse postRequest(EvaluateRequest req) {
//    return RULE.client()
//        .target("http://localhost:" + RULE.getLocalPort() + "/sheet/evaluate")
//        .request()
//        .post(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE))
//        .readEntity(EvaluateResponse.class);
//  }

  @Test
  public void testLogFileWritten() throws IOException {
    // The log file is using a size and time based policy, which used to silently
    // fail (and not write to a log file). This test ensures not only that the
    // log file exists, but also contains the log line that jetty prints on startup
    final Path log = Paths.get("./logs/application.log");
    assertThat(log).exists();
    final String actual = new String(Files.readAllBytes(log), UTF_8);
    assertThat(actual).contains("0.0.0.0:" + RULE.getLocalPort());
  }
}
