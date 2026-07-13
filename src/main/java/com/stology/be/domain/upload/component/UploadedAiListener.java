package com.stology.be.domain.upload.component;


import com.stology.be.domain.upload.dto.res.UploadSseRes;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.domain.upload.event.UploadedEvent;
import com.stology.be.domain.upload.service.AiSummarizeService;
import com.stology.be.domain.upload.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class UploadedAiListener {

    private final AiSummarizeService aiSummarizeServiceService;
    private final SseService sseService;

    /*
    private final AiClient aiClient;
    */

    @Async("aiTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(UploadedEvent event) {
        try {
            // 1. AI 처리 중으로 상태 변경
            aiSummarizeServiceService.changeState(
                    event.studyMaterialId(),
                    DataState.EXTRACTING
            );

            // 2. 처리 중 SSE 전송
            sendStatus(
                    event,
                    DataState.EXTRACTING
            );

            // 3. 실제 AI 서버 요청
            /*
            AiResponse response = aiClient.analyze(
                    event.studyMaterialId()
            );
            */

            /*
             * AI 응답을 DB에 저장하는 로직
             * aiResultService.saveResult(...);
             */

            // 4. 검토 필요 상태로 변경
            aiSummarizeServiceService.changeState(
                    event.studyMaterialId(),
                    DataState.NEEDREVIEW
            );

            // 5. AI 처리 완료 SSE 전송
            sendStatus(
                    event,
                    DataState.NEEDREVIEW
            );

        } catch (Exception exception) {
            handleFailure(event);
        }
    }

    private void handleFailure(UploadedEvent event) {
        try {
            aiSummarizeServiceService.changeState(
                    event.studyMaterialId(),
                    DataState.EXTRACTIONFAILED
            );
        } finally {
            sendStatus(
                    event,
                    DataState.EXTRACTIONFAILED
            );
        }
    }

    private void sendStatus(
            UploadedEvent event,
            DataState dataState
    ) {
        UploadSseRes response =
                new UploadSseRes(
                        event.studyId(),
                        event.studyMaterialId(),
                        event.uploaderMemberId(),
                        event.uploaderName(),
                        event.dataTitle(),
                        event.week(),
                        dataState,
                        event.createdAt()
                );

        sseService.sendToStudy(
                event.studyId(),
                "data-ai-status",
                response
        );
    }
}