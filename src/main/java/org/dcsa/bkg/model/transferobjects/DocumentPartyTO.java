package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.core.events.model.enums.PartyFunction;
import org.dcsa.core.events.model.transferobjects.PartyTO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class DocumentPartyTO {

  private PartyTO party;

  @NotNull(message = "PartyFunction is required.")
  private PartyFunction partyFunction;

  @Size(max = 250, message = "DisplayedAddress has a max size of 250.")
  private String displayedAddress;

  @NotNull(message = "PartyContactDetails is required.")
  private PartyContactDetailsTO partyContactDetails;

  @JsonProperty("isToBeNotified")
  private boolean isToBeNotified;
}
