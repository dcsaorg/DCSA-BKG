package org.dcsa.bkg.model.transferobjects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@DisplayName("Tests for BookingConfirmationTOTest")
class ShipmentTOTest {
  private Validator validator;
  private ShipmentTO shipmentTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    shipmentTO = new ShipmentTO();
    shipmentTO.setCarrierBookingReference("x".repeat(35));
    shipmentTO.setTermsAndConditions("x".repeat(50));
    shipmentTO.setBooking(new BookingTO());
    shipmentTO.setTransports(List.of(new TransportTO()));
    shipmentTO.setShipmentLocations(List.of(new ShipmentLocationTO()));
    shipmentTO.setShipmentCutOffTimes(List.of(new ShipmentCutOffTimeTO()));
    shipmentTO.setConfirmedEquipments(List.of(new ConfirmedEquipmentTO()));
    shipmentTO.setCharges(List.of(new ChargeTO()));
    shipmentTO.setCarrierClauses(List.of(new CarrierClauseTO()));
    shipmentTO.setConfirmationDateTime(OffsetDateTime.now());
  }

  @Test
  @DisplayName("BookingConfirmationTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<ShipmentTO>> violations =
        validator.validate(shipmentTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("BookingConfirmationTO should throw error if carrierBookingReference is not set.")
  void testToVerifyCarrierBookingReferenceIsRequired() {
    shipmentTO.setCarrierBookingReference(null);
    Set<ConstraintViolation<ShipmentTO>> violations =
        validator.validate(shipmentTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "CarrierBookingReference is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "BookingConfirmationTO should throw error if carrierBookingReference length exceeds max size of 35.")
  void testToVerifyCarrierBookingReferenceIDIsNotAllowedToExceed35() {
    shipmentTO.setCarrierBookingReference("x".repeat(36));
    Set<ConstraintViolation<ShipmentTO>> violations =
        validator.validate(shipmentTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "CarrierBookingReference has a max size of 35.".equals(v.getMessage())));
  }
}
