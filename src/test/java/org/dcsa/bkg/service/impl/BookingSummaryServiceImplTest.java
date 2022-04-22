package org.dcsa.bkg.service.impl;

import org.dcsa.bkg.model.mappers.BookingSummaryMapper;
import org.dcsa.core.events.model.Booking;
import org.dcsa.core.events.model.enums.ShipmentEventTypeCode;
import org.dcsa.core.events.repository.BookingRepository;
import org.dcsa.skernel.model.Vessel;
import org.dcsa.skernel.repositority.VesselRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for BookingSummary service Implementation.")
class BookingSummaryServiceImplTest {

  @Mock BookingRepository bookingRepository;

  @Mock VesselRepository vesselRepository;

  private BookingSummaryServiceImpl service;
  private BookingSummaryMapper bookingMapper = Mappers.getMapper(BookingSummaryMapper.class);
  private Booking booking;
  private Vessel vessel;

  @BeforeEach
  void init() {
    service = new BookingSummaryServiceImpl(bookingRepository, vesselRepository, bookingMapper);

    UUID vesselId = UUID.randomUUID();

    booking = new Booking();
    booking.setBookingRequestDateTime(OffsetDateTime.now());
    booking.setUpdatedDateTime(OffsetDateTime.now());
    booking.setCarrierBookingRequestReference("CBR1");
    booking.setDocumentStatus(ShipmentEventTypeCode.RECE);
    booking.setId(vesselId);

    vessel = new Vessel();
    vessel.setId(vesselId);
    vessel.setVesselIMONumber("IMONum");
    vessel.setVesselFlag("Flag");
  }

  @Test
  @DisplayName("Test with bookings found with vessel found should return booking with vessel")
  void testBookingSummaryWithVessel() {
    when(vesselRepository.findByIdOrEmpty(any())).thenReturn(Mono.just(vessel));

    StepVerifier.create(service.bulkMapDM2TO(Flux.just(booking)))
        .assertNext(
            bookingSummaryTO -> {
              assertEquals(vessel.getVesselIMONumber(), bookingSummaryTO.getVesselIMONumber());
              assertEquals(
                  booking.getCarrierBookingRequestReference(),
                  bookingSummaryTO.getCarrierBookingRequestReference());
            })
        .verifyComplete();
  }

  @Test
  @DisplayName(
      "Test with bookings found without vessels should return bookingSummary without vesselIMONumber")
  void testBookingSummaryWithoutVessel() {
    when(vesselRepository.findByIdOrEmpty(any())).thenReturn(Mono.empty());

    StepVerifier.create(service.bulkMapDM2TO(Flux.just(booking)))
        .assertNext(
            bookingSummaryTO -> {
              assertNull(bookingSummaryTO.getVesselIMONumber());
              assertEquals(
                  booking.getCarrierBookingRequestReference(),
                  bookingSummaryTO.getCarrierBookingRequestReference());
            })
        .verifyComplete();
  }

  @Test
  @DisplayName("Test without bookings found should return an empty Flux")
  void testNoBookingsFound() {
    StepVerifier.create(service.bulkMapDM2TO(Flux.empty())).verifyComplete();
  }
}
