package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.validator.EnumSubset;

import javax.validation.constraints.NotNull;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.DOCUMENT_STATUSES;

@Data
public class BookingCancellationRequestTO {

	@NotNull(message = "DocumentStatus is required")
	@EnumSubset(anyOf = DOCUMENT_STATUSES)
	private ShipmentEventTypeCode documentStatus;
	private String reason;
}
