package org.dcsa.bkg.model.transferobjects;

import lombok.Data;
import org.dcsa.bkg.model.enums.ValueAddedServiceCode;

import javax.validation.constraints.NotNull;

@Data
public class ValueAddedServiceRequestTO {
    @NotNull(message = "ValueAddedServiceCode is required.")
    private ValueAddedServiceCode valueAddedServiceCode;
}
