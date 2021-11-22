package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.mappers.*;
import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.DocumentStatus;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.events.model.transferobjects.PartyTO;
import org.dcsa.core.events.repository.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

  // repositories
  private final BookingRepository bookingRepository;
  private final ShipmentRepository bookingConfirmationRepository;
  private final LocationRepository locationRepository;
  private final AddressRepository addressRepository;
  private final FacilityRepository facilityRepository;
  private final CommodityRepository commodityRepository;
  private final ValueAddedServiceRequestRepository valueAddedServiceRequestRepository;
  private final ReferenceRepository referenceRepository;
  private final RequestedEquipmentRepository requestedEquipmentRepository;
  private final DocumentPartyRepository documentPartyRepository;
  private final PartyRepository partyRepository;
  private final PartyContactDetailsRepository partyContactDetailsRepository;
  private final PartyIdentifyingCodeRepository partyIdentifyingCodeRepository;
  private final ShipmentLocationRepository shipmentLocationRepository;
  private final DisplayedAddressRepository displayedAddressRepository;
  private final ShipmentCutOffTimeRepository shipmentCutOffTimeRepository;
  private final VesselRepository vesselRepository;

  // mappers
  private final BookingMapper bookingMapper;
  private final BookingSummaryMapper bookingSummaryMapper;
  private final LocationMapper locationMapper;
  private final CommodityMapper commodityMapper;
  private final PartyMapper partyMapper;
  private final ShipmentMapper shipmentMapper;

  @Override
  public Flux<BookingSummaryTO> getBookingRequestSummaries(
      String carrierBookingRequestReference, DocumentStatus documentStatus, Pageable pageable) {

    Flux<Booking> queryResponse =
        bookingRepository.findAllByCarrierBookingReferenceAndDocumentStatus(
            carrierBookingRequestReference, documentStatus, pageable);

    return queryResponse.flatMap(
        booking ->
            vesselRepository
                .findByIdOrEmpty(booking.getVesselId())
                .mapNotNull(
                    vessel -> {
                      BookingSummaryTO bookingSummaryTO =
                          bookingSummaryMapper.bookingSummaryTOFromBooking(booking);
                      bookingSummaryTO.setVesselIMONumber(vessel.getVesselIMONumber());
                      return bookingSummaryTO;
                    })
                .defaultIfEmpty(bookingSummaryMapper.bookingSummaryTOFromBooking(booking)));
  }

  @Override
  @Transactional
  public Mono<BookingTO> createBooking(final BookingTO bookingRequest) {

    Booking requestedBooking = bookingMapper.dtoToBooking(bookingRequest);
    requestedBooking.setDocumentStatus(DocumentStatus.RECE);
    requestedBooking.setBookingRequestDateTime(OffsetDateTime.now());

    return bookingRepository
        .save(requestedBooking)
        .flatMap(
            bookingRefreshed ->
                // have to refresh booking as the default values created for columns in booking (for
                // example, in this case
                // `carrier_booking_request_reference`) are not being updated on the model on save.
                // Also prefer setting the uuid value at db level and not application level to avoid
                // collisions
                bookingRepository.findById(bookingRefreshed.getId()))
        .flatMap(
            booking -> {
              final String cbReqRef = booking.getCarrierBookingRequestReference();
              final UUID bookingID = booking.getId();

              return Mono.zip(
                  Mono.just(bookingToDTOWithNullLocations(booking)),
                  createLocationByTO(
                      bookingRequest.getInvoicePayableAt(),
                      invPayAT -> bookingRepository.setInvoicePayableAtFor(invPayAT, cbReqRef)),
                  createLocationByTO(
                      bookingRequest.getPlaceOfIssue(),
                      placeOfIss -> bookingRepository.setPlaceOfIssueIDFor(placeOfIss, cbReqRef)),
                  createCommoditiesByBookingIDAndTOs(bookingID, bookingRequest.getCommodities()),
                  createValueAddedServiceRequestsByBookingIDAndTOs(
                      bookingID, bookingRequest.getValueAddedServiceRequests()),
                  createReferencesByBookingIDAndTOs(bookingID, bookingRequest.getReferences()),
                  createRequestedEquipmentsByBookingIDAndTOs(
                      bookingID, bookingRequest.getRequestedEquipments()));
            })
        .flatMap(
            t -> {
              BookingTO bookingTO = t.getT1();
              Optional<LocationTO> invoicePayableAtOpt = t.getT2();
              Optional<LocationTO> placeOfIssueOpt = t.getT3();
              Optional<List<CommodityTO>> commoditiesOpt = t.getT4();
              Optional<List<ValueAddedServiceRequestTO>> valueAddedServiceRequestsOpt = t.getT5();
              Optional<List<ReferenceTO>> referencesOpt = t.getT6();
              Optional<List<RequestedEquipmentTO>> requestedEquipmentsOpt = t.getT7();

              // populate the booking DTO
              invoicePayableAtOpt.ifPresent(bookingTO::setInvoicePayableAt);
              placeOfIssueOpt.ifPresent(bookingTO::setPlaceOfIssue);
              commoditiesOpt.ifPresent(bookingTO::setCommodities);
              valueAddedServiceRequestsOpt.ifPresent(bookingTO::setValueAddedServiceRequests);
              referencesOpt.ifPresent(bookingTO::setReferences);
              requestedEquipmentsOpt.ifPresent(bookingTO::setRequestedEquipments);

              return Mono.just(bookingTO);
            });
  }

  private BookingTO bookingToDTOWithNullLocations(Booking booking) {
    BookingTO bookingTO = bookingMapper.bookingToDTO(booking);
    // the mapper creates a new instance of location even if value of
    // invoicePayableAt is
    // null in booking
    // hence we set it to null if its a null object
    bookingTO.setInvoicePayableAt(null);
    bookingTO.setPlaceOfIssue(null);
    return bookingTO;
  }

  private Mono<Optional<LocationTO>> createLocationByTO(
      LocationTO locationTO, Function<String, Mono<Boolean>> updateBookingCallback) {

    if (Objects.isNull(locationTO)) {
      return Mono.just(Optional.empty());
    }

    Location location = locationMapper.dtoToLocation(locationTO);

    if (Objects.isNull(locationTO.getAddress())) {
      return locationRepository
          .save(location)
          .flatMap(l -> updateBookingCallback.apply(l.getId()).thenReturn(l))
          .map(locationMapper::locationToDTO)
          .map(Optional::of);
    } else {
      return addressRepository
          .save(locationTO.getAddress())
          .flatMap(
              a -> {
                location.setAddressID(a.getId());
                return locationRepository
                    .save(location)
                    .flatMap(l -> updateBookingCallback.apply(l.getId()).thenReturn(l))
                    .map(
                        l -> {
                          LocationTO lTO = locationMapper.locationToDTO(l);
                          lTO.setAddress(a);
                          return lTO;
                        })
                    .map(Optional::of);
              });
    }
  }

  private Mono<Optional<List<CommodityTO>>> createCommoditiesByBookingIDAndTOs(
      UUID bookingID, List<CommodityTO> commodities) {

    if (Objects.isNull(commodities) || commodities.isEmpty()) {
      return Mono.just(Optional.of(Collections.emptyList()));
    }

    Stream<Commodity> commodityStream =
        commodities.stream()
            .map(
                c -> {
                  Commodity commodity = commodityMapper.dtoToCommodity(c);
                  commodity.setBookingID(bookingID);
                  return commodity;
                });

    return commodityRepository
        .saveAll(Flux.fromStream(commodityStream))
        .map(commodityMapper::commodityToDTO)
        .collectList()
        .map(Optional::of);
  }

  private Mono<Optional<List<ValueAddedServiceRequestTO>>>
      createValueAddedServiceRequestsByBookingIDAndTOs(
          UUID bookingID, List<ValueAddedServiceRequestTO> valueAddedServiceRequests) {

    if (Objects.isNull(valueAddedServiceRequests) || valueAddedServiceRequests.isEmpty()) {
      return Mono.just(Optional.of(Collections.emptyList()));
    }

    Stream<ValueAddedServiceRequest> vasrStream =
        valueAddedServiceRequests.stream()
            .map(
                vasr -> {
                  ValueAddedServiceRequest valueAddedServiceRequest =
                      new ValueAddedServiceRequest();
                  valueAddedServiceRequest.setBookingID(bookingID);
                  valueAddedServiceRequest.setValueAddedServiceCode(
                      vasr.getValueAddedServiceCode());
                  return valueAddedServiceRequest;
                });

    return valueAddedServiceRequestRepository
        .saveAll(Flux.fromStream(vasrStream))
        .map(
            savedVasr -> {
              ValueAddedServiceRequestTO valueAddedServiceRequestTO =
                  new ValueAddedServiceRequestTO();
              valueAddedServiceRequestTO.setValueAddedServiceCode(
                  savedVasr.getValueAddedServiceCode());
              return valueAddedServiceRequestTO;
            })
        .collectList()
        .map(Optional::of);
  }

  private Mono<Optional<List<ReferenceTO>>> createReferencesByBookingIDAndTOs(
      UUID bookingID, List<ReferenceTO> references) {

    if (Objects.isNull(references) || references.isEmpty()) {
      return Mono.just(Optional.of(Collections.emptyList()));
    }

    Stream<Reference> referenceStream =
        references.stream()
            .map(
                r -> {
                  Reference reference = new Reference();
                  reference.setBookingID(bookingID);
                  reference.setReferenceType(r.getReferenceType());
                  reference.setReferenceValue(r.getReferenceValue());
                  return reference;
                });

    return referenceRepository
        .saveAll(Flux.fromStream(referenceStream))
        .map(
            r -> {
              ReferenceTO referenceTO = new ReferenceTO();
              referenceTO.setReferenceType(r.getReferenceType());
              referenceTO.setReferenceValue(r.getReferenceValue());
              return referenceTO;
            })
        .collectList()
        .map(Optional::of);
  }

  private Mono<Optional<List<RequestedEquipmentTO>>> createRequestedEquipmentsByBookingIDAndTOs(
      UUID bookingID, List<RequestedEquipmentTO> requestedEquipments) {

    if (Objects.isNull(requestedEquipments) || requestedEquipments.isEmpty()) {
      return Mono.just(Optional.of(Collections.emptyList()));
    }

    Stream<RequestedEquipment> requestedEquipmentsStream =
        requestedEquipments.stream()
            .map(
                reTo -> {
                  RequestedEquipment requestedEquipment = new RequestedEquipment();
                  requestedEquipment.setBookingID(bookingID);
                  requestedEquipment.setRequestedEquipmentType(
                      reTo.getRequestedEquipmentSizeType());
                  requestedEquipment.setRequestedEquipmentUnits(reTo.getRequestedEquipmentUnits());
                  requestedEquipment.setIsShipperOwned(reTo.isShipperOwned());
                  return requestedEquipment;
                });

    return requestedEquipmentRepository
        .saveAll(Flux.fromStream(requestedEquipmentsStream))
        .map(
            re -> {
              RequestedEquipmentTO requestedEquipmentTO = new RequestedEquipmentTO();
              requestedEquipmentTO.setRequestedEquipmentSizeType(re.getRequestedEquipmentType());
              requestedEquipmentTO.setRequestedEquipmentUnits(re.getRequestedEquipmentUnits());
              requestedEquipmentTO.setShipperOwned(re.getIsShipperOwned());
              return requestedEquipmentTO;
            })
        .collectList()
        .map(Optional::of);
  }

  @Override
  public Mono<BookingTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest) {
    return Mono.empty();
  }

  @Override
  public Mono<BookingTO> getBookingByCarrierBookingRequestReference(
      String carrierBookingRequestReference) {
    return bookingRepository
        .findByCarrierBookingRequestReference(carrierBookingRequestReference)
        .map(b -> Tuples.of(b.getId(), bookingMapper.bookingToDTO(b)))
        .doOnSuccess(
            t -> {
              // the mapper creates a new instance of location even if value of invoicePayableAt is
              // null in booking
              // hence we set it to null if its a null object
              if (t.getT2().getInvoicePayableAt().getId() == null) {
                t.getT2().setInvoicePayableAt(null);
              }
              if (t.getT2().getPlaceOfIssue().getId() == null) {
                t.getT2().setPlaceOfIssue(null);
              }
            })
        .flatMap(
            t -> {
              BookingTO bookingTO = t.getT2();

              String invoicePayableAtLocID =
                  Objects.isNull(bookingTO.getInvoicePayableAt())
                      ? null
                      : bookingTO.getInvoicePayableAt().getId();

              String placeOfIssueLocID =
                  Objects.isNull(bookingTO.getPlaceOfIssue())
                      ? null
                      : bookingTO.getPlaceOfIssue().getId();

              return Mono.zip(
                      fetchLocationByID(invoicePayableAtLocID),
                      fetchLocationByID(placeOfIssueLocID),
                      fetchCommoditiesByBookingID(t.getT1()),
                      fetchValueAddedServiceRequestsByBookingID(t.getT1()),
                      fetchReferencesByBookingID(t.getT1()),
                      fetchRequestedEquipmentsByBookingID(t.getT1()),
                      fetchDocumentPartiesByBookingID(t.getT1()),
                      fetchShipmentLocationsByBookingID(t.getT1()))
                  .doOnSuccess(
                      deepObjs -> {
                        Optional<LocationTO> locationToOpt1 = deepObjs.getT1();
                        Optional<LocationTO> locationToOpt2 = deepObjs.getT2();
                        Optional<List<CommodityTO>> commoditiesToOpt = deepObjs.getT3();
                        Optional<List<ValueAddedServiceRequestTO>> valueAddedServiceRequestsToOpt =
                            deepObjs.getT4();
                        Optional<List<ReferenceTO>> referenceTOsOpt = deepObjs.getT5();
                        Optional<List<RequestedEquipmentTO>> requestedEquipmentsToOpt =
                            deepObjs.getT6();
                        Optional<List<DocumentPartyTO>> documentPartiesToOpt = deepObjs.getT7();
                        Optional<List<ShipmentLocationTO>> shipmentLocationsToOpt =
                            deepObjs.getT8();

                        locationToOpt1.ifPresent(bookingTO::setInvoicePayableAt);
                        locationToOpt2.ifPresent(bookingTO::setPlaceOfIssue);
                        commoditiesToOpt.ifPresent(bookingTO::setCommodities);
                        valueAddedServiceRequestsToOpt.ifPresent(
                            bookingTO::setValueAddedServiceRequests);
                        referenceTOsOpt.ifPresent(bookingTO::setReferences);
                        requestedEquipmentsToOpt.ifPresent(bookingTO::setRequestedEquipments);
                        documentPartiesToOpt.ifPresent(bookingTO::setDocumentParties);
                        shipmentLocationsToOpt.ifPresent(bookingTO::setShipmentLocations);
                      })
                  .thenReturn(bookingTO);
            });
  }

  @Override
  public Mono<BookingConfirmationTO> getBookingConfirmationByCarrierBookingReference(
      String carrierBookingRequestReference) {
    return bookingConfirmationRepository
        .findByCarrierBookingReference(carrierBookingRequestReference)
        .map(b -> Tuples.of(b, shipmentMapper.shipmentToDTO(b)))
        .flatMap(
            t -> {
              BookingConfirmationTO bookingConfirmationTO = t.getT2();
              return Mono.zip(
                      fetchShipmentCutOffTimeByBookingID(t.getT1().getShipmentID()),
                      fetchShipmentLocationsByBookingID(t.getT1().getBookingID()))
                  .flatMap(
                      deepObjs -> {
                        Optional<List<ShipmentCutOffTimeTO>> shipmentCutOffTimeTOpt =
                            deepObjs.getT1();
                        Optional<List<ShipmentLocationTO>> shipmentLocationsToOpt =
                            deepObjs.getT2();
                        shipmentCutOffTimeTOpt.ifPresent(
                            bookingConfirmationTO::setShipmentCutOffTimes);
                        shipmentLocationsToOpt.ifPresent(
                            bookingConfirmationTO::setShipmentLocations);
                        return Mono.just(bookingConfirmationTO);
                      })
                  .thenReturn(bookingConfirmationTO);
            });
  }

  private Mono<Optional<LocationTO>> fetchLocationByID(String id) {
    if (id == null) return Mono.just(Optional.empty());
    return locationRepository
        .findById(id)
        .flatMap(
            location ->
                Mono.zip(
                        addressRepository
                            .findByIdOrEmpty(location.getAddressID())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty()),
                        facilityRepository
                            .findByIdOrEmpty(location.getFacilityID())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty()))
                    .flatMap(
                        t2 -> {
                          LocationTO locTO = locationMapper.locationToDTO(location);
                          t2.getT1().ifPresent(locTO::setAddress);
                          t2.getT2().ifPresent(locTO::setFacility);
                          return Mono.just(locTO);
                        }))
        .map(Optional::of);
  }

  private Mono<Optional<List<CommodityTO>>> fetchCommoditiesByBookingID(UUID bookingID) {
    return commodityRepository
        .findByBookingID(bookingID)
        .map(commodityMapper::commodityToDTO)
        .collectList()
        .map(Optional::of);
  }

  private Mono<Optional<List<ShipmentCutOffTimeTO>>> fetchShipmentCutOffTimeByBookingID(
      UUID shipmentID) {
    return shipmentCutOffTimeRepository
        .findAllByShipmentID(shipmentID)
        .map(shipmentMapper::shipmentCutOffTimeToDTO)
        .collectList()
        .map(Optional::of);
  }

  private Mono<Optional<List<ValueAddedServiceRequestTO>>>
      fetchValueAddedServiceRequestsByBookingID(UUID bookingID) {
    return valueAddedServiceRequestRepository
        .findByBookingID(bookingID)
        .map(
            vasr -> {
              ValueAddedServiceRequestTO vTo = new ValueAddedServiceRequestTO();
              vTo.setValueAddedServiceCode(vasr.getValueAddedServiceCode());
              return vTo;
            })
        .collectList()
        .map(Optional::of);
  }

  private Mono<Optional<List<ReferenceTO>>> fetchReferencesByBookingID(UUID bookingID) {
    return referenceRepository
        .findByBookingID(bookingID)
        .map(
            r -> {
              ReferenceTO referenceTO = new ReferenceTO();
              referenceTO.setReferenceType(r.getReferenceType());
              referenceTO.setReferenceValue(r.getReferenceValue());
              return referenceTO;
            })
        .collectList()
        .map(Optional::of);
  }

  private Mono<Optional<List<RequestedEquipmentTO>>> fetchRequestedEquipmentsByBookingID(
      UUID bookingId) {
    return requestedEquipmentRepository
        .findByBookingID(bookingId)
        .map(
            re -> {
              RequestedEquipmentTO requestedEquipmentTO = new RequestedEquipmentTO();
              requestedEquipmentTO.setRequestedEquipmentUnits(re.getRequestedEquipmentUnits());
              requestedEquipmentTO.setRequestedEquipmentSizeType(re.getRequestedEquipmentType());
              return requestedEquipmentTO;
            })
        .collectList()
        .map(Optional::of);
  }

  private Mono<Optional<List<DocumentPartyTO>>> fetchDocumentPartiesByBookingID(UUID bookingId) {
    return documentPartyRepository
        .findByBookingID(bookingId)
        .flatMap(
            dp ->
                Mono.zip(
                        fetchPartyByID(dp.getPartyID()),
                        fetchPartyContactDetailsByPartyID(dp.getPartyID()),
                        fetchDisplayAddressByDocumentID(dp.getId()))
                    .flatMap(
                        t -> {
                          Optional<PartyTO> partyToOpt = t.getT1();
                          Optional<List<PartyContactDetailsTO>> partyContactDetailsToOpt =
                              t.getT2();
                          Optional<List<String>> displayAddressOpt = t.getT3();

                          DocumentPartyTO documentPartyTO = new DocumentPartyTO();
                          partyToOpt.ifPresent(documentPartyTO::setParty);
                          documentPartyTO.setPartyFunction(dp.getPartyFunction());
                          documentPartyTO.setToBeNotified(dp.getIsToBeNotified());
                          displayAddressOpt.ifPresent(documentPartyTO::setDisplayedAddress);
                          partyContactDetailsToOpt.ifPresent(
                              documentPartyTO::setPartyContactDetails);
                          return Mono.just(documentPartyTO);
                        }))
        .collectList()
        .map(Optional::of);
  }

  private Mono<Optional<PartyTO>> fetchPartyByID(String partyID) {
    return partyRepository
        .findByIdOrEmpty(partyID)
        .flatMap(
            p ->
                Mono.zip(
                        addressRepository
                            .findByIdOrEmpty(p.getAddressID())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty()),
                        partyIdentifyingCodeRepository
                            .findAllByPartyID(partyID)
                            .map(
                                idc ->
                                    PartyTO.IdentifyingCode.builder()
                                        .partyCode(idc.getPartyCode())
                                        .codeListName(idc.getCodeListName())
                                        .dcsaResponsibleAgencyCode(
                                            idc.getDcsaResponsibleAgencyCode())
                                        .build())
                            .collectList()
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty()))
                    .flatMap(
                        t -> {
                          Optional<Address> addressOpt = t.getT1();
                          Optional<List<PartyTO.IdentifyingCode>> identifyingCodesOpt = t.getT2();
                          PartyTO partyTO = partyMapper.partyToDTO(p);
                          addressOpt.ifPresent(partyTO::setAddress);
                          identifyingCodesOpt.ifPresent(partyTO::setIdentifyingCodes);
                          return Mono.just(partyTO);
                        }))
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  private Mono<Optional<List<PartyContactDetailsTO>>> fetchPartyContactDetailsByPartyID(
      String partyID) {
    return partyContactDetailsRepository
        .findByPartyID(partyID)
        .map(pcd -> new PartyContactDetailsTO(pcd.getName(), pcd.getEmail(), pcd.getPhone()))
        .collectList()
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  private Mono<Optional<List<String>>> fetchDisplayAddressByDocumentID(UUID documentPartyID) {
    return displayedAddressRepository
        .findByDocumentPartyIDOrderByAddressLineNumber(documentPartyID)
        .map(DisplayedAddress::getAddressLine)
        .collectList()
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  private Mono<Optional<List<ShipmentLocationTO>>> fetchShipmentLocationsByBookingID(
      UUID bookingID) {
    return shipmentLocationRepository
        .findByBookingID(bookingID)
        .flatMap(
            sl ->
                fetchLocationByID(sl.getLocationID())
                    .flatMap(
                        lopt -> {
                          ShipmentLocationTO shipmentLocationTO =
                              shipmentMapper.shipmentLocationToDTO(sl);
                          lopt.ifPresent(shipmentLocationTO::setLocation);
                          return Mono.just(shipmentLocationTO);
                        }))
        .collectList()
        .map(Optional::of);
  }

  @Override
  public Mono<Void> cancelBookingByCarrierBookingReference(String carrierBookingReference) {
    return Mono.empty();
  }
}
