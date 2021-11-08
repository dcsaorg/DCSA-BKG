package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {

  private final R2dbcEntityTemplate template;

  public Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries(
      String carrierBookingReferenceID, DocumentStatus documentStatus, int limit) {

    ReactiveSelectOperation.ReactiveSelect<Shipment> result = template.select(Shipment.class);
    ReactiveSelectOperation.TerminatingSelect<Shipment> splat = result;

    if (carrierBookingReferenceID != null) {
      splat =
          result.matching(query(where("carrierBookingReferenceID").is(carrierBookingReferenceID)));
    }

    if (documentStatus != null) {
      splat = result.matching(query(where("documentStatus").is(documentStatus)));
    }

    return splat
        .all()  // Seemingly no way to only take a certain amount?
        .take(limit)
        .map(
            x -> {
              BookingConfirmationSummaryTO result2 = new BookingConfirmationSummaryTO();
              result2.setCarrierBookingReferenceID(x.getCarrierBookingReferenceID());
              result2.setConfirmationDateTime(x.getConfirmationDateTime());
              result2.setTermsAndConditions(x.getTermsAndConditions());
              return result2;
            });
  }

  @Override
  public Flux<BookingSummaryTO> getBookingRequestSummaries() {
    return Flux.empty();
  }

  @Override
  public Mono<BookingTO> createBooking(BookingTO bookingRequest) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingTO> getBookingByCarrierBookingRequestReference(
      String carrierBookingRequestReference) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingConfirmationTO> getBookingByCarrierBookingReference(
      String carrierBookingReference) {
    return Mono.empty();
  }

  @Override
  public Mono<Void> cancelBookingByCarrierBookingReference(String carrierBookingReference) {
    return Mono.empty();
  }
}
