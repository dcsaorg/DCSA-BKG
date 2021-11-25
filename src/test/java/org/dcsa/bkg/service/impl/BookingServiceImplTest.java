package org.dcsa.bkg.service.impl;

import org.dcsa.bkg.model.mappers.*;
import org.dcsa.bkg.model.transferobjects.*;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.model.transferobjects.LocationTO;
import org.dcsa.core.events.repository.*;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.exception.UpdateException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for BookingService Implementation.")
class BookingServiceImplTest {

  @Mock BookingRepository bookingRepository;
  @Mock ShipmentRepository shipmentRepository;
  @Mock LocationRepository locationRepository;
  @Mock AddressRepository addressRepository;
  @Mock FacilityRepository facilityRepository;
  @Mock CommodityRepository commodityRepository;
  @Mock ValueAddedServiceRequestRepository valueAddedServiceRequestRepository;
  @Mock ReferenceRepository referenceRepository;
  @Mock RequestedEquipmentRepository requestedEquipmentRepository;
  @Mock DocumentPartyRepository documentPartyRepository;
  @Mock PartyRepository partyRepository;
  @Mock DisplayedAddressRepository displayedAddressRepository;
  @Mock PartyContactDetailsRepository partyContactDetailsRepository;
  @Mock PartyIdentifyingCodeRepository partyIdentifyingCodeRepository;
  @Mock ShipmentLocationRepository shipmentLocationRepository;
  @Mock ShipmentCutOffTimeRepository shipmentCutOffTimeRepository;
  @Mock VesselRepository vesselRepository;

  @Mock ShipmentEventService shipmentEventService;

  @InjectMocks BookingServiceImpl bookingServiceImpl;

  @Spy BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);
  @Spy LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);
  @Spy CommodityMapper commodityMapper = Mappers.getMapper(CommodityMapper.class);
  @Spy PartyMapper partyMapper = Mappers.getMapper(PartyMapper.class);
  @Spy ShipmentMapper shipmentMapper = Mappers.getMapper(ShipmentMapper.class);
  @Spy BookingSummaryMapper bookingSummaryMapping = Mappers.getMapper(BookingSummaryMapper.class);
  @Spy ConfirmedEquipmentMapper confirmedEquipmentMapper = Mappers.getMapper(ConfirmedEquipmentMapper.class);

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

  @BeforeEach
  void init() {
    booking = new Booking();
    booking.setId(UUID.randomUUID());
    booking.setCarrierBookingRequestReference("ef223019-ff16-4870-be69-9dbaaaae9b11");
    booking.setInvoicePayableAt("c703277f-84ca-4816-9ccf-fad8e202d3b6");
    booking.setPlaceOfIssueID("7bf6f428-58f0-4347-9ce8-d6be2f5d5745");

    location1 = new Location();
    location1.setId("c703277f-84ca-4816-9ccf-fad8e202d3b6");
    location1.setLocationName("Hamburg");
    location1.setAddressID(UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"));
    location1.setFacilityID(UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"));

    location2 = new Location();
    location2.setId("7bf6f428-58f0-4347-9ce8-d6be2f5d5745");
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

    commodity = new Commodity();
    commodity.setCommodityType("Mobile phones");
    commodity.setCargoGrossWeight(12000.00);
    commodity.setHsCode("720711");
    commodity.setCargoGrossWeightUnit(CargoGrossWeight.KGM);
    commodity.setExportLicenseIssueDate(LocalDate.now());
    commodity.setExportLicenseExpiryDate(LocalDate.now());

    valueAddedServiceRequest = new ValueAddedServiceRequest();
    valueAddedServiceRequest.setValueAddedServiceCode(ValueAddedServiceCode.CDECL);

    reference = new Reference();
    reference.setReferenceValue("test");
    reference.setReferenceType(ReferenceTypeCode.FF);

    requestedEquipment = new RequestedEquipment();
    requestedEquipment.setRequestedEquipmentSizetype("22GP");
    requestedEquipment.setRequestedEquipmentUnits(3);

    confirmedEquipment = new RequestedEquipment();
    confirmedEquipment.setConfirmedEquipmentSizetype("22GP");
    confirmedEquipment.setConfirmedEquipmentUnits(2);

    documentParty = new DocumentParty();
    documentParty.setId(UUID.fromString("3d9542f8-c362-4fa5-8902-90e30d87f1d4"));
    documentParty.setPartyID("d04fb8c6-eb9c-474d-9cf7-86aa6bfcc2a2");
    documentParty.setBookingID(booking.getId());
    documentParty.setPartyFunction(PartyFunction.DDS);

    party = new Party();
    party.setId("a680fe72-503e-40b3-9cfc-dcadafdecf15");
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
  }

  @Nested
  @DisplayName("Tests for the method createBooking(#BookingTO)")
  class CreateBookingTest {

    BookingTO bookingTO;
    LocationTO invoicePayableAt;
    LocationTO placeOfIssue;
    CommodityTO commodityTO;
    ReferenceTO referenceTO;
    ValueAddedServiceRequestTO valueAddedServiceRequestTO;
    RequestedEquipmentTO requestedEquipmentTO;

    @BeforeEach
    void init() {
      bookingTO = new BookingTO();

      invoicePayableAt = new LocationTO();
      invoicePayableAt.setLocationName(location1.getLocationName());

      bookingTO.setInvoicePayableAt(invoicePayableAt);

      placeOfIssue = new LocationTO();
      placeOfIssue.setLocationName(location2.getLocationName());

      bookingTO.setPlaceOfIssue(placeOfIssue);

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
      requestedEquipmentTO.setRequestedEquipmentSizetype(
          requestedEquipment.getRequestedEquipmentSizetype());
      requestedEquipmentTO.setRequestedEquipmentUnits(
          requestedEquipment.getRequestedEquipmentUnits());

      bookingTO.setRequestedEquipments(Collections.singletonList(requestedEquipmentTO));
    }

    @Test
    @DisplayName("Method should save and return shallow booking for given booking request")
    void testCreateBookingShallow() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);
      bookingTO.setInvoicePayableAt(null);
      bookingTO.setPlaceOfIssue(null);
      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));

      StepVerifier.create(bookingServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertNull(b.getInvoicePayableAt());
                Assertions.assertNull(b.getPlaceOfIssue());
                Assertions.assertEquals(0, b.getCommodities().size());
                Assertions.assertEquals(0, b.getValueAddedServiceRequests().size());
                Assertions.assertEquals(0, b.getReferences().size());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should save and return booking with location for given booking request")
    void testCreateBookingWithLocation() {

      bookingTO.setCommodities(null);
      bookingTO.setValueAddedServiceRequests(null);
      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));

      when(locationRepository.save(locationMapper.dtoToLocation(invoicePayableAt)))
          .thenReturn(Mono.just(location1));
      when(locationRepository.save(locationMapper.dtoToLocation(placeOfIssue)))
          .thenReturn(Mono.just(location2));

      StepVerifier.create(bookingServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(0, b.getCommodities().size());
                Assertions.assertEquals(0, b.getValueAddedServiceRequests().size());
                Assertions.assertEquals(0, b.getReferences().size());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
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

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));

      when(locationRepository.save(locationMapper.dtoToLocation(invoicePayableAt)))
          .thenReturn(Mono.just(location1));
      when(locationRepository.save(locationMapper.dtoToLocation(placeOfIssue)))
          .thenReturn(Mono.just(location2));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));

      StepVerifier.create(bookingServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(0, b.getValueAddedServiceRequests().size());
                Assertions.assertEquals(0, b.getReferences().size());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with location, commodities and valueAddedServiceRequests for given booking request")
    void testCreateBookingWithLocationAndCommoditiesAndValAddSerReq() {

      bookingTO.setReferences(null);
      bookingTO.setRequestedEquipments(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));

      when(locationRepository.save(locationMapper.dtoToLocation(invoicePayableAt)))
          .thenReturn(Mono.just(location1));
      when(locationRepository.save(locationMapper.dtoToLocation(placeOfIssue)))
          .thenReturn(Mono.just(location2));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.saveAll(any(Flux.class)))
          .thenReturn(Flux.just(valueAddedServiceRequest));

      StepVerifier.create(bookingServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                Assertions.assertEquals(0, b.getReferences().size());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with location, commodities, valueAddedServiceRequests and references for given booking request")
    void testCreateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferences() {

      bookingTO.setRequestedEquipments(null);

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));

      when(locationRepository.save(locationMapper.dtoToLocation(invoicePayableAt)))
          .thenReturn(Mono.just(location1));
      when(locationRepository.save(locationMapper.dtoToLocation(placeOfIssue)))
          .thenReturn(Mono.just(location2));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.saveAll(any(Flux.class)))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(reference));

      StepVerifier.create(bookingServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                Assertions.assertEquals(
                    ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should save and return booking with location, commodities, valueAddedServiceRequests, references and requestedEquipments for given booking request")
    void testCreateBookingWithLocationAndCommoditiesAndValAddSerReqAndReferencesAndReqEquip() {

      when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
      when(bookingRepository.findById(any(UUID.class))).thenReturn(Mono.just(booking));
      when(bookingRepository.setInvoicePayableAtFor(any(), any())).thenReturn(Mono.just(true));
      when(bookingRepository.setPlaceOfIssueIDFor(any(), any())).thenReturn(Mono.just(true));

      when(locationRepository.save(locationMapper.dtoToLocation(invoicePayableAt)))
          .thenReturn(Mono.just(location1));
      when(locationRepository.save(locationMapper.dtoToLocation(placeOfIssue)))
          .thenReturn(Mono.just(location2));
      when(commodityRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.saveAll(any(Flux.class)))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(reference));
      when(requestedEquipmentRepository.saveAll(any(Flux.class)))
          .thenReturn(Flux.just(requestedEquipment));

      StepVerifier.create(bookingServiceImpl.createBooking(bookingTO))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                Assertions.assertEquals(
                    ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                Assertions.assertEquals(
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizetype());
              })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("Tests for the method getBookingByCarrierBookingRequestReference(#String)")
  class BookingByCarrierBookingRequestReferenceTest {
    @Test
    @DisplayName("Method should return shallow booking for given carrierBookingRequestReference")
    void testGETBookingShallow() {

      booking.setInvoicePayableAt(null);
      booking.setPlaceOfIssueID(null);

      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(booking));

      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(valueAddedServiceRequestRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(referenceRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bookingServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertNull(b.getInvoicePayableAt());
                Assertions.assertNull(b.getInvoicePayableAt());
                Assertions.assertEquals(0, b.getCommodities().size());
                Assertions.assertEquals(0, b.getValueAddedServiceRequests().size());
                Assertions.assertEquals(0, b.getReferences().size());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
                Assertions.assertEquals(0, b.getDocumentParties().size());
                Assertions.assertEquals(0, b.getShipmentLocations().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return shallow booking with shallow location for given carrierBookingRequestReference")
    void testGETBookingShallowWithLocationShallow() {

      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(booking));

      when(locationRepository.findById("c703277f-84ca-4816-9ccf-fad8e202d3b6"))
          .thenReturn(Mono.just(location1));
      when(locationRepository.findById("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"))
          .thenReturn(Mono.just(location2));
      when(addressRepository.findByIdOrEmpty(any())).thenReturn(Mono.empty());
      when(facilityRepository.findByIdOrEmpty(any())).thenReturn(Mono.empty());
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(valueAddedServiceRequestRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(referenceRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bookingServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(0, b.getCommodities().size());
                Assertions.assertEquals(0, b.getValueAddedServiceRequests().size());
                Assertions.assertEquals(0, b.getReferences().size());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
                Assertions.assertEquals(0, b.getDocumentParties().size());
                Assertions.assertEquals(0, b.getShipmentLocations().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location for given carrierBookingRequestReference")
    void testGETBookingShallowWithLocationDeep() {

      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(booking));

      when(locationRepository.findById("c703277f-84ca-4816-9ccf-fad8e202d3b6"))
          .thenReturn(Mono.just(location1));
      when(locationRepository.findById("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"))
          .thenReturn(Mono.just(location2));
      when(addressRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(address));
      when(facilityRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(facility));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(valueAddedServiceRequestRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(referenceRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bookingServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                Assertions.assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                Assertions.assertEquals(0, b.getCommodities().size());
                Assertions.assertEquals(0, b.getValueAddedServiceRequests().size());
                Assertions.assertEquals(0, b.getReferences().size());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
                Assertions.assertEquals(0, b.getDocumentParties().size());
                Assertions.assertEquals(0, b.getShipmentLocations().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities for given carrierBookingRequestReference")
    void testGETBookingShallowWithLocationDeepAndCommodities() {

      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(booking));

      when(locationRepository.findById("c703277f-84ca-4816-9ccf-fad8e202d3b6"))
          .thenReturn(Mono.just(location1));
      when(locationRepository.findById("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"))
          .thenReturn(Mono.just(location2));
      when(addressRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(address));
      when(facilityRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(facility));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(referenceRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bookingServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                Assertions.assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(0, b.getValueAddedServiceRequests().size());
                Assertions.assertEquals(0, b.getReferences().size());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
                Assertions.assertEquals(0, b.getDocumentParties().size());
                Assertions.assertEquals(0, b.getShipmentLocations().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities & valueAddedServiceRequests for given carrierBookingRequestReference")
    void testGETBookingShallowWithLocationDeepAndCommoditiesAndValAddedServ() {

      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(booking));

      when(locationRepository.findById("c703277f-84ca-4816-9ccf-fad8e202d3b6"))
          .thenReturn(Mono.just(location1));
      when(locationRepository.findById("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"))
          .thenReturn(Mono.just(location2));
      when(addressRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(address));
      when(facilityRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(facility));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bookingServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                Assertions.assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                Assertions.assertEquals(0, b.getReferences().size());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
                Assertions.assertEquals(0, b.getDocumentParties().size());
                Assertions.assertEquals(0, b.getShipmentLocations().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities, "
            + "valueAddedServiceRequests, references & for given carrierBookingRequestReference")
    void testGETBookingWithLocationDeepAndCommoditiesAndValAddedServAndRefs() {

      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(booking));

      when(locationRepository.findById("c703277f-84ca-4816-9ccf-fad8e202d3b6"))
          .thenReturn(Mono.just(location1));
      when(locationRepository.findById("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"))
          .thenReturn(Mono.just(location2));
      when(addressRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(address));
      when(facilityRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(facility));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceRepository.findByBookingID(any())).thenReturn(Flux.just(reference));
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(documentPartyRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bookingServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                Assertions.assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                Assertions.assertEquals(
                    ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                Assertions.assertEquals(0, b.getRequestedEquipments().size());
                Assertions.assertEquals(0, b.getDocumentParties().size());
                Assertions.assertEquals(0, b.getShipmentLocations().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities, "
            + "valueAddedServiceRequests, references & requestedEquipments for given carrierBookingRequestReference")
    void testGETBookingWithLocationDeepAndCommoditiesAndValAddedServAndRefsAndReqEqs() {

      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(booking));

      when(locationRepository.findById("c703277f-84ca-4816-9ccf-fad8e202d3b6"))
          .thenReturn(Mono.just(location1));
      when(locationRepository.findById("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"))
          .thenReturn(Mono.just(location2));
      when(addressRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(address));
      when(facilityRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(facility));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceRepository.findByBookingID(any())).thenReturn(Flux.just(reference));
      when(requestedEquipmentRepository.findByBookingID(any()))
          .thenReturn(Flux.just(requestedEquipment));
      when(documentPartyRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bookingServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                Assertions.assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                Assertions.assertEquals(
                    ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                Assertions.assertEquals(
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizetype());
                Assertions.assertEquals(0, b.getDocumentParties().size());
                Assertions.assertEquals(0, b.getShipmentLocations().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName(
        "Method should return booking with deep location, commodities, "
            + "valueAddedServiceRequests, references, requestedEquipments & documentParties for given carrierBookingRequestReference")
    void
        testGETBookingWithLocationDeepAndCommoditiesAndValAddedServAndRefsAndReqEqsAndDocParties() {

      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(booking));

      when(locationRepository.findById("c703277f-84ca-4816-9ccf-fad8e202d3b6"))
          .thenReturn(Mono.just(location1));
      when(locationRepository.findById("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"))
          .thenReturn(Mono.just(location2));
      when(addressRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(address));
      when(facilityRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(facility));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceRepository.findByBookingID(any())).thenReturn(Flux.just(reference));
      when(requestedEquipmentRepository.findByBookingID(any()))
          .thenReturn(Flux.just(requestedEquipment));
      when(documentPartyRepository.findByBookingID(any())).thenReturn(Flux.just(documentParty));
      when(partyRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(party));
      when(partyIdentifyingCodeRepository.findAllByPartyID(any()))
          .thenReturn(Flux.just(partyIdentifyingCode));
      when(partyContactDetailsRepository.findByPartyID(any()))
          .thenReturn(Flux.just(partyContactDetails));
      when(displayedAddressRepository.findByDocumentPartyIDOrderByAddressLineNumber(any()))
          .thenReturn(Flux.just(displayedAddress));
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bookingServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                Assertions.assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                Assertions.assertEquals(
                    ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                Assertions.assertEquals(
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizetype());
                Assertions.assertEquals(1, b.getDocumentParties().size());
                Assertions.assertEquals(
                    "DCSA", b.getDocumentParties().get(0).getParty().getPartyName());
                Assertions.assertEquals(
                    "Denmark", b.getDocumentParties().get(0).getParty().getAddress().getCountry());
                Assertions.assertEquals(
                    "MSK",
                    b.getDocumentParties()
                        .get(0)
                        .getParty()
                        .getIdentifyingCodes()
                        .get(0)
                        .getPartyCode());
                Assertions.assertEquals(
                    "Peanut",
                    b.getDocumentParties().get(0).getPartyContactDetails().get(0).getName());
                Assertions.assertEquals(
                    "Javastraat", b.getDocumentParties().get(0).getDisplayedAddress().get(0));
                Assertions.assertEquals(0, b.getShipmentLocations().size());
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

      when(bookingRepository.findByCarrierBookingRequestReference(any()))
          .thenReturn(Mono.just(booking));

      when(locationRepository.findById("c703277f-84ca-4816-9ccf-fad8e202d3b6"))
          .thenReturn(Mono.just(location1));
      when(locationRepository.findById("7bf6f428-58f0-4347-9ce8-d6be2f5d5745"))
          .thenReturn(Mono.just(location2));
      when(addressRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(address));
      when(facilityRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(facility));
      when(commodityRepository.findByBookingID(any())).thenReturn(Flux.just(commodity));
      when(valueAddedServiceRequestRepository.findByBookingID(any()))
          .thenReturn(Flux.just(valueAddedServiceRequest));
      when(referenceRepository.findByBookingID(any())).thenReturn(Flux.just(reference));
      when(requestedEquipmentRepository.findByBookingID(any()))
          .thenReturn(Flux.just(requestedEquipment));
      when(documentPartyRepository.findByBookingID(any())).thenReturn(Flux.just(documentParty));
      when(partyRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(party));
      when(partyIdentifyingCodeRepository.findAllByPartyID(any()))
          .thenReturn(Flux.just(partyIdentifyingCode));
      when(partyContactDetailsRepository.findByPartyID(any()))
          .thenReturn(Flux.just(partyContactDetails));
      when(displayedAddressRepository.findByDocumentPartyIDOrderByAddressLineNumber(any()))
          .thenReturn(Flux.just(displayedAddress));
      when(shipmentLocationRepository.findByBookingID(any()))
          .thenReturn(Flux.just(shipmentLocation));

      StepVerifier.create(
              bookingServiceImpl.getBookingByCarrierBookingRequestReference(
                  "ef223019-ff16-4870-be69-9dbaaaae9b11"))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    "ef223019-ff16-4870-be69-9dbaaaae9b11", b.getCarrierBookingRequestReference());
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6", b.getInvoicePayableAt().getId());
                Assertions.assertEquals(
                    "7bf6f428-58f0-4347-9ce8-d6be2f5d5745", b.getPlaceOfIssue().getId());
                Assertions.assertEquals(
                    UUID.fromString("8fecc6d0-2a78-401d-948a-b9753f6b53d5"),
                    b.getInvoicePayableAt().getAddress().getId());
                Assertions.assertEquals(
                    UUID.fromString("74dcf8e6-4ed4-439e-a935-ec183df73013"),
                    b.getInvoicePayableAt().getFacility().getFacilityID());
                Assertions.assertEquals(
                    "Mobile phones", b.getCommodities().get(0).getCommodityType());
                Assertions.assertEquals(
                    ValueAddedServiceCode.CDECL,
                    b.getValueAddedServiceRequests().get(0).getValueAddedServiceCode());
                Assertions.assertEquals(
                    ReferenceTypeCode.FF, b.getReferences().get(0).getReferenceType());
                Assertions.assertEquals(
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizetype());
                Assertions.assertEquals(1, b.getDocumentParties().size());
                Assertions.assertEquals(
                    "DCSA", b.getDocumentParties().get(0).getParty().getPartyName());
                Assertions.assertEquals(
                    "Denmark", b.getDocumentParties().get(0).getParty().getAddress().getCountry());
                Assertions.assertEquals(
                    "MSK",
                    b.getDocumentParties()
                        .get(0)
                        .getParty()
                        .getIdentifyingCodes()
                        .get(0)
                        .getPartyCode());
                Assertions.assertEquals(
                    "Peanut",
                    b.getDocumentParties().get(0).getPartyContactDetails().get(0).getName());
                Assertions.assertEquals(
                    "Javastraat", b.getDocumentParties().get(0).getDisplayedAddress().get(0));
                Assertions.assertEquals(
                    "c703277f-84ca-4816-9ccf-fad8e202d3b6",
                    b.getShipmentLocations().get(0).getLocation().getId());
                Assertions.assertEquals(
                    LocationType.FCD,
                    b.getShipmentLocations().get(0).getShipmentLocationTypeCode());
                Assertions.assertEquals(
                    "Singapore", b.getShipmentLocations().get(0).getDisplayedName());
              })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("Tests for the method getBookingConfirmationByCarrierBookingReference(#String)")
  class BookingConfirmationByCarrierBookingReferenceTest {
    @Test
    @DisplayName("Method should return shallow booking for given carrierBookingReference")
    void testGETBookingConfirmationShallow() {

      when(shipmentRepository.findByCarrierBookingReference(any())).thenReturn(Mono.just(shipment));
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());

      StepVerifier.create(
              bookingServiceImpl.getBookingConfirmationByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                Assertions.assertEquals(
                    shipment.getTermsAndConditions(), b.getTermsAndConditions());
                Assertions.assertEquals(
                    shipment.getConfirmationDateTime(), b.getConfirmationDateTime());
                Assertions.assertNull(b.getBooking());
                Assertions.assertEquals(0, b.getShipmentLocations().size());
                Assertions.assertEquals(0, b.getShipmentCutOffTimes().size());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should return shallow booking for given carrierBookingRequestReference")
    void testGETBookingConfirmationWithShipmentLocationsAndShipmentCutOffTimes() {

      when(shipmentRepository.findByCarrierBookingReference(any())).thenReturn(Mono.just(shipment));
      when(shipmentLocationRepository.findByBookingID(any()))
          .thenReturn(Flux.just(shipmentLocation));
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any()))
          .thenReturn(Flux.just(shipmentCutOffTime));
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(locationRepository.findById(shipmentLocation.getLocationID()))
          .thenReturn(Mono.just(location1));
      when(addressRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(address));
      when(facilityRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(facility));

      StepVerifier.create(
              bookingServiceImpl.getBookingConfirmationByCarrierBookingReference(
                  shipment.getCarrierBookingReference()))
          .assertNext(
              b -> {
                Assertions.assertEquals(
                    shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
                Assertions.assertNull(b.getBooking());
                Assertions.assertEquals(1, b.getShipmentLocations().size());
                Assertions.assertEquals(
                    shipmentLocation.getShipmentLocationTypeCode(),
                    b.getShipmentLocations().get(0).getShipmentLocationTypeCode());
                Assertions.assertEquals(
                    shipmentLocation.getDisplayedName(),
                    b.getShipmentLocations().get(0).getDisplayedName());
                Assertions.assertEquals(
                    shipmentLocation.getEventDateTime(),
                    b.getShipmentLocations().get(0).getEventDateTime());
                Assertions.assertEquals(
                    location1.getId(), b.getShipmentLocations().get(0).getLocation().getId());
                Assertions.assertEquals(
                    address.getId(),
                    b.getShipmentLocations().get(0).getLocation().getAddress().getId());
                Assertions.assertEquals(
                    facility.getFacilityID(),
                    b.getShipmentLocations().get(0).getLocation().getFacility().getFacilityID());
              })
          .verifyComplete();
    }

    @Test
    @DisplayName("Method should return confirmed booking for given carrierBookingRequestReference with confirmedEquipment")
    void testGETBookingConfirmationWithConfirmedEquipment() {

      when(shipmentRepository.findByCarrierBookingReference(any())).thenReturn(Mono.just(shipment));
      when(shipmentLocationRepository.findByBookingID(any())).thenReturn(Flux.empty());
      when(shipmentCutOffTimeRepository.findAllByShipmentID(any())).thenReturn(Flux.empty());
      when(requestedEquipmentRepository.findByBookingID(any())).thenReturn(Flux.just(confirmedEquipment));

      StepVerifier.create(
          bookingServiceImpl.getBookingConfirmationByCarrierBookingReference(
            shipment.getCarrierBookingReference()))
        .assertNext(
          b -> {
            Assertions.assertEquals(
              shipment.getCarrierBookingReference(), b.getCarrierBookingReference());
            Assertions.assertNull(b.getBooking());
            Assertions.assertEquals(1, b.getConfirmedEquipments().size());
            Assertions.assertEquals(confirmedEquipment.getConfirmedEquipmentSizetype(), b.getConfirmedEquipments().get(0).getConfirmedEquipmentSizetype());
          })
        .verifyComplete();
    }

  }

  @Nested
  @DisplayName("Tests for BKG Summaries Service")
  class BookingRequestSummariesTest {

    private Booking initializeBookingTestInstance(
        UUID carrierBookingRequestReference, DocumentStatus documentStatus, UUID vesselId) {
      Booking booking = new Booking();
      booking.setCarrierBookingRequestReference(carrierBookingRequestReference.toString());
      booking.setDocumentStatus(documentStatus);
      booking.setCargoMovementTypeAtOrigin(CargoMovementType.FCL);
      booking.setCargoMovementTypeAtDestination(CargoMovementType.FCL);
      booking.setBookingRequestDateTime(OffsetDateTime.now());
      booking.setServiceContractReference("234ase3q4");
      booking.setPaymentTermCode(PaymentTerm.PRE);
      booking.setIsPartialLoadAllowed(true);
      booking.setIsExportDeclarationRequired(true);
      booking.setExportDeclarationReference("ABC123123");
      booking.setIsImportLicenseRequired(true);
      booking.setImportLicenseReference("ABC123123");
      booking.setSubmissionDateTime(OffsetDateTime.now());
      booking.setIsAMSACIFilingRequired(true);
      booking.setIsDestinationFilingRequired(true);
      booking.setContractQuotationReference("DKK");
      booking.setExpectedDepartureDate(LocalDate.now());
      booking.setTransportDocumentTypeCode(TransportDocumentTypeCode.BOL);
      booking.setTransportDocumentReference("ASV23142ASD");
      booking.setBookingChannelReference("ABC12313");
      booking.setIncoTerms(IncoTerms.FCA);
      booking.setCommunicationChannelCode(CommunicationChannel.AO);
      booking.setIsEquipmentSubstitutionAllowed(true);
      booking.setVesselId(vesselId);

      return booking;
    }

    private Vessel initializeVesselTestInstance(UUID vesselId) {
      Vessel vessel = new Vessel();
      vessel.setId(vesselId);
      vessel.setVesselIMONumber("ABC12313");

      return vessel;
    }

    @Test
    @DisplayName(
        "Get booking summaries with carrierBookingRequestReference and DocumentStatus should return valid list of booking request summaries.")
    void
        bookingSummaryRequestWithCarrierBookingRequestReferenceAndDocumentStatusShouldReturnValidBooking() {

      UUID carrierBookingRequestReference = UUID.randomUUID();
      UUID vesselId = UUID.randomUUID();
      DocumentStatus documentStatus = DocumentStatus.APPR;
      PageRequest pageRequest = PageRequest.of(0, 100);

      when(bookingRepository.findAllByCarrierBookingReferenceAndDocumentStatus(
              carrierBookingRequestReference.toString(), documentStatus, pageRequest))
          .thenReturn(
              Flux.just(
                  initializeBookingTestInstance(
                      carrierBookingRequestReference, documentStatus, vesselId)));

      when(vesselRepository.findByIdOrEmpty(vesselId))
          .thenReturn(Mono.just(initializeVesselTestInstance(vesselId)));

      Flux<BookingSummaryTO> bookingToResponse =
          bookingServiceImpl.getBookingRequestSummaries(
              carrierBookingRequestReference.toString(), documentStatus, pageRequest);

      StepVerifier.create(bookingToResponse)
          .assertNext(
              bookingSummaryTO -> {
                Assertions.assertEquals(
                    carrierBookingRequestReference.toString(),
                    bookingSummaryTO.getCarrierBookingRequestReference());
                Assertions.assertEquals(DocumentStatus.APPR, bookingSummaryTO.getDocumentStatus());
                Assertions.assertEquals("ABC12313", bookingSummaryTO.getVesselIMONumber());
              })
          .expectComplete()
          .verify();
    }

    @Test
    @DisplayName(
        "Get booking summaries with carrierBookingRequestReference and DocumentStatus should return valid list of booking request summaries when no vessel can be found.")
    void
        bookingSummaryRequestWithCarrierBookingRequestReferenceAndDocumentStatusNoVesselFoundShouldReturnValidBooking() {

      UUID carrierBookingRequestReference = UUID.randomUUID();
      UUID vesselId = UUID.randomUUID();
      DocumentStatus documentStatus = DocumentStatus.APPR;
      PageRequest pageRequest = PageRequest.of(0, 100);

      when(bookingRepository.findAllByCarrierBookingReferenceAndDocumentStatus(
              carrierBookingRequestReference.toString(), documentStatus, pageRequest))
          .thenReturn(
              Flux.just(
                  initializeBookingTestInstance(
                      carrierBookingRequestReference, documentStatus, vesselId)));

      when(vesselRepository.findByIdOrEmpty(vesselId)).thenReturn(Mono.empty());

      Flux<BookingSummaryTO> bookingToResponse =
          bookingServiceImpl.getBookingRequestSummaries(
              carrierBookingRequestReference.toString(), documentStatus, pageRequest);

      StepVerifier.create(bookingToResponse)
          .assertNext(
              bookingSummaryTO -> {
                Assertions.assertEquals(
                    carrierBookingRequestReference.toString(),
                    bookingSummaryTO.getCarrierBookingRequestReference());
                Assertions.assertEquals(DocumentStatus.APPR, bookingSummaryTO.getDocumentStatus());
              })
          .expectComplete()
          .verify();
    }

    @Test
    @DisplayName(
        "Get booking summaries with carrierBookingRequestReference and DocumentStatus should return valid list of booking request summaries when no vesselId is present.")
    void
        bookingSummaryRequestWithCarrierBookingRequestReferenceAndDocumentStatusNoVesselIdShouldReturnValidBooking() {

      UUID carrierBookingRequestReference = UUID.randomUUID();
      UUID vesselId = null;
      DocumentStatus documentStatus = DocumentStatus.APPR;
      PageRequest pageRequest = PageRequest.of(0, 100);

      when(bookingRepository.findAllByCarrierBookingReferenceAndDocumentStatus(
              carrierBookingRequestReference.toString(), documentStatus, pageRequest))
          .thenReturn(
              Flux.just(
                  initializeBookingTestInstance(
                      carrierBookingRequestReference, documentStatus, vesselId)));

      when(vesselRepository.findByIdOrEmpty(vesselId)).thenReturn(Mono.empty());

      Flux<BookingSummaryTO> bookingToResponse =
          bookingServiceImpl.getBookingRequestSummaries(
              carrierBookingRequestReference.toString(), documentStatus, pageRequest);

      StepVerifier.create(bookingToResponse)
          .assertNext(
              bookingSummaryTO -> {
                Assertions.assertEquals(
                    carrierBookingRequestReference.toString(),
                    bookingSummaryTO.getCarrierBookingRequestReference());
                Assertions.assertEquals(DocumentStatus.APPR, bookingSummaryTO.getDocumentStatus());
              })
          .expectComplete()
          .verify();
    }

    @Test
    @DisplayName(
        "Get booking summaries with carrierBookingRequestReference should return valid list of booking request summaries.")
    void bookingSummaryRequestWithCarrierBookingRequestReferenceShouldReturnValidBooking() {

      UUID carrierBookingRequestReference = UUID.randomUUID();
      UUID vesselId = UUID.randomUUID();
      PageRequest pageRequest = PageRequest.of(0, 100);

      when(bookingRepository.findAllByCarrierBookingReferenceAndDocumentStatus(
              carrierBookingRequestReference.toString(), null, pageRequest))
          .thenReturn(
              Flux.just(
                  initializeBookingTestInstance(carrierBookingRequestReference, null, vesselId)));

      Mockito.when(vesselRepository.findByIdOrEmpty(vesselId))
          .thenReturn(Mono.just(initializeVesselTestInstance(vesselId)));

      Flux<BookingSummaryTO> bookingToResponse =
          bookingServiceImpl.getBookingRequestSummaries(
              carrierBookingRequestReference.toString(), null, pageRequest);

      StepVerifier.create(bookingToResponse)
          .assertNext(
              bookingSummaryTO -> {
                Assertions.assertEquals(
                    carrierBookingRequestReference.toString(),
                    bookingSummaryTO.getCarrierBookingRequestReference());
              })
          .expectComplete()
          .verify();
    }

    @Test
    @DisplayName(
        "Get booking summaries with DocumentStatus should return valid list of booking request summaries.")
    void bookingSummaryRequestWithDocumentStatusShouldReturnValidBooking() {

      UUID carrierBookingRequestReference = UUID.randomUUID();
      UUID vesselId = UUID.randomUUID();
      DocumentStatus documentStatus = DocumentStatus.APPR;
      PageRequest pageRequest = PageRequest.of(0, 100);

      when(bookingRepository.findAllByCarrierBookingReferenceAndDocumentStatus(
              null, documentStatus, pageRequest))
          .thenReturn(
              Flux.just(
                  initializeBookingTestInstance(
                      carrierBookingRequestReference, documentStatus, vesselId)));

      when(vesselRepository.findByIdOrEmpty(vesselId))
          .thenReturn(Mono.just(initializeVesselTestInstance(vesselId)));

      Flux<BookingSummaryTO> bookingToResponse =
          bookingServiceImpl.getBookingRequestSummaries(null, documentStatus, pageRequest);

      StepVerifier.create(bookingToResponse)
          .assertNext(
              bookingSummaryTO -> {
                Assertions.assertEquals(
                    carrierBookingRequestReference.toString(),
                    bookingSummaryTO.getCarrierBookingRequestReference());
                Assertions.assertEquals(DocumentStatus.APPR, bookingSummaryTO.getDocumentStatus());
              })
          .expectComplete()
          .verify();
    }

    @Test
    @DisplayName("Get booking summaries should return valid list of booking request summaries.")
    void bookingSummaryRequestShouldReturnValidBooking() {

      UUID carrierBookingRequestReference = UUID.randomUUID();
      UUID vesselId = UUID.randomUUID();
      DocumentStatus documentStatus = DocumentStatus.APPR;
      PageRequest pageRequest = PageRequest.of(0, 100);

      when(bookingRepository.findAllByCarrierBookingReferenceAndDocumentStatus(
              null, null, pageRequest))
          .thenReturn(
              Flux.just(
                  initializeBookingTestInstance(
                      carrierBookingRequestReference, documentStatus, vesselId)));

      when(vesselRepository.findByIdOrEmpty(vesselId))
          .thenReturn(Mono.just(initializeVesselTestInstance(vesselId)));

      Flux<BookingSummaryTO> bookingToResponse =
          bookingServiceImpl.getBookingRequestSummaries(null, null, pageRequest);

      StepVerifier.create(bookingToResponse)
          .assertNext(
              bookingSummaryTO -> {
                Assertions.assertEquals(
                    carrierBookingRequestReference.toString(),
                    bookingSummaryTO.getCarrierBookingRequestReference());
              })
          .expectComplete()
          .verify();
    }

    @Test
    @DisplayName(
        "Get booking summaries should return empty list of booking request summaries when no booking can be found.")
    void bookingSummaryRequestShouldReturnEmptyWhenNoBookingsFound() {

      UUID carrierBookingRequestReference = UUID.randomUUID();
      PageRequest pageRequest = PageRequest.of(0, 100);

      when(bookingRepository.findAllByCarrierBookingReferenceAndDocumentStatus(
              carrierBookingRequestReference.toString(), null, pageRequest))
          .thenReturn(Flux.empty());

      Flux<BookingSummaryTO> bookingToResponse =
          bookingServiceImpl.getBookingRequestSummaries(
              carrierBookingRequestReference.toString(), null, pageRequest);

      StepVerifier.create(bookingToResponse).expectComplete().verify();
    }
  }

  @Nested
  @DisplayName("Tests for BKG Cancellation")
  class BookingCancellationTests {

    @Test
    @DisplayName("Cancel of a booking with document status PENA should result in an error")
    void cancelBookingWithInvalidDocumentStatusShouldResultToError() {

      String carrierBookingRequestReference = UUID.randomUUID().toString();
      Booking mockBookingResponse = new Booking();
      mockBookingResponse.setCarrierBookingRequestReference(carrierBookingRequestReference);
      mockBookingResponse.setDocumentStatus(DocumentStatus.PENA);

      when(bookingRepository.findByCarrierBookingRequestReference(carrierBookingRequestReference))
          .thenReturn(Mono.just(mockBookingResponse));

      Mono<Void> cancelBookingResponse =
          bookingServiceImpl.cancelBookingByCarrierBookingReference(carrierBookingRequestReference);

      StepVerifier.create(cancelBookingResponse)
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof UpdateException);
                Assertions.assertEquals(
                    "Cannot Cancel Booking that is not in status RECE, PENU or CONF",
                    throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Cancel of a non existent booking should result in an error")
    void cancelNonExistentBookingShouldResultToError() {

      String carrierBookingRequestReference = UUID.randomUUID().toString();

      when(bookingRepository.findByCarrierBookingRequestReference(any())).thenReturn(Mono.empty());

      Mono<Void> cancelBookingResponse =
          bookingServiceImpl.cancelBookingByCarrierBookingReference(carrierBookingRequestReference);

      StepVerifier.create(cancelBookingResponse)
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof UpdateException);
                Assertions.assertEquals(
                    "No Booking found with: ." + carrierBookingRequestReference,
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
      mockBookingResponse.setDocumentStatus(DocumentStatus.PENU);

      when(bookingRepository.findByCarrierBookingRequestReference(carrierBookingRequestReference))
          .thenReturn(Mono.just(mockBookingResponse));
      when(bookingRepository.updateDocumentStatusForCarrierBookingRequestReference(
              DocumentStatus.CANC, carrierBookingRequestReference))
          .thenReturn(Mono.just(false));

      Mono<Void> cancelBookingResponse =
          bookingServiceImpl.cancelBookingByCarrierBookingReference(carrierBookingRequestReference);

      StepVerifier.create(cancelBookingResponse)
          .expectErrorSatisfies(
              throwable -> {
                Assertions.assertTrue(throwable instanceof UpdateException);
                Assertions.assertEquals("Cancellation of booking failed.", throwable.getMessage());
              })
          .verify();
    }

    @Test
    @DisplayName("Cancellation of the booking should result in an updated booking")
    void cancelBookingSuccess() {

      String carrierBookingRequestReference = UUID.randomUUID().toString();
      Booking mockBookingResponse = new Booking();
      mockBookingResponse.setCarrierBookingRequestReference(carrierBookingRequestReference);
      mockBookingResponse.setDocumentStatus(DocumentStatus.RECE);

      when(bookingRepository.findByCarrierBookingRequestReference(carrierBookingRequestReference))
          .thenReturn(Mono.just(mockBookingResponse));
      when(bookingRepository.updateDocumentStatusForCarrierBookingRequestReference(
              DocumentStatus.CANC, carrierBookingRequestReference))
          .thenReturn(Mono.just(true));
      when(shipmentEventService.create(any()))
          .thenAnswer(arguments -> Mono.just(arguments.getArguments()[0]));

      Mono<Void> cancelBookingResponse =
          bookingServiceImpl.cancelBookingByCarrierBookingReference(carrierBookingRequestReference);

      StepVerifier.create(cancelBookingResponse).verifyComplete();

      verify(shipmentEventService).create(any());
    }
  }
}
