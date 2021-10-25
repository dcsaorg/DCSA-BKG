package org.dcsa.bkg.model.transferobjects;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CarrierClauseTO {

  @NotNull private String clauseContent;
}
