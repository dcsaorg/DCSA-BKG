package org.dcsa.bkg.model.validators;

import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.core.events.model.enums.PartyFunction;
import org.dcsa.core.events.model.transferobjects.DocumentPartyTO;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
public class DocumentPartyTOValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return BookingTO.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    BookingTO bookingTO = (BookingTO) target;
    EnumSet<PartyFunction> allowedPartyFunctions =
        EnumSet.of(
            PartyFunction.OS,
            PartyFunction.CN,
            PartyFunction.COW,
            PartyFunction.COX,
            PartyFunction.N1,
            PartyFunction.N2,
            PartyFunction.NI,
            PartyFunction.SFA,
            PartyFunction.DDR,
            PartyFunction.DDS,
            PartyFunction.CA,
            PartyFunction.HE,
            PartyFunction.SCO,
            PartyFunction.BA);
    Optional<List<DocumentPartyTO>> documentParties = Optional.ofNullable(bookingTO.getDocumentParties());

    documentParties.ifPresent(
        documentPartyTOS ->
            documentPartyTOS.forEach(
                documentParty -> {
                  PartyFunction partyFunction = documentParty.getPartyFunction();
                  if (!allowedPartyFunctions.contains(partyFunction)) {
                    throw ConcreteRequestErrorMessageException.invalidParameter(
                      "The provided partyFunction " + partyFunction +" is not allowed in Booking.");
                  }
                }));
  }
}
