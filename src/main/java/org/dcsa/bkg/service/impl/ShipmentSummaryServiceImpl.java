package org.dcsa.bkg.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.bkg.model.transferobjects.ShipmentSummaryTO;
import org.dcsa.bkg.repository.ShipmentSummaryRepository;
import org.dcsa.core.service.impl.QueryServiceImpl;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentSummaryServiceImpl
    extends QueryServiceImpl<ShipmentSummaryRepository, ShipmentSummaryTO, UUID> {

  private final ShipmentSummaryRepository repository;

  @Override
  protected ShipmentSummaryRepository getRepository() {
    return repository;
  }
}
