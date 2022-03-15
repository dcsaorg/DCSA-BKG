package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dcsa.core.events.edocumentation.model.transferobject.ShipmentLocationTO;
import org.dcsa.core.events.model.enums.LocationType;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Set;

@DisplayName("Tests for ShipmentLocationTOTest")
class ShipmentLocationTOTest {
  private Validator validator;
  private ObjectMapper objectMapper;
  private ShipmentLocationTO shipmentLocationTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    // replicating default spring boot config for object mapper
    // for reference
    // https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/jackson/JacksonAutoConfiguration.java
    objectMapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    LocationTO location = new LocationTO();
    location.setId("x".repeat(100));

    shipmentLocationTO = new ShipmentLocationTO();
    shipmentLocationTO.setLocation(location);
    shipmentLocationTO.setShipmentLocationTypeCode(LocationType.DRL);
    shipmentLocationTO.setDisplayedName("x".repeat(250));
    shipmentLocationTO.setEventDateTime(OffsetDateTime.now());
  }

  @Test
  @DisplayName("ShipmentLocationTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<ShipmentLocationTO>> violations =
        validator.validate(shipmentLocationTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("ShipmentLocationTO should throw error if location is not set.")
  void testToVerifyLocationTOIsRequired() {
    shipmentLocationTO.setLocation(null);
    Set<ConstraintViolation<ShipmentLocationTO>> violations =
        validator.validate(shipmentLocationTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Location is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "ShipmentLocationTO should throw error if displayedName length exceeds max size of 250.")
  void testToDisplayedNameIsNotAllowedToExceed250() {
    shipmentLocationTO.setDisplayedName("x".repeat(251));
    Set<ConstraintViolation<ShipmentLocationTO>> violations =
        validator.validate(shipmentLocationTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "DisplayName has a max size of 250.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ShipmentLocationTO should throw error if locationType is not set.")
  void testToVerifyLocationTypeIsRequired() {
    shipmentLocationTO.setShipmentLocationTypeCode(null);
    Set<ConstraintViolation<ShipmentLocationTO>> violations =
        validator.validate(shipmentLocationTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "LocationType is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ShipmentLocationTO should return yyyy-MM-dd date format for eventDateTime.")
  void testToCheckISODateFormatForExportLicenseExpiryDate() throws JsonProcessingException {
    JsonNode object = objectMapper.readTree(objectMapper.writeValueAsString(shipmentLocationTO));
    String eventDateTime = object.get("eventDateTime").asText();
    SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
    sdfrmt.setLenient(false);
    Assertions.assertDoesNotThrow(
        () -> {
          sdfrmt.parse(eventDateTime);
        });
  }
}
