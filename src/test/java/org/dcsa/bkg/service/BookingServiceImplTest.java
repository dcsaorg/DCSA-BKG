package org.dcsa.bkg.service;

import org.dcsa.bkg.model.mappers.BookingMapper;
import org.dcsa.bkg.model.mappers.CommodityMapper;
import org.dcsa.bkg.model.mappers.LocationMapper;
import org.dcsa.bkg.model.mappers.PartyMapper;
import org.dcsa.bkg.service.impl.BookingServiceImpl;
import org.dcsa.core.events.model.PartyContactDetails;
import org.dcsa.core.events.model.ShipmentLocation;
import org.dcsa.core.events.model.*;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for BookingService Implementation.")
class BookingServiceImplTest {

  @Mock BookingRepository bookingRepository;
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

  @InjectMocks BookingServiceImpl bookingServiceImpl;

  @Spy BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);
  @Spy LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);
  @Spy CommodityMapper commodityMapper = Mappers.getMapper(CommodityMapper.class);
  @Spy PartyMapper partyMapper = Mappers.getMapper(PartyMapper.class);

  Booking booking;
  Location location1;
  Location location2;
  Address address;
  Facility facility;
  Commodity commodity;
  ValueAddedServiceRequest valueAddedServiceRequest;
  Reference reference;
  RequestedEquipment requestedEquipment;
  DocumentParty documentParty;
  Party party;
  PartyIdentifyingCode partyIdentifyingCode;
  DisplayedAddress displayedAddress;
  PartyContactDetails partyContactDetails;
  ShipmentLocation shipmentLocation;

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

    location2 = new Location();
    location2.setId("7bf6f428-58f0-4347-9ce8-d6be2f5d5745");
    location2.setLocationName("Singapore");

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
    requestedEquipment.setRequestedEquipmentType("22GP");
    requestedEquipment.setRequestedEquipmentUnits(3);

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

    shipmentLocation = new ShipmentLocation();
    shipmentLocation.setLocationID(location1.getId());
    shipmentLocation.setBookingID(booking.getId());
    shipmentLocation.setShipmentLocationTypeCode(LocationType.FCD);
    shipmentLocation.setDisplayedName("Singapore");
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
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizeType());
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
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizeType());
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
                    "22GP", b.getRequestedEquipments().get(0).getRequestedEquipmentSizeType());
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
                    LocationType.FCD, b.getShipmentLocations().get(0).getShipmentLocationTypeCode());
                Assertions.assertEquals(
                    "Singapore", b.getShipmentLocations().get(0).getDisplayedName());
              })
          .verifyComplete();
    }
  }
}
