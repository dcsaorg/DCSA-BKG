package org.dcsa.bkg.model.transferobjects;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CarrierClauseTO {
  @NotNull(message = "ClauseContent is required.")
  private String clauseContent;
}
