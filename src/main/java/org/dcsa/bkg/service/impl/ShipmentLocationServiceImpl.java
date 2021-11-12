package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.ShipmentLocationTO;
import org.dcsa.bkg.repositories.ShipmentLocationRepository;
import org.dcsa.bkg.service.ShipmentLocationService;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ShipmentLocationServiceImpl
    extends ExtendedBaseServiceImpl<ShipmentLocationRepository, ShipmentLocationTO, UUID>
    implements ShipmentLocationService {

  private final ShipmentLocationRepository shipmentLocationRepository;
  private final ExtendedParameters extendedParameters;
  private final R2dbcDialect r2dbcDialect;

  public Flux<ShipmentLocationTO> findAll() {
    ExtendedRequest<ShipmentLocationTO> extendedRequest = newExtendedRequest();
    extendedRequest.parseParameter(Collections.emptyMap());
    return findAllExtended(extendedRequest);
  }

  @Override
  public Flux<ShipmentLocationTO> findAllByShipmentID(UUID shipmentID) {
    ExtendedRequest<ShipmentLocationTO> extendedRequest = newExtendedRequest();
    Map<String, List<String>> params = new HashMap<>();
    params.put("shipmentID", Collections.singletonList(shipmentID.toString()));
    extendedRequest.parseParameter(params);
    return shipmentLocationRepository.findAllExtended(extendedRequest);
  }

  @Override
  public Flux<ShipmentLocationTO> findAllByBookingID(UUID bookingID) {
    ExtendedRequest<ShipmentLocationTO> extendedRequest = newExtendedRequest();
    Map<String, List<String>> params = new HashMap<>();
    params.put("bookingID", Collections.singletonList(bookingID.toString()));
    extendedRequest.parseParameter(params);
    return shipmentLocationRepository.findAllExtended(extendedRequest);
  }

  public Flux<ShipmentLocationTO> findAllExtended(
      ExtendedRequest<ShipmentLocationTO> extendedRequest) {
    return shipmentLocationRepository.findAllExtended(extendedRequest);
  }

  @Override
  public ShipmentLocationRepository getRepository() {
    return shipmentLocationRepository;
  }

  public ExtendedRequest<ShipmentLocationTO> newExtendedRequest() {
    return new ExtendedRequest<>(extendedParameters, r2dbcDialect, ShipmentLocationTO.class);
  }
}
