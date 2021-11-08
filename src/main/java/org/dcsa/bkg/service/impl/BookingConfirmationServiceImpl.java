package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import org.dcsa.bkg.service.BookingConfirmationService;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.dcsa.core.service.impl.ExtendedBaseServiceImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookingConfirmationServiceImpl extends ExtendedBaseServiceImpl<ShipmentRepository, Shipment, UUID> implements BookingConfirmationService {

  private final ShipmentRepository shipmentRepository;

  public Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries() {
    return findAll().map(x -> {
      BookingConfirmationSummaryTO result = new BookingConfirmationSummaryTO();
      result.setCarrierBookingReferenceID(x.getCarrierBookingReferenceID());
      result.setConfirmationDateTime(x.getConfirmationDateTime());
      result.setTermsAndConditions(x.getTermsAndConditions());
      return result;
    });
  }

  @Override
  public ShipmentRepository getRepository() {
    return shipmentRepository;
  }
}
