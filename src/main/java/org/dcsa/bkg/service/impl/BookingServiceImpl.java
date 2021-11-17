package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.mappers.*;
import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.bkg.repositories.BookingConfirmationRepository;
import org.dcsa.bkg.repositories.ShipmentCutOffTimeRepository;
import org.dcsa.bkg.service.BookingService;
import org.dcsa.core.events.model.Address;
import org.dcsa.core.events.model.DisplayedAddress;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.events.model.transferobjects.PartyTO;
import org.dcsa.core.events.repository.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

  // repositories
  private final BookingRepository bookingRepository;
  private final BookingConfirmationRepository bookingConfirmationRepository;
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

  // mappers
  private final BookingMapper bookingMapper;
  private final LocationMapper locationMapper;
  private final CommodityMapper commodityMapper;
  private final PartyMapper partyMapper;
  private final ShipmentMapper bookingConfirmationMapper;

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
        .map(b -> Tuples.of(b, bookingConfirmationMapper.shipmentToDTO(b)))
        .flatMap(
            t -> {
              BookingConfirmationTO bookingConfirmationTO = t.getT2();
              return Mono.zip(
                      fetchShipmentCutOffTimeByBookingID(t.getT1().getBookingID()),
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
      UUID bookingID) {
    return shipmentCutOffTimeRepository
        .findAllByBookingID(bookingID)
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
                          ShipmentLocationTO shipmentLocationTO = new ShipmentLocationTO();
                          lopt.ifPresent(shipmentLocationTO::setLocation);
                          shipmentLocationTO.setDisplayedName(sl.getDisplayedName());
                          shipmentLocationTO.setShipmentLocationTypeCode(
                              sl.getShipmentLocationTypeCode());
                          shipmentLocationTO.setEventDateTime(sl.getEventDateTime());
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
