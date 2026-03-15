package com.chaing.api.facade.transport.scheduler;

import com.chaing.api.facade.transport.TransportManagementFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransportBatchScheduler {

    private final TransportManagementFacade transportManagementFacade;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkExpiredContracts() {
        transportManagementFacade.processExpiredContracts();
    }
}
