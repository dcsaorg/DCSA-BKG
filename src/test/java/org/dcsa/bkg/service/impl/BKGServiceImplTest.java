package org.dcsa.bkg.service.impl;

import org.dcsa.bkg.model.mappers.BookingSummaryMapper;
import org.dcsa.bkg.model.transferobjects.BookingCancellationRequestTO;
import org.dcsa.core.events.edocumentation.model.mapper.*;
import org.dcsa.core.events.edocumentation.model.transferobject.*;
import org.dcsa.core.events.edocumentation.repository.RequestedEquipmentRepository;
import org.dcsa.core.events.edocumentation.repository.ShipmentCutOffTimeRepository;
import org.dcsa.core.events.edocumentation.repository.ShipmentLocationRepository;
import org.dcsa.core.events.edocumentation.service.CarrierClauseService;
import org.dcsa.core.events.edocumentation.service.ChargeService;
import org.dcsa.core.events.edocumentation.service.ShipmentLocationService;
import org.dcsa.core.events.edocumentation.service.TransportService;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.mapper.RequestedEquipmentMapper;
import org.dcsa.core.events.model.transferobjects.DocumentPartyTO;
import org.dcsa.core.events.model.transferobjects.ReferenceTO;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.CommodityRepository;
import org.dcsa.core.events.repository.ShipmentRepository;
import org.dcsa.core.events.repository.ValueAddedServiceRequestRepository;
import org.dcsa.core.events.repository.VoyageRepository;
import org.dcsa.core.events.service.DocumentPartyService;
import org.dcsa.core.events.service.ReferenceService;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.exception.NotFoundException;
import org.dcsa.skernel.model.*;
import org.dcsa.skernel.model.enums.DCSAResponsibleAgencyCode;
import org.dcsa.skernel.model.enums.FacilityCodeListProvider;
import org.dcsa.skernel.model.enums.PartyFunction;
import org.dcsa.skernel.model.mapper.LocationMapper;
import org.dcsa.skernel.model.mapper.PartyContactDetailsMapper;
import org.dcsa.skernel.model.mapper.PartyMapper;
import org.dcsa.skernel.model.transferobjects.LocationTO;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.dcsa.skernel.model.transferobjects.PartyTO;
import org.dcsa.skernel.repositority.LocationRepository;
import org.dcsa.skernel.repositority.VesselRepository;
import org.dcsa.skernel.service.LocationService;
import org.dcsa.skernel.service.VesselService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for BookingService Implementation.")
class BKGServiceImplTest {

  @Mock BookingRepository bookingRepository;
  @Mock ShipmentRepository shipmentRepository;
  @Mock LocationRepository locationRepository;
  @Mock CommodityRepository commodityRepository;
  @Mock ValueAddedServiceRequestRepository valueAddedServiceRequestRepository;
  @Mock RequestedEquipmentRepository requestedEquipmentRepository;
  @Mock ShipmentLocationRepository shipmentLocationRepository;
  @Mock ShipmentCutOffTimeRepository shipmentCutOffTimeRepository;
  @Mock VesselRepository vesselRepository;
  @Mock VoyageRepository voyageRepository;

  @Mock DocumentPartyService documentPartyService;
  @Mock ShipmentEventService shipmentEventService;
  @Mock LocationService locationService;
  @Mock ReferenceService referenceService;
  @Mock ChargeService chargeService;
  @Mock CarrierClauseService carrierClauseService;
  @Mock TransportService transportService;
  @Mock ShipmentLocationService shipmentLocationService;

  @Mock VesselService vesselService;

  @InjectMocks BKGServiceImpl bkgServiceImpl;

  @Spy BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);
  @Spy LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);
  @Spy CommodityMapper commodityMapper = Mappers.getMapper(CommodityMapper.class);
  @Spy PartyMapper partyMapper = Mappers.getMapper(PartyMapper.class);
  @Spy ShipmentMapper shipmentMapper = Mappers.getMapper(ShipmentMapper.class);
  @Spy BookingSummaryMapper bookingSummaryMapping = Mappers.getMapper(BookingSummaryMapper.class);
  @Spy CarrierClauseMapper carrierClauseMapper = Mappers.getMapper(CarrierClauseMapper.class);
  @Spy ShipmentEventMapper shipmentEventMapper = Mappers.getMapper(ShipmentEventMapper.class);

  @Spy
  ShipmentLocationMapper shipmentLocationMapper = Mappers.getMapper(ShipmentLocationMapper.class);

  @Spy
  RequestedEquipmentMapper requestedEquipmentMapper =
      Mappers.getMapper(RequestedEquipmentMapper.class);

  @Spy
  ConfirmedEquipmentMapper confirmedEquipmentMapper =
      Mappers.getMapper(ConfirmedEquipmentMapper.class);

  @Spy ChargeMapper chargeMapper = Mappers.getMapper(ChargeMapper.class);

  @Spy
  PartyContactDetailsMapper partyContactDetailsMapper =
      Mappers.getMapper(PartyContactDetailsMapper.class);

  Booking booking;
  Location location1;
  Location location2;
  Address address;
  Facility facility;
  Commodity commodity;
  ValueAddedServiceRequest valueAddedServiceRequest;
  Reference reference;
  RequestedEquipment requestedEquipment;
  RequestedEquipment confirmedEquipment;
  DocumentParty documentParty;
  Party party;
  PartyIdentifyingCode partyIdentifyingCode;
  DisplayedAddress displayedAddress;
  PartyContactDetails partyContactDetails;
  ShipmentLocation shipmentLocation;
  Shipment shipment;
  Carrier carrier;
  ShipmentCutOffTime shipmentCutOffTime;
  ShipmentCarrierClause shipmentCarrierClause;
  CarrierClause carrierClause;
  Charge charge;
  ModeOfTransport modeOfTransport;
  TransportCall dischargeTransportCall;
  TransportCall loadTransportCall;
  Voyage voyage;
  Transport transport;
  ShipmentTransport shipmentTransport;
  Vessel vessel;
  TransportEvent departureTransportEvent;
  TransportEvent arrivalTransportEvent;
  BookingCancellationRequestTO bookingCancellationRequestTO;

  @BeforeEach
  void init() {
    booking = new Booking();
    booking.setId(UUID.randomUUID());
    booking.setCarrierBookingRequestReference("ef223019-ff16-4870-be69-9dbaaaae9b11");
    booking.setInvoicePayableAt(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6"));
    booking.setPlaceOfIssueID(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"));
    booking.setDocumentStatus(ShipmentEventTypeCode.RECE);

    location1 = new Location();
    location1.setId(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6"));
    location1.setLocationName("Hamburg");
    location1.setAddressID(UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"));
    location1.setFacilityID(UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"));

    location2 = new Location();
    location2.setId(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"));
    location2.setLocationName("Singapore");
    location2.setAddressID(UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"));
    location2.setFacilityID(UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"));

    address = new Address();
    address.setId(UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"));
    address.setName("Fraz");
    address.setStreet("Kronprincessegade");
    address.setPostalCode("1306");
    address.setCity("København");
    address.setCountry("Denmark");

    facility = new Facility();
    facility.setFacilityID(UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"));
    facility.setFacilityName("DP WORLD JEBEL ALI - CT1");
    facility.setUnLocationCode("AEJEA");
    facility.setFacilitySMDGCode("DPWJA");

    vessel = new Vessel();
    vessel.setId(UUID.fromString("8aa766f3-73ff-4173-8a5e-c13079dcbffb"));
    vessel.setVesselName("Rum Runner");
    vessel.setVesselIMONumber("9321483");
    vessel.setVesselOperatorCarrierID(UUID.randomUUID());
    vessel.setVesselFlag("FU");

    commodity = new Commodity();
    commodity.setCommodityType("Mobile phones");
    commodity.setCargoGrossWeight(12000.00);
    commodity.setHsCode("720711");
    commodity.setCargoGrossWeightUnit(WeightUnit.KGM);
    commodity.setExportLicenseIssueDate(LocalDate.now());
    commodity.setExportLicenseExpiryDate(LocalDate.now());

    valueAddedServiceRequest = new ValueAddedServiceRequest();
    valueAddedServiceRequest.setValueAddedServiceCode(ValueAddedServiceCode.CDECL);

    reference = new Reference();
    reference.setReferenceValue("test");
    reference.setReferenceType(ReferenceTypeCode.FF);

    requestedEquipment = new RequestedEquipment();
    requestedEquipment.setRequestedEquipmentSizeType("22GP");
    requestedEquipment.setRequestedEquipmentUnits(3);

    confirmedEquipment = new RequestedEquipment();
    confirmedEquipment.setConfirmedEquipmentSizetype("22GP");
    confirmedEquipment.setConfirmedEquipmentUnits(2);

    documentParty = new DocumentParty();
    documentParty.setId(UUID.fromString("3d9542f8-c362-4fa5-8902-90e30d87f1d4"));
    documentParty.setPartyID(UUID.fromString("d04fb8c6-eb9c-474d-9cf7-86aa6bfcc2a2"));
    documentParty.setBookingID(booking.getId());
    documentParty.setPartyFunction(PartyFunction.DDS);

    party = new Party();
    party.setId(UUID.fromString("a680fe72-503e-40b3-9cfc-dcadafdecf15"));
    party.setPartyName("DCSA");
    party.setAddressID(address.getId());

    partyIdentifyingCode = new PartyIdentifyingCode();
    partyIdentifyingCode.setPartyID(party.getId());
    partyIdentifyingCode.setCodeListName("LCL");
    partyIdentifyingCode.setDcsaResponsibleAgencyCode(DCSAResponsibleAgencyCode.ISO);
    partyIdentifyingCode.setPartyCode("MSK");

    displayedAddress = new DisplayedAddress();
    displayedAddress.setDocumentPartyID(documentParty.getId());
    displayedAddress.setAddressLine("Javastraat");
    displayedAddress.setAddressLineNumber(1);

    partyContactDetails = new PartyContactDetails();
    partyContactDetails.setName("Peanut");
    partyContactDetails.setEmail("peanut@jeff-fa-fa.com");

    carrier = new Carrier();
    carrier.setId(UUID.randomUUID());
    carrier.setCarrierName("Ocean Network Express Pte. Ltd.");
    carrier.setSmdgCode("TWO");
    carrier.setNmftaCode("THREE");

    shipment = new Shipment();
    shipment.setShipmentID(UUID.randomUUID());
    shipment.setBookingID(booking.getId());
    shipment.setCarrierID(carrier.getId());
    shipment.setCarrierBookingReference(UUID.randomUUID().toString());
    shipment.setTermsAndConditions("Terms and conditions etc...");
    shipment.setConfirmationDateTime(OffsetDateTime.now());

    shipmentLocation = new ShipmentLocation();
    shipmentLocation.setShipmentID(shipment.getShipmentID());
    shipmentLocation.setLocationID(location1.getId());
    shipmentLocation.setBookingID(booking.getId());
    shipmentLocation.setShipmentLocationTypeCode(LocationType.FCD);
    shipmentLocation.setDisplayedName("Singapore");

    shipmentCutOffTime = new ShipmentCutOffTime();
    shipmentCutOffTime.setShipmentID(shipment.getShipmentID());
    shipmentCutOffTime.setCutOffDateTimeCode(CutOffDateTimeCode.AFD);
    shipmentCutOffTime.setCutOffDateTime(OffsetDateTime.now());

    carrierClause = new CarrierClause();
    carrierClause.setId(UUID.randomUUID());
    carrierClause.setClauseContent("x".repeat(150));

    shipmentCarrierClause = new ShipmentCarrierClause();
    shipmentCarrierClause.setCarrierClauseID(carrierClause.getId());
    shipmentCarrierClause.setShipmentID(shipment.getShipmentID());

    charge = new Charge();
    charge.setChargeType("x".repeat(20));
    charge.setId(UUID.randomUUID().toString());
    charge.setShipmentID(shipment.getShipmentID());
    charge.setCalculationBasis("WHAT");
    charge.setCurrencyAmount(12.12);
    charge.setCurrencyCode("x".repeat(20));
    charge.setPaymentTermCode(PaymentTerm.PRE);
    charge.setQuantity(123d);
    charge.setUnitPrice(12.12d);

    modeOfTransport = new ModeOfTransport();
    modeOfTransport.setId("1");
    modeOfTransport.setDescription("Transport of goods and/or persons is by sea.");
    modeOfTransport.setName("Maritime transport");
    modeOfTransport.setDcsaTransportType(DCSATransportType.VESSEL);

    voyage = new Voyage();
    voyage.setId(UUID.randomUUID());
    voyage.setCarrierVoyageNumber("CarrierVoyageNumber");

    loadTransportCall = new TransportCall();
    loadTransportCall.setTransportCallID(UUID.randomUUID());
    loadTransportCall.setFacilityID(facility.getFacilityID());
    loadTransportCall.setLocationID(location1.getId());
    loadTransportCall.setModeOfTransportID(modeOfTransport.getId());
    loadTransportCall.setVesselID(vessel.getId());
    loadTransportCall.setImportVoyageID(voyage.getId());
    loadTransportCall.setExportVoyageID(voyage.getId());

    dischargeTransportCall = new TransportCall();
    dischargeTransportCall.setTransportCallID(UUID.randomUUID());
    dischargeTransportCall.setFacilityID(facility.getFacilityID());
    dischargeTransportCall.setLocationID(location2.getId());
    dischargeTransportCall.setModeOfTransportID(modeOfTransport.getId());
    dischargeTransportCall.setVesselID(vessel.getId());
    dischargeTransportCall.setImportVoyageID(voyage.getId());
    dischargeTransportCall.setExportVoyageID(voyage.getId());

    transport = new Transport();
    transport.setTransportID(UUID.randomUUID());
    transport.setLoadTransportCallID(loadTransportCall.getTransportCallID());
    transport.setDischargeTransportCallID(dischargeTransportCall.getTransportCallID());
    transport.setTransportReference("DUNNO");
    transport.setTransportName("STILL DUNNO");

    shipmentTransport = new ShipmentTransport();
    shipmentTransport.setId(UUID.randomUUID());
    shipmentTransport.setShipmentID(shipment.getShipmentID());
    shipmentTransport.setTransportID(transport.getTransportID());
    shipmentTransport.setTransportPlanStageSequenceNumber(432);
    shipmentTransport.setTransportPlanStageCode(TransportPlanStageCode.ONC);
    shipmentTransport.setIsUnderShippersResponsibility(false);

    // Departure
    departureTransportEvent = new TransportEvent();
    departureTransportEvent.setTransportCallID(loadTransportCall.getTransportCallID());
    departureTransportEvent.setEventDateTime(OffsetDateTime.now().minusHours(1));
    departureTransportEvent.setEventCreatedDateTime(OffsetDateTime.now().minusHours(2));

    // Arrival
    arrivalTransportEvent = new TransportEvent();
    arrivalTransportEvent.setTransportCallID(dischargeTransportCall.getTransportCallID());
    arrivalTransportEvent.setEventDateTime(OffsetDateTime.now());
    arrivalTransportEvent.setEventCreatedDateTime(OffsetDateTime.now().plusHours(1));

    bookingCancellationRequestTO = new BookingCancellationRequestTO();
    bookingCancellationRequestTO.setReason("Booking Cancelled");
    bookingCancellationRequestTO.setDocumentStatus(ShipmentEventTypeCode.CANC);

    // Date & Time
    OffsetDateTime now = OffsetDateTime.now();
    booking.setBookingRequestDateTime(now);
    booking.setUpdatedDateTime(now);
  }

  @Nested
  @DisplayName("Tests for the method createBooking(#BookingTO)")
  class CreateBookingTest {

    BookingTO bookingTO;
    BookingResponseTO bookingResponseTO;
    LocationTO invoicePayableAt;
    LocationTO placeOfIssue;
    CommodityTO commodityTO;
    ReferenceTO referenceTO;
    ValueAddedServiceRequestTO valueAddedServiceRequestTO;
    RequestedEquipmentTO requestedEquipmentTO;
    PartyTO partyTO;
    PartyTO.IdentifyingCode identifyingCode;
    PartyContactDetailsTO partyContactDetailsTO;
    DocumentPartyTO documentPartyTO;
    ShipmentLocationTO shipmentLocationTO;

    @BeforeEach
    void init() {
      bookingTO = new BookingTO();

      bookingTO.setExpectedArrivalAtPlaceOfDeliveryStartDate(LocalDate.now());
      bookingTO.setExpectedArrivalAtPlaceOfDeliveryEndDate(LocalDate.now().plusDays(1));
      bookingTO.setExpectedDepartureDate(LocalDate.now().plusDays(10));

      bookingTO.setIsImportLicenseRequired(true);
      bookingTO.setImportLicenseReference("import_license_reference");

      bookingTO.setIsExportDeclarationRequired(true);
      bookingTO.setExportDeclarationReference("export_declaration_reference");

      invoicePayableAt = new LocationTO();
      invoicePayableAt.setLocationName(location1.getLocationName());

      bookingTO.setInvoicePayableAt(invoicePayableAt);

      placeOfIssue = new LocationTO();
      placeOfIssue.setLocationName(location2.getLocationName());

      bookingTO.setPlaceOfIssue(placeOfIssue);

      bookingTO.setVesselName("Rum Runner");
      bookingTO.setVesselIMONumber("9321483");

      commodityTO = new CommodityTO();
      commodityTO.setCommodityType(commodity.getCommodityType());

      bookingTO.setCommodities(Collections.singletonList(commodityTO));

      referenceTO = new ReferenceTO();
      referenceTO.setReferenceValue(reference.getReferenceValue());
      referenceTO.setReferenceType(reference.getReferenceType());

      bookingTO.setReferences(Collections.singletonList(referenceTO));

      valueAddedServiceRequestTO = new ValueAddedServiceRequestTO();
      valueAddedServiceRequestTO.setValueAddedServiceCode(
          valueAddedServiceRequest.getValueAddedServiceCode());

      bookingTO.setValueAddedServiceRequests(Collections.singletonList(valueAddedServiceRequestTO));

      requestedEquipmentTO = new RequestedEquipmentTO();
      requestedEquipmentTO.setRequestedEquipmentSizeType(
          requestedEquipment.getRequestedEquipmentSizeType());
      requestedEquipmentTO.setRequestedEquipmentUnits(
          requestedEquipment.getRequestedEquipmentUnits());

      bookingTO.setRequestedEquipments(Collections.singletonList(requestedEquipmentTO));

      partyContactDetailsTO = new PartyContactDetailsTO();
      partyContactDetails.setName("Bit");
      partyContactDetails.setEmail("coin@gmail.com");

      identifyingCode =
          PartyTO.IdentifyingCode.builder()
              .dcsaResponsibleAgencyCode(DCSAResponsibleAgencyCode.ISO)
              .codeListName("LCL")
              .partyCode("MSK")
              .build();

      partyTO = new PartyTO();
      partyTO.setPartyName("DCSA");
      partyTO.setAddress(address);
      partyTO.setPartyContactDetails(Collections.singletonList(partyContactDetailsTO));
      partyTO.setIdentifyingCodes(Collections.singletonList(identifyingCode));

      documentPartyTO = new DocumentPartyTO();
      documentPartyTO.setParty(partyTO);
      documentPartyTO.setPartyFunction(PartyFunction.DDS);
      documentPartyTO.setDisplayedAddress(
          Stream.of("test 1", "test 2").collect(Collectors.toList()));
      documentPartyTO.setIsToBeNotified(true);

      bookingTO.setDocumentParties(Collections.singletonList(documentPartyTO));

      shipmentLocationTO = new ShipmentLocationTO();
      shipmentLocationTO.setDisplayedName("Singapore");
      shipmentLocationTO.setShipmentLocationTypeCode(LocationType.FCD);
      shipmentLocationTO.setLocationTO(locationMapper.locationToDTO(location1));

      bookingTO.setShipmentLocations(Collections.singletonList(shipmentLocationTO));

      bookingResponseTO = new BookingResponseTO();
      bookingResponseTO.setCarrierBookingRequestReference(
          bookingTO.getCarrierBookingRequestReference());

      bookingTO.setExportVoyageNumber("export-voyage-number");
    }

    @Test
    void testShipmentEventFromBooking() {
      ShipmentEventMapper mapper = Mappers.getMapper(ShipmentEventMapper.class);
      //    ShipmentEvent shipmentEvent = bkgServiceImpl.shipmentEventFromBooking2(booking.getId(),
      // booking, "reason");
      ShipmentEvent shipmentEvent = mapper.shipmentEventFromBooking(booking, "reason");

      assertEquals(DocumentTypeCode.CBR, shipmentEvent.getDocumentTypeCode());
      assertEquals(booking.getId(), shipmentEvent.getDocumentID());
      assertEquals(
          ShipmentEventTypeCode.valueOf(booking.getDocumentStatus().name()),
          shipmentEvent.getShipmentEventTypeCode());
      assertEquals("reason", shipmentEvent.getReason());
      assertEquals(
          booking.getCarrierBookingRequestReference(), shipmentEvent.getDocumentReference());
    }

    @Test
    void testShipmentEventFromBookingTO() {
      ShipmentEventMapper mapper = Mappers.getMapper(ShipmentEventMapper.class);
      //    ShipmentEvent shipmentEvent = bkgServiceImpl.shipmentEventFromBooking2(booking.getId(),
      // booking, "reason");
      bookingTO.setDocumentStatus(booking.getDocumentStatus());
      ShipmentEvent shipmentEvent =
          mapper.shipmentEventFromBookingTO(bookingTO, booking.getId(), "reason");

      assertEquals(DocumentTypeCode.CBR, shipmentEvent.getDocumentTypeCode());
      assertEquals(booking.getId(), shipmentEvent.getDocumentID());
      assertEquals(
          ShipmentEventTypeCode.valueOf(bookingTO.getDocumentStatus().name()),
          shipmentEvent.getShipmentEventTypeCode());
      assertEquals("reason", shipmentEvent.getReason());
      assertEquals(
          bookingTO.getCarrierBookingRequestReference(), shipmentEvent.getDocumentReference());
    }

    @Test
    @DisplayName(
        "Method should throw an exception when isImportLicenseRequired is true and importLicenseReference null")
    void testCreateBookingWhenIsImportLicenseRequiredIsTrueAndImportLicenseReferenceIsNull() {

      bookingTO.setIsImportLicenseRequired(true);
      bookingTO.setImportLicenseReference(null);
      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "The attribute importLicenseReference cannot be null if isImportLicenseRequired is true.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Method should save and return with acceptable permutations of vesselIMONumber, exportVoyageNumber, expectedArrivalDateStart, expectedArrivalDateEnd, and expectedDepartureDate")
    void testCreateBookingTestAllAcceptablePermutationsOfArrivalDepartureVesselAndVoyage() {

      OffsetDateTime now = OffsetDateTime.now();
      booking.setBookingRequestDateTime(now);
      booking.setUpdatedDateTime(now);

      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(vessel.getVesselIMONumber()))
          .thenReturn(Mono.just(vessel));
      when(vesselRepository.findByVesselNameOrEmpty(vessel.getVesselName()))
          .thenReturn(Flux.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      // Test all permutations of null values for this check
      for (int i = 1; i < 7; i++) {

        char[] binary =
            String.format("%3s", Integer.toBinaryString(i)).replace(' ', '0').toCharArray();

        // Reset
        bookingTO.setVesselIMONumber("9321483");
        bookingTO.setExportVoyageNumber("export-voyage-number");
        bookingTO.setExpectedDepartureDate(LocalDate.now());
        bookingTO.setExpectedArrivalAtPlaceOfDeliveryStartDate(LocalDate.now().plusDays(1));
        bookingTO.setExpectedArrivalAtPlaceOfDeliveryEndDate(LocalDate.now().plusDays(2));

        if (binary[0] == '1') {
          bookingTO.setVesselName("Rum Runner");
          bookingTO.setVesselIMONumber(null);
          bookingTO.setExportVoyageNumber(null);
        } else if (binary[1] == '1') {
          bookingTO.setExpectedDepartureDate(null);
        } else if (binary[2] == '1') {
          bookingTO.setExpectedArrivalAtPlaceOfDeliveryStartDate(null);
          bookingTO.setExpectedArrivalAtPlaceOfDeliveryEndDate(null);
        }

        StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
            .assertNext(
                b -> {
                  assertEquals(
                      "ef223019-ff16-4870-be69-9dbaaaae9b11",
                      b.getCarrierBookingRequestReference());
                  assertEquals("Received", b.getDocumentStatus().getValue());
                  assertNotNull(b.getBookingRequestCreatedDateTime());
                  assertNotNull(b.getBookingRequestUpdatedDateTime());
                })
            .verifyComplete();
      }
    }

    @Test
    @DisplayName(
        "Method should save and return when at least one of vesselIMONumber, exportVoyageNumber, expectedArrivalDateStart, expectedArrivalDateEnd, and expectedDepartureDate is not null")
    void
        testCreateBookingExpectedDepartureDateAndVesselIMONumberAndExportVoyageNumberExpectedArrivalDateStartAndExpectedArrivalDateEndWhenAtLeastOneNotNull() {

      bookingTO.setVesselIMONumber(null);
      bookingTO.setExportVoyageNumber(null);
      bookingTO.setExpectedDepartureDate(null);
      bookingTO.setExpectedArrivalAtPlaceOfDeliveryStartDate(null);
      bookingTO.setExpectedArrivalAtPlaceOfDeliveryEndDate(null);
      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "The attributes expectedArrivalAtPlaceOfDeliveryStartDate, expectedArrivalAtPlaceOfDeliveryEndDate, expectedDepartureDate and vesselIMONumber/exportVoyageNumber cannot all be null at the same time. These fields are conditional and require that at least one of them is not empty.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Method should throw an exception when isExportDeclarationRequired is true and exportDeclarationReference is null")
    void
        testCreateBookingWhenIsExportDeclarationRequiredIsTrueAndExportDeclarationReferenceIsNull() {
      bookingTO.setIsExportDeclarationRequired(true);
      bookingTO.setExportDeclarationReference(null);
      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "The attribute exportDeclarationReference cannot be null if isExportDeclarationRequired is true.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Method should validate expected arrival dates")
    void testCreateBookingWhenExpectedArrivalDatesAreInvalid() {
      bookingTO.setExpectedArrivalAtPlaceOfDeliveryStartDate(LocalDate.now());
      bookingTO.setExpectedArrivalAtPlaceOfDeliveryEndDate(
          LocalDate.now().minus(1, ChronoUnit.DAYS));
      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "The attribute expectedArrivalAtPlaceOfDeliveryEndDate must be the same or after expectedArrivalAtPlaceOfDeliveryStartDate.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Method should save and return shallow booking for given booking request")
    void testCreateBookingShallow() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);

      bookingTO.setVesselName(null);
      bookingTO.setVesselIMONumber(null);
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertNull(argumentCaptor.getValue().getInvoicePayableAt());
                assertNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNull(argumentCaptor.getValue().getCommodities());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return shallow booking with existing vessel (IMO exists and name matches) "
            + "for given booking request")
    void testCreateBookingShallowWithExistingVessel() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      booking.setVesselId(vessel.getId());

      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(vessel.getVesselIMONumber()))
          .thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                verify(vesselRepository, never()).save(any());
                verify(vesselRepository)
                    .findByVesselIMONumberOrEmpty(bookingTO.getVesselIMONumber());
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertNull(argumentCaptor.getValue().getInvoicePayableAt());
                assertNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNull(argumentCaptor.getValue().getCommodities());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Creation of booking should result in an error if provided vesselName does not match existing vesselName (based on vesselIMONumber).")
    void testCreateBookingShallowWithExistingVesselButWrongVesselNameShouldFail() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      bookingTO.setVesselName("Freedom");
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "Provided vessel name does not match vessel name of existing vesselIMONumber.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Creation of booking should result in an error if provided vesselName is linked to multiple vessels.")
    void testCreateBookingShallowWithExistingVesselButNonUniqueVesselNameShouldFail() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      bookingTO.setVesselName("Rum Runner");
      bookingTO.setVesselIMONumber(null);
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      when(vesselRepository.findByVesselNameOrEmpty(any()))
          .thenReturn(Flux.just(vessel, new Vessel()));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "Unable to identify unique vessel, please provide a vesselIMONumber.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Method should save and return shallow booking with vessel (by vesselName) for given booking request")
    void testCreateBookingShallowWithVesselByVesselName() {

      vessel.setId(UUID.randomUUID());

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      booking.setVesselId(vessel.getId());

      bookingTO.setVesselIMONumber(null);
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselNameOrEmpty(vessel.getVesselName()))
          .thenReturn(Flux.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                verify(vesselRepository, never()).save(any());
                verify(vesselRepository).findByVesselNameOrEmpty(bookingTO.getVesselName());
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertNull(argumentCaptor.getValue().getInvoicePayableAt());
                assertNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNull(argumentCaptor.getValue().getCommodities());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should save and return booking with location for given booking request")
    void testCreateBookingWithLocation() {

      vessel.setId(UUID.randomUUID());
      booking.setVesselId(vessel.getId());

      bookingTO.setInvoicePayableAt(invoicePayableAt);
      bookingTO.setPlaceOfIssue(placeOfIssue);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(any())).thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(placeOfIssue))).thenReturn(Mono.just(locationTO2));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                verify(locationService, times(2)).ensureResolvable(any());
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertNull(argumentCaptor.getValue().getCommodities());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with location and commodities for given booking request")
    void testCreateBookingWithLocationAndCommodities() {

      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(any())).thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(placeOfIssue))).thenReturn(Mono.just(locationTO2));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                verify(locationService, times(2)).ensureResolvable(any());
                verify(commodityRepository).saveAll(any(Flux.class));
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with location, commodities and valueAddedServiceRequests for given booking request")
    void testCreateBookingWithLocationAndCommoditiesAndValAddSerReq() {

      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(any())).thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(placeOfIssue))).thenReturn(Mono.just(locationTO2));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                verify(locationService, times(2)).ensureResolvable(any());
                verify(commodityRepository).saveAll(any(Flux.class));
                verify(valueAddedServiceRequestRepository).save(any());
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with location, commodities, valueAddedServiceRequests and references for given booking request")
    void testCreateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferences() {

      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(any())).thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(placeOfIssue))).thenReturn(Mono.just(locationTO2));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                verify(locationService, times(2)).ensureResolvable(any());
                verify(commodityRepository).saveAll(any(Flux.class));
                verify(valueAddedServiceRequestRepository).save(any());
                verify(referenceService).createReferencesByBookingIDAndTOs(any(UUID.class), any());
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertEquals(
                    ReferenceTypeCode.FF,
                    argumentCaptor.getValue().getReferences().get(0).getReferenceType());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with location, commodities, valueAddedServiceRequests, references and requestedEquipments for given booking request")
    void testCreateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferencesAndReqEquip() {

      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(any())).thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(placeOfIssue))).thenReturn(Mono.just(locationTO2));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(requestedEquipmentRepository.save(any(RequestedEquipment.class)))
          .thenReturn(Mono.just(requestedEquipment));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                verify(locationService, times(2)).ensureResolvable(any());
                verify(commodityRepository).saveAll(any(Flux.class));
                verify(valueAddedServiceRequestRepository).save(any());
                verify(referenceService).createReferencesByBookingIDAndTOs(any(UUID.class), any());
                verify(requestedEquipmentRepository).save(any(RequestedEquipment.class));
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertEquals(
                    ReferenceTypeCode.FF,
                    argumentCaptor.getValue().getReferences().get(0).getReferenceType());
                assertEquals(
                    "22GP",
                    argumentCaptor
                        .getValue()
                        .getRequestedEquipments()
                        .get(0)
                        .getRequestedEquipmentSizeType());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with invalid list of equipment referenes in requestedEquipment should result in an error")
    void testCreateBookingWithInvalidEquipmentReferencesinRequestedEquipmentShouldResultInError() {

      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);
      requestedEquipmentTO.setEquipmentReferences(List.of("This", "is", "not", "valid"));

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));

      when(locationService.ensureResolvable(eq(invoicePayableAt)))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location1)));
      when(locationService.ensureResolvable(eq(placeOfIssue)))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location2)));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "Requested Equipment Units cannot be lower than quantity of Equipment References.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Failing creation a shipment event should result in an error")
    void testShipmentEventFailedShouldResultInError() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      bookingTO.setVesselName(null);
      bookingTO.setVesselIMONumber(null);
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));
      when(shipmentEventService.create(any())).thenReturn(Mono.empty());
      when(documentPartyService.createDocumentPartiesByBookingID(any(), isNull()))
          .thenReturn(Mono.empty());
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "Failed to create shipment event for Booking.", throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with location, commodities, valueAddedServiceRequests, references,"
            + " requestedEquipments and documentParties for given booking request")
    void
        testCreateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferencesAndReqEquipAndDocParties() {

      PartyTO partyTO = new PartyTO();
      partyTO.setPartyName("DCSA");
      partyTO.setPartyContactDetails(
          List.of(partyContactDetailsMapper.partyContactDetailsToDTO(partyContactDetails)));
      partyTO.setAddress(address);
      DocumentPartyTO documentPartyTO = new DocumentPartyTO();
      documentPartyTO.setParty(partyTO);
      documentPartyTO.setDisplayedAddress(List.of(displayedAddress.getAddressLine()));
      documentPartyTO.setPartyFunction(PartyFunction.DDS);

      bookingTO.setShipmentLocations(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(any())).thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(placeOfIssue))).thenReturn(Mono.just(locationTO2));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(requestedEquipmentRepository.save(any(RequestedEquipment.class)))
          .thenReturn(Mono.just(requestedEquipment));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                verify(locationService, times(2)).ensureResolvable(any());
                verify(documentPartyService).createDocumentPartiesByBookingID(any(), any());
                verify(commodityRepository).saveAll(any(Flux.class));
                verify(valueAddedServiceRequestRepository).save(any());
                verify(referenceService).createReferencesByBookingIDAndTOs(any(UUID.class), any());
                verify(requestedEquipmentRepository).save(any(RequestedEquipment.class));
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertEquals(
                    ReferenceTypeCode.FF,
                    argumentCaptor.getValue().getReferences().get(0).getReferenceType());
                assertEquals(
                    "22GP",
                    argumentCaptor
                        .getValue()
                        .getRequestedEquipments()
                        .get(0)
                        .getRequestedEquipmentSizeType());

                assertEquals(
                    "DCSA",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyName());
                assertEquals(
                    "coin@gmail.com",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyContactDetails()
                        .get(0)
                        .getEmail());
                assertEquals(
                    "København",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getAddress()
                        .getCity());
                assertEquals(
                    "Javastraat",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getDisplayedAddress()
                        .get(0));
                assertEquals(
                    PartyFunction.DDS,
                    argumentCaptor.getValue().getDocumentParties().get(0).getPartyFunction());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with location, commodities, valueAddedServiceRequests, references, requestedEquipments, documentParties and shipmentLocations for given booking request")
    void
        testCreateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferencesAndReqEquipAndDocPartiesAndShipmentLocations() {

      PartyTO partyTO = new PartyTO();
      partyTO.setPartyName("DCSA");
      partyTO.setPartyContactDetails(
          List.of(partyContactDetailsMapper.partyContactDetailsToDTO(partyContactDetails)));
      partyTO.setAddress(address);
      DocumentPartyTO documentPartyTO = new DocumentPartyTO();
      documentPartyTO.setParty(partyTO);
      documentPartyTO.setDisplayedAddress(List.of(displayedAddress.getAddressLine()));
      documentPartyTO.setPartyFunction(PartyFunction.DDS);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(voyageRepository.findByCarrierVoyageNumber(any())).thenReturn(Flux.just(voyage));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(any())).thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(placeOfIssue))).thenReturn(Mono.just(locationTO2));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));
      when(requestedEquipmentRepository.save(any(RequestedEquipment.class)))
          .thenReturn(Mono.just(requestedEquipment));
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(bkgServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                verify(locationService, times(2)).ensureResolvable(any());
                verify(documentPartyService).createDocumentPartiesByBookingID(any(), any());
                verify(commodityRepository).saveAll(any(Flux.class));
                verify(valueAddedServiceRequestRepository).save(any());
                verify(referenceService).createReferencesByBookingIDAndTOs(any(UUID.class), any());
                verify(requestedEquipmentRepository).save(any(RequestedEquipment.class));
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertEquals(
                    ReferenceTypeCode.FF,
                    argumentCaptor.getValue().getReferences().get(0).getReferenceType());
                assertEquals(
                    "22GP",
                    argumentCaptor
                        .getValue()
                        .getRequestedEquipments()
                        .get(0)
                        .getRequestedEquipmentSizeType());

                assertEquals(
                    "DCSA",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyName());
                assertEquals(
                    "coin@gmail.com",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyContactDetails()
                        .get(0)
                        .getEmail());
                assertEquals(
                    "København",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getAddress()
                        .getCity());
                assertEquals(
                    "Javastraat",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getDisplayedAddress()
                        .get(0));
                assertEquals(
                    PartyFunction.DDS,
                    argumentCaptor.getValue().getDocumentParties().get(0).getPartyFunction());
                assertEquals(
                    bookingTO.getShipmentLocations().get(0).getDisplayedName(),
                    argumentCaptor.getValue().getShipmentLocations().get(0).getDisplayedName());
                assertEquals(
                    bookingTO.getShipmentLocations().get(0).getShipmentLocationTypeCode(),
                    argumentCaptor
                        .getValue()
                        .getShipmentLocations()
                        .get(0)
                        .getShipmentLocationTypeCode());
                assertEquals(
                    bookingTO.getShipmentLocations().get(0).getLocationTO().getLocationName(),
                    argumentCaptor
                        .getValue()
                        .getShipmentLocations()
                        .get(0)
                        .getLocationTO()
                        .getLocationName());
              })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName(
      "Tests for the method updateBookingByReferenceCarrierBookingRequestReference(#BookingTO)")
  class UpdateBookingTest {

    BookingTO bookingTO;
    LocationTO invoicePayableAt;
    LocationTO placeOfIssue;
    CommodityTO commodityTO;
    ReferenceTO referenceTO;
    ValueAddedServiceRequestTO valueAddedServiceRequestTO;
    RequestedEquipmentTO requestedEquipmentTO;
    PartyTO partyTO;
    PartyTO.IdentifyingCode identifyingCode;
    PartyContactDetailsTO partyContactDetailsTO;
    DocumentPartyTO documentPartyTO;
    ShipmentLocationTO shipmentLocationTO;

    @BeforeEach
    void init() {
      bookingTO = new BookingTO();

      bookingTO.setExpectedArrivalAtPlaceOfDeliveryStartDate(LocalDate.now());
      bookingTO.setExpectedArrivalAtPlaceOfDeliveryEndDate(LocalDate.now().plusDays(1));
      bookingTO.setExpectedDepartureDate(LocalDate.now().plusDays(10));
      // carrierBookingRequestReference needs to be set for PUT request
      bookingTO.setCarrierBookingRequestReference("ef223019-ff16-4870-be69-9dbaaaae9b11");

      invoicePayableAt = new LocationTO();
      invoicePayableAt.setLocationName(location1.getLocationName());

      bookingTO.setInvoicePayableAt(invoicePayableAt);

      placeOfIssue = new LocationTO();
      placeOfIssue.setLocationName(location2.getLocationName());

      bookingTO.setPlaceOfIssue(placeOfIssue);

      bookingTO.setVesselName("Rum Runner");
      bookingTO.setVesselIMONumber("9321483");

      commodityTO = new CommodityTO();
      commodityTO.setCommodityType(commodity.getCommodityType());

      bookingTO.setCommodities(Collections.singletonList(commodityTO));

      referenceTO = new ReferenceTO();
      referenceTO.setReferenceValue(reference.getReferenceValue());
      referenceTO.setReferenceType(reference.getReferenceType());

      bookingTO.setReferences(Collections.singletonList(referenceTO));

      valueAddedServiceRequestTO = new ValueAddedServiceRequestTO();
      valueAddedServiceRequestTO.setValueAddedServiceCode(
          valueAddedServiceRequest.getValueAddedServiceCode());

      bookingTO.setValueAddedServiceRequests(Collections.singletonList(valueAddedServiceRequestTO));

      requestedEquipmentTO = new RequestedEquipmentTO();
      requestedEquipmentTO.setRequestedEquipmentSizeType(
          requestedEquipment.getRequestedEquipmentSizeType());
      requestedEquipmentTO.setRequestedEquipmentUnits(
          requestedEquipment.getRequestedEquipmentUnits());

      bookingTO.setRequestedEquipments(Collections.singletonList(requestedEquipmentTO));

      partyContactDetailsTO = new PartyContactDetailsTO();
      partyContactDetails.setName("Bit");
      partyContactDetails.setEmail("coin@gmail.com");

      identifyingCode =
          PartyTO.IdentifyingCode.builder()
              .dcsaResponsibleAgencyCode(DCSAResponsibleAgencyCode.ISO)
              .codeListName("LCL")
              .partyCode("MSK")
              .build();

      partyTO = new PartyTO();
      partyTO.setPartyName("DCSA");
      partyTO.setAddress(address);
      partyTO.setPartyContactDetails(Collections.singletonList(partyContactDetailsTO));
      partyTO.setIdentifyingCodes(Collections.singletonList(identifyingCode));

      documentPartyTO = new DocumentPartyTO();
      documentPartyTO.setParty(partyTO);
      documentPartyTO.setPartyFunction(PartyFunction.DDS);
      documentPartyTO.setDisplayedAddress(
          Stream.of("test 1", "test 2").collect(Collectors.toList()));
      documentPartyTO.setIsToBeNotified(true);

      bookingTO.setDocumentParties(Collections.singletonList(documentPartyTO));

      shipmentLocationTO = new ShipmentLocationTO();
      shipmentLocationTO.setDisplayedName("Singapore");
      shipmentLocationTO.setShipmentLocationTypeCode(LocationType.FCD);
      shipmentLocationTO.setLocationTO(locationMapper.locationToDTO(location1));

      bookingTO.setShipmentLocations(Collections.singletonList(shipmentLocationTO));

      bookingTO.setIsImportLicenseRequired(true);
      bookingTO.setIsExportDeclarationRequired(false);
      bookingTO.setImportLicenseReference("ABC123123");
    }

    @Test
    @DisplayName("Failing to find a booking should result in an error")
    void testUpdateBookingShouldResultInError() {

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No booking found with carrier booking request reference: "
                        + bookingTO.getCarrierBookingRequestReference(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Method should update and return shallow booking for given booking request")
    void testUpdateBookingShallow() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      bookingTO.setVesselName(null);
      bookingTO.setVesselIMONumber(null);
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));

      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals("Received", b.getDocumentStatus().getValue());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertNull(argumentCaptor.getValue().getInvoicePayableAt());
                assertNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNull(argumentCaptor.getValue().getCommodities());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update and return with acceptable permutations of vesselIMONumber, exportVoyageNumber, expectedArrivalDateStart, expectedArrivalDateEnd, and expectedDepartureDate")
    void testUpdateBookingTestAllAcceptablePermutationsOfArrivalDepartureVesselAndVoyage() {

      OffsetDateTime now = OffsetDateTime.now();
      booking.setBookingRequestDateTime(now);
      booking.setUpdatedDateTime(now);
      booking.setValidUntil(null);

      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(vessel.getVesselIMONumber()))
          .thenReturn(Mono.just(vessel));
      when(vesselRepository.findByVesselNameOrEmpty(vessel.getVesselName()))
          .thenReturn(Flux.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      // Test all permutations of null values for this check
      for (int i = 1; i < 7; i++) {

        char[] binary =
            String.format("%3s", Integer.toBinaryString(i)).replace(' ', '0').toCharArray();

        // Reset
        bookingTO.setVesselIMONumber("9321483");
        bookingTO.setExportVoyageNumber("export-voyage-number");
        bookingTO.setExpectedDepartureDate(LocalDate.now());
        bookingTO.setExpectedArrivalAtPlaceOfDeliveryStartDate(LocalDate.now().plusDays(1));
        bookingTO.setExpectedArrivalAtPlaceOfDeliveryEndDate(LocalDate.now().plusDays(2));
        booking.setValidUntil(null);

        if (binary[0] == '1') {
          bookingTO.setVesselName("Rum Runner");
          bookingTO.setVesselIMONumber(null);
          bookingTO.setExportVoyageNumber(null);
        } else if (binary[1] == '1') {
          bookingTO.setExpectedDepartureDate(null);
        } else if (binary[2] == '1') {
          bookingTO.setExpectedArrivalAtPlaceOfDeliveryStartDate(null);
          bookingTO.setExpectedArrivalAtPlaceOfDeliveryEndDate(null);
        }

        StepVerifier.create(
                bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
            .assertNext(
                b -> {
                  assertEquals(
                      "ef223019-ff16-4870-be69-9dbaaaae9b11",
                      b.getCarrierBookingRequestReference());
                  assertEquals("Received", b.getDocumentStatus().getValue());
                  assertNotNull(b.getBookingRequestCreatedDateTime());
                  assertNotNull(b.getBookingRequestUpdatedDateTime());
                })
            .verifyComplete();
      }
    }

    @Test
    @DisplayName(
        "Method should throw an exception when expectedArrivalDateStart, expectedArrivalDateEnd, vesselIMONumber, exportVoyageNumber, and expectedDepartureDate are null")
    void
        testUpdateBookingExpectedDepartureDateCannotBeNullIfVesselIMONumberAndExportVoyageNumberAndExpectedArrivalDateStartAndExpectedArrivalDateEndAreNull() {

      bookingTO.setExportVoyageNumber(null);
      bookingTO.setVesselIMONumber(null);
      bookingTO.setExpectedDepartureDate(null);
      bookingTO.setExpectedArrivalAtPlaceOfDeliveryStartDate(null);
      bookingTO.setExpectedArrivalAtPlaceOfDeliveryEndDate(null);
      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "The attributes expectedArrivalAtPlaceOfDeliveryStartDate, expectedArrivalAtPlaceOfDeliveryEndDate, expectedDepartureDate and vesselIMONumber/exportVoyageNumber cannot all be null at the same time. These fields are conditional and require that at least one of them is not empty.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Method should update and return shallow booking with existing vessel (IMO exists and name matches) "
            + "for given booking request")
    void testUpdateBookingShallowWithExistingVessel() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));

      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertNull(argumentCaptor.getValue().getInvoicePayableAt());
                assertNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNull(argumentCaptor.getValue().getCommodities());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Update of booking should result in an error if provided vesselName does not match existing vesselName (based on vesselIMONumber).")
    void testUpdateBookingShallowWithExistingVesselButWrongVesselNameShouldFail() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      bookingTO.setVesselName("Freedom");
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "Provided vessel name does not match vessel name of existing vesselIMONumber.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Update of booking should result in an error if provided vesselName is linked to multiple vessels.")
    void testUpdateBookingShallowWithExistingVesselButNonUniqueVesselNameShouldFail() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      bookingTO.setVesselName("Rum Runner");
      bookingTO.setVesselIMONumber(null);
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselNameOrEmpty(any()))
          .thenReturn(Flux.just(vessel, new Vessel()));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "Unable to identify unique vessel, please provide a vesselIMONumber.",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Method should update and return shallow booking with vessel (by vesselName) for given booking request")
    void testUpdateBookingShallowWithNewVessel() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      bookingTO.setVesselIMONumber(null);
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselNameOrEmpty(any())).thenReturn(Flux.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));

      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertNull(argumentCaptor.getValue().getInvoicePayableAt());
                assertNull(argumentCaptor.getValue().getCommodities());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should update and return booking with location for given booking request")
    void testUpdateBookingWithLocation() {

      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));

      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(eq(bookingTO.getInvoicePayableAt())))
          .thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(bookingTO.getPlaceOfIssue())))
          .thenReturn(Mono.just(locationTO2));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertNull(argumentCaptor.getValue().getCommodities());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update and return booking with cleared location if TO contains a NULL location object for given booking request")
    void testUpdateBookingWithClearedLocation() {

      // we assume booking has a linked location
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);

      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));

      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertNull(argumentCaptor.getValue().getInvoicePayableAt());
                assertNull(argumentCaptor.getValue().getPlaceOfIssue());
                assertNull(argumentCaptor.getValue().getCommodities());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update and return booking with location and commodities for given booking request")
    void testUpdateBookingWithLocationAndCommodities() {

      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));

      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(eq(bookingTO.getInvoicePayableAt())))
          .thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(bookingTO.getPlaceOfIssue())))
          .thenReturn(Mono.just(locationTO2));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));

      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertNull(argumentCaptor.getValue().getValueAddedServiceRequests());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update and return booking with location, commodities and valueAddedServiceRequests for given booking request")
    void testUpdateBookingWithLocationAndCommoditiesAndValAddSerReq() {

      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));

      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(eq(bookingTO.getInvoicePayableAt())))
          .thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(bookingTO.getPlaceOfIssue())))
          .thenReturn(Mono.just(locationTO2));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));

      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));

      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.empty());
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertNull(argumentCaptor.getValue().getReferences());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update and return booking with location, commodities, valueAddedServiceRequests and references for given booking request")
    void testUpdateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferences() {

      bookingTO.setRequestedEquipments(null);
      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));

      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(eq(bookingTO.getInvoicePayableAt())))
          .thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(bookingTO.getPlaceOfIssue())))
          .thenReturn(Mono.just(locationTO2));
      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));

      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));

      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));

      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));

      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertEquals(
                    ReferenceTypeCode.FF,
                    argumentCaptor.getValue().getReferences().get(0).getReferenceType());
                assertNull(argumentCaptor.getValue().getRequestedEquipments());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update and return booking with location, commodities, valueAddedServiceRequests, references and requestedEquipments for given booking request")
    void testUpdateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferencesAndReqEquip() {

      bookingTO.setDocumentParties(null);
      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));

      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(eq(bookingTO.getInvoicePayableAt())))
          .thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(bookingTO.getPlaceOfIssue())))
          .thenReturn(Mono.just(locationTO2));

      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));

      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));

      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));

      when(requestedEquipmentRepository.save(any(RequestedEquipment.class)))
          .thenReturn(Mono.just(requestedEquipment));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(Collections.emptyList()));

      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertEquals(
                    ReferenceTypeCode.FF,
                    argumentCaptor.getValue().getReferences().get(0).getReferenceType());
                assertEquals(
                    "22GP",
                    argumentCaptor
                        .getValue()
                        .getRequestedEquipments()
                        .get(0)
                        .getRequestedEquipmentSizeType());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update and return booking with location, commodities, valueAddedServiceRequests, references,"
            + " requestedEquipments and documentParties for given booking request")
    void
        testUpdateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferencesAndReqEquipAndDocParties() {

      PartyTO partyTO = new PartyTO();
      partyTO.setPartyName("DCSA");
      partyTO.setPartyContactDetails(
          List.of(partyContactDetailsMapper.partyContactDetailsToDTO(partyContactDetails)));
      partyTO.setAddress(address);
      DocumentPartyTO documentPartyTO = new DocumentPartyTO();
      documentPartyTO.setParty(partyTO);
      documentPartyTO.setDisplayedAddress(List.of(displayedAddress.getAddressLine()));
      documentPartyTO.setPartyFunction(PartyFunction.DDS);

      bookingTO.setShipmentLocations(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));

      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(eq(bookingTO.getInvoicePayableAt())))
          .thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(bookingTO.getPlaceOfIssue())))
          .thenReturn(Mono.just(locationTO2));
      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));

      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));

      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));

      when(requestedEquipmentRepository.save(any(RequestedEquipment.class)))
          .thenReturn(Mono.just(requestedEquipment));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));

      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertEquals(
                    ReferenceTypeCode.FF,
                    argumentCaptor.getValue().getReferences().get(0).getReferenceType());
                assertEquals(
                    "22GP",
                    argumentCaptor
                        .getValue()
                        .getRequestedEquipments()
                        .get(0)
                        .getRequestedEquipmentSizeType());
                assertEquals(
                    "22GP",
                    argumentCaptor
                        .getValue()
                        .getRequestedEquipments()
                        .get(0)
                        .getRequestedEquipmentSizeType());
                assertEquals(
                    "DCSA",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyName());
                assertEquals(
                    "coin@gmail.com",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyContactDetails()
                        .get(0)
                        .getEmail());
                assertEquals(
                    "København",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getAddress()
                        .getCity());
                assertEquals(
                    "Javastraat",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getDisplayedAddress()
                        .get(0));
                assertEquals(
                    PartyFunction.DDS,
                    argumentCaptor.getValue().getDocumentParties().get(0).getPartyFunction());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should update and return booking with location, commodities, valueAddedServiceRequests, references,"
            + " requestedEquipments, documentParties and shipmentLocations for given booking request")
    void
        testUpdateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferencesAndReqEquipAndDocPartiesAndShipmentLocations() {

      PartyTO partyTO = new PartyTO();
      partyTO.setPartyName("DCSA");
      partyTO.setPartyContactDetails(
          List.of(partyContactDetailsMapper.partyContactDetailsToDTO(partyContactDetails)));
      partyTO.setAddress(address);
      DocumentPartyTO documentPartyTO = new DocumentPartyTO();
      documentPartyTO.setParty(partyTO);
      documentPartyTO.setDisplayedAddress(List.of(displayedAddress.getAddressLine()));
      documentPartyTO.setPartyFunction(PartyFunction.DDS);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));
      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));

      when(vesselRepository.findByVesselIMONumberOrEmpty(any())).thenReturn(Mono.just(vessel));
      when(bookingRepository.setVesselIDFor(any(), any())).thenReturn(Mono.just(true));

      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      when(locationService.ensureResolvable(eq(bookingTO.getInvoicePayableAt())))
          .thenReturn(Mono.just(locationTO1));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);
      when(locationService.ensureResolvable(eq(bookingTO.getPlaceOfIssue())))
          .thenReturn(Mono.just(locationTO2));

      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));

      when(valueAddedServiceRequestRepository.save(any()))
          .thenReturn(Mono.just(valueAddedServiceRequest));

      when(referenceService.createReferencesByBookingIDAndTOs(any(UUID.class), any()))
          .thenReturn(Mono.just(List.of(referenceTO)));

      when(requestedEquipmentRepository.save(any(RequestedEquipment.class)))
          .thenReturn(Mono.just(requestedEquipment));

      when(documentPartyService.createDocumentPartiesByBookingID(any(), any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));

      when(shipmentLocationService.createShipmentLocationsByBookingIDAndTOs(any(), any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      ArgumentCaptor<BookingTO> argumentCaptor = ArgumentCaptor.forClass(BookingTO.class);

      StepVerifier.create(
              bkgServiceImpl.updateBookingByReferenceCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11", bookingTO))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertNotNull(b.getBookingRequestCreatedDateTime());
                assertNotNull(b.getBookingRequestUpdatedDateTime());

                // Since the response type of createBooking has changed
                // we capture the bookingTO -> bookingResponseTO mapping argument
                verify(bookingMapper).dtoToBookingResponseTO(argumentCaptor.capture());
                assertEquals("Rum Runner", argumentCaptor.getValue().getVesselName());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    argumentCaptor.getValue().getInvoicePayableAt().getId().toString());
                assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745",
                    argumentCaptor.getValue().getPlaceOfIssue().getId().toString());
                assertEquals(
                    "Mobile phones",
                    argumentCaptor.getValue().getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    argumentCaptor
                        .getValue()
                        .getValueAddedServiceRequests()
                        .get(0)
                        .getValueAddedServiceCode());
                assertEquals(
                    ReferenceTypeCode.FF,
                    argumentCaptor.getValue().getReferences().get(0).getReferenceType());
                assertEquals(
                    "22GP",
                    argumentCaptor
                        .getValue()
                        .getRequestedEquipments()
                        .get(0)
                        .getRequestedEquipmentSizeType());
                assertEquals(
                    "22GP",
                    argumentCaptor
                        .getValue()
                        .getRequestedEquipments()
                        .get(0)
                        .getRequestedEquipmentSizeType());
                assertEquals(
                    "DCSA",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyName());
                assertEquals(
                    "coin@gmail.com",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyContactDetails()
                        .get(0)
                        .getEmail());
                assertEquals(
                    "København",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getAddress()
                        .getCity());
                assertEquals(
                    "Javastraat",
                    argumentCaptor
                        .getValue()
                        .getDocumentParties()
                        .get(0)
                        .getDisplayedAddress()
                        .get(0));
                assertEquals(
                    PartyFunction.DDS,
                    argumentCaptor.getValue().getDocumentParties().get(0).getPartyFunction());
                assertEquals(
                    bookingTO.getShipmentLocations().get(0).getDisplayedName(),
                    argumentCaptor.getValue().getShipmentLocations().get(0).getDisplayedName());
                assertEquals(
                    bookingTO.getShipmentLocations().get(0).getShipmentLocationTypeCode(),
                    argumentCaptor
                        .getValue()
                        .getShipmentLocations()
                        .get(0)
                        .getShipmentLocationTypeCode());
                assertEquals(
                    bookingTO.getShipmentLocations().get(0).getLocationTO().getLocationName(),
                    argumentCaptor
                        .getValue()
                        .getShipmentLocations()
                        .get(0)
                        .getLocationTO()
                        .getLocationName());
              })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("Tests for the method getBookingByCarrierBookingRequestReference(#String)")
  class BookingByCarrierBookingRequestReferenceTest {

    ReferenceTO referenceTO;
    DocumentPartyTO documentPartyTO;

    ShipmentLocationTO shipmentLocationTO;

    @BeforeEach
    public void init() {
      referenceTO = new ReferenceTO();
      referenceTO.setReferenceType(reference.getReferenceType());
      referenceTO.setReferenceValue(reference.getReferenceValue());

      documentPartyTO = new DocumentPartyTO();
      documentPartyTO.setPartyFunction(PartyFunction.AG);
      documentPartyTO.setIsToBeNotified(false);
      documentPartyTO.setDisplayedAddress(List.of("Javastraat"));
    }

    @Test
    @DisplayName("Method should return shallow booking for given carrierBookingRequestReference")
    void testGETBookingShallow() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(valueAddedServiceRequestRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(referenceService.findByBookingID(any())).thenReturn(Mono.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyService.fetchDocumentPartiesByBookingID(any())).thenReturn(Mono.empty());
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());
      when(locationService.fetchLocationDeepObjByID(isNull())).thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertNull(b.getInvoicePayableAt().getAddress());
                Assertions.assertNull(b.getInvoicePayableAt().getFacility());
                assertEquals(0, b.getCommodities().size());
                assertEquals(0, b.getValueAddedServiceRequests().size());
                assertNull(b.getReferences());
                assertEquals(0, b.getRequestedEquipments().size());
                assertNull(b.getDocumentParties());
                assertNull(b.getShipmentLocations());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return shallow booking with shallow location for given carrierBookingRequestReference")
    void testGETBookingShallowWithLocationShallow() {

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      when(locationService.fetchLocationDeepObjByID(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6")))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location1)));
      when(locationService.fetchLocationDeepObjByID(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745")))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location2)));

      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(valueAddedServiceRequestRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(referenceService.findByBookingID(any())).thenReturn(Mono.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());
      when(documentPartyService.fetchDocumentPartiesByBookingID(any())).thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId().toString());
                assertEquals("7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId().toString());
                assertEquals(0, b.getCommodities().size());
                assertEquals(0, b.getValueAddedServiceRequests().size());
                assertNull(b.getReferences());
                assertEquals(0, b.getRequestedEquipments().size());
                assertNull(b.getDocumentParties());
                assertNull(b.getShipmentLocations());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location for given carrierBookingRequestReference")
    void testGETBookingShallowWithLocationDeep() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      locationTO1.setAddress(address);
      locationTO1.setFacility(facility);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      when(locationService.fetchLocationDeepObjByID(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6")))
          .thenReturn(Mono.just(locationTO1));
      when(locationService.fetchLocationDeepObjByID(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745")))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location2)));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(valueAddedServiceRequestRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(referenceService.findByBookingID(any())).thenReturn(Mono.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyService.fetchDocumentPartiesByBookingID(any())).thenReturn(Mono.empty());
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId().toString());
                assertEquals("7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId().toString());
                assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                assertEquals(0, b.getCommodities().size());
                assertEquals(0, b.getValueAddedServiceRequests().size());
                assertNull(b.getReferences());
                assertEquals(0, b.getRequestedEquipments().size());
                assertNull(b.getDocumentParties());
                assertNull(b.getShipmentLocations());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities for given carrierBookingRequestReference")
    void testGETBookingShallowWithLocationDeepAndCommodities() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      locationTO1.setAddress(address);
      locationTO1.setFacility(facility);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      when(locationService.fetchLocationDeepObjByID(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6")))
          .thenReturn(Mono.just(locationTO1));
      when(locationService.fetchLocationDeepObjByID(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745")))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location2)));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(referenceService.findByBookingID(any())).thenReturn(Mono.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyService.fetchDocumentPartiesByBookingID(any())).thenReturn(Mono.empty());
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId().toString());
                assertEquals("7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId().toString());
                assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                assertEquals("Mobile phones", b.getCommodities().get(0).getCommodityType());
                assertEquals(0, b.getValueAddedServiceRequests().size());
                assertNull(b.getReferences());
                assertEquals(0, b.getRequestedEquipments().size());
                assertNull(b.getDocumentParties());
                assertNull(b.getShipmentLocations());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities & valueAddedServiceRequests for given carrierBookingRequestReference")
    void testGETBookingShallowWithLocationDeepAndCommoditiesAndValAddedServ() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      locationTO1.setAddress(address);
      locationTO1.setFacility(facility);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      when(locationService.fetchLocationDeepObjByID(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6")))
          .thenReturn(Mono.just(locationTO1));
      when(locationService.fetchLocationDeepObjByID(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745")))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location2)));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceService.findByBookingID(any())).thenReturn(Mono.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyService.fetchDocumentPartiesByBookingID(any())).thenReturn(Mono.empty());
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId().toString());
                assertEquals("7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId().toString());
                assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                assertEquals("Mobile phones", b.getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                assertNull(b.getReferences());
                assertEquals(0, b.getRequestedEquipments().size());
                assertNull(b.getDocumentParties());
                assertNull(b.getShipmentLocations());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities, "
            + "valueAddedServiceRequests, references & for given carrierBookingRequestReference")
    void testGETBookingWithLocationDeepAndCommoditiesAndValAddedServAndRefs() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      locationTO1.setAddress(address);
      locationTO1.setFacility(facility);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      when(locationService.fetchLocationDeepObjByID(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6")))
          .thenReturn(Mono.just(locationTO1));
      when(locationService.fetchLocationDeepObjByID(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745")))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location2)));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceService.findByBookingID(any())).thenReturn(Mono.just(List.of(referenceTO)));
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyService.fetchDocumentPartiesByBookingID(any())).thenReturn(Mono.empty());
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId().toString());
                assertEquals("7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId().toString());
                assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                assertEquals("Mobile phones", b.getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                assertEquals(ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                assertEquals(0, b.getRequestedEquipments().size());
                assertNull(b.getDocumentParties());
                assertNull(b.getShipmentLocations());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities, "
            + "valueAddedServiceRequests, references & requestedEquipments for given carrierBookingRequestReference")
    void testGETBookingWithLocationDeepAndCommoditiesAndValAddedServAndRefsAndReqEqs() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      locationTO1.setAddress(address);
      locationTO1.setFacility(facility);
      LocationTO locationTO2 = locationMapper.locationToDTO(location2);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      when(locationService.fetchLocationDeepObjByID(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6")))
          .thenReturn(Mono.just(locationTO1));
      when(locationService.fetchLocationDeepObjByID(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745")))
          .thenReturn(Mono.just(locationTO2));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceService.findByBookingID(any())).thenReturn(Mono.just(List.of(referenceTO)));
      when(requestedEquipmentRepository.findByBookingID(any()))
          .thenReturn(Flux.just(requestedEquipment));
      when(documentPartyService.fetchDocumentPartiesByBookingID(any())).thenReturn(Mono.empty());
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId().toString());
                assertEquals("7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId().toString());
                assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                assertEquals("Mobile phones", b.getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                assertEquals(ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                assertEquals(
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizeType());
                assertNull(b.getDocumentParties());
                assertNull(b.getShipmentLocations());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities, "
            + "valueAddedServiceRequests, references, requestedEquipments & documentParties for given carrierBookingRequestReference")
    void
        testGETBookingWithLocationDeepAndCommoditiesAndValAddedServAndRefsAndReqEqsAndDocParties() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      locationTO1.setAddress(address);
      locationTO1.setFacility(facility);
      PartyTO partyTO = partyMapper.partyToDTO(party);
      partyTO.setAddress(address);
      partyTO.setPartyContactDetails(
          List.of(
              new PartyContactDetailsTO(
                  partyContactDetails.getName(),
                  partyContactDetails.getEmail(),
                  partyContactDetails.getPhone(),
                  partyContactDetails.getUrl())));
      partyTO.setIdentifyingCodes(
          List.of(
              PartyTO.IdentifyingCode.builder()
                  .partyCode(partyIdentifyingCode.getPartyCode())
                  .codeListName(partyIdentifyingCode.getCodeListName())
                  .dcsaResponsibleAgencyCode(partyIdentifyingCode.getDcsaResponsibleAgencyCode())
                  .build()));

      documentPartyTO.setParty(partyTO);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      when(locationService.fetchLocationDeepObjByID(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6")))
          .thenReturn(Mono.just(locationTO1));
      when(locationService.fetchLocationDeepObjByID(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745")))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location2)));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceService.findByBookingID(any())).thenReturn(Mono.just(List.of(referenceTO)));
      when(requestedEquipmentRepository.findByBookingID(any()))
          .thenReturn(Flux.just(requestedEquipment));
      when(documentPartyService.fetchDocumentPartiesByBookingID(any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId().toString());
                assertEquals("7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId().toString());
                assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                assertEquals("Mobile phones", b.getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                assertEquals(ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                assertEquals(
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizeType());
                assertEquals(1, b.getDocumentParties().size());
                assertEquals("DCSA", b.getDocumentParties().get(0).getParty().getPartyName());
                assertEquals(
                    "Denmark", b.getDocumentParties().get(0).getParty().getAddress().getCountry());
                assertEquals(
                    "Peanut",
                    b.getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyContactDetails()
                        .get(0)
                        .getName());
                assertEquals(
                    "MSK",
                    b.getDocumentParties()
                        .get(0)
                        .getParty()
                        .getIdentifyingCodes()
                        .get(0)
                        .getPartyCode());
                assertEquals(
                    "Javastraat", b.getDocumentParties().get(0).getDisplayedAddress().get(0));
                assertNull(b.getShipmentLocations());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities, "
            + "valueAddedServiceRequests, references, requestedEquipments, documentParties & shipmentLocations "
            + "for given carrierBookingRequestReference")
    void
        testGETBookingWithLocationDeepAndCommoditiesAndValAddedServAndRefsAndReqEqsAndDocPartiesAndShipLocs() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      locationTO1.setAddress(address);
      locationTO1.setFacility(facility);
      PartyTO partyTO = partyMapper.partyToDTO(party);
      partyTO.setAddress(address);
      partyTO.setPartyContactDetails(
          List.of(
              new PartyContactDetailsTO(
                  partyContactDetails.getName(),
                  partyContactDetails.getEmail(),
                  partyContactDetails.getPhone(),
                  partyContactDetails.getUrl())));
      partyTO.setIdentifyingCodes(
          List.of(
              PartyTO.IdentifyingCode.builder()
                  .partyCode(partyIdentifyingCode.getPartyCode())
                  .codeListName(partyIdentifyingCode.getCodeListName())
                  .dcsaResponsibleAgencyCode(partyIdentifyingCode.getDcsaResponsibleAgencyCode())
                  .build()));

      documentPartyTO.setParty(partyTO);

      shipmentLocationTO =
          shipmentLocationMapper.shipmentLocationToDtoWithLocationTO(shipmentLocation, locationTO1);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      when(locationService.fetchLocationDeepObjByID(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6")))
          .thenReturn(Mono.just(locationTO1));
      when(locationService.fetchLocationDeepObjByID(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745")))
          .thenAnswer(answer -> Mono.just(locationMapper.locationToDTO(location2)));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceService.findByBookingID(any())).thenReturn(Mono.just(List.of(referenceTO)));
      when(requestedEquipmentRepository.findByBookingID(any()))
          .thenReturn(Flux.just(requestedEquipment));
      when(documentPartyService.fetchDocumentPartiesByBookingID(any()))
          .thenReturn(Mono.just(List.of(documentPartyTO)));
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId().toString());
                assertEquals("7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId().toString());
                assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                assertEquals("Mobile phones", b.getCommodities().get(0).getCommodityType());
                assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                assertEquals(ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                assertEquals(
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizeType());
                assertEquals(1, b.getDocumentParties().size());
                assertEquals("DCSA", b.getDocumentParties().get(0).getParty().getPartyName());
                assertEquals(
                    "Denmark", b.getDocumentParties().get(0).getParty().getAddress().getCountry());
                assertEquals(
                    "Peanut",
                    b.getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyContactDetails()
                        .get(0)
                        .getName());
                assertEquals(
                    "MSK",
                    b.getDocumentParties()
                        .get(0)
                        .getParty()
                        .getIdentifyingCodes()
                        .get(0)
                        .getPartyCode());
                assertEquals(
                    "Javastraat", b.getDocumentParties().get(0).getDisplayedAddress().get(0));
                assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    b.getShipmentLocations().get(0).getLocationTO().getId().toString());
                assertEquals(
                    LocationType.FCD,
                    b.getShipmentLocations().get(0).getShipmentLocationTypeCode());
                assertEquals("Singapore", b.getShipmentLocations().get(0).getDisplayedName());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should return shallow booking for given carrierBookingRequestReference")
    void testGETBookingNotFound() {

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(bkgServiceImpl.getBookingByCarrierBookingRequestReference("IdoNotExist"))
          .expectError(NotFoundException.class);
    }

    @Test
    @DisplayName(
        "Method should return an error when no booking is found for given carrierBookingRequestReference")
    void testGETBookingNoBookingFound() {

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  booking.getCarrierBookingRequestReference()))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No booking found with carrier booking request reference: "
                        + booking.getCarrierBookingRequestReference(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Method should return an error when no active booking is found for given carrierBookingRequestReference")
    void testGETShipmentNoActiveShipmentFound() {
      booking.setValidUntil(OffsetDateTime.now());
      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(booking));

      StepVerifier.create(
              bkgServiceImpl.getBookingByCarrierBookingRequestReference(
                  booking.getCarrierBookingRequestReference()))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "All bookings are inactive, at least one active booking should be present.",
                    throwable.getMessage());
              })
          .verify();
    }
  }

  @Nested
  @DisplayName("Tests for the method getShipmentByCarrierBookingReference(#String)")
  class ShipmentByCarrierBookingReferenceTest {

    ReferenceTO referenceTO;
    DocumentPartyTO documentPartyTO;
    ChargeTO chargeTO;
    CarrierClauseTO carrierClauseTO;
    TransportTO transportTO;

    ShipmentLocationTO shipmentLocationTO;

    @BeforeEach
    public void init() {
      referenceTO = new ReferenceTO();
      referenceTO.setReferenceValue(reference.getReferenceValue());
      referenceTO.setReferenceType(reference.getReferenceType());

      documentPartyTO = new DocumentPartyTO();
      documentPartyTO.setPartyFunction(PartyFunction.DDS);
      documentPartyTO.setIsToBeNotified(false);
      documentPartyTO.setDisplayedAddress(List.of("Displayded address"));

      chargeTO = chargeMapper.chargeToDTO(charge);
      carrierClauseTO = carrierClauseMapper.carrierClauseToDTO(carrierClause);

      shipmentLocationTO =
          shipmentLocationMapper.shipmentLocationToDtoWithLocationTO(
              shipmentLocation, locationMapper.locationToDTO(location1));

      LocationTO dischargeLocation = new LocationTO();
      dischargeLocation.setFacilityCode("123456");
      dischargeLocation.setFacilityCodeListProvider(FacilityCodeListProvider.SMDG);
      dischargeLocation.setId(UUID.fromString("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"));
      dischargeLocation.setAddressID(UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"));
      dischargeLocation.setFacilityID(UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"));

      LocationTO loadLocation = new LocationTO();
      loadLocation.setFacilityCode("654321");
      loadLocation.setFacilityCodeListProvider(FacilityCodeListProvider.SMDG);
      loadLocation.setId(UUID.fromString("c703277f-84ca-4816-9ccf-fad8e202d3b6"));

      transportTO = new TransportTO();
      transportTO.setTransportPlanStageSequenceNumber(
          shipmentTransport.getTransportPlanStageSequenceNumber());
      transportTO.setTransportPlanStage(shipmentTransport.getTransportPlanStageCode());
      transportTO.setIsUnderShippersResponsibility(false);
      transportTO.setModeOfTransport(DCSATransportType.VESSEL);
      transportTO.setVesselName(vessel.getVesselName());
      transportTO.setVesselIMONumber(vessel.getVesselIMONumber());
      transportTO.setImportVoyageNumber(voyage.getCarrierVoyageNumber());
      transportTO.setExportVoyageNumber(voyage.getCarrierVoyageNumber());
      transportTO.setPlannedArrivalDate(arrivalTransportEvent.getEventDateTime());
      transportTO.setPlannedDepartureDate(departureTransportEvent.getEventDateTime());
      transportTO.setTransportName(transport.getTransportName());
      transportTO.setTransportReference(transport.getTransportReference());
      transportTO.setDischargeLocation(dischargeLocation);
      transportTO.setLoadLocation(loadLocation);
    }

    @Test
    @DisplayName("Method should return shallow shipment for given carrierBookingReference")
    void testGETShipmentShallow() {

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(shipment));
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(carrierClauseService.fetchCarrierClausesByShipmentID(any())).thenReturn(Flux.empty());
      when(bookingRepository.findById((UUID) any())).thenReturn(Mono.empty());
      when(chargeService.fetchChargesByShipmentID(any())).thenReturn(Flux.empty());
      when(transportService.findByShipmentID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                assertEquals(shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                assertEquals(shipment.getTermsAndConditions(), b.getTermsAndConditions());
                assertEquals(shipment.getConfirmationDateTime(), b.getShipmentCreatedDateTime());
                Assertions.assertNull(b.getBooking());
                assertNull(b.getShipmentLocations());
                assertEquals(0, b.getShipmentCutOffTimes().size());
                assertEquals(0, b.getCharges().size());
                assertEquals(0, b.getCarrierClauses().size());
                assertEquals(0, b.getTransports().size());
                assertEquals(0, b.getConfirmedEquipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return shipment for given carrierBookingRequestReference with ShipmentLocation and ShipmentCutOffTimes")
    void testGETShipmentWithShipmentLocationsAndShipmentCutOffTimes() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      locationTO1.setAddress(address);
      locationTO1.setFacility(facility);

      shipmentLocationTO =
          shipmentLocationMapper.shipmentLocationToDtoWithLocationTO(shipmentLocation, locationTO1);

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(shipment));
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any()))
          .thenReturn(Flux.just(shipmentCutOffTime));
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());

      when(carrierClauseService.fetchCarrierClausesByShipmentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(bookingRepository.findById((UUID) any())).thenReturn(Mono.empty());
      when(chargeService.fetchChargesByShipmentID(any())).thenReturn(Flux.empty());
      when(transportService.findByShipmentID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                assertEquals(shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                Assertions.assertNull(b.getBooking());
                assertEquals(1, b.getShipmentLocations().size());
                assertEquals(
                    shipmentLocation.getShipmentLocationTypeCode(),
                    b.getShipmentLocations().get(0).getShipmentLocationTypeCode());
                assertEquals(
                    shipmentLocation.getDisplayedName(),
                    b.getShipmentLocations().get(0).getDisplayedName());
                assertEquals(
                    shipmentLocation.getEventDateTime(),
                    b.getShipmentLocations().get(0).getEventDateTime());
                assertEquals(
                    location1.getId(), b.getShipmentLocations().get(0).getLocationTO().getId());
                assertEquals(
                    address.getId(),
                    b.getShipmentLocations().get(0).getLocationTO().getAddress().getId());
                assertEquals(
                    facility.getFacilityID(),
                    b.getShipmentLocations().get(0).getLocationTO().getFacility().getFacilityID());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return shipment for given carrierBookingRequestReference with ShipmentLocations, ShipmentCutOffTimes, and CarrierClauses")
    void testGETShipmentWithShipmentLocationsAndShipmentCutOffTimesAndCarrierClauses() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      locationTO1.setAddress(address);
      locationTO1.setFacility(facility);

      shipmentLocationTO =
          shipmentLocationMapper.shipmentLocationToDtoWithLocationTO(shipmentLocation, locationTO1);

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(shipment));
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any()))
          .thenReturn(Flux.just(shipmentCutOffTime));
      when(carrierClauseService.fetchCarrierClausesByShipmentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(chargeService.fetchChargesByShipmentID(any())).thenReturn(Flux.empty());
      when(bookingRepository.findById((UUID) any())).thenReturn(Mono.empty());
      when(transportService.findByShipmentID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                System.out.println(b);
                assertEquals(shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                assertNull(b.getBooking());
                assertEquals(1, b.getShipmentLocations().size());
                assertEquals(1, b.getCarrierClauses().size());
                assertEquals(
                    carrierClause.getClauseContent(),
                    b.getCarrierClauses().get(0).getClauseContent());
                assertEquals(
                    shipmentLocation.getShipmentLocationTypeCode(),
                    b.getShipmentLocations().get(0).getShipmentLocationTypeCode());
                assertEquals(
                    shipmentLocation.getDisplayedName(),
                    b.getShipmentLocations().get(0).getDisplayedName());
                assertEquals(
                    shipmentLocation.getEventDateTime(),
                    b.getShipmentLocations().get(0).getEventDateTime());
                assertEquals(
                    location1.getId(), b.getShipmentLocations().get(0).getLocationTO().getId());
                assertEquals(
                    address.getId(),
                    b.getShipmentLocations().get(0).getLocationTO().getAddress().getId());
                assertEquals(
                    facility.getFacilityID(),
                    b.getShipmentLocations().get(0).getLocationTO().getFacility().getFacilityID());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return shipment for given carrierBookingRequestReference with confirmedEquipment")
    void testGETShipmentWithConfirmedEquipment() {

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(shipment));
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any()))
          .thenReturn(Flux.just(confirmedEquipment));
      when(carrierClauseService.fetchCarrierClausesByShipmentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(bookingRepository.findById((UUID) any())).thenReturn(Mono.empty());
      when(chargeService.fetchChargesByShipmentID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any()))
          .thenReturn(Flux.just(confirmedEquipment));
      when(transportService.findByShipmentID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                assertEquals(shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                Assertions.assertNull(b.getBooking());
                assertEquals(1, b.getConfirmedEquipments().size());
                assertEquals(
                    confirmedEquipment.getConfirmedEquipmentSizetype(),
                    b.getConfirmedEquipments().get(0).getConfirmedEquipmentSizetype());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return shipment for given carrierBookingRequestReference with charges")
    void testGETShipmentWithCharges() {

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(shipment));
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(carrierClauseService.fetchCarrierClausesByShipmentID(any())).thenReturn(Flux.empty());
      when(chargeService.fetchChargesByShipmentID(any())).thenReturn(Flux.just(chargeTO));
      when(bookingRepository.findById((UUID) any())).thenReturn(Mono.empty());
      when(transportService.findByShipmentID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                assertEquals(shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                assertEquals(shipment.getTermsAndConditions(), b.getTermsAndConditions());
                assertEquals(shipment.getConfirmationDateTime(), b.getShipmentCreatedDateTime());
                Assertions.assertNull(b.getBooking());
                Assertions.assertNotNull(b.getCharges());
                assertEquals(1, b.getCharges().size());
                assertEquals(charge.getChargeType(), b.getCharges().get(0).getChargeType());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return shipment for given carrierBookingRequestReference with everything")
    void testGETShipmentWithTransports() {

      when(bookingRepository.findById((UUID) any())).thenReturn(Mono.empty());
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(carrierClauseService.fetchCarrierClausesByShipmentID(any())).thenReturn(Flux.empty());
      when(chargeService.fetchChargesByShipmentID(any())).thenReturn(Flux.empty());

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(shipment));
      when(transportService.findByShipmentID(any())).thenReturn(Flux.just(transportTO));

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                assertEquals(shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                assertEquals(1, b.getTransports().size());
                Assertions.assertNotNull(b.getTransports().get(0).getDischargeLocation());
                Assertions.assertNotNull(b.getTransports().get(0).getLoadLocation());
                assertEquals(location1.getId(), b.getTransports().get(0).getLoadLocation().getId());
                assertEquals(
                    location2.getId(), b.getTransports().get(0).getDischargeLocation().getId());
                assertEquals(
                    address.getId(),
                    b.getTransports().get(0).getDischargeLocation().getAddressID());
                assertEquals(
                    facility.getFacilityID(),
                    b.getTransports().get(0).getDischargeLocation().getFacilityID());
                assertEquals(
                    departureTransportEvent.getEventDateTime(),
                    b.getTransports().get(0).getPlannedDepartureDate());
                assertEquals(
                    arrivalTransportEvent.getEventDateTime(),
                    b.getTransports().get(0).getPlannedArrivalDate());
                assertEquals(
                    voyage.getCarrierVoyageNumber(),
                    b.getTransports().get(0).getExportVoyageNumber());
                Assertions.assertEquals(
                    voyage.getCarrierVoyageNumber(),
                    b.getTransports().get(0).getImportVoyageNumber());
                Assertions.assertEquals(
                    vessel.getVesselName(), b.getTransports().get(0).getVesselName());
                assertEquals(
                    vessel.getVesselIMONumber(), b.getTransports().get(0).getVesselIMONumber());
                assertEquals(
                    modeOfTransport.getDcsaTransportType(),
                    b.getTransports().get(0).getModeOfTransport());
                assertEquals(
                    shipmentTransport.getIsUnderShippersResponsibility(),
                    b.getTransports().get(0).getIsUnderShippersResponsibility());
                assertEquals(
                    shipmentTransport.getTransportPlanStageCode(),
                    b.getTransports().get(0).getTransportPlanStage());
                assertEquals(
                    shipmentTransport.getTransportPlanStageSequenceNumber(),
                    b.getTransports().get(0).getTransportPlanStageSequenceNumber());
                assertEquals(
                    transport.getTransportName(), b.getTransports().get(0).getTransportName());
                assertEquals(
                    transport.getTransportReference(),
                    b.getTransports().get(0).getTransportReference());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return shipment for given carrierBookingRequestReference with different import and export voyages")
    void testGETShipmentWithTransportsWithDifferentVoyages() {
      Voyage exportVoyage = new Voyage();
      exportVoyage.setId(UUID.randomUUID());
      exportVoyage.setCarrierVoyageNumber("exportCarrierVoyageNumber");
      transportTO.setExportVoyageNumber(exportVoyage.getCarrierVoyageNumber());

      loadTransportCall.setExportVoyageID(exportVoyage.getId());

      when(bookingRepository.findById((UUID) any())).thenReturn(Mono.empty());
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.empty());
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(chargeService.fetchChargesByShipmentID(any())).thenReturn(Flux.empty());
      when(carrierClauseService.fetchCarrierClausesByShipmentID(any())).thenReturn(Flux.empty());
      when(transportService.findByShipmentID(any())).thenReturn(Flux.just(transportTO));

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(shipment));

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                Assertions.assertEquals(1, b.getTransports().size());
                Assertions.assertNotNull(b.getTransports().get(0).getDischargeLocation());
                Assertions.assertNotNull(b.getTransports().get(0).getLoadLocation());
                Assertions.assertEquals(
                    location1.getId(), b.getTransports().get(0).getLoadLocation().getId());
                Assertions.assertEquals(
                    location2.getId(), b.getTransports().get(0).getDischargeLocation().getId());
                Assertions.assertEquals(
                    address.getId(),
                    b.getTransports().get(0).getDischargeLocation().getAddressID());
                Assertions.assertEquals(
                    facility.getFacilityID(),
                    b.getTransports().get(0).getDischargeLocation().getFacilityID());
                Assertions.assertEquals(
                    departureTransportEvent.getEventDateTime(),
                    b.getTransports().get(0).getPlannedDepartureDate());
                Assertions.assertEquals(
                    arrivalTransportEvent.getEventDateTime(),
                    b.getTransports().get(0).getPlannedArrivalDate());
                Assertions.assertEquals(
                    exportVoyage.getCarrierVoyageNumber(),
                    b.getTransports().get(0).getExportVoyageNumber());
                Assertions.assertEquals(
                    voyage.getCarrierVoyageNumber(),
                    b.getTransports().get(0).getImportVoyageNumber());
                Assertions.assertEquals(
                    vessel.getVesselName(), b.getTransports().get(0).getVesselName());
                Assertions.assertEquals(
                    vessel.getVesselIMONumber(), b.getTransports().get(0).getVesselIMONumber());
                Assertions.assertEquals(
                    modeOfTransport.getDcsaTransportType(),
                    b.getTransports().get(0).getModeOfTransport());
                Assertions.assertEquals(
                    shipmentTransport.getIsUnderShippersResponsibility(),
                    b.getTransports().get(0).getIsUnderShippersResponsibility());
                Assertions.assertEquals(
                    shipmentTransport.getTransportPlanStageCode(),
                    b.getTransports().get(0).getTransportPlanStage());
                Assertions.assertEquals(
                    shipmentTransport.getTransportPlanStageSequenceNumber(),
                    b.getTransports().get(0).getTransportPlanStageSequenceNumber());
                Assertions.assertEquals(
                    transport.getTransportName(), b.getTransports().get(0).getTransportName());
                Assertions.assertEquals(
                    transport.getTransportReference(),
                    b.getTransports().get(0).getTransportReference());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return shipment for given carrierBookingRequestReference with everything")
    void testGETShipmentWithEverything() {
      LocationTO locationTO1 = locationMapper.locationToDTO(location1);
      PartyTO partyTO = partyMapper.partyToDTO(party);
      partyTO.setAddress(address);
      partyTO.setPartyContactDetails(
          List.of(
              new PartyContactDetailsTO(
                  partyContactDetails.getName(),
                  partyContactDetails.getEmail(),
                  partyContactDetails.getPhone(),
                  partyContactDetails.getUrl())));

      documentPartyTO.setParty(partyTO);

      shipmentLocationTO =
          shipmentLocationMapper.shipmentLocationToDtoWithLocationTO(shipmentLocation, locationTO1);

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(shipment));
      when(shipmentLocationService.fetchShipmentLocationsByBookingID(any()))
          .thenReturn(Mono.just(List.of(shipmentLocationTO)));
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any()))
          .thenReturn(Flux.just(shipmentCutOffTime));
      when(requestedEquipmentRepository.findByBookingID(any()))
          .thenReturn(Flux.just(confirmedEquipment));
      when(carrierClauseService.fetchCarrierClausesByShipmentID(any()))
          .thenReturn(Flux.just(carrierClauseTO));
      when(chargeService.fetchChargesByShipmentID(any())).thenReturn(Flux.just(chargeTO));

      when(locationService.fetchLocationDeepObjByID(any())).thenReturn(Mono.just(locationTO1));

      when(bookingRepository.findById((UUID) any())).thenReturn(Mono.just(booking));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceService.findByBookingID(any())).thenReturn(Mono.just(List.of(referenceTO)));
      when(documentPartyService.fetchDocumentPartiesByBookingID(any(UUID.class)))
          .thenReturn(Mono.just(List.of(documentPartyTO)));
      when(transportService.findByShipmentID(any())).thenReturn(Flux.just(transportTO));

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                assertEquals(shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                Assertions.assertNotNull(b.getBooking());
                assertEquals(
                    booking.getCarrierBookingRequestReference(),
                    b.getBooking().getCarrierBookingRequestReference());
                Assertions.assertNotNull(b.getBooking().getInvoicePayableAt());
                Assertions.assertNotNull(b.getBooking().getPlaceOfIssue());
                assertEquals(1, b.getConfirmedEquipments().size());
                assertEquals(1, b.getCharges().size());
                assertEquals(1, b.getCarrierClauses().size());
                assertEquals(1, b.getShipmentLocations().size());
                assertEquals(1, b.getShipmentCutOffTimes().size());
                assertEquals(1, b.getBooking().getShipmentLocations().size());
                assertEquals(1, b.getBooking().getCommodities().size());
                assertEquals(1, b.getBooking().getReferences().size());
                assertEquals(1, b.getBooking().getDocumentParties().size());
                assertEquals(
                    1, b.getBooking().getDocumentParties().get(0).getDisplayedAddress().size());
                assertEquals(
                    1,
                    b.getBooking()
                        .getDocumentParties()
                        .get(0)
                        .getParty()
                        .getPartyContactDetails()
                        .size());
                assertEquals(1, b.getBooking().getRequestedEquipments().size());
                assertEquals(1, b.getBooking().getValueAddedServiceRequests().size());
                assertEquals(1, b.getTransports().size());
                Assertions.assertNotNull(b.getBooking().getPlaceOfIssue());
                Assertions.assertNotNull(b.getBooking().getInvoicePayableAt());
                Assertions.assertNotNull(b.getTransports().get(0).getDischargeLocation());
                Assertions.assertNotNull(b.getTransports().get(0).getLoadLocation());
                Assertions.assertNotNull(b.getTransports().get(0).getPlannedDepartureDate());
                Assertions.assertNotNull(b.getTransports().get(0).getPlannedArrivalDate());
                assertEquals(
                    confirmedEquipment.getConfirmedEquipmentSizetype(),
                    b.getConfirmedEquipments().get(0).getConfirmedEquipmentSizetype());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return an error when no shipment is found for given carrierBookingReference")
    void testGETShipmentNoShipmentFound() {

      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.empty());

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No shipment found with carrier booking reference: "
                        + shipment.getCarrierBookingReference(),
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName(
        "Method should return an error when no active shipment is found for given carrierBookingReference")
    void testGETShipmentNoActiveShipmentFound() {
      shipment.setValidUntil(OffsetDateTime.now());
      when(shipmentRepository.findByCarrierBookingReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.just(shipment));

      StepVerifier.create(
              bkgServiceImpl.getShipmentByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "All shipments are inactive, at least one active shipment should be present.",
                    throwable.getMessage());
              })
          .verify();
    }
  }

  @Nested
  @DisplayName("Tests for BKG Cancellation")
  class BookingCancellationTests {

    @Test
    @DisplayName("Cancel of a booking with document status COMP should result in an error")
    void cancelBookingWithInvalidDocumentStatusShouldResultToError() {

      String carrierBookingRequestReference = UUID.randomUUID().toString();
      Booking mockBookingResponse = new Booking();
      mockBookingResponse.setCarrierBookingRequestReference(carrierBookingRequestReference);
      mockBookingResponse.setDocumentStatus(ShipmentEventTypeCode.CANC);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(
              carrierBookingRequestReference))
          .thenReturn(Mono.just(mockBookingResponse));

      Mono<BookingResponseTO> cancelBookingResponse =
          bkgServiceImpl.cancelBookingByCarrierBookingReference(
              carrierBookingRequestReference, bookingCancellationRequestTO);

      StepVerifier.create(cancelBookingResponse)
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "Cannot Cancel Booking that is not in status RECE, PENU, CONF or PENC",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Cancel of a non existent booking should result in an error")
    void cancelNonExistentBookingShouldResultToError() {

      String carrierBookingRequestReference = UUID.randomUUID().toString();

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(any()))
          .thenReturn(Mono.empty());

      Mono<BookingResponseTO> cancelBookingResponse =
          bkgServiceImpl.cancelBookingByCarrierBookingReference(
              carrierBookingRequestReference, bookingCancellationRequestTO);

      StepVerifier.create(cancelBookingResponse)
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals(
                    "No booking found with carrier booking request reference: "
                        + carrierBookingRequestReference,
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Failure of a booking cancellation should result in an error")
    void cancelBookingFailedShouldResultToError() {

      String carrierBookingRequestReference = UUID.randomUUID().toString();
      Booking mockBookingResponse = new Booking();
      mockBookingResponse.setCarrierBookingRequestReference(carrierBookingRequestReference);
      mockBookingResponse.setDocumentStatus(ShipmentEventTypeCode.PENU);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(
              carrierBookingRequestReference))
          .thenReturn(Mono.just(mockBookingResponse));
      when(bookingRepository
              .updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(
                  eq(ShipmentEventTypeCode.CANC), eq(carrierBookingRequestReference), any()))
          .thenReturn(Mono.just(false));

      Mono<BookingResponseTO> cancelBookingResponse =
          bkgServiceImpl.cancelBookingByCarrierBookingReference(
              carrierBookingRequestReference, bookingCancellationRequestTO);

      StepVerifier.create(cancelBookingResponse)
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof ConcreteRequestErrorMessageException);
                assertEquals("Cancellation of booking failed.", throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Cancellation of the booking should result in an updated booking")
    void cancelBookingSuccess() {

      OffsetDateTime now = OffsetDateTime.now().minusSeconds(10);
      String carrierBookingRequestReference = UUID.randomUUID().toString();
      Booking mockBookingResponse = new Booking();
      mockBookingResponse.setCarrierBookingRequestReference(carrierBookingRequestReference);
      mockBookingResponse.setBookingRequestDateTime(now);
      mockBookingResponse.setDocumentStatus(ShipmentEventTypeCode.RECE);

      when(bookingRepository.findByCarrierBookingRequestReferenceAndValidUntilIsNull(
              carrierBookingRequestReference))
          .thenReturn(Mono.just(mockBookingResponse));
      when(bookingRepository
              .updateDocumentStatusAndUpdatedDateTimeForCarrierBookingRequestReference(
                  eq(ShipmentEventTypeCode.CANC), eq(carrierBookingRequestReference), any()))
          .thenReturn(Mono.just(true));
      when(shipmentEventService.create(any())).thenAnswer(i -> Mono.just(i.getArguments()[0]));

      Mono<BookingResponseTO> cancelBookingResponse =
          bkgServiceImpl.cancelBookingByCarrierBookingReference(
              carrierBookingRequestReference, bookingCancellationRequestTO);

      StepVerifier.create(cancelBookingResponse)
          .assertNext(
              b -> {
                assertEquals(carrierBookingRequestReference, b.getCarrierBookingRequestReference());
                assertEquals(ShipmentEventTypeCode.CANC, b.getDocumentStatus());
                Assertions.assertNotNull(b.getBookingRequestCreatedDateTime());
                Assertions.assertNotNull(b.getBookingRequestUpdatedDateTime());
                assertEquals(now, b.getBookingRequestCreatedDateTime());
                Assertions.assertTrue(
                    b.getBookingRequestUpdatedDateTime()
                        .isAfter(b.getBookingRequestCreatedDateTime()));
              })
          .verifyComplete();
    }
  }
}
