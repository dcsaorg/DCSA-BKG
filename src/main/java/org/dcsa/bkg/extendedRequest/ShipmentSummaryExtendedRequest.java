package org.dcsa.bkg.extendedRequest;

import org.dcsa.bkg.model.transferobjects.ShipmentSummaryTO;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.query.DBEntityAnalysis;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dcsa.core.events.model.enums.ShipmentEventTypeCode.BOOKING_DOCUMENT_STATUSES;

public class ShipmentSummaryExtendedRequest extends ExtendedRequest<ShipmentSummaryTO> {
  public ShipmentSummaryExtendedRequest(
      ExtendedParameters extendedParameters, R2dbcDialect r2dbcDialect) {
    super(extendedParameters, r2dbcDialect, ShipmentSummaryTO.class);
  }

  @Override
  public void parseParameter(Map<String, List<String>> params) {
    Map<String, List<String>> allowedParams = new HashMap<>(params);
    allowedParams.putIfAbsent("documentStatus", List.of(BOOKING_DOCUMENT_STATUSES));
    super.parseParameter(allowedParams);
  }

  @Override
  protected DBEntityAnalysis.DBEntityAnalysisBuilder<ShipmentSummaryTO> prepareDBEntityAnalysis() {
    DBEntityAnalysis.DBEntityAnalysisBuilder<ShipmentSummaryTO> builder =
        super.prepareDBEntityAnalysis();
    return builder.registerQueryFieldAlias("booking.documentStatus", "documentStatus");
  }
}
