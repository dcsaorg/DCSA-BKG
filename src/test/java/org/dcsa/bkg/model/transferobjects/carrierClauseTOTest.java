package org.dcsa.bkg.model.transferobjects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@DisplayName("Tests for ShipmentLocationTOTest")
class carrierClauseTOTest {
  private Validator validator;
  private CarrierClauseTO carrierClauseTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    carrierClauseTO = new CarrierClauseTO();
    carrierClauseTO.setClauseContent("A bunch of text!");
  }

  @Test
  @DisplayName("ShipmentLocationTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<CarrierClauseTO>> violations = validator.validate(carrierClauseTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("ShipmentLocationTO should throw error if locationType is not set.")
  void testToVerifyLocationTypeIsRequired() {
    carrierClauseTO.setClauseContent(null);
    Set<ConstraintViolation<CarrierClauseTO>> violations = validator.validate(carrierClauseTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Clause Content is required.".equals(v.getMessage())));
  }
}
