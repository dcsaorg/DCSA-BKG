package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.core.events.model.enums.CargoGrossWeight;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class CommodityTO {

  @NotBlank(message = "CommodityType is required.")
  @Size(max = 20, message = "CommodityType has a max size of 20.")
  private String commodityType;

  @Size(max = 10, message = "HSCode has a max size of 10.")
  @JsonProperty("HSCode")
  private String hsCode;

  @NotNull(message = "CargoGrossWeight is required.")
  private Double cargoGrossWeight;

  @NotNull(message = "CargoGrossWeightUnit is required.")
  private CargoGrossWeight cargoGrossWeightUnit;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate exportLicenseIssueDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate exportLicenseExpiryDate;
}
