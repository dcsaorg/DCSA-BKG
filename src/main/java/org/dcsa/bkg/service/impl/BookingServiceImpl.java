package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import org.dcsa.bkg.model.transferobjects.BookingConfirmationTO;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.model.transferobjects.BookingTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
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
      String carrierBookingReferenceID, DocumentStatus documentStatus, int limit, String cursor) {

    ReactiveSelectOperation.ReactiveSelect<Shipment> selectResults =
        template.select(Shipment.class);
    ReactiveSelectOperation.TerminatingSelect<Shipment> results;

    Criteria criteria = null;

    if (carrierBookingReferenceID != null) {
      criteria = where("carrierBookingReferenceID").is(carrierBookingReferenceID);
    }

    if (documentStatus != null) {
      if (criteria == null) {
        criteria = where("documentStatus").is(documentStatus);
      } else {
        criteria.and(where("documentStatus").is(documentStatus));
      }
    }

    if (criteria != null) {
      results = selectResults.matching(query(criteria).limit(limit));
    } else {
      results = selectResults.matching(Query.empty().limit(limit));
    }

    return results
        .all()
        .map(
            x -> {
              BookingConfirmationSummaryTO result = new BookingConfirmationSummaryTO();
              result.setCarrierBookingReferenceID(x.getCarrierBookingReferenceID());
              result.setConfirmationDateTime(x.getConfirmationDateTime());
              result.setTermsAndConditions(x.getTermsAndConditions());
              return result;
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
