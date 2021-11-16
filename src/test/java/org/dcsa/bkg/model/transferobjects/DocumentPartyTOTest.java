package org.dcsa.bkg.model.transferobjects;

import org.dcsa.core.events.model.enums.PartyFunction;
import org.dcsa.core.events.model.transferobjects.PartyTO;
import org.dcsa.core.exception.InvalidParameterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.Set;

@DisplayName("Tests for DocumentPartyTO")
class DocumentPartyTOTest {

  private Validator validator;
  private DocumentPartyTO validDocumentPartyTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    validDocumentPartyTO = new DocumentPartyTO();
    validDocumentPartyTO.setParty(new PartyTO());
    validDocumentPartyTO.setPartyFunction(PartyFunction.N1);
    validDocumentPartyTO.setDisplayedAddress(Collections.singletonList("x".repeat(250)));
    validDocumentPartyTO.setPartyContactDetails(
        Collections.singletonList(new PartyContactDetailsTO()));
    validDocumentPartyTO.setToBeNotified(true);
  }

  @Test
  @DisplayName("DocumentPartyTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<DocumentPartyTO>> violations = validator.validate(validDocumentPartyTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("DocumentPartyTO should throw error if partyFunction is not set.")
  void testToVerifyPartyFunctionIsRequired() {
    validDocumentPartyTO.setPartyFunction(null);
    Set<ConstraintViolation<DocumentPartyTO>> violations = validator.validate(validDocumentPartyTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "PartyFunction is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "DocumentPartyTO should throw error if displayedAddress length exceeds max size of 250.")
  void testToVerifyDisplayedAddressIsNotAllowedToExceed250() {
    Exception exception =
        Assertions.assertThrows(
            InvalidParameterException.class,
            () ->
                validDocumentPartyTO.setDisplayedAddress(
                    Collections.singletonList("x".repeat(251))));
    Assertions.assertEquals(
        "A single displayedAddress has a max size of 250.", exception.getMessage());
  }

  @Test
  @DisplayName("DocumentPartyTO should throw error if partyContactDetails is not set.")
  void testToVerifyPartyContactDetailsIsRequired() {
    validDocumentPartyTO.setPartyContactDetails(null);
    Set<ConstraintViolation<DocumentPartyTO>> violations = validator.validate(validDocumentPartyTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "PartyContactDetails is required.".equals(v.getMessage())));
  }
}
