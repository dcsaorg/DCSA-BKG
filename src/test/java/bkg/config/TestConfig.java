package bkg.config;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.restassured.RestAssured;
import org.hamcrest.Matcher;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV4;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestConfig {

  // API endpoints
  public static final String BOOKING = "/v1/bookings";

  private TestConfig() {}

  public static void init() throws IOException {
    File file = new File("src/test/resources/test.properties");
    try (FileInputStream fis = new FileInputStream(file)) {
      Properties properties = new Properties();
      properties.load(fis);
      RestAssured.baseURI = properties.getProperty("base_url");
      RestAssured.port = Integer.parseInt(properties.getProperty("port"));
    }
  }

  public static Matcher<?> jsonSchemaValidator(String fileName) {
    return matchesJsonSchemaInClasspath("schema/" + fileName + ".json")
        .using(
            JsonSchemaFactory.newBuilder()
                .setValidationConfiguration(
                    ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV4).freeze())
                .freeze());
  }

  public static void validateAgainstJsonSchema(
      EntityExchangeResult<byte[]> response, String schemaName) {
    String responseJson = new String(response.getResponseBody(), StandardCharsets.UTF_8);
    Matcher matcher = jsonSchemaValidator(schemaName);
    assertThat(responseJson, matcher);
  }
}
