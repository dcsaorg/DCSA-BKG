package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dcsa.bkg.model.transferobjects.BookingCancellationRequestTO;
import org.dcsa.bkg.service.BKGService;
import org.dcsa.core.events.edocumentation.model.mapper.*;
import org.dcsa.core.events.edocumentation.model.transferobject.*;
import org.dcsa.core.events.edocumentation.repository.*;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventClassifierCode;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.enums.TransportEventTypeCode;
import org.dcsa.core.events.model.mapper.RequestedEquipmentMapper;
import org.dcsa.core.events.model.transferobjects.DocumentPartyTO;
import org.dcsa.core.events.model.transferobjects.ReferenceTO;
import org.dcsa.core.events.repository.*;
import org.dcsa.core.events.service.DocumentPartyService;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.exception.CreateException;
import org.dcsa.core.exception.NotFoundException;
import org.dcsa.core.exception.UpdateException;
import org.dcsa.skernel.model.Location;
import org.dcsa.skernel.model.Vessel;
import org.dcsa.skernel.model.mapper.LocationMapper;
import org.dcsa.skernel.model.transferobjects.LocationTO;
import org.dcsa.skernel.repositority.LocationRepository;
import org.dcsa.skernel.repositority.VesselRepository;
import org.dcsa.skernel.service.AddressService;
import org.dcsa.skernel.service.LocationService;
import org.dcsa.skernel.service.PartyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BKGServiceImpl implements BKGService {

  // repositories
  private final BookingRepository bookingRepository;
  private final LocationRepository locationRepository;
  private final CommodityRepository commodityRepository;
  private final ValueAddedServiceRequestRepository valueAddedServiceRequestRepository;
  private final ReferenceRepository referenceRepository;
  private final RequestedEquipmentRepository requestedEquipmentRepository;
  private final DocumentPartyRepository documentPartyRepository;
  private final DocumentPartyService documentPartyService;
  private final PartyService partyService;
  private final ShipmentLocationRepository shipmentLocationRepository;
  private final DisplayedAddressRepository displayedAddressRepository;
  private final ShipmentCutOffTimeRepository shipmentCutOffTimeRepository;
  private final ShipmentRepository shipmentRepository;
  private final VesselRepository vesselRepository;
  private final ShipmentCarrierClausesRepository shipmentCarrierClausesRepository;
  private final CarrierClauseRepository carrierClauseRepository;
  private final ChargeRepository chargeRepository;
  private final TransportRepository transportRepository;
  private final ShipmentTransportRepository shipmentTransportRepository;
  private final TransportCallRepository transportCallRepository;
  private final TransportEventRepository transportEventRepository;
  private final ModeOfTransportRepository modeOfTransportRepository;
  private final VoyageRepository voyageRepository;
  private final RequestedEquipmentEquipmentRepository requestedEquipmentEquipmentRepository;

  // mappers
  private final BookingMapper bookingMapper;
  private final LocationMapper locationMapper;
  private final CommodityMapper commodityMapper;
  private final ShipmentMapper shipmentMapper;
  private final CarrierClauseMapper carrierClauseMapper;
  private final ConfirmedEquipmentMapper confirmedEquipmentMapper;
  private final ChargeMapper chargeMapper;
  private final TransportMapper transportMapper;
  private final RequestedEquipmentMapper requestedEquipmentMapper;

  // services
  private final ShipmentEventService shipmentEventService;
  private final LocationService locationService;
  private final AddressService addressService;

  @Override
  @Transactional
  public Mono<BookingResponseTO> createBooking(final BookingTO bookingRequest) {

    String bookingRequestError = validateBookingRequest(bookingRequest);
    if (!bookingRequestError.isEmpty()) {
      return Mono.error(new CreateException(bookingRequestError));
    }

    OffsetDateTime now = OffsetDateTime.now();
    Booking requestedBooking = bookingMapper.dtoToBooking(bookingRequest);
    // CarrierBookingRequestReference is not allowed to be set by request
    requestedBooking.setCarrierBookingRequestReference(null);
    requestedBooking.setDocumentStatus(ShipmentEventTypeCode.RECE);
    requestedBooking.setBookingRequestDateTime(now);
    requestedBooking.setUpdatedDateTime(now);

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
        .flatMap(booking -> createDeepObjectsForBooking(bookingRequest, booking))
        .flatMap(bTO -> createShipmentEventFromBookingTO(bTO).thenReturn(bTO))
        .flatMap(bTO -> Mono.just(bookingMapper.dtoToBookingResponseTO(bTO)));
  }

  private Mono<BookingTO> createDeepObjectsForBooking(BookingTO bookingRequest, Booking booking) {
    UUID bookingID = booking.getId();
    BookingTO bookingTO = bookingToDTOWithNullLocations(booking);
    return Mono.when(
            findVesselAndUpdateBooking(
                    bookingRequest.getVesselName(), bookingRequest.getVesselIMONumber(), bookingID)
                .doOnNext(
                    v -> {
                      bookingTO.setVesselName(v.getVesselName());
                      bookingTO.setVesselIMONumber(v.getVesselIMONumber());
                    }),
            locationService
                .ensureResolvable(bookingRequest.getInvoicePayableAt())
                .flatMap(
                    lTO ->
                        bookingRepository
                            .setInvoicePayableAtFor(lTO.getId(), bookingID)
                            .thenReturn(lTO))
                .doOnNext(bookingTO::setInvoicePayableAt),
            locationService
                .ensureResolvable(bookingRequest.getPlaceOfIssue())
                .flatMap(
                    lTO ->
                        bookingRepository
                            .setPlaceOfIssueIDFor(lTO.getId(), bookingID)
                            .thenReturn(lTO))
                .doOnNext(bookingTO::setPlaceOfIssue),
            createCommoditiesByBookingIDAndTOs(bookingID, bookingRequest.getCommodities())
                .doOnNext(bookingTO::setCommodities),
            createValueAddedServiceRequestsByBookingIDAndTOs(
                    bookingID, bookingRequest.getValueAddedServiceRequests())
                .doOnNext(bookingTO::setValueAddedServiceRequests),
            createReferencesByBookingIDAndTOs(bookingID, bookingRequest.getReferences())
                .doOnNext(bookingTO::setReferences),
            createRequestedEquipmentsByBookingIDAndTOs(
                    bookingID, bookingRequest.getRequestedEquipments())
                .doOnNext(bookingTO::setRequestedEquipments),
            documentPartyService
                .createDocumentPartiesByBookingID(bookingID, bookingRequest.getDocumentParties())
                .doOnNext(bookingTO::setDocumentParties),
            createShipmentLocationsByBookingIDAndTOs(
                    bookingID, bookingRequest.getShipmentLocations())
                .doOnNext(bookingTO::setShipmentLocations))
        .thenReturn(bookingTO);
  }

  private BookingTO bookingToDTOWithNullLocations(Booking booking) {
    BookingTO bookingTO = bookingMapper.bookingToDTO(booking);
    // the mapper creates a new instance of location even if value of invoicePayableAt is null in
    // booking hence we set it to null if it's a null object
    bookingTO.setInvoicePayableAt(null);
    bookingTO.setPlaceOfIssue(null);
    return bookingTO;
  }

  // Booking should not create a vessel, only use existing ones.
  // If the requested vessel does not exist or the values don't match
  // an error should be thrown
  private Mono<Vessel> findVesselAndUpdateBooking(
      String vesselName, String vesselIMONumber, UUID bookingID) {

    Vessel vessel = new Vessel();
    vessel.setVesselName(vesselName);
    vessel.setVesselIMONumber(vesselIMONumber);

    if (!StringUtils.isEmpty(vesselIMONumber)) {
      return vesselRepository
          .findByVesselIMONumberOrEmpty(vesselIMONumber)
          .flatMap(
              v -> {
                if (!vesselName.equals(v.getVesselName())) {
                  return Mono.error(
                      new CreateException(
                          "Provided vessel name does not match vessel name of existing vesselIMONumber."));
                }
                return Mono.just(v);
              })
          .flatMap(v -> bookingRepository.setVesselIDFor(v.getId(), bookingID).thenReturn(v));
    } else if (!StringUtils.isEmpty(vesselName)) {
      return vesselRepository
          .findByVesselNameOrEmpty(vesselName)
          .collectList()
          .flatMap(
              vs -> {
                if (vs.size() > 1) {
                  return Mono.error(
                      new CreateException(
                          "Unable to identify unique vessel, please provide a vesselIMONumber."));
                }
                return Mono.just(vs.get(0));
              })
          .flatMap(v -> bookingRepository.setVesselIDFor(v.getId(), bookingID).thenReturn(v));
    } else {
      return Mono.just(new Vessel());
    }
  }

  private Mono<List<CommodityTO>> createCommoditiesByBookingIDAndTOs(
      UUID bookingID, List<CommodityTO> commodities) {

    if (Objects.isNull(commodities) || commodities.isEmpty()) {
      return Mono.empty();
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
        .collectList();
  }

  private Mono<List<ValueAddedServiceRequestTO>>
      createValueAddedServiceRequestsByBookingIDAndTOs(
          UUID bookingID, List<ValueAddedServiceRequestTO> valueAddedServiceRequests) {

    if (Objects.isNull(valueAddedServiceRequests) || valueAddedServiceRequests.isEmpty()) {
      return Mono.empty();
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
        .collectList();
  }

  private Mono<List<ReferenceTO>> createReferencesByBookingIDAndTOs(
      UUID bookingID, List<ReferenceTO> references) {

    if (Objects.isNull(references) || references.isEmpty()) {
      return Mono.empty();
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
        .collectList();
  }

  private Mono<List<RequestedEquipmentTO>> createRequestedEquipmentsByBookingIDAndTOs(
      UUID bookingID, List<RequestedEquipmentTO> requestedEquipments) {

    if (Objects.isNull(requestedEquipments) || requestedEquipments.isEmpty()) {
      return Mono.empty();
    }

    return Flux.fromIterable(requestedEquipments)
        .filter(this::isValidRequestedEquipmentTO)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Requested Equipment Units cannot be lower than quantity of Equipment References.")))
        .flatMap(
            requestedEquipmentTO ->
                saveRequestedEquipmentAndEquipmentReferences(bookingID, requestedEquipmentTO))
        .collectList();
  }

  private Mono<RequestedEquipmentTO> saveRequestedEquipmentAndEquipmentReferences(
      UUID bookingID, RequestedEquipmentTO requestedEquipmentTO) {
    List<String> equipmentReferences = requestedEquipmentTO.getEquipmentReferences();
    return Mono.just(
            requestedEquipmentMapper.dtoToRequestedEquipmentWithBookingId(
                requestedEquipmentTO, bookingID))
        .flatMap(requestedEquipmentRepository::save)
        .filter(requestedEquipment -> Objects.nonNull(equipmentReferences))
        .flatMap(
            requestedEquipment ->
                mapAndSaveRequestedEquipmentEquipment(requestedEquipment, equipmentReferences))
        .thenReturn(requestedEquipmentTO);
  }

  private Mono<List<RequestedEquipmentEquipment>> mapAndSaveRequestedEquipmentEquipment(
      RequestedEquipment requestedEquipment, List<String> equipmentReferences) {
    return Flux.fromIterable(equipmentReferences)
        .flatMap(
            equipmentReference ->
                mapRequestedEquipmentToRequestedEquipmentEquipment(
                    requestedEquipment, equipmentReference))
        .collectList();
  }

  private Mono<RequestedEquipmentEquipment> mapRequestedEquipmentToRequestedEquipmentEquipment(
      RequestedEquipment requestedEquipment, String equipmentReference) {
    RequestedEquipmentEquipment requestedEquipmentEquipment = new RequestedEquipmentEquipment();
    requestedEquipmentEquipment.setRequestedEquipmentId(requestedEquipment.getId());
    requestedEquipmentEquipment.setEquipmentReference(equipmentReference);
    return requestedEquipmentEquipmentRepository.save(requestedEquipmentEquipment);
  }

  private Boolean isValidRequestedEquipmentTO(RequestedEquipmentTO requestedEquipmentTO) {
    Boolean isValid = true;
    if (Objects.nonNull(requestedEquipmentTO.getEquipmentReferences())) {
      isValid =
          requestedEquipmentTO.getEquipmentReferences().size()
              <= requestedEquipmentTO.getRequestedEquipmentUnits();
    }
    return isValid;
  }

  private Mono<List<ShipmentLocationTO>> createShipmentLocationsByBookingIDAndTOs(
      final UUID bookingID, List<ShipmentLocationTO> shipmentLocations) {

    if (Objects.isNull(shipmentLocations) || shipmentLocations.isEmpty()) {
      return Mono.just(Collections.emptyList());
    }

    return Flux.fromStream(shipmentLocations.stream())
        .flatMap(
            slTO -> {
              ShipmentLocation shipmentLocation = new ShipmentLocation();
              shipmentLocation.setBookingID(bookingID);
              shipmentLocation.setShipmentLocationTypeCode(slTO.getShipmentLocationTypeCode());
              shipmentLocation.setDisplayedName(slTO.getDisplayedName());
              shipmentLocation.setEventDateTime(slTO.getEventDateTime());

              Location location = locationMapper.dtoToLocation(slTO.getLocation());

              if (Objects.isNull(slTO.getLocation().getAddress())) {
                return locationRepository
                    .save(location)
                    .map(
                        l -> {
                          LocationTO lTO = locationMapper.locationToDTO(l);
                          shipmentLocation.setLocationID(l.getId());
                          return Tuples.of(lTO, shipmentLocation);
                        });
              } else {
                return addressService
                    .ensureResolvable(slTO.getLocation().getAddress())
                    .flatMap(
                        a -> {
                          location.setAddressID(a.getId());
                          return locationRepository
                              .save(location)
                              .map(
                                  l -> {
                                    LocationTO lTO = locationMapper.locationToDTO(l);
                                    lTO.setAddress(a);
                                    shipmentLocation.setLocationID(l.getId());
                                    return Tuples.of(lTO, shipmentLocation);
                                  });
                        });
              }
            })
        .flatMap(
            t ->
                shipmentLocationRepository
                    .save(t.getT2())
                    .map(
                        savedSl -> {
                          ShipmentLocationTO shipmentLocationTO = new ShipmentLocationTO();
                          shipmentLocationTO.setLocation(t.getT1());
                          shipmentLocationTO.setShipmentLocationTypeCode(
                              savedSl.getShipmentLocationTypeCode());
                          shipmentLocationTO.setDisplayedName(savedSl.getDisplayedName());
                          shipmentLocationTO.setEventDateTime(savedSl.getEventDateTime());
                          return shipmentLocationTO;
                        }))
        .collectList();
  }

  @Override
  @Transactional
  public Mono<BookingResponseTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest) {

    String bookingRequestError = validateBookingRequest(bookingRequest);
    if (!bookingRequestError.isEmpty()) {
      return Mono.error(new CreateException(bookingRequestError));
    }

    return bookingRepository
        .findByCarrierBookingRequestReference(carrierBookingRequestReference)
        .flatMap(checkUpdateBookingStatus)
        .flatMap(booking -> {
          // update the valid_until field for to be copied booking
          // to ensure the copy is unique and the one to be used
          booking.setValidUntil(OffsetDateTime.now());
          return bookingRepository.save(booking).thenReturn(booking);
        })
        .flatMap(
            b -> {
              // update booking with new booking request
              Booking booking = bookingMapper.dtoToBooking(bookingRequest);
              // set booking ID to null so that it creates a copy and inserts with a new ID
              booking.setId(null);
              booking.setDocumentStatus(b.getDocumentStatus());
              booking.setBookingRequestDateTime(b.getBookingRequestDateTime());
              booking.setUpdatedDateTime(OffsetDateTime.now());
              return bookingRepository.save(booking).thenReturn(b);
            })
        .flatMap(
            booking ->
                // resolve entities linked to booking
                createDeepObjectsForBooking(bookingRequest, booking))
        .flatMap(bTO -> createShipmentEventFromBookingTO(bTO).thenReturn(bTO))
        .switchIfEmpty(
            Mono.defer(
                () ->
                    Mono.error(
                        new UpdateException(
                            "No booking found for given carrierBookingRequestReference."))))
        .flatMap(bTO -> Mono.just(bookingMapper.dtoToBookingResponseTO(bTO)));
  }

  private Mono<List<CommodityTO>> resolveCommoditiesForBookingID(
      List<CommodityTO> commodities, UUID bookingID) {

    return commodityRepository
        .deleteByBookingID(bookingID)
        .then(createCommoditiesByBookingIDAndTOs(bookingID, commodities));
  }

  private Mono<List<ValueAddedServiceRequestTO>> resolveValueAddedServiceReqForBookingID(
      List<ValueAddedServiceRequestTO> valAddedSerReqs, UUID bookingID) {

    return valueAddedServiceRequestRepository
        .deleteByBookingID(bookingID)
        .then(createValueAddedServiceRequestsByBookingIDAndTOs(bookingID, valAddedSerReqs));
  }

  private Mono<List<ReferenceTO>> resolveReferencesForBookingID(
      List<ReferenceTO> references, UUID bookingID) {

    return referenceRepository
        .deleteByBookingID(bookingID)
        .then(createReferencesByBookingIDAndTOs(bookingID, references));
  }

  private Mono<List<RequestedEquipmentTO>> resolveReqEqForBookingID(
      List<RequestedEquipmentTO> requestedEquipments, UUID bookingID) {

    return requestedEquipmentRepository
        .deleteByBookingID(bookingID)
        .then(requestedEquipmentEquipmentRepository.deleteByBookingId(bookingID))
        .then(createRequestedEquipmentsByBookingIDAndTOs(bookingID, requestedEquipments));
  }

  private Mono<List<DocumentPartyTO>> resolveDocumentPartiesForBookingID(
      List<DocumentPartyTO> documentPartyTOs, UUID bookingID) {

    // this will create orphan parties
    return documentPartyRepository
        .deleteByBookingID(bookingID)
        .then(documentPartyService.createDocumentPartiesByBookingID(bookingID, documentPartyTOs));
  }

  private Mono<List<ShipmentLocationTO>> resolveShipmentLocationsForBookingID(
      List<ShipmentLocationTO> shipmentLocationTOs, UUID bookingID) {

    // this will create orphan locations
    return shipmentLocationRepository
        .deleteByBookingID(bookingID)
        .then(createShipmentLocationsByBookingIDAndTOs(bookingID, shipmentLocationTOs));
  }

  @Override
  public Mono<BookingTO> getBookingByCarrierBookingRequestReference(
      String carrierBookingRequestReference) {
    return bookingRepository
        .findByCarrierBookingRequestReference(carrierBookingRequestReference)
        .map(b -> Tuples.of(b.getId(), bookingMapper.bookingToDTO(b), b))
        .switchIfEmpty(
            Mono.error(
                new NotFoundException(
                    "No booking found with carrier booking request reference: "
                        + carrierBookingRequestReference)))
        .doOnSuccess(
            t -> {
              // the mapper creates a new instance of location even if value of invoicePayableAt
              // is null in booking hence we set it to null if it's a null object
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
              Booking booking = t.getT3();

              String invoicePayableAtLocID =
                  Objects.isNull(bookingTO.getInvoicePayableAt())
                      ? null
                      : bookingTO.getInvoicePayableAt().getId();

              String placeOfIssueLocID =
                  Objects.isNull(bookingTO.getPlaceOfIssue())
                      ? null
                      : bookingTO.getPlaceOfIssue().getId();

              return Mono.when(
                      fetchLocationTupleByID(invoicePayableAtLocID, placeOfIssueLocID)
                          .doOnNext(locations -> bookingTO.setInvoicePayableAt(locations.getT1()))
                          .doOnNext(locations -> bookingTO.setPlaceOfIssue(locations.getT2())),
                      fetchVesselByVesselID(booking.getVesselId())
                          .doOnNext(vessel -> bookingTO.setVesselName(vessel.getVesselName()))
                          .doOnNext(
                              vessel -> bookingTO.setVesselIMONumber(vessel.getVesselIMONumber())),
                      fetchCommoditiesByBookingID(booking.getId())
                          .doOnNext(bookingTO::setCommodities),
                      fetchValueAddedServiceRequestsByBookingID(booking.getId())
                          .doOnNext(bookingTO::setValueAddedServiceRequests),
                      fetchReferencesByBookingID(booking.getId())
                          .doOnNext(bookingTO::setReferences),
                      fetchRequestedEquipmentsByBookingID(booking.getId())
                          .doOnNext(bookingTO::setRequestedEquipments),
                      fetchDocumentPartiesByBookingID(booking.getId())
                          .doOnNext(bookingTO::setDocumentParties),
                      fetchShipmentLocationsByBookingID(booking.getId())
                          .doOnNext(bookingTO::setShipmentLocations))
                  .thenReturn(bookingTO);
            });
  }

  @Override
  public Mono<ShipmentTO> getShipmentByCarrierBookingReference(
      String carrierBookingRequestReference) {

    return shipmentRepository
        .findByCarrierBookingReference(carrierBookingRequestReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No booking found with carrier booking reference: "
                        + carrierBookingRequestReference)))
        .flatMap(
            shipment -> {
              ShipmentTO shipmentTO = shipmentMapper.shipmentToDTO(shipment);
              return Mono.when(
                      fetchShipmentCutOffTimeByBookingID(shipment.getBookingID())
                          .doOnNext(shipmentTO::setShipmentCutOffTimes),
                      fetchShipmentLocationsByBookingID(shipment.getBookingID())
                          .doOnNext(shipmentTO::setShipmentLocations),
                      fetchCarrierClausesByShipmentID(shipment.getShipmentID())
                          .doOnNext(shipmentTO::setCarrierClauses),
                      fetchConfirmedEquipmentByByBookingID(shipment.getBookingID())
                          .doOnNext(shipmentTO::setConfirmedEquipments),
                      fetchChargesByShipmentID(shipment.getShipmentID())
                          .doOnNext(shipmentTO::setCharges),
                      fetchBookingByBookingID(shipment.getBookingID())
                          .doOnNext(shipmentTO::setBooking),
                      fetchTransports(shipment.getShipmentID()).doOnNext(shipmentTO::setTransports))
                  .thenReturn(shipmentTO);
            });
  }

  private Mono<Tuple2<LocationTO, LocationTO>> fetchLocationTupleByID(
      String invoicePayableAtLocID, String placeOfIssueLocID) {
    return Mono.zip(
            locationService.fetchLocationDeepObjByID(invoicePayableAtLocID),
            locationService.fetchLocationDeepObjByID(placeOfIssueLocID))
        .map(deepObjs -> Tuples.of(deepObjs.getT1(), deepObjs.getT2()));
  }

  private Mono<LocationTO> fetchLocationByTransportCallId(String id) {
    if (id == null) return Mono.empty();
    return transportCallRepository
        .findById(id)
        .flatMap(
            transportCall ->
                locationService.fetchLocationDeepObjByID(transportCall.getLocationID()));
  }

  private Mono<List<CarrierClauseTO>> fetchCarrierClausesByShipmentID(UUID shipmentID) {
    if (shipmentID == null) return Mono.empty();
    return shipmentCarrierClausesRepository
        .findAllByShipmentID(shipmentID)
        .flatMap(
            shipmentCarrierClause ->
                carrierClauseRepository.findById(shipmentCarrierClause.getCarrierClauseID()))
        .flatMap(x -> Mono.just(carrierClauseMapper.carrierClauseToDTO(x)))
        .collectList();
  }

  private Mono<List<CommodityTO>> fetchCommoditiesByBookingID(UUID bookingID) {
    return commodityRepository
        .findByBookingID(bookingID)
        .map(commodityMapper::commodityToDTO)
        .collectList();
  }

  private Mono<List<ChargeTO>> fetchChargesByShipmentID(UUID shipmentID) {
    return chargeRepository
        .findAllByShipmentID(shipmentID)
        .map(chargeMapper::chargeToDTO)
        .collectList();
  }

  private Mono<BookingTO> fetchBookingByBookingID(UUID bookingID) {
    if (bookingID == null) return Mono.empty();
    return bookingRepository
        .findById(bookingID)
        .flatMap(
            booking -> {
              BookingTO bookingTO = bookingMapper.bookingToDTO(booking);
              return Mono.when(
                      locationService
                          .fetchLocationDeepObjByID(booking.getInvoicePayableAt())
                          .doOnNext(bookingTO::setInvoicePayableAt),
                      locationService
                          .fetchLocationDeepObjByID(booking.getPlaceOfIssueID())
                          .doOnNext(bookingTO::setPlaceOfIssue),
                      fetchCommoditiesByBookingID(booking.getId())
                          .doOnNext(bookingTO::setCommodities),
                      fetchValueAddedServiceRequestsByBookingID(booking.getId())
                          .doOnNext(bookingTO::setValueAddedServiceRequests),
                      fetchReferencesByBookingID(booking.getId())
                          .doOnNext(bookingTO::setReferences),
                      fetchRequestedEquipmentsByBookingID(booking.getId())
                          .doOnNext(bookingTO::setRequestedEquipments),
                      fetchDocumentPartiesByBookingID(booking.getId())
                          .doOnNext(bookingTO::setDocumentParties),
                      fetchShipmentLocationsByBookingID(booking.getId())
                          .doOnNext(bookingTO::setShipmentLocations))
                  .thenReturn(bookingTO);
            });
  }

  private Mono<List<ShipmentCutOffTimeTO>> fetchShipmentCutOffTimeByBookingID(UUID shipmentID) {
    return shipmentCutOffTimeRepository
        .findAllByShipmentID(shipmentID)
        .map(shipmentMapper::shipmentCutOffTimeToDTO)
        .collectList();
  }

  private Mono<List<ValueAddedServiceRequestTO>> fetchValueAddedServiceRequestsByBookingID(
      UUID bookingID) {
    return valueAddedServiceRequestRepository
        .findByBookingID(bookingID)
        .map(
            vasr -> {
              ValueAddedServiceRequestTO vTo = new ValueAddedServiceRequestTO();
              vTo.setValueAddedServiceCode(vasr.getValueAddedServiceCode());
              return vTo;
            })
        .collectList();
  }

  private Mono<List<ReferenceTO>> fetchReferencesByBookingID(UUID bookingID) {
    return referenceRepository
        .findByBookingID(bookingID)
        .map(
            r -> {
              ReferenceTO referenceTO = new ReferenceTO();
              referenceTO.setReferenceType(r.getReferenceType());
              referenceTO.setReferenceValue(r.getReferenceValue());
              return referenceTO;
            })
        .collectList();
  }

  private Mono<List<RequestedEquipmentTO>> fetchRequestedEquipmentsByBookingID(UUID bookingId) {
    return requestedEquipmentRepository
        .findByBookingID(bookingId)
        .map(
            re -> {
              RequestedEquipmentTO requestedEquipmentTO = new RequestedEquipmentTO();
              requestedEquipmentTO.setRequestedEquipmentUnits(re.getRequestedEquipmentUnits());
              requestedEquipmentTO.setRequestedEquipmentSizetype(
                  re.getRequestedEquipmentSizetype());
              requestedEquipmentTO.setShipperOwned(re.getIsShipperOwned());
              return requestedEquipmentTO;
            })
        .collectList();
  }

  private Mono<List<DocumentPartyTO>> fetchDocumentPartiesByBookingID(UUID bookingId) {
    if (bookingId == null) return Mono.empty();
    return documentPartyRepository
        .findByBookingID(bookingId)
        .flatMap(
            dp -> {
              DocumentPartyTO documentPartyTO = new DocumentPartyTO();
              return Mono.when(
                      partyService
                          .findTOById(dp.getPartyID())
                          .doOnNext(documentPartyTO::setParty)
                          .doOnNext(
                              party -> documentPartyTO.setPartyFunction(dp.getPartyFunction()))
                          .doOnNext(
                              party -> documentPartyTO.setIsToBeNotified(dp.getIsToBeNotified())),
                      fetchDisplayAddressByDocumentID(dp.getId())
                          .doOnNext(documentPartyTO::setDisplayedAddress))
                  .thenReturn(documentPartyTO);
            })
        .collectList();
  }

  private Mono<List<String>> fetchDisplayAddressByDocumentID(UUID documentPartyID) {
    return displayedAddressRepository
        .findByDocumentPartyIDOrderByAddressLineNumber(documentPartyID)
        .map(DisplayedAddress::getAddressLine)
        .collectList();
  }

  private Mono<List<ShipmentLocationTO>> fetchShipmentLocationsByBookingID(UUID bookingID) {
    if (bookingID == null) return Mono.empty();
    return shipmentLocationRepository
        .findByBookingID(bookingID)
        .flatMap(
            sl ->
                locationService
                    .fetchLocationDeepObjByID(sl.getLocationID())
                    .flatMap(
                        locationTO -> {
                          ShipmentLocationTO shipmentLocationTO =
                              shipmentMapper.shipmentLocationToDTO(sl);
                          shipmentLocationTO.setLocation(locationTO);
                          return Mono.just(shipmentLocationTO);
                        }))
        .collectList();
  }

  private Mono<List<ConfirmedEquipmentTO>> fetchConfirmedEquipmentByByBookingID(UUID bookingID) {
    return requestedEquipmentRepository
        .findByBookingID(bookingID)
        .map(confirmedEquipmentMapper::requestedEquipmentToDto)
        .collectList();
  }

  private Mono<Tuple2<TransportEvent, TransportEvent>> fetchTransportEventByTransportId(
      UUID transportId) {
    return transportRepository
        .findById(transportId)
        .flatMap(
            x ->
                Mono.zip(
                        transportEventRepository
                            .findFirstByTransportCallIDAndEventTypeCodeAndEventClassifierCodeOrderByEventDateTimeDesc(
                                x.getLoadTransportCallID(),
                                TransportEventTypeCode.ARRI,
                                EventClassifierCode.PLN),
                        transportEventRepository
                            .findFirstByTransportCallIDAndEventTypeCodeAndEventClassifierCodeOrderByEventDateTimeDesc(
                                x.getDischargeTransportCallID(),
                                TransportEventTypeCode.DEPA,
                                EventClassifierCode.PLN))
                    .flatMap(y -> Mono.just(Tuples.of(y.getT1(), y.getT2()))));
  }

  private Mono<Vessel> fetchVesselByTransportCallId(String transportCallId) {

    if (transportCallId == null) return Mono.empty();
    return transportCallRepository
        .findById(transportCallId)
        .flatMap(
            x -> {
              if (x.getVesselID() == null) {
                return Mono.empty();
              }
              return vesselRepository.findById(x.getVesselID());
            });
  }

  private Mono<TransportCall> fetchTransportCallById(String transportCallId) {
    if (transportCallId == null) return Mono.empty();
    return transportCallRepository.findById(transportCallId);
  }

  private Mono<Map<String, String>> fetchImportExportVoyageNumberByTransportCallId(
      TransportCall transportCall) {
    if (transportCall == null) return Mono.empty();
    if (transportCall.getImportVoyageID() == null) return Mono.empty();

    return voyageRepository
        .findById(transportCall.getImportVoyageID())
        .flatMap(
            voyage -> {
              Mono<Voyage> exportVoyage;
              if (!transportCall.getExportVoyageID().equals(transportCall.getImportVoyageID())) {
                exportVoyage = voyageRepository.findById(transportCall.getExportVoyageID());
              } else {
                exportVoyage = Mono.just(voyage);
              }
              return Mono.zip(Mono.just(voyage), exportVoyage);
            })
        .map(
            voyages ->
                Map.of(
                    "importVoyageNumber",
                    voyages.getT1().getCarrierVoyageNumber(),
                    "exportVoyageNumber",
                    voyages.getT2().getCarrierVoyageNumber()));
  }

  private Mono<Vessel> fetchVesselByVesselID(UUID vesselID) {
    if (vesselID == null) return Mono.empty();
    return vesselRepository.findById(vesselID);
  }

  private Mono<ModeOfTransport> fetchModeOfTransportByTransportCallId(String transportCallId) {
    if (transportCallId == null) return Mono.empty();
    return modeOfTransportRepository.findByTransportCallID(transportCallId);
  }

  private Mono<List<TransportTO>> fetchTransports(UUID shipmentId) {
    return shipmentTransportRepository
        .findAllByShipmentID(shipmentId)
        .flatMap(
            shipmentTransport ->
                transportRepository
                    .findAllById(List.of(shipmentTransport.getTransportID()))
                    .flatMap(
                        transport ->
                            Mono.zip(
                                Mono.just(transport),
                                fetchTransportCallById(transport.getLoadTransportCallID())))
                    .flatMap(
                        transportAndTransportCall -> {
                          Transport transport = transportAndTransportCall.getT1();
                          TransportCall transportCall = transportAndTransportCall.getT2();
                          TransportTO transportTO = transportMapper.transportToDTO(transport);
                          transportTO.setIsUnderShippersResponsibility(
                              shipmentTransport.getIsUnderShippersResponsibility());
                          transportTO.setTransportPlanStage(
                              shipmentTransport.getTransportPlanStageCode());
                          transportTO.setTransportPlanStageSequenceNumber(
                              shipmentTransport.getTransportPlanStageSequenceNumber());
                          return Mono.when(
                                  fetchTransportEventByTransportId(transport.getTransportID())
                                      .doOnNext(
                                          t ->
                                              transportTO.setPlannedDepartureDate(
                                                  t.getT1().getEventDateTime()))
                                      .doOnNext(
                                          t ->
                                              transportTO.setPlannedArrivalDate(
                                                  t.getT2().getEventDateTime())),
                                  fetchLocationByTransportCallId(transport.getLoadTransportCallID())
                                      .doOnNext(transportTO::setLoadLocation),
                                  fetchLocationByTransportCallId(
                                          transport.getDischargeTransportCallID())
                                      .doOnNext(transportTO::setDischargeLocation),
                                  fetchModeOfTransportByTransportCallId(
                                          transport.getLoadTransportCallID())
                                      .doOnNext(
                                          modeOfTransport ->
                                              transportTO.setModeOfTransport(
                                                  modeOfTransport.getDcsaTransportType())),
                                  fetchVesselByTransportCallId(transportCall.getTransportCallID())
                                      .doOnNext(
                                          vessel ->
                                              transportTO.setVesselName(vessel.getVesselName()))
                                      .doOnNext(
                                          vessel ->
                                              transportTO.setVesselIMONumber(
                                                  vessel.getVesselIMONumber())),
                                  fetchImportExportVoyageNumberByTransportCallId(transportCall)
                                      .doOnNext(
                                          voyageNumberMap ->
                                              transportTO.setImportVoyageNumber(
                                                  voyageNumberMap.get("importVoyageNumber")))
                                      .doOnNext(
                                          voyageNumberMap ->
                                              transportTO.setExportVoyageNumber(
                                                  voyageNumberMap.get("exportVoyageNumber"))))
                              .thenReturn(transportTO);
                        }))
        .collectList();
  }

  @Override
  @Transactional
  public Mono<BookingResponseTO> cancelBookingByCarrierBookingReference(
      String carrierBookingRequestReference,
      BookingCancellationRequestTO bookingCancellationRequestTO) {
    OffsetDateTime updatedDateTime = OffsetDateTime.now();
    return bookingRepository
        .findByCarrierBookingRequestReference(carrierBookingRequestReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "No Booking found with: ." + carrierBookingRequestReference)))
        .flatMap(checkCancelBookingStatus)
        .flatMap(
            booking ->
                bookingRepository
                    .updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(
                        bookingCancellationRequestTO.getDocumentStatus(),
                        carrierBookingRequestReference,
                        updatedDateTime)
                    .flatMap(verifyCancellation)
                    .thenReturn(booking))
        .map(
            booking -> {
              booking.setDocumentStatus(ShipmentEventTypeCode.CANC);
              return booking;
            })
        .flatMap(
            booking ->
                createShipmentEventFromBookingCancellation(booking, bookingCancellationRequestTO)
                    .thenReturn(booking))
        .map(
            booking -> {
              BookingResponseTO response = new BookingResponseTO();
              response.setBookingRequestCreatedDateTime(booking.getBookingRequestDateTime());
              response.setBookingRequestUpdatedDateTime(updatedDateTime);
              response.setDocumentStatus(booking.getDocumentStatus());
              response.setCarrierBookingRequestReference(
                  booking.getCarrierBookingRequestReference());
              return response;
            });
  }

  private Mono<ShipmentEvent> createShipmentEventFromBookingCancellation(
      Booking booking, BookingCancellationRequestTO bookingCancellationRequestTO) {
    return shipmentEventFromBooking(booking, bookingCancellationRequestTO.getReason())
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Failed to create shipment event for Booking.")));
  }

  private Mono<ShipmentEvent> createShipmentEventFromBookingTO(BookingTO bookingTo) {
    return createShipmentEvent(shipmentEventFromBooking(bookingMapper.dtoToBooking(bookingTo)));
  }

  private Mono<ShipmentEvent> createShipmentEvent(Mono<ShipmentEvent> shipmentEventMono) {
    return shipmentEventMono
        .flatMap(shipmentEventService::create)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Failed to create shipment event for Booking.")));
  }

  private Mono<ShipmentEvent> shipmentEventFromBooking(Booking booking) {
    return shipmentEventFromBooking(booking, null);
  }

  private Mono<ShipmentEvent> shipmentEventFromBooking(Booking booking, String reason) {
    ShipmentEvent shipmentEvent = new ShipmentEvent();
    shipmentEvent.setShipmentEventTypeCode(
        ShipmentEventTypeCode.valueOf(booking.getDocumentStatus().name()));
    shipmentEvent.setDocumentTypeCode(DocumentTypeCode.CBR);
    shipmentEvent.setEventClassifierCode(EventClassifierCode.ACT);
    shipmentEvent.setEventType(null);
    shipmentEvent.setDocumentID(booking.getId());
    shipmentEvent.setDocumentReference(booking.getCarrierBookingRequestReference());
    shipmentEvent.setEventDateTime(booking.getUpdatedDateTime());
    shipmentEvent.setEventCreatedDateTime(OffsetDateTime.now());
    shipmentEvent.setReason(reason);
    return Mono.just(shipmentEvent);
  }

  private String validateBookingRequest(BookingTO bookingRequest) {
    if (bookingRequest.getIsImportLicenseRequired()
        && bookingRequest.getImportLicenseReference() == null) {
      return "The attribute importLicenseReference cannot be null if isImportLicenseRequired is true.";
    }

    if (bookingRequest.getIsExportDeclarationRequired()
        && bookingRequest.getExportDeclarationReference() == null) {
      return "The attribute exportDeclarationReference cannot be null if isExportDeclarationRequired is true.";
    }

    if (bookingRequest.getExpectedArrivalDateStart() == null
        && bookingRequest.getExpectedArrivalDateEnd() == null
        && bookingRequest.getExpectedDepartureDate() == null
        && bookingRequest.getVesselIMONumber() == null
        && bookingRequest.getExportVoyageNumber() == null) {
      return "The attributes expectedArrivalDateStart, expectedArrivalDateEnd, expectedDepartureDate and vesselIMONumber/exportVoyageNumber cannot all be null at the same time. These fields are conditional and require that at least one of them is not empty.";
    }

    if (bookingRequest.getExpectedArrivalDateStart() != null
        && bookingRequest.getExpectedArrivalDateEnd() != null
        && bookingRequest
            .getExpectedArrivalDateStart()
            .isAfter(bookingRequest.getExpectedArrivalDateEnd())) {
      return "The attribute expectedArrivalDateEnd must be the same or after expectedArrivalDateStart.";
    }
    return StringUtils.EMPTY;
  }

  private final Function<Boolean, Mono<? extends Boolean>> verifyCancellation =
      isRecordUpdated -> {
        if (isRecordUpdated) {
          return Mono.just(true);
        } else {
          return Mono.error(new UpdateException("Cancellation of booking failed."));
        }
      };

  private final Function<Booking, Mono<Booking>> checkCancelBookingStatus =
      booking -> {
        EnumSet<ShipmentEventTypeCode> allowedDocumentStatuses =
            EnumSet.of(
                ShipmentEventTypeCode.RECE,
                ShipmentEventTypeCode.PENU,
                ShipmentEventTypeCode.CONF,
                ShipmentEventTypeCode.PENC);
        if (allowedDocumentStatuses.contains(booking.getDocumentStatus())) {
          return Mono.just(booking);
        }
        return Mono.error(
            ConcreteRequestErrorMessageException.invalidParameter(
                "Cannot Cancel Booking that is not in status RECE, PENU, CONF or PENC"));
      };

  private final Function<Booking, Mono<Booking>> checkUpdateBookingStatus =
      booking -> {
        EnumSet<ShipmentEventTypeCode> allowedDocumentStatuses =
            EnumSet.of(ShipmentEventTypeCode.RECE, ShipmentEventTypeCode.PENU);
        if (allowedDocumentStatuses.contains(booking.getDocumentStatus())) {
          return Mono.just(booking);
        }
        return Mono.error(
          ConcreteRequestErrorMessageException.invalidParameter("Cannot Update Booking that is not in status RECE or PENU"));
      };
}
