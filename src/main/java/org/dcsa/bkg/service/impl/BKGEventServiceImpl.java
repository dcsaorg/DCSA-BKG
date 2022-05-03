package org.dcsa.bkg.service.impl;

import org.dcsa.bkg.service.BKGEventService;
import org.dcsa.core.events.model.Event;
import org.dcsa.core.events.model.ShipmentEvent;
import org.dcsa.core.events.model.enums.DocumentTypeCode;
import org.dcsa.core.events.model.enums.EventType;
import org.dcsa.core.events.repository.EventRepository;
import org.dcsa.core.events.repository.PendingEventRepository;
import org.dcsa.core.events.service.EquipmentEventService;
import org.dcsa.core.events.service.OperationsEventService;
import org.dcsa.core.events.service.ShipmentEventService;
import org.dcsa.core.events.service.TransportEventService;
import org.dcsa.core.events.service.impl.GenericEventServiceImpl;
import org.dcsa.core.exception.NotFoundException;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

@Service
public class BKGEventServiceImpl extends GenericEventServiceImpl implements BKGEventService {

  private final Set<EventType> SUPPORTED_EVENT_TYPES = Set.of(EventType.SHIPMENT);

  public BKGEventServiceImpl(
      TransportEventService transportEventService,
      EquipmentEventService equipmentEventService,
      ShipmentEventService shipmentEventService,
      OperationsEventService operationsEventService,
      EventRepository eventRepository,
      PendingEventRepository pendingEventRepository) {
    super(
        shipmentEventService,
        transportEventService,
        equipmentEventService,
        operationsEventService,
        eventRepository,
        pendingEventRepository);
  }

  protected Set<EventType> getSupportedEvents() {
    return SUPPORTED_EVENT_TYPES;
  }

  @Override
  public Flux<Event> findAllExtended(ExtendedRequest<Event> extendedRequest) {
    return super.findAllExtended(extendedRequest)
        .concatMap(
            event -> {
              if (event.getEventType() == EventType.SHIPMENT
                  && (((ShipmentEvent) event).getDocumentTypeCode() == DocumentTypeCode.CBR
                      || ((ShipmentEvent) event).getDocumentTypeCode() == DocumentTypeCode.BKG)) {
                return shipmentEventService.loadRelatedEntities((ShipmentEvent) event);
              }
              return Mono.empty();
            });
  }

  @Override
  public Mono<Event> findById(UUID id) {
    return Mono.<Event>empty()
        .switchIfEmpty(getShipmentEventRelatedEntities(id))
        .switchIfEmpty(Mono.error(new NotFoundException("No event was found with id: " + id)));
  }
}
