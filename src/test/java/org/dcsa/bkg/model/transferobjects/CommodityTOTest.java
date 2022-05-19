package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dcsa.core.events.edocumentation.model.transferobject.CommodityTO;
import org.dcsa.core.events.model.enums.WeightUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Set;

@DisplayName("Tests for CommodityTO")
class CommodityTOTest {

  private Validator validator;
  private ObjectMapper objectMapper;
  private CommodityTO validCommodityTO;

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

    validCommodityTO = new CommodityTO();
    validCommodityTO.setCommodityType("x".repeat(20));
    validCommodityTO.setHsCode("x".repeat(10));
    validCommodityTO.setCargoGrossWeight(12.12);
    validCommodityTO.setCargoGrossWeightUnit(WeightUnit.KGM);
    validCommodityTO.setExportLicenseIssueDate(LocalDate.now());
    validCommodityTO.setExportLicenseExpiryDate(LocalDate.now());
  }

  @Test
  @DisplayName("CommodityTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<CommodityTO>> violations = validator.validate(validCommodityTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("CommodityTO should throw error if commodityType is not set.")
  void testToVerifyCommodityTypeIsRequired() {
    validCommodityTO.setCommodityType(null);
    Set<ConstraintViolation<CommodityTO>> violations = validator.validate(validCommodityTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "CommodityType is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("CommodityTO should throw error if commodityType length exceeds max size of 20")
  void testToVerifyCommodityTypeDoesNotExceedALengthOf20() {
    validCommodityTO.setCommodityType("x".repeat(21));
    Set<ConstraintViolation<CommodityTO>> violations = validator.validate(validCommodityTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(
                v -> "CommodityType has a max size of 20.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("CommodityTO should throw error if cargoGrossWeight is not set.")
  void testToVerifyCargoGrossWeightIsRequired() {
    validCommodityTO.setCargoGrossWeight(null);
    Set<ConstraintViolation<CommodityTO>> violations = validator.validate(validCommodityTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "CargoGrossWeight is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("CommodityTO should throw error if cargoGrossWeight is not set.")
  void testToVerifyCargoGrossWeightUnitIsRequired() {
    validCommodityTO.setCargoGrossWeightUnit(null);
    Set<ConstraintViolation<CommodityTO>> violations = validator.validate(validCommodityTO);
    Assertions.assertTrue(
            violations.stream()
                    .anyMatch(v -> "CargoGrossWeightUnit is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("CommodityTO should throw error if hsCode length exceeds max size of 10.")
  void testToVerifyCargoGrossWeightDoesNotExceedALengthOf10() {
    validCommodityTO.setHsCode("x".repeat(11));
    Set<ConstraintViolation<CommodityTO>> violations = validator.validate(validCommodityTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "HSCode has a max size of 10.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("CommodityTO should return yyyy-MM-dd date format for exportLicenseIssueDate.")
  void testToCheckISODateFormatForExportLicenseIssueDate() throws JsonProcessingException {
    JsonNode object = objectMapper.readTree(objectMapper.writeValueAsString(validCommodityTO));
    String exportLicenseIssueDate = object.get("exportLicenseIssueDate").asText();
    SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
    sdfrmt.setLenient(false);
    Assertions.assertDoesNotThrow(
        () -> {
          sdfrmt.parse(exportLicenseIssueDate);
        });
  }

  @Test
  @DisplayName("CommodityTO should return yyyy-MM-dd date format for exportLicenseExpiryDate.")
  void testToCheckISODateFormatForExportLicenseExpiryDate() throws JsonProcessingException {
    JsonNode object = objectMapper.readTree(objectMapper.writeValueAsString(validCommodityTO));
    String exportLicenseIssueDate = object.get("exportLicenseExpiryDate").asText();
    SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
    sdfrmt.setLenient(false);
    Assertions.assertDoesNotThrow(
        () -> {
          sdfrmt.parse(exportLicenseIssueDate);
        });
  }
}
