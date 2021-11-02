package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dcsa.bkg.model.enums.CutOffDateTimeCode;
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

@DisplayName("Tests for ShipmentCutOffTimeTOTest")
class ShipmentCutOffTimeTOTest {
  private Validator validator;
  private ObjectMapper objectMapper;
  private ShipmentCutOffTimeTO shipmentCutOffTimeTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    shipmentCutOffTimeTO = new ShipmentCutOffTimeTO();
    shipmentCutOffTimeTO.setCutOffDateTime(OffsetDateTime.now());
    shipmentCutOffTimeTO.setCutOffDateTimeCode(CutOffDateTimeCode.AFD);
  }

  @Test
  @DisplayName("ConfirmedEquipmentTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<ShipmentCutOffTimeTO>> violations =
        validator.validate(shipmentCutOffTimeTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("ShipmentCutOffTimeTO should throw error if CutOffDateTime is not set.")
  void testToVerifyCutOffDateTimeIsRequired() {
    shipmentCutOffTimeTO.setCutOffDateTime(null);
    Set<ConstraintViolation<ShipmentCutOffTimeTO>> violations =
        validator.validate(shipmentCutOffTimeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Cut Off Date Time is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ShipmentCutOffTimeTO should throw error if CutOffDateTimeCode is not set.")
  void testToVerifyCutOffDateTimeCodeIsRequired() {
    shipmentCutOffTimeTO.setCutOffDateTimeCode(null);
    Set<ConstraintViolation<ShipmentCutOffTimeTO>> violations =
        validator.validate(shipmentCutOffTimeTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "Cut Off Date Time Code is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ShipmentCutOffTimeTO should return yyyy-MM-dd date format for cutOffDateTime.")
  void testToCheckISODateFormatForExportLicenseExpiryDate() throws JsonProcessingException {
    JsonNode object = objectMapper.readTree(objectMapper.writeValueAsString(shipmentCutOffTimeTO));
    String eventDateTime = object.get("cutOffDateTime").asText();
    SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
    sdfrmt.setLenient(false);
    Assertions.assertDoesNotThrow(
        () -> {
          sdfrmt.parse(eventDateTime);
        });
  }
}
