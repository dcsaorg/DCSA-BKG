package org.dcsa.bkg.model.transferobjects;

import org.dcsa.bkg.model.enums.ValueAddedServiceCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@DisplayName("Tests for ValueAddedServiceRequestTO")
class ValueAddedServiceRequestTOTest {

  private Validator validator;
  private ValueAddedServiceRequestTO valueAddedServiceRequestTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    valueAddedServiceRequestTO = new ValueAddedServiceRequestTO();
    valueAddedServiceRequestTO.setValueAddedServiceCode(ValueAddedServiceCode.CDECL);
  }

  @Test
  @DisplayName("ValueAddedServiceRequestTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<ValueAddedServiceRequestTO>> violations =
        validator.validate(valueAddedServiceRequestTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("ValueAddedServiceRequestTO should throw error if valueAddedServiceCode is not set.")
  void testToVerifyValueAddedServiceCodeIsRequired() {
    valueAddedServiceRequestTO.setValueAddedServiceCode(null);
    Set<ConstraintViolation<ValueAddedServiceRequestTO>> violations =
        validator.validate(valueAddedServiceRequestTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "ValueAddedServiceCode is required.".equals(v.getMessage())));
  }
}
