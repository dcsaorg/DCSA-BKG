package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import javax.validation.constraints.Min;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/booking-summaries",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGSummariesController {

  private final BookingService bookingService;

  @GetMapping
  public Flux<BookingSummaryTO> getBookingRequestSummaries(
      @RequestParam(value = "carrierBookingRequestReference", required = false)
          String carrierBookingRequestReference,
      @RequestParam(value = "documentStatus", required = false) DocumentStatus documentStatus,
      @RequestParam(
              value = "limit",
              defaultValue = "${pagination.defaultPageSize}",
              required = false)
          @Min(1)
          int limit,
      @RequestParam(value = "cursor", defaultValue = "0", required = false) String cursor,
      ServerHttpResponse response) {

    return bookingService
        .getBookingRequestSummaries(
            documentStatus,
            PageRequest.of(
                Integer.parseInt(cursor), limit, Sort.Direction.DESC, "bookingRequestDateTime"))
        .doOnNext(
            objects -> {
              if (response.getHeaders().get("foo-count") == null) {
                response.getHeaders().add("foo-count", String.valueOf(objects.getT2()));
              }
            })
        .map(Tuple2::getT1);
  }
}
