package org.dcsa.bkg.model.transferobjects;

import org.dcsa.core.events.edocumentation.model.transferobject.CarrierClauseTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@DisplayName("Tests for CarrierClauseTOTest")
class CarrierClauseTOTest {
  private Validator validator;
  private CarrierClauseTO carrierClauseTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    carrierClauseTO = new CarrierClauseTO();
    carrierClauseTO.setClauseContent("x".repeat(150));
 }

  @Test
  @DisplayName("CarrierClauseTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<CarrierClauseTO>> violations = validator.validate(carrierClauseTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("CarrierClauseTO should throw error if unitPrice is not set.")
  void testToVerifyClauseContentIsRequired() {
    carrierClauseTO.setClauseContent(null);
    Set<ConstraintViolation<CarrierClauseTO>> violations = validator.validate(carrierClauseTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "ClauseContent is required.".equals(v.getMessage())));
  }
}
