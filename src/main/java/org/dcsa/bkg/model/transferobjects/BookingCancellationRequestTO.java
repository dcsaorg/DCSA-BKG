package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;

import javax.validation.constraints.NotNull;

@Data
public class BookingCancellationRequestTO {

	@NotNull(message = "DocumentStatus is required")
	@EnumSubset(anyOf = "CANC")
	private ShipmentEventTypeCode documentStatus;
	private String reason;
}
