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

@DisplayName("Tests for ChargeTOTest")
class ChargeTOTest {
  private Validator validator;
  private ChargeTO chargeTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    chargeTO = new ChargeTO();
    chargeTO.setPaymentTermCode(PaymentTerm.PRE);
    chargeTO.setChargeType("x".repeat(20));
    chargeTO.setUnitPrice(20.20);
    chargeTO.setQuantity(20.20);
    chargeTO.setCurrencyCode("x".repeat(3));
    chargeTO.setCurrencyAmount(20.20);
    chargeTO.setCalculationBasis("x".repeat(50));
  }

  @Test
  @DisplayName("ChargeTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("ChargeTO should throw error if unitPrice is not set.")
  void testToVerifyUnitPriceIsRequired() {
    chargeTO.setUnitPrice(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "UnitPrice is required.".equals(v.getMessage())));
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
        violations.stream().anyMatch(v -> "CurrencyAmount is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if paymentTermCode is not set.")
  void testToVerifyIsUnderShippersResponsibilityIsRequired() {
    chargeTO.setPaymentTermCode(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "PaymentTermCode is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if chargeType is not set.")
  void testToVerifyChargeTypeIsRequired() {
    chargeTO.setChargeType(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "ChargeType is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if currencyCode is not set.")
  void testToVerifyCurrencyCodeIsRequired() {
    chargeTO.setCurrencyCode(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "CurrencyCode is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("ChargeTO should throw error if calculationBasis is not set.")
  void testToVerifyCalculationBasisIsRequired() {
    chargeTO.setCalculationBasis(null);
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "CalculationBasis is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "ChargeTO should throw error if chargeType length exceeds max size of 20.")
  void testToChargeTypeIsNotAllowedToExceed20() {
    chargeTO.setChargeType("x".repeat(21));
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "ChargeType has a max size of 20.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "ChargeTO should throw error if currencyCode length exceeds max size of 3.")
  void testToCurrencyCodeIsNotAllowedToExceed3() {
    chargeTO.setCurrencyCode("x".repeat(4));
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "CurrencyCode has a max size of 3.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "ChargeTO should throw error if calculationBasis length exceeds max size of 50.")
  void testToCalculationBasisIsNotAllowedToExceed50() {
    chargeTO.setCalculationBasis("x".repeat(51));
    Set<ConstraintViolation<ChargeTO>> violations = validator.validate(chargeTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "CalculationBasis has a max size of 50.".equals(v.getMessage())));
  }
}
