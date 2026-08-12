package com.stology.be.domain.study.event;

import com.stology.be.domain.template.service.TemplateActivateService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyCreatedEventListener {

    private final TemplateActivateService templateActivateService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(StudyCreatedEvent event) {
        try {
            templateActivateService.activateTemplate(event.studyId(), event.templateId());
        } catch (Exception e) {
            log.error("템플릿 복제 실패 studyId={}, templateId={}", event.studyId(), event.templateId(), e);
        }
    }
}
