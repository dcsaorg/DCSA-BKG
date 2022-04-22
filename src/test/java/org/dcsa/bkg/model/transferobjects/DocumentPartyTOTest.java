package org.dcsa.bkg.model.transferobjects;

import org.dcsa.core.events.model.transferobjects.DocumentPartyTO;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.model.enums.PartyFunction;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.dcsa.skernel.model.transferobjects.PartyTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.dcsa.skernel.model.enums.PartyFunction.DOCUMENTATION_PARTY_FUNCTION_CODES;

@DisplayName("Tests for DocumentPartyTO")
class DocumentPartyTOTest {

  private Validator validator;
  private DocumentPartyTO validDocumentPartyTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    PartyTO partyTO = new PartyTO();
    partyTO.setPartyContactDetails(Collections.singletonList(new PartyContactDetailsTO()));

    validDocumentPartyTO = new DocumentPartyTO();
    validDocumentPartyTO.setParty(partyTO);
    validDocumentPartyTO.setPartyFunction(PartyFunction.N1);
    validDocumentPartyTO.setDisplayedAddress(Collections.singletonList("x".repeat(250)));
    validDocumentPartyTO.setIsToBeNotified(true);
  }

  @Test
  @DisplayName("DocumentPartyTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<DocumentPartyTO>> violations = validator.validate(validDocumentPartyTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("DocumentPartyTO should throw error if party is not set.")
  void testToVerifyPartyIsRequired() {
    validDocumentPartyTO.setParty(null);
    Set<ConstraintViolation<DocumentPartyTO>> violations = validator.validate(validDocumentPartyTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Party is required.".equals(v.getMessage())));
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
      "DocumentPartyTO should throw error if partyFunction is set to a value that is not in its enum subset.")
  void testToVerifyNotAllowedEnumsInPartyFunctionIsInvalid() {
    List<String> documentPartySubset = Arrays.asList(DOCUMENTATION_PARTY_FUNCTION_CODES);
    for (PartyFunction pf : PartyFunction.values()) {
      if (documentPartySubset.contains(pf.name())) continue;
      validDocumentPartyTO.setPartyFunction(pf);
      for (ConstraintViolation<DocumentPartyTO> violation :
          validator.validate(validDocumentPartyTO)) {
        Assertions.assertEquals(
            violation.getMessage(),
            "must be any of [" + String.join(", ", documentPartySubset) + "]");
      }
    }
  }

  @Test
  @DisplayName(
      "DocumentPartyTO should throw error if displayedAddress length exceeds max size of 250.")
  void testToVerifyDisplayedAddressIsNotAllowedToExceed250() {
    Exception exception =
        Assertions.assertThrows(
            ConcreteRequestErrorMessageException.class,
            () ->
                validDocumentPartyTO.setDisplayedAddress(
                    Collections.singletonList("x".repeat(251))));
    Assertions.assertEquals("BadRequestException", exception.getClass().getSimpleName());
    Assertions.assertEquals(
        "A single displayedAddress has a max size of 250.", exception.getMessage());
  }

  @Test
  @DisplayName("DocumentPartyTO should throw error if partyContactDetails is not set.")
  void testToVerifyPartyContactDetailsIsRequired() {
    validDocumentPartyTO.getParty().setPartyContactDetails(null);
    Set<ConstraintViolation<DocumentPartyTO>> violations = validator.validate(validDocumentPartyTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "PartyContactDetails is required.".equals(v.getMessage())));
  }
}
