package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dcsa.bkg.model.transferobjects.BookingCancellationRequestTO;
import org.dcsa.bkg.service.BKGService;
import org.dcsa.core.events.edocumentation.model.mapper.*;
import org.dcsa.core.events.edocumentation.model.transferobject.*;
import org.dcsa.core.events.edocumentation.repository.RequestedEquipmentRepository;
import org.dcsa.core.events.edocumentation.repository.ShipmentCutOffTimeRepository;
import org.dcsa.core.events.edocumentation.service.CarrierClauseService;
import org.dcsa.core.events.edocumentation.service.ChargeService;
import org.dcsa.core.events.edocumentation.service.TransportService;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.model.mapper.RequestedEquipmentMapper;
import org.dcsa.core.events.repository.*;
import org.dcsa.core.events.service.DocumentPartyService;
import org.dcsa.core.events.service.ReferenceService;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.events.edocumentation.service.ShipmentLocationService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.model.Vessel;
import org.dcsa.skernel.repositority.VesselRepository;
import org.dcsa.skernel.service.LocationService;
import org.dcsa.skernel.service.VesselService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BKGServiceImpl implements BKGService {

  // repositories
  private final BookingRepository bookingRepository;
  private final CommodityRepository commodityRepository;
  private final ValueAddedServiceRequestRepository valueAddedServiceRequestRepository;
  private final RequestedEquipmentRepository requestedEquipmentRepository;
  private final ShipmentRepository shipmentRepository;
  private final VesselRepository vesselRepository;
  private final ShipmentCutOffTimeRepository shipmentCutOffTimeRepository;
  private final RequestedEquipmentEquipmentRepository requestedEquipmentEquipmentRepository;
  private final VoyageRepository voyageRepository;

  // mappers
  private final BookingMapper bookingMapper;
  private final CommodityMapper commodityMapper;
  private final ShipmentMapper shipmentMapper;
  private final ConfirmedEquipmentMapper confirmedEquipmentMapper;
  private final RequestedEquipmentMapper requestedEquipmentMapper;
  private final ShipmentEventMapper shipmentEventMapper;

  // services
  private final DocumentPartyService documentPartyService;
  private final ShipmentLocationService shipmentLocationService;
  private final ShipmentEventService shipmentEventService;
  private final LocationService locationService;
  private final ReferenceService referenceService;
  private final ChargeService chargeService;
  private final VesselService vesselService;
  private final CarrierClauseService carrierClauseService;
  private final TransportService transportService;

  @Override
  @Transactional
  public Mono<BookingResponseTO> createBooking(final BookingTO bookingRequest) {

    String bookingRequestError = validateBookingRequest(bookingRequest);
    if (!bookingRequestError.isEmpty()) {
      return Mono.error(ConcreteRequestErrorMessageException.invalidInput(bookingRequestError));
    }

    OffsetDateTime now = OffsetDateTime.now();
    Booking requestedBooking = bookingMapper.dtoToBooking(bookingRequest);
    // CarrierBookingRequestReference is not allowed to be set by request
    requestedBooking.setCarrierBookingRequestReference(null);
    requestedBooking.setDocumentStatus(ShipmentEventTypeCode.RECE);
    requestedBooking.setBookingRequestDateTime(now);
    requestedBooking.setUpdatedDateTime(now);

    Mono<Voyage> voyageMono = Mono.empty();
    if (bookingRequest.getExportVoyageNumber() != null) {
      // Since carrierVoyageNumber is not unique in Voyage and booking does not supply a service to make it
      // unique we just take the first Voyage found.
      voyageMono = voyageRepository.findByCarrierVoyageNumber(bookingRequest.getExportVoyageNumber())
        .switchIfEmpty(Mono.error(ConcreteRequestErrorMessageException.invalidInput("No voyage found with exportVoyageNumber " + bookingRequest.getExportVoyageNumber())))
        .next().doOnNext(voyage -> {
          requestedBooking.setVoyageID(voyage.getId());
        });
    }

    return voyageMono.then(
      bookingRepository
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
            booking ->
                createDeepObjectsForBooking(bookingRequest, booking)
                    .flatMap(
                        bookingTO ->
                            this.createShipmentEvent(
                                    shipmentEventMapper.shipmentEventFromBookingTO(
                                        bookingTO, booking.getId(), null))
                                .thenReturn(bookingTO)))
        .map(bookingMapper::dtoToBookingResponseTO)
    );
  }

  private Mono<BookingTO> createDeepObjectsForBooking(BookingTO bookingRequest, Booking booking) {
    UUID bookingID = booking.getId();
    if (bookingID == null)
      return Mono.error(
          ConcreteRequestErrorMessageException.internalServerError("BookingID is null"));
    BookingTO bookingTO = bookingToDTOWithNullLocations(booking);
    return Mono.when(
            findVesselAndUpdateBooking(
                    bookingRequest.getVesselName(), bookingRequest.getVesselIMONumber(), bookingID)
                .doOnNext(
                    v -> {
                      bookingTO.setVesselName(v.getVesselName());
                      bookingTO.setVesselIMONumber(v.getVesselIMONumber());
                    }),
            Mono.justOrEmpty(bookingRequest.getInvoicePayableAt())
                .flatMap(locationService::ensureResolvable)
                .flatMap(
                    lTO ->
                        bookingRepository
                            .setInvoicePayableAtFor(lTO.getId(), bookingID)
                            .thenReturn(lTO))
                .doOnNext(bookingTO::setInvoicePayableAt),
            Mono.justOrEmpty(bookingRequest.getPlaceOfIssue())
                .flatMap(locationService::ensureResolvable)
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
            referenceService
                .createReferencesByBookingIDAndTOs(bookingID, bookingRequest.getReferences())
                .doOnNext(bookingTO::setReferences),
            createRequestedEquipmentsByBookingIDAndTOs(
                    bookingID, bookingRequest.getRequestedEquipments())
                .doOnNext(bookingTO::setRequestedEquipments),
            documentPartyService
                .createDocumentPartiesByBookingID(bookingID, bookingRequest.getDocumentParties())
                .doOnNext(bookingTO::setDocumentParties),
            shipmentLocationService
                .createShipmentLocationsByBookingIDAndTOs(
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
                      ConcreteRequestErrorMessageException.invalidInput(
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
                      ConcreteRequestErrorMessageException.invalidInput(
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

  private Mono<List<ValueAddedServiceRequestTO>> createValueAddedServiceRequestsByBookingIDAndTOs(
      UUID bookingID, List<ValueAddedServiceRequestTO> valueAddedServiceRequests) {

    if (Objects.isNull(valueAddedServiceRequests) || valueAddedServiceRequests.isEmpty()) {
      return Mono.empty();
    }

    return Flux.fromIterable(valueAddedServiceRequests)
        .map(
            valueAddedServiceRequestTO -> {
              ValueAddedServiceRequest valueAddedServiceRequest = new ValueAddedServiceRequest();
              valueAddedServiceRequest.setBookingID(bookingID);
              valueAddedServiceRequest.setValueAddedServiceCode(
                  valueAddedServiceRequestTO.getValueAddedServiceCode());
              return valueAddedServiceRequest;
            })
        .concatMap(valueAddedServiceRequestRepository::save)
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

  @Override
  @Transactional
  public Mono<BookingResponseTO> updateBookingByReferenceCarrierBookingRequestReference(
      String carrierBookingRequestReference, BookingTO bookingRequest) {

    String bookingRequestError = validateBookingRequest(bookingRequest);
    if (!bookingRequestError.isEmpty()) {
      return Mono.error(ConcreteRequestErrorMessageException.invalidInput(bookingRequestError));
    }

    return getActiveBooking(carrierBookingRequestReference)
        .map(Booking::checkCancelBookingStatus)
        .flatMap(
            booking -> {
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
              booking.setCarrierBookingRequestReference(b.getCarrierBookingRequestReference());
              return bookingRepository.save(booking);
            })
        .flatMap(
            booking ->
                // resolve entities linked to booking
                createDeepObjectsForBooking(bookingRequest, booking)
                    .flatMap(
                        bookingTO ->
                            this.createShipmentEvent(
                                    shipmentEventMapper.shipmentEventFromBookingTO(
                                        bookingTO, booking.getId(), null))
                                .thenReturn(bookingTO)))
        .switchIfEmpty(
            Mono.defer(
                () ->
                    Mono.error(
                        ConcreteRequestErrorMessageException.notFound(
                            "No booking found for given carrierBookingRequestReference."))))
        .flatMap(bTO -> Mono.just(bookingMapper.dtoToBookingResponseTO(bTO)));
  }

  @Override
  public Mono<BookingTO> getBookingByCarrierBookingRequestReference(
      String carrierBookingRequestReference) {
    return getActiveBooking(carrierBookingRequestReference)
        .flatMap(
            booking -> {
              BookingTO bookingTO = bookingMapper.bookingToDTO(booking);
              return getBookingDeepObjects(bookingTO, booking);
            });
  }

  private Mono<Booking> getActiveBooking(String carrierBookingRequestReference) {
    return bookingRepository
        .findByCarrierBookingRequestReferenceAndValidUntilIsNull(carrierBookingRequestReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No booking found with carrier booking request reference: "
                        + carrierBookingRequestReference)))
        .filter(booking -> Objects.isNull(booking.getValidUntil()))
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "All bookings are inactive, at least one active booking should be present.")));
  }

  private Mono<BookingTO> getBookingDeepObjects(BookingTO bookingTO, Booking booking) {
    return Mono.when(
            locationService
                .fetchLocationDeepObjByID(booking.getInvoicePayableAt())
                .doOnNext(bookingTO::setInvoicePayableAt),
            locationService
                .fetchLocationDeepObjByID(booking.getPlaceOfIssueID())
                .doOnNext(bookingTO::setPlaceOfIssue),
            Mono.justOrEmpty(booking.getVesselId())
                .flatMap(vesselService::findById)
                .doOnNext(vessel -> bookingTO.setVesselName(vessel.getVesselName()))
                .doOnNext(vessel -> bookingTO.setVesselIMONumber(vessel.getVesselIMONumber())),
            fetchCommoditiesByBookingID(booking.getId()).doOnNext(bookingTO::setCommodities),
            fetchValueAddedServiceRequestsByBookingID(booking.getId())
                .doOnNext(bookingTO::setValueAddedServiceRequests),
            referenceService.findByBookingID(booking.getId()).doOnNext(bookingTO::setReferences),
            fetchRequestedEquipmentsByBookingID(booking.getId())
                .doOnNext(bookingTO::setRequestedEquipments),
            documentPartyService
                .fetchDocumentPartiesByBookingID(booking.getId())
                .doOnNext(bookingTO::setDocumentParties),
            shipmentLocationService
                .fetchShipmentLocationsByBookingID(booking.getId())
                .doOnNext(bookingTO::setShipmentLocations),
            Mono.justOrEmpty(booking.getVoyageID())
                .flatMap(voyageRepository::findById)
                .doOnNext(voyage -> bookingTO.setExportVoyageNumber(voyage.getCarrierVoyageNumber()))
      )
        .thenReturn(bookingTO);
  }

  @Override
  public Mono<ShipmentTO> getShipmentByCarrierBookingReference(
      String carrierBookingRequestReference) {

    return shipmentRepository
        .findByCarrierBookingReferenceAndValidUntilIsNull(carrierBookingRequestReference)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "No shipment found with carrier booking reference: "
                        + carrierBookingRequestReference)))
        .filter(shipment -> Objects.isNull(shipment.getValidUntil()))
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.internalServerError(
                    "All shipments are inactive, at least one active shipment should be present.")))
        .flatMap(
            shipment -> {
              ShipmentTO shipmentTO = shipmentMapper.shipmentToDTO(shipment);
              return Mono.when(
                      fetchShipmentCutOffTimeByBookingID(shipment.getBookingID())
                          .doOnNext(shipmentTO::setShipmentCutOffTimes),
                      shipmentLocationService
                          .fetchShipmentLocationsByBookingID(shipment.getBookingID())
                          .doOnNext(shipmentTO::setShipmentLocations),
                      carrierClauseService
                          .fetchCarrierClausesByShipmentID(shipment.getShipmentID())
                          .collectList()
                          .doOnNext(shipmentTO::setCarrierClauses),
                      fetchConfirmedEquipmentByByBookingID(shipment.getBookingID())
                          .doOnNext(shipmentTO::setConfirmedEquipments),
                      chargeService
                          .fetchChargesByShipmentID(shipment.getShipmentID())
                          .collectList()
                          .doOnNext(shipmentTO::setCharges),
                      fetchBookingByBookingID(shipment.getBookingID())
                          .doOnNext(shipmentTO::setBooking),
                      transportService
                          .findByShipmentID(shipment.getShipmentID())
                          .collectList()
                          .doOnNext(shipmentTO::setTransports))
                  .thenReturn(shipmentTO);
            });
  }

  private Mono<List<CommodityTO>> fetchCommoditiesByBookingID(UUID bookingID) {
    return commodityRepository
        .findByBookingID(bookingID)
        .map(commodityMapper::commodityToDTO)
        .collectList();
  }

  private Mono<BookingTO> fetchBookingByBookingID(UUID bookingID) {
    if (bookingID == null) return Mono.empty();
    return bookingRepository
        .findById(bookingID)
        .flatMap(
            booking -> {
              BookingTO bookingTO = bookingMapper.bookingToDTO(booking);
              return getBookingDeepObjects(bookingTO, booking);
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

  private Mono<List<RequestedEquipmentTO>> fetchRequestedEquipmentsByBookingID(UUID bookingId) {
    return requestedEquipmentRepository
        .findByBookingID(bookingId)
        .map(
            re -> {
              RequestedEquipmentTO requestedEquipmentTO = new RequestedEquipmentTO();
              requestedEquipmentTO.setRequestedEquipmentUnits(re.getRequestedEquipmentUnits());
              requestedEquipmentTO.setRequestedEquipmentSizeType(re.getRequestedEquipmentSizeType());
              requestedEquipmentTO.setShipperOwned(re.getIsShipperOwned());
              return requestedEquipmentTO;
            })
        .collectList();
  }

  private Mono<List<ConfirmedEquipmentTO>> fetchConfirmedEquipmentByByBookingID(UUID bookingID) {
    return requestedEquipmentRepository
        .findByBookingID(bookingID)
        .map(confirmedEquipmentMapper::requestedEquipmentToDto)
        .collectList();
  }

  @Override
  @Transactional
  public Mono<BookingResponseTO> cancelBookingByCarrierBookingReference(
      String carrierBookingRequestReference,
      BookingCancellationRequestTO bookingCancellationRequestTO) {
    OffsetDateTime updatedDateTime = OffsetDateTime.now();
    return getActiveBooking(carrierBookingRequestReference)
        .map(Booking::checkCancelBookingStatus)
        .flatMap(
            booking ->
                performBookingCancellation(
                    bookingCancellationRequestTO.getDocumentStatus(), updatedDateTime, booking))
        .doOnNext(booking -> booking.setDocumentStatus(ShipmentEventTypeCode.CANC))
        .flatMap(
            booking ->
                this.createShipmentEvent(
                        shipmentEventMapper.shipmentEventFromBooking(
                            booking, bookingCancellationRequestTO.getReason()))
                    .thenReturn(booking))
        .map(bookingMapper::bookingToBookingResponseTO)
        .doOnNext(
            bookingResponseTO ->
                bookingResponseTO.setBookingRequestUpdatedDateTime(updatedDateTime));
  }

  private Mono<Booking> performBookingCancellation(
      ShipmentEventTypeCode documentStatus, OffsetDateTime updatedDateTime, Booking booking) {
    return bookingRepository
        .updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(
            documentStatus, booking.getCarrierBookingRequestReference(), updatedDateTime)
        .filter(Boolean::booleanValue) // equals true if update succeeded
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidInput(
                    "Cancellation of booking failed.")))
        .thenReturn(booking);
  }

  private Mono<ShipmentEvent> createShipmentEvent(ShipmentEvent shipmentEvent) {
    return shipmentEventService
        .create(shipmentEvent)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Failed to create shipment event for Booking.")));
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

    if (bookingRequest.getExpectedArrivalAtPlaceOfDeliveryStartDate() == null
        && bookingRequest.getExpectedArrivalAtPlaceOfDeliveryEndDate() == null
        && bookingRequest.getExpectedDepartureDate() == null
        && bookingRequest.getVesselIMONumber() == null
        && bookingRequest.getExportVoyageNumber() == null) {
      return "The attributes expectedArrivalAtPlaceOfDeliveryStartDate, expectedArrivalAtPlaceOfDeliveryEndDate, expectedDepartureDate and vesselIMONumber/exportVoyageNumber cannot all be null at the same time. These fields are conditional and require that at least one of them is not empty.";
    }

    if (bookingRequest.getExpectedArrivalAtPlaceOfDeliveryStartDate() != null
        && bookingRequest.getExpectedArrivalAtPlaceOfDeliveryEndDate() != null
        && bookingRequest
            .getExpectedArrivalAtPlaceOfDeliveryStartDate()
            .isAfter(bookingRequest.getExpectedArrivalAtPlaceOfDeliveryEndDate())) {
      return "The attribute expectedArrivalAtPlaceOfDeliveryEndDate must be the same or after expectedArrivalAtPlaceOfDeliveryStartDate.";
    }
    return StringUtils.EMPTY;
  }
}
