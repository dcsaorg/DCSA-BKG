package org.dcsa.bkg.repository;

import org.dcsa.bkg.model.transferobjects.BookingConfirmationSummaryTO;
import org.dcsa.core.repository.ExtendedRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface BKGConfirmationSummaryTORepository extends ExtendedRepository<BookingConfirmationSummaryTO, UUID> {

}
