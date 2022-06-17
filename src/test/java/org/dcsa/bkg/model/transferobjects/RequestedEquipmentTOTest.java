package org.dcsa.bkg.model.transferobjects;

import org.dcsa.core.events.edocumentation.model.transferobject.RequestedEquipmentTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@DisplayName("Tests for RequestedEquipmentTO")
class RequestedEquipmentTOTest {
  private Validator validator;
  private RequestedEquipmentTO validRequestedEquipmentTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    validRequestedEquipmentTO = new RequestedEquipmentTO();
    validRequestedEquipmentTO.setRequestedEquipmentSizeType("x".repeat(4));
    validRequestedEquipmentTO.setRequestedEquipmentUnits((int) (Math.random() * 100));
    validRequestedEquipmentTO.setShipperOwned(true);
  }

  @Test
  @DisplayName("RequestedEquipmentTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<RequestedEquipmentTO>> violations =
        validator.validate(validRequestedEquipmentTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("RequestedEquipmentTO should throw error if requestedEquipmentSizeType is not set.")
  void testToVerifyRequestedEquipmentSizeTypeIsRequired() {
    validRequestedEquipmentTO.setRequestedEquipmentSizeType(null);
    Set<ConstraintViolation<RequestedEquipmentTO>> violations =
        validator.validate(validRequestedEquipmentTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "RequestedEquipmentSizeType is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "RequestedEquipmentTO should throw error if requestedEquipmentSizeType length exceeds max size of 4.")
  void testToVerifyRequestedEquipmentSizeTypeIsNotAllowedToExceed4() {
    validRequestedEquipmentTO.setRequestedEquipmentSizeType("x".repeat(5));
    Set<ConstraintViolation<RequestedEquipmentTO>> violations =
        validator.validate(validRequestedEquipmentTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(
                v -> "RequestedEquipmentSizeType has a max size of 4.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "RequestedEquipmentTO should throw error if requestedEquipmentUnits is not a positive value.")
  void testToVerifyRequestedEquipmentUnitsCannotBeZeroOrNegative() {
    validRequestedEquipmentTO.setRequestedEquipmentUnits(-1);
    Set<ConstraintViolation<RequestedEquipmentTO>> violations =
        validator.validate(validRequestedEquipmentTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(
                v -> "RequestedEquipmentUnits has to be a positive value.".equals(v.getMessage())));
  }
}
