package org.dcsa.bkg.model.transferobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dcsa.core.events.model.enums.CargoGrossWeight;
import org.dcsa.core.events.model.enums.CargoMovementType;
import org.dcsa.core.events.model.enums.CommunicationChannel;
import org.dcsa.core.events.model.enums.ReceiptDeliveryType;
import org.dcsa.core.exception.InvalidParameterException;
import org.junit.jupiter.api.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;

@DisplayName("Tests for BookingTO")
class BookingTOTest {

  private Validator validator;
  private ObjectMapper objectMapper;
  private BookingTO validBookingTO;

  @BeforeEach
  void init() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    // replicating default spring boot config for object mapper
    // for reference
    // https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/jackson/JacksonAutoConfiguration.java
    objectMapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    validBookingTO = new BookingTO();
    validBookingTO.setReceiptDeliveryTypeAtOrigin(ReceiptDeliveryType.CY);
    validBookingTO.setDeliveryTypeAtDestination(ReceiptDeliveryType.SD);
    validBookingTO.setCargoMovementTypeAtOrigin(CargoMovementType.FCL);
    validBookingTO.setCargoMovementTypeAtDestination(CargoMovementType.LCL);
    validBookingTO.setServiceContractReference("x".repeat(30));
    validBookingTO.setCommunicationChannel(CommunicationChannel.AO);
    CommodityTO commodityTO = new CommodityTO();
    commodityTO.setCommodityType("x".repeat(20));
    commodityTO.setHsCode("x".repeat(10));
    commodityTO.setCargoGrossWeight(12.12);
    commodityTO.setCargoGrossWeightUnit(CargoGrossWeight.KGM);
    validBookingTO.setCommodities(Collections.singletonList(commodityTO));
  }

  @Test
  @DisplayName("BookingTO should not throw error for valid TO.")
  void testToVerifyNoErrorIsThrowForValidTo() {
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("BookingTO should throw error if receiptDeliveryTypeAtOrigin is not set.")
  void testToVerifyReceiptDeliveryTypeAtOriginIsRequired() {
    validBookingTO.setReceiptDeliveryTypeAtOrigin(null);
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "ReceiptDeliveryTypeAtOrigin is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if deliveryTypeAtDestination is not set.")
  void testToVerifyDeliveryTypeAtDestinationIsRequired() {
    validBookingTO.setDeliveryTypeAtDestination(null);
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "DeliveryTypeAtDestination is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if cargoMovementTypeAtOrigin is not set.")
  void testToVerifyCargoMovementTypeAtOriginIsRequired() {
    validBookingTO.setCargoMovementTypeAtOrigin(null);
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "CargoMovementTypeAtOrigin is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if cargoMovementTypeAtDestination is not set.")
  void testToVerifyCargoMovementTypeAtDestinationAtOriginIsRequired() {
    validBookingTO.setCargoMovementTypeAtDestination(null);
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "CargoMovementTypeAtDestination is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if ServiceContractReference is not set.")
  void testToVerifyServiceContractReferenceIsRequired() {
    validBookingTO.setServiceContractReference(null);
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "ServiceContractReference is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "BookingTO should throw error if ServiceContractReference length exceeds max size of 30.")
  void testToVerifyServiceContractReferenceDoesNotExceedALengthOf30() {
    validBookingTO.setServiceContractReference("x".repeat(31));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(
                v -> "ServiceContractReference has a max size of 30.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "BookingTO should throw error if ImportLicenseReference length exceeds max size of 35.")
  void testToVerifyImportLicenseReferenceDoesNotExceedALengthOf35() {
    validBookingTO.setImportLicenseReference("x".repeat(36));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "ImportLicenseReference has a max size of 35.".equals(v.getMessage())));
  }

  // Disabled as simple date format only supports upto millisecond resolution, That means up to 3
  // digits of a decimal fraction of second.
  // Need to find another way to test the format, as of now its assumed to return
  // yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZZ by default.
  @Disabled
  @Test
  @DisplayName(
      "BookingTO should return yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZZ date format for submissionDateTime.")
  void testToCheckISODateFormatForSubmissionDateTime() throws JsonProcessingException {
    validBookingTO.setSubmissionDateTime(OffsetDateTime.now());
    JsonNode object = objectMapper.readTree(objectMapper.writeValueAsString(validBookingTO));
    String submissionDateTime = object.get("submissionDateTime").asText();
    SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZZ");
    sdfrmt.setLenient(false);
    Assertions.assertDoesNotThrow(
        () -> {
          sdfrmt.parse(submissionDateTime);
        });
  }

  @Test
  @DisplayName(
      "BookingTO should throw error if ContractQuotationReference length exceeds max size of 35.")
  void testToVerifyContractQuotationReferenceDoesNotExceedALengthOf35() {
    validBookingTO.setContractQuotationReference("x".repeat(36));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(
                v -> "ContractQuotationReference has a max size of 35.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should return yyyy-MM-dd date format for expectedDepartureDate.")
  void testToCheckISODateFormatForExpectedDepartureDate() throws JsonProcessingException {
    validBookingTO.setExpectedDepartureDate(LocalDate.now());
    JsonNode object = objectMapper.readTree(objectMapper.writeValueAsString(validBookingTO));
    String expectedDepartureDate = object.get("expectedDepartureDate").asText();
    SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
    sdfrmt.setLenient(false);
    Assertions.assertDoesNotThrow(
        () -> {
          sdfrmt.parse(expectedDepartureDate);
        });
  }

  @Test
  @DisplayName(
      "BookingTO should throw error if transportDocumentReference length exceeds max size of 20.")
  void testToVerifyTransportDocumentReferenceDoesNotExceedALengthOf20() {
    validBookingTO.setTransportDocumentReference("x".repeat(21));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(
                v -> "TransportDocumentReference has a max size of 20.".equals(v.getMessage())));
  }

  @Test
  @DisplayName(
      "BookingTO should throw error if bookingChannelReference length exceeds max size of 20.")
  void testToVerifyBookingChannelReferenceDoesNotExceedALengthOf20() {
    validBookingTO.setBookingChannelReference("x".repeat(21));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "BookingChannelReference has a max size of 20.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if communicationChannel is not set.")
  void testToVerifyCommunicationChannelIsRequired() {
    validBookingTO.setCommunicationChannel(null);
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "CommunicationChannel is required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if vesselName length exceeds max size of 35.")
  void testToVerifyVesselNameDoesNotExceedALengthOf35() {
    validBookingTO.setVesselName("x".repeat(36));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "VesselName has a max size of 35.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if vesselIMONumber is invalid.")
  void testToVerifyVesselIMONumberIsInvalid() {
    validBookingTO.setVesselIMONumber("123456");
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "VesselIMONumber is invalid.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if carrierVoyageNumber length exceeds max size of 50.")
  void testToVerifyCarrierVoyageNumberDoesNotExceedALengthOf50() {
    validBookingTO.setCarrierVoyageNumber("0".repeat(51));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream()
            .anyMatch(v -> "CarrierVoyageNumber has a max size of 50.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if Commodities is not set.")
  void testToVerifyCommoditiesIsRequired() {
    validBookingTO.setCommodities(null);
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Commodities are required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if Commodities is an empty list.")
  void testToVerifyCommoditiesCannotBeAnEmptyList() {
    validBookingTO.setCommodities(Collections.emptyList());
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.stream().anyMatch(v -> "Commodities are required.".equals(v.getMessage())));
  }

  @Test
  @DisplayName("BookingTO should throw error if Commodities list contains invalid commodity.")
  void testToVerifyCommoditiesCannotHaveInvalidCommodity() {
    validBookingTO.setCommodities(Collections.singletonList(new CommodityTO()));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.size() > 0
            && violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("commodities")));
  }

  @Test
  @DisplayName(
      "BookingTO should throw error if valueAddedServiceRequests list contains invalid ValueAddedServiceRequestTO.")
  void testToVerifyValueAddedServiceRequestsCannotHaveInvalidValueAddedServiceRequest() {
    validBookingTO.setValueAddedServiceRequests(
        Collections.singletonList(new ValueAddedServiceRequestTO()));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.size() > 0
            && violations.stream()
                .anyMatch(
                    v -> v.getPropertyPath().toString().contains("valueAddedServiceRequests")));
  }

  @Test
  @DisplayName("BookingTO should throw error if references list contains invalid reference.")
  void testToVerifyReferencesCannotHaveInvalidReference() {
    validBookingTO.setReferences(Collections.singletonList(new ReferenceTO()));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.size() > 0
            && violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("references")));
  }

  @Test
  @DisplayName(
      "BookingTO should throw error if requestedEquipments list contains invalid requestedEquipment.")
  void testToVerifyRequestedEquipmentsCannotHaveInvalidRequestedEquipment() {
    validBookingTO.setRequestedEquipments(Collections.singletonList(new RequestedEquipmentTO()));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.size() > 0
            && violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("requestedEquipments")));
  }

  @Test
  @DisplayName(
      "BookingTO should throw error if documentParties list contains invalid documentParty.")
  void testToVerifyDocumentPartiesCannotHaveInvalidDocumentParty() {
    // TODO: Create custom validator for length of strings in a list
    // maybe create a custom validation annotation for checking length of strings in a list
    // so that the spring validator can be invoked
    DocumentPartyTO documentPartyTO = new DocumentPartyTO();
    Exception exception =
            Assertions.assertThrows(
                    InvalidParameterException.class,
                    () ->
                            documentPartyTO.setDisplayedAddress(
                                    Collections.singletonList("x".repeat(251))));
    Assertions.assertEquals(
            "A single displayedAddress has a max size of 250.", exception.getMessage());
//    validBookingTO.setDocumentParties(Collections.singletonList(documentPartyTO));
//    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
//    Assertions.assertTrue(
//        violations.size() > 0
//            && violations.stream()
//                .anyMatch(v -> v.getPropertyPath().toString().contains("documentParties")));
  }

  @Test
  @DisplayName(
      "BookingTO should throw error if shipmentLocations list contains invalid shipmentLocation.")
  void testToVerifyShipmentLocationsCannotHaveInvalidShipmentLocations() {
    validBookingTO.setShipmentLocations(Collections.singletonList(new ShipmentLocationTO()));
    Set<ConstraintViolation<BookingTO>> violations = validator.validate(validBookingTO);
    Assertions.assertTrue(
        violations.size() > 0
            && violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("shipmentLocations")));
  }
}
