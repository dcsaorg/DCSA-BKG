package org.dcsa.bkg.model.transferobjects;

import org.dcsa.core.events.model.enums.PaymentTerm;
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
class ChargeTOTest {
  private Validator validator;
  private ChargeTO chargeTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    chargeTO = new ChargeTO();
    chargeTO.setIsUnderShippersResponsibility(PaymentTerm.PRE);
    chargeTO.setChargeType("x".repeat(20));
    chargeTO.setUnitPrice(20);
    chargeTO.setQuantity(20);
    chargeTO.setCurrencyCode("x".repeat(3));
    chargeTO.setCurrencyAmount(20);
    chargeTO.setCalculationBasis("x".repeat(50));
  }

  @Test
  @DisplayName("ChargeTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("ChargeTO should throw error if unit price is not set.")
  void testToVerifyUnitPriceIsRequired() {
    chargeTO.setUnitPrice(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Unit Price is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if quantity is not set.")
  void testToVerifyQuantityIsRequired() {
    chargeTO.setQuantity(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Quantity is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if currency amount is not set.")
  void testToVerifyCurrencyAmountIsRequired() {
    chargeTO.setCurrencyAmount(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Currency Amount is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if is under shippers responsibility is not set.")
  void testToVerifyIsUnderShippersResponsibilityIsRequired() {
    chargeTO.setIsUnderShippersResponsibility(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Is Under Shippers Responsibility is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if charge type is not set.")
  void testToVerifyChargeTypeIsRequired() {
    chargeTO.setChargeType(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Charge Type is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if currency code is not set.")
  void testToVerifyCurrencyCodeIsRequired() {
    chargeTO.setCurrencyCode(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Currency Code is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if calculation basis is not set.")
  void testToVerifyCalculationBasisIsRequired() {
    chargeTO.setCalculationBasis(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Calculation Basis is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "ShipmentLocationTO should throw error if displayedName length exceeds max size of 250.")
  void testToChargeTypeIsNotAllowedToExceed20() {
    chargeTO.setChargeType("x".repeat(21));
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "Charge Type has a max size of 20.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "ShipmentLocationTO should throw error if displayedName length exceeds max size of 250.")
  void testToCurrencyCodeIsNotAllowedToExceed3() {
    chargeTO.setCurrencyCode("x".repeat(4));
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "Currency Code has a max size of 3.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "ShipmentLocationTO should throw error if displayedName length exceeds max size of 250.")
  void testToCalculationBasisIsNotAllowedToExceed50() {
    chargeTO.setCalculationBasis("x".repeat(51));
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "Calculation Basis has a max size of 50.".equals(v.getMessage())));
  }
}
