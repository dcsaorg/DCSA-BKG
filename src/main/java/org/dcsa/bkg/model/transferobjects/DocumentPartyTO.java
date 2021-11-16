package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.core.events.model.enums.PartyFunction;
import org.dcsa.core.events.model.transferobjects.PartyTO;
import org.dcsa.core.exception.InvalidParameterException;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DocumentPartyTO {

  private PartyTO party;

  @NotNull(message = "PartyFunction is required.")
  private PartyFunction partyFunction;

  private List<String> displayedAddress;

  @NotNull(message = "PartyContactDetails is required.")
  @NotEmpty(message = "PartyContactDetails is required.")
  private List<PartyContactDetailsTO> partyContactDetails;

  @JsonProperty("isToBeNotified")
  private boolean isToBeNotified;

  public void setDisplayedAddress(List<String> displayedAddress) {
    for (String da : displayedAddress) {
      if (da.length() > 250) {
        throw new InvalidParameterException("A single displayedAddress has a max size of 250.");
      }
    }
    this.displayedAddress = displayedAddress;
  }
}
