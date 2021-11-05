package org.dcsa.bkg.model.transferobjects;

import org.dcsa.core.events.model.enums.ReferenceTypeCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@DisplayName("Tests for ReferenceTO")
class ReferenceTOTest {
  private Validator validator;
  private ReferenceTO validReferenceTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    validReferenceTO = new ReferenceTO();
    validReferenceTO.setReferenceType(ReferenceTypeCode.FF);
    validReferenceTO.setReferenceValue("x".repeat(100));
  }

  @Test
  @DisplayName("ReferenceTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<ReferenceTO>> violations = validator.validate(validReferenceTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("ReferenceTO should throw error if referenceType is not set.")
  void testToVerifyReferenceTypeIsRequired() {
    validReferenceTO.setReferenceType(null);
    Set<ConstraintViolation<ReferenceTO>> violations = validator.validate(validReferenceTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "ReferenceTypeCode is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ReferenceTO should throw error if referenceValue is not set.")
  void testToVerifyReferenceValueIsRequired() {
    validReferenceTO.setReferenceValue(null);
    Set<ConstraintViolation<ReferenceTO>> violations = validator.validate(validReferenceTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "ReferenceValue is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ReferenceTO should throw error if referenceValue length exceeds max size of 100.")
  void testToVerifyReferenceValueExceeds100() {
    validReferenceTO.setReferenceValue("x".repeat(101));
    Set<ConstraintViolation<ReferenceTO>> violations = validator.validate(validReferenceTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "ReferenceValue has max size of 100.".equals(v.getMessage())));
  }
}
