package com.example.backend.jenkins.job.service;


import com.example.backend.jenkins.job.repository.PipelineRepository;
import com.example.backend.service.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventListener {

    private final HttpClientService httpClientService;
    private final PipelineRepository pipelineRepository;
    private final CompensationService compensationService;

    /**
     * AFTER_COMMIT 단계에서 호출: Jenkins API 요청, 실패 시 보상(compensating) 삭제.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJobCreated(JobEvent.JobCreatedEvent<?> evt) {
        try {
            httpClientService.exchange(evt.getUrl(), evt.getMethod(), evt.getRequestEntity(), evt.getResponseType());
        } catch (Exception e) {
            compensationService.deletePipeline(evt.getPipelineId());
        }
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJobUpdate(JobEvent.JobUpdatedEvent<?> evt) {
        try {
            httpClientService.exchange(evt.getUrl(), evt.getMethod(), evt.getRequestEntity(), evt.getResponseType());
        } catch (Exception e) {
            //TODO update 실패시 전버전으로 롤백
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJobDeleted(JobEvent.JobDeletedEvent<?> evt) {
        try {
            httpClientService.exchange(evt.getUrl(), evt.getMethod(), evt.getRequestEntity(), evt.getResponseType());
        } catch (Exception e) {
            //TODO 삭제 요청 실패시 soft-delete 롤백
            //TODO jenkins 서버에 이미 없는 job일 경우 soft-delete 그대로
        }
    }
}

