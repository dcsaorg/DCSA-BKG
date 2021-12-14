package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Min;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/booking-summaries",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGSummariesController {

  private final BookingService bookingService;
  private final R2dbcDialect r2dbcDialect;

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
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "sort", required = false) String[] sort,
      ServerHttpResponse response) {

    Sort sort1 = parseSort(sort);
    PageRequest pageRequest;
    if(cursor == null) {

      pageRequest = PageRequest.of(0, limit, sort1);
    } else {
      pageRequest = parseCursor(cursor, sort1);
    }

    Mono<Page<BookingSummaryTO>> bookingSummaries =
        bookingService.getBookingRequestSummaries(
            documentStatus,
            pageRequest);

    return bookingSummaries
        .doOnNext(
            bookingSummaryTOS -> {
              response.getHeaders().addAll(setPaginationHeaders(bookingSummaryTOS));
            })
        .flatMapMany(bookingSummaryTOS -> Flux.fromIterable(bookingSummaryTOS));
  }

  private static MultiValueMap<String, String> setPaginationHeaders(Page page) {
    MultiValueMap<String, String> paginationHeaders = new LinkedMultiValueMap<>();

    String currentPage = formatCursor(page.getNumber(), page.getSize(), page.getSort());
    paginationHeaders.add("Current-Page", Base64.getUrlEncoder().encodeToString(currentPage.getBytes(StandardCharsets.UTF_8)));
    if(page.getTotalPages()>1){
      String nextPage = formatCursor(page.getNumber()+1, page.getSize(), page.getSort());
      paginationHeaders.add("Next-Page", Base64.getUrlEncoder().encodeToString(nextPage.getBytes(StandardCharsets.UTF_8)));
      String lastPage = formatCursor(page.getTotalPages()-1, page.getSize(), page.getSort());
      paginationHeaders.add("Last-Page", Base64.getUrlEncoder().encodeToString(lastPage.getBytes(StandardCharsets.UTF_8)));
    }

    return paginationHeaders;
  }

  private static String formatCursor(int page, int size, Sort sort) {
    StringBuilder sb = new StringBuilder();
    sb.append("page=").append(page);
    sb.append("&size=").append(size);
    sb.append("&sort=").append(sort);
    return sb.toString();
  }

  private static PageRequest parseCursor(String cursor, Sort sort) {

    String decodedCursor = new String(Base64.getUrlDecoder().decode(cursor));
    String[] decodedCursurItems = decodedCursor.split("&");

    int page = Integer.parseInt(decodedCursurItems[0].split("=")[1]);
    int size = Integer.parseInt(decodedCursurItems[1].split("=")[1]);

    PageRequest pageRequest = PageRequest.of(page, size, sort);
    return pageRequest;
  }

  private static Sort parseSort(String[] sort) {

    if(sort == null ) {
      return Sort.by(Sort.Direction.DESC, "bookingRequestDateTime");
    }

    List<Sort.Order> orderList = new ArrayList<>();
    for (String sortItem : sort) {
        String[] sortComponents = sortItem.split(":");
        orderList.add(new Sort.Order(replaceOrderStringThroughDirection(sortComponents[1]), sortComponents[0]));
    }
    return Sort.by(orderList);
  }

  private static Sort.Direction replaceOrderStringThroughDirection(String sortDirection) {
      if (sortDirection.equalsIgnoreCase("DESC")) {
        return Sort.Direction.DESC;
      } else {
        return Sort.Direction.ASC;
      }
    }
}
