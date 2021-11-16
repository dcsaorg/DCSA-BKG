package org.dcsa.bkg.service;

import org.dcsa.bkg.model.mappers.BookingSummaryMapper;
import org.dcsa.bkg.model.transferobjects.BookingSummaryTO;
import org.dcsa.bkg.service.impl.BookingServiceImpl;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.Vessel;
import org.dcsa.core.events.model.enums.*;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.core.events.repository.VesselRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@DisplayName("Tests for BKG Summaries Service")
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @InjectMocks
    BookingServiceImpl bookingService;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    VesselRepository vesselRepository;

    @Spy
    BookingSummaryMapper bookingSummaryMapping = Mappers.getMapper(BookingSummaryMapper.class);

    private Booking initializeBookingTestInstance(UUID carrierBookingRequestReference, DocumentStatus documentStatus, UUID vesselId) {
        Booking booking = new Booking();
        booking.setCarrierBookingRequestReference(carrierBookingRequestReference.toString());
        booking.setDocumentStatus(documentStatus);
        booking.setReceiptDeliveryTypeAtOrigin(ReceiptDeliveryType.CY);
        booking.setReceiptDeliveryTypeAtOrigin(ReceiptDeliveryType.CY);
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

        return  vessel;
    }

    @Test
    @DisplayName(
            "Get booking summaries with carrierBookingRequestReference and DocumentStatus should return valid list of booking request summaries.")
    void bookingSummaryRequestWithCarrierBookingRequestReferenceAndDocumentStatusShouldReturnValidBooking() {

        ArgumentCaptor<Example> argument = ArgumentCaptor.forClass(Example.class);

        UUID carrierBookingRequestReference = UUID.randomUUID();
        UUID vesselId = UUID.randomUUID();
        DocumentStatus documentStatus = DocumentStatus.APPR;
        PageRequest pageRequest = PageRequest.of(0, 100);

        Mockito.when(bookingRepository.findAllOrderByBookingRequestDateTime(any(), eq(pageRequest)))
                .thenReturn(Flux.just(initializeBookingTestInstance(carrierBookingRequestReference, documentStatus, vesselId)));

        Mockito.when(vesselRepository.findByIdOrEmpty(vesselId)).thenReturn(Mono.just(initializeVesselTestInstance(vesselId)));

        Flux<BookingSummaryTO> bookingToResponse = bookingService.getBookingRequestSummaries(carrierBookingRequestReference.toString(), documentStatus, pageRequest);

        Mockito.verify(bookingRepository).findAllOrderByBookingRequestDateTime(argument.capture(), eq(pageRequest));

        Booking bookingRequest = (Booking) argument.getValue().getProbe();
        assertEquals(bookingRequest.getCarrierBookingRequestReference(), carrierBookingRequestReference.toString());
        assertEquals(bookingRequest.getDocumentStatus(), documentStatus);

        StepVerifier
                .create(bookingToResponse)
                .expectNextMatches(bookingSummaryTO ->
                        bookingSummaryTO.getCarrierBookingRequestReference().equals(carrierBookingRequestReference.toString()) && bookingSummaryTO.getDocumentStatus().equals(DocumentStatus.APPR) && bookingSummaryTO.getVesselIMONumber().equals("ABC12313"))
                .expectComplete()
                .verify();

    }

    @Test
    @DisplayName(
            "Get booking summaries with carrierBookingRequestReference and DocumentStatus should return valid list of booking request summaries when no vessel can be found.")
    void bookingSummaryRequestWithCarrierBookingRequestReferenceAndDocumentStatusNoVesselFoundShouldReturnValidBooking() {

        ArgumentCaptor<Example> argument = ArgumentCaptor.forClass(Example.class);

        UUID carrierBookingRequestReference = UUID.randomUUID();
        UUID vesselId = UUID.randomUUID();
        DocumentStatus documentStatus = DocumentStatus.APPR;
        PageRequest pageRequest = PageRequest.of(0, 100);

        Mockito.when(bookingRepository.findAllOrderByBookingRequestDateTime(any(), eq(pageRequest)))
                .thenReturn(Flux.just(initializeBookingTestInstance(carrierBookingRequestReference, documentStatus, vesselId)));

        Mockito.when(vesselRepository.findByIdOrEmpty(vesselId)).thenReturn(Mono.empty());

        Flux<BookingSummaryTO> bookingToResponse = bookingService.getBookingRequestSummaries(carrierBookingRequestReference.toString(), documentStatus, pageRequest);

        Mockito.verify(bookingRepository).findAllOrderByBookingRequestDateTime(argument.capture(), eq(pageRequest));

        Booking bookingRequest = (Booking) argument.getValue().getProbe();
        assertEquals(bookingRequest.getCarrierBookingRequestReference(), carrierBookingRequestReference.toString());
        assertEquals(bookingRequest.getDocumentStatus(), documentStatus);

        StepVerifier
                .create(bookingToResponse)
                .expectNextMatches(bookingSummaryTO ->
                        bookingSummaryTO.getCarrierBookingRequestReference().equals(carrierBookingRequestReference.toString()) && bookingSummaryTO.getDocumentStatus().equals(DocumentStatus.APPR))
                .expectComplete()
                .verify();

    }

    @Test
    @DisplayName(
            "Get booking summaries with carrierBookingRequestReference and DocumentStatus should return valid list of booking request summaries when no vesselId is present.")
    void bookingSummaryRequestWithCarrierBookingRequestReferenceAndDocumentStatusNoVesselIdShouldReturnValidBooking() {

        ArgumentCaptor<Example> argument = ArgumentCaptor.forClass(Example.class);

        UUID carrierBookingRequestReference = UUID.randomUUID();
        UUID vesselId = null;
        DocumentStatus documentStatus = DocumentStatus.APPR;
        PageRequest pageRequest = PageRequest.of(0, 100);

        Mockito.when(bookingRepository.findAllOrderByBookingRequestDateTime(any(), eq(pageRequest)))
                .thenReturn(Flux.just(initializeBookingTestInstance(carrierBookingRequestReference, documentStatus, vesselId)));

        Mockito.when(vesselRepository.findByIdOrEmpty(vesselId)).thenReturn(Mono.empty());

        Flux<BookingSummaryTO> bookingToResponse = bookingService.getBookingRequestSummaries(carrierBookingRequestReference.toString(), documentStatus, pageRequest);

        Mockito.verify(bookingRepository).findAllOrderByBookingRequestDateTime(argument.capture(), eq(pageRequest));

        Booking bookingRequest = (Booking) argument.getValue().getProbe();
        assertEquals(bookingRequest.getCarrierBookingRequestReference(), carrierBookingRequestReference.toString());
        assertEquals(bookingRequest.getDocumentStatus(), documentStatus);

        StepVerifier
                .create(bookingToResponse)
                .expectNextMatches(bookingSummaryTO ->
                        bookingSummaryTO.getCarrierBookingRequestReference().equals(carrierBookingRequestReference.toString()) && bookingSummaryTO.getDocumentStatus().equals(DocumentStatus.APPR))
                .expectComplete()
                .verify();

    }

    @Test
    @DisplayName(
            "Get booking summaries with carrierBookingRequestReference should return valid list of booking request summaries.")
    void bookingSummaryRequestWithCarrierBookingRequestReferenceShouldReturnValidBooking() {
        ArgumentCaptor<Example> argument = ArgumentCaptor.forClass(Example.class);

        UUID carrierBookingRequestReference = UUID.randomUUID();
        UUID vesselId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 100);

        Mockito.when(bookingRepository.findAllOrderByBookingRequestDateTime(any(), eq(pageRequest)))
                .thenReturn(Flux.just(initializeBookingTestInstance(carrierBookingRequestReference, null, vesselId)));

        Mockito.when(vesselRepository.findByIdOrEmpty(vesselId)).thenReturn(Mono.just(initializeVesselTestInstance(vesselId)));

        Flux<BookingSummaryTO> bookingToResponse = bookingService.getBookingRequestSummaries(carrierBookingRequestReference.toString(), null, pageRequest);

        Mockito.verify(bookingRepository).findAllOrderByBookingRequestDateTime(argument.capture(), eq(pageRequest));

        Booking bookingRequest = (Booking) argument.getValue().getProbe();
        assertEquals(bookingRequest.getCarrierBookingRequestReference(), carrierBookingRequestReference.toString());

        StepVerifier
                .create(bookingToResponse)
                .expectNextMatches(bookingSummaryTO -> bookingSummaryTO.getCarrierBookingRequestReference().equals(carrierBookingRequestReference.toString()))
                .expectComplete()
                .verify();

    }

    @Test
    @DisplayName(
            "Get booking summaries with DocumentStatus should return valid list of booking request summaries.")
    void bookingSummaryRequestWithDocumentStatusShouldReturnValidBooking() {
        ArgumentCaptor<Example> argument = ArgumentCaptor.forClass(Example.class);

        UUID carrierBookingRequestReference = UUID.randomUUID();
        UUID vesselId = UUID.randomUUID();
        DocumentStatus documentStatus = DocumentStatus.APPR;
        PageRequest pageRequest = PageRequest.of(0, 100);

        Mockito.when(bookingRepository.findAllOrderByBookingRequestDateTime(any(), eq(pageRequest)))
                .thenReturn(Flux.just(initializeBookingTestInstance(carrierBookingRequestReference, documentStatus, vesselId)));

        Mockito.when(vesselRepository.findByIdOrEmpty(vesselId)).thenReturn(Mono.just(initializeVesselTestInstance(vesselId)));

        Flux<BookingSummaryTO> bookingToResponse = bookingService.getBookingRequestSummaries(null, documentStatus, pageRequest);

        Mockito.verify(bookingRepository).findAllOrderByBookingRequestDateTime(argument.capture(), eq(pageRequest));

        Booking bookingRequest = (Booking) argument.getValue().getProbe();
        assertEquals(bookingRequest.getDocumentStatus(), documentStatus);

        StepVerifier
                .create(bookingToResponse)
                .expectNextMatches(bookingSummaryTO -> bookingSummaryTO.getCarrierBookingRequestReference().equals(carrierBookingRequestReference.toString()) && bookingSummaryTO.getDocumentStatus().equals(DocumentStatus.APPR))
                .expectComplete()
                .verify();

    }

    @Test
    @DisplayName(
            "Get booking summaries should return valid list of booking request summaries.")
    void bookingSummaryRequestShouldReturnValidBooking() {
        ArgumentCaptor<Example> argument = ArgumentCaptor.forClass(Example.class);

        UUID carrierBookingRequestReference = UUID.randomUUID();
        UUID vesselId = UUID.randomUUID();
        DocumentStatus documentStatus = DocumentStatus.APPR;
        PageRequest pageRequest = PageRequest.of(0, 100);

        Mockito.when(bookingRepository.findAllOrderByBookingRequestDateTime(any(), eq(pageRequest)))
                .thenReturn(Flux.just(initializeBookingTestInstance(carrierBookingRequestReference, documentStatus, vesselId)));

        Mockito.when(vesselRepository.findByIdOrEmpty(vesselId)).thenReturn(Mono.just(initializeVesselTestInstance(vesselId)));

        Flux<BookingSummaryTO> bookingToResponse = bookingService.getBookingRequestSummaries(null, null, pageRequest);

        Mockito.verify(bookingRepository).findAllOrderByBookingRequestDateTime(argument.capture(), eq(pageRequest));

        StepVerifier
                .create(bookingToResponse)
                .expectNextMatches(bookingSummaryTO -> bookingSummaryTO.getCarrierBookingRequestReference().equals(carrierBookingRequestReference.toString()))
                .expectComplete()
                .verify();

    }

}
