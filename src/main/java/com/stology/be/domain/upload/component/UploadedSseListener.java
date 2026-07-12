package com.stology.be.domain.upload.component;

import com.stology.be.domain.upload.dto.res.UploadSseRes;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.domain.upload.event.UploadedEvent;
import com.stology.be.domain.upload.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UploadedSseListener {

    private final SseService sseService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(UploadedEvent event) {

        UploadSseRes response =
                new UploadSseRes(
                        event.studyId(),
                        event.uploaderMemberId(),
                        event.uploaderName(),
                        event.dataTitle(),
                        event.week(),
                        DataState.READY,
                        event.createdAt()
                );

        sseService.sendToStudy(
                event.studyId(),
                "data-upload-status",
                response
        );
    }
}