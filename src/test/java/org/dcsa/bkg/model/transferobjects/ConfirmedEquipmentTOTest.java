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

@DisplayName("Tests for ConfirmedEquipmentTOTest")
class ConfirmedEquipmentTOTest {
  private Validator validator;
  private ConfirmedEquipmentTO confirmedEquipmentTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    confirmedEquipmentTO = new ConfirmedEquipmentTO();
    confirmedEquipmentTO.setConfirmedEquipmentUnits(25);
    confirmedEquipmentTO.setConfirmedEquipmentSizetype("x".repeat(4));
  }

  @Test
  @DisplayName("ConfirmedEquipmentTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<ConfirmedEquipmentTO>> violations =
        validator.validate(confirmedEquipmentTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("ConfirmedEquipmentTO should throw error if confirmedEquipmentSizeType is not set.")
  void testToVerifyConfirmedEquipmentSizeTypeIsRequired() {
    confirmedEquipmentTO.setConfirmedEquipmentSizetype(null);
    Set<ConstraintViolation<ConfirmedEquipmentTO>> violations =
        validator.validate(confirmedEquipmentTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "ConfirmedEquipmentSizeType is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "ConfirmedEquipmentTO should throw error if confirmedEquipmentSizeType length exceeds max size of 4.")
  void testToConfirmedEquipmentSizeTypeIsNotAllowedToExceed4() {
    confirmedEquipmentTO.setConfirmedEquipmentSizetype("x".repeat(5));
    Set<ConstraintViolation<ConfirmedEquipmentTO>> violations =
        validator.validate(confirmedEquipmentTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(
                v -> "ConfirmedEquipmentSizeType has a max size of 4.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "ConfirmedEquipmentTO should throw error if confirmedEquipmentUnits is not a positive value.")
  void testToConfirmedEquipmentUnitsCannotBeNegative() {
    confirmedEquipmentTO.setConfirmedEquipmentUnits(-1);
    Set<ConstraintViolation<ConfirmedEquipmentTO>> violations =
        validator.validate(confirmedEquipmentTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(
                v -> "ConfirmedEquipmentUnits has to be a positive value.".equals(v.getMessage())));
  }
}
