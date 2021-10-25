package org.dcsa.bkg.model.transferobjects;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ConfirmedEquipmentTO {

  @NotNull
  @Size(max = 4)
  private String confirmedEquipmentSizeType;

  private int confirmedEquipmentUnits;
}
