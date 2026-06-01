package com.example.backend.demand.pattern.observer;

import com.example.backend.common.PageResult;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.dto.DemandCreateDto;
import com.example.backend.demand.dto.DemandDto;
import com.example.backend.demand.dto.DemandSearchCriteria;
import com.example.backend.demand.dto.DemandUpdateDto;
import com.example.backend.demand.service.DemandService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class DemandStatusListenerTest {

    @Test
    void acceptorCancelBeforeDeliveryShouldReopenDemand() {
        RecordingDemandService demandService = new RecordingDemandService();
        DemandStatusListener listener = new DemandStatusListener(demandService);

        listener.onOrderStatusChanged(new OrderStatusChangedEvent(
                1L, 10L, 100L, 200L,
                OrderStatus.ACCEPTED, OrderStatus.CANCELLED, 200L, "cancel"));

        Assertions.assertEquals(DemandStatus.OPEN, demandService.lastStatus().status());
    }

    @Test
    void publisherCancelShouldCancelDemand() {
        RecordingDemandService demandService = new RecordingDemandService();
        DemandStatusListener listener = new DemandStatusListener(demandService);

        listener.onOrderStatusChanged(new OrderStatusChangedEvent(
                1L, 10L, 100L, 200L,
                OrderStatus.ACCEPTED, OrderStatus.CANCELLED, 100L, "cancel"));

        Assertions.assertEquals(DemandStatus.CANCELLED, demandService.lastStatus().status());
    }

    @Test
    void acceptorCancelAfterDeliveryShouldNotReopenDemand() {
        RecordingDemandService demandService = new RecordingDemandService();
        DemandStatusListener listener = new DemandStatusListener(demandService);

        listener.onOrderStatusChanged(new OrderStatusChangedEvent(
                1L, 10L, 100L, 200L,
                OrderStatus.PENDING_CONFIRM, OrderStatus.CANCELLED, 200L, "cancel"));

        Assertions.assertEquals(DemandStatus.CANCELLED, demandService.lastStatus().status());
    }

    static final class RecordingDemandService implements DemandService {
        private final List<RecordedStatus> statuses = new ArrayList<RecordedStatus>();

        @Override
        public DemandDto createDemand(Long userId, DemandCreateDto req) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PageResult<DemandDto> search(DemandSearchCriteria criteria, int page, int size) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DemandDto getDetail(Long demandId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateStatus(Long demandId, DemandStatus status) {
            statuses.add(new RecordedStatus(demandId, status));
        }

        @Override
        public DemandDto update(Long demandId, Long userId, DemandUpdateDto req) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cancel(Long demandId, Long userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(Long demandId, Long userId) {
            throw new UnsupportedOperationException();
        }

        private RecordedStatus lastStatus() {
            return statuses.get(statuses.size() - 1);
        }

        record RecordedStatus(Long demandId, DemandStatus status) {
        }
    }
}
