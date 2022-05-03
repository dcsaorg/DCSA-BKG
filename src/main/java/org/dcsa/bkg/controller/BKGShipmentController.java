package org.dcsa.bkg.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.service.BKGService;
import org.dcsa.core.events.edocumentation.model.transferobject.ShipmentTO;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Size;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(
    value = "/shipments",
    produces = {MediaType.APPLICATION_JSON_VALUE})
public class BKGShipmentController {

  private final BKGService bookingService;

  @GetMapping(path = "{carrierBookingReference}")
  public Mono<ShipmentTO> getBookingReference(
      @PathVariable @Size(max = 35) String carrierBookingReference) {
    return bookingService.getShipmentByCarrierBookingReference(carrierBookingReference);
  }

}
