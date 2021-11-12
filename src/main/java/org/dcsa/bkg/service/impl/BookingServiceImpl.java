package org.dcsa.bkg.service.impl;

import antlr.Utils;
import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.bkg.repository.BookingConfirmationRepository;
import org.dcsa.bkg.repository.ChargeTORepository;
import org.dcsa.bkg.repository.ShipmentLocationRepository;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.Location;
import org.dcsa.core.events.model.ModeOfTransport;
import org.dcsa.core.events.model.Shipment;
import org.dcsa.core.events.model.Vessel;
import org.dcsa.core.events.model.base.AbstractLocation;
import org.dcsa.core.events.model.enums.DCSATransportType;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.events.repository.*;
import org.dcsa.core.util.MappingUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {

  private final BookingConfirmationRepository bookingConfirmationRepository;
  private final BookingRepository bookingRepository;
  private final ShipmentRepository shipmentRepository;
  private final LocationRepository locationRepository;
  private final TransportRepository transportRepository;
  private final VesselRepository vesselRepository;
  private final ShipmentLocationRepository shipmentLocationRepository;
  private final ChargeTORepository chargeTORepository;
  private final ModeOfTransportRepository modeOfTransportRepository;

  public Flux<BookingConfirmationSummaryTO> getBookingConfirmationSummaries(
      String carrierBookingReferenceID, DocumentStatus documentStatus, int limit) {

    return shipmentRepository
        .findAll()
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
      ModeOfTransport modeOfTransport = new ModeOfTransport();
    Shipment shipment = new Shipment();
    BookingConfirmationTO bookingConfirmationTO = new BookingConfirmationTO();
    return bookingConfirmationRepository
        .findByCarrierBookingReferenceID(carrierBookingReference)
        .flatMap(
            x -> {
              shipment.setId(x.getId());
              shipment.setCarrierID(x.getCarrierID());
              shipment.setBookingID(x.getBookingID());
              bookingConfirmationTO.setCarrierBookingReference(x.getCarrierBookingReferenceID());
              bookingConfirmationTO.setTermsAndConditions(x.getTermsAndConditions());
              return bookingRepository.findById(x.getBookingID());
            })
        .doOnNext(bookingConfirmationTO::setBooking)
        //        .flatMap(ignored -> locationRepository.findById(shipment.getPlaceOfIssueID()))
        //        .doOnNext(
        //            x ->
        //                bookingConfirmationTO.setPlaceOfIssue(
        //                    MappingUtils.instanceFrom(x, LocationTO::new,
        // AbstractLocation.class)))
        .flatMapMany(
            x ->
                shipmentLocationRepository.findAllByCarrierBookingReference(
                    bookingConfirmationTO.getCarrierBookingReference()))
        .collectList()
        .doOnNext(bookingConfirmationTO::setShipmentLocations)
        .flatMapMany(x -> chargeTORepository.findAllByShipmentID(shipment.getId()))
        .collectList()
        .doOnNext(bookingConfirmationTO::setCharges)
            .flatMap(x -> modeOfTransportRepository.findById("1"))
            .flatMap(x -> {
                modeOfTransport.setId(x.getId());
                modeOfTransport.setName(x.getName());
                modeOfTransport.setDescription(x.getDescription());
                modeOfTransport.setDcsaTransportType(x.getDcsaTransportType());
                return vesselRepository.findById(bookingConfirmationTO.getBooking().getVesselId());
            })
            .flatMapMany(vessel -> transportRepository.getTransports(modeOfTransport.getId(), vessel.getVesselIMONumber()))
            .collectList()
            .doOnNext(bookingConfirmationTO::setTransports)
        .thenReturn(bookingConfirmationTO);
  }

  @Override
  public Mono<Void> cancelBookingByCarrierBookingReference(String carrierBookingReference) {
    return Mono.empty();
  }
}
