package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.mappers.BookingMapper;
import org.dcsa.bkg.model.mappers.CommodityMapper;
import org.dcsa.bkg.model.mappers.LocationMapper;
import org.dcsa.bkg.model.mappers.PartyMapper;
import org.dcsa.bkg.model.transferobjects.*;
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

  // mappers
  private final BookingMapper bookingMapper;
  private final LocationMapper locationMapper;
  private final CommodityMapper commodityMapper;
  private final PartyMapper partyMapper;

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
              if (t.getT2().getInvoicePayableAt().getId() == null)
                t.getT2().setInvoicePayableAt(null);
            })
        .flatMap(
            t -> {
              BookingTO bookingTO = t.getT2();
              return Mono.zip(
                      fetchLocationByBooking(bookingTO),
                      fetchCommoditiesByBookingID(t.getT1()),
                      fetchValueAddedServiceRequestsByBookingID(t.getT1()),
                      fetchReferencesByBookingID(t.getT1()),
                      fetchRequestedEquipmentsByBookingID(t.getT1()),
                      fetchDocumentPartiesByBookingID(t.getT1()),
                      fetchShipmentLocationsByBookingID(t.getT1()))
                  .doOnSuccess(
                      deepObjs -> {
                        Optional<LocationTO> locationToOpt = deepObjs.getT1();
                        Optional<List<CommodityTO>> commoditiesToOpt = deepObjs.getT2();
                        Optional<List<ValueAddedServiceRequestTO>> valueAddedServiceRequestsToOpt =
                            deepObjs.getT3();
                        Optional<List<ReferenceTO>> referenceTOsOpt = deepObjs.getT4();
                        Optional<List<RequestedEquipmentTO>> requestedEquipmentsToOpt =
                            deepObjs.getT5();
                        Optional<List<DocumentPartyTO>> documentPartiesToOpt = deepObjs.getT6();
                        Optional<List<ShipmentLocationTO>> shipmentLocationsToOpt =
                            deepObjs.getT7();

                        locationToOpt.ifPresent(bookingTO::setInvoicePayableAt);
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

  private Mono<Optional<LocationTO>> fetchLocationByBooking(BookingTO bookingTO) {

    if (Objects.isNull(bookingTO.getInvoicePayableAt())) {
      return Mono.just(Optional.empty());
    }

    return locationRepository
        .findById(bookingTO.getInvoicePayableAt().getId())
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
                          LocationTO locationTO = locationMapper.locationToDTO(location);
                          t2.getT1().ifPresent(locationTO::setAddress);
                          t2.getT2().ifPresent(locationTO::setFacility);
                          return Mono.just(locationTO);
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
                locationRepository
                    .findById(sl.getLocationID())
                    .map(Optional::of)
                    .defaultIfEmpty(Optional.empty())
                    .flatMap(
                        lopt -> {
                          ShipmentLocationTO shipmentLocationTO = new ShipmentLocationTO();
                          lopt.ifPresent(
                              l -> shipmentLocationTO.setLocation(locationMapper.locationToDTO(l)));
                          shipmentLocationTO.setDisplayedName(sl.getDisplayedName());
                          shipmentLocationTO.setLocationType(sl.getShipmentLocationTypeCode());
                          shipmentLocationTO.setEventDateTime(sl.getEventDateTime());
                          return Mono.just(shipmentLocationTO);
                        }))
        .collectList()
        .map(Optional::of);
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
