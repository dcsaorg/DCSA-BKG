package org.dcsa.bkg.service.impl;

import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.service.impl.BaseServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class BookingServiceImpl extends BaseServiceImpl<BookingSummaryTO, UUID> implements BookingService {

  @Override
  public Flux<BookingSummaryTO> getBookingRequestSummaries() {
    return Flux.empty();
  }

  @Override
  public Flux<BookingSummaryTO> findAll() {
    return Flux.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  public Mono<BookingSummaryTO> findById(UUID id) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  public Mono<BookingSummaryTO> create(BookingSummaryTO bookingSummaryTO) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  public Mono<BookingSummaryTO> update(BookingSummaryTO bookingSummaryTO) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  public Mono<Void> deleteById(UUID id) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  public Mono<Void> delete(BookingSummaryTO bookingSummaryTO) {
    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  @Override
  public UUID getIdOfEntity(BookingSummaryTO entity) {
    return null;
  }
}
