package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.enums.PartyFunction;
import org.dcsa.core.events.model.transferobjects.PartyTO;
import org.dcsa.core.exception.InvalidParameterException;
import org.dcsa.core.validator.EnumSubset;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DocumentPartyTO {

  @Valid
  @NotNull(message = "Party is required.")
  private PartyTO party;

  @EnumSubset(anyOf = {"OS", "CN", "COW", "COX", "N1", "N2", "NI", "SFA", "DDR", "DDS", "CA", "HE", "SCO", "BA"})
  @NotNull(message = "PartyFunction is required.")
  private PartyFunction partyFunction;

  private List<String> displayedAddress;

  @NotNull(message = "IsToBeNotified is required.")
  private Boolean isToBeNotified;

  public void setDisplayedAddress(List<String> displayedAddress) {
    for (String da : displayedAddress) {
      if (da.length() > 250) {
        throw new InvalidParameterException("A single displayedAddress has a max size of 250.");
      }
    }
    this.displayedAddress = displayedAddress;
  }
}
