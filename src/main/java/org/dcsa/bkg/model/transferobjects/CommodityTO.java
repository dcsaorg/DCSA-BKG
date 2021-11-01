package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.dcsa.core.events.model.enums.CargoGrossWeight;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Data
public class CommodityTO {

  @NotBlank(message = "Commodity Type is required.")
  @Size(max = 20, message = "A max length of 20 is permitted for commodityType.")
  private String commodityType;

  @Size(max = 10, message = "A max length of 10 is permitted for HSCode.")
  @JsonProperty("HSCode")
  private String hsCode;

  @NotNull(message = "cargoGrossWeight cannot be empty!")
  private CargoGrossWeight cargoGrossWeight;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private OffsetDateTime exportLicenseIssueDate;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private OffsetDateTime exportLicenseExpiryDate;
}
