package org.dcsa.bkg.model.transferobjects;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ConfirmedEquipmentTO {

  @NotNull(message = "ConfirmedEquipmentSizeType is required.")
  @Size(max = 4, message = "ConfirmedEquipmentSizeType has a max size of 4.")
  private String confirmedEquipmentSizeType;

  private int confirmedEquipmentUnits;
}
