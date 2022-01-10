package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.core.events.model.enums.DocumentStatus;

import javax.validation.constraints.NotNull;

@Data
public class BookingCancellationRequestTO {

	@NotNull(message = "DocumentStatus is required")
	private DocumentStatus documentStatus;
	private String reason;
}
