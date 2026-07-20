package com.stology.be.domain.upload.component;


import com.stology.be.domain.upload.dto.res.UploadSseRes;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.domain.upload.event.UploadedEvent;
import com.stology.be.global.external.ai.AiSummarizeService;
import com.stology.be.domain.upload.service.SseService;
import com.stology.be.global.external.ai.UploadFilePromptBuilder;
import com.stology.be.global.external.ai.dto.AiSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class UploadedAiListener {

    private final AiSummarizeService aiSummarizeService;
    private final SseService sseService;
    private final UploadFilePromptBuilder uploadFilePromptBuilder;

    @Async("aiTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(UploadedEvent event) {
        try {
            // 1. AI 처리 중으로 상태 변경
            aiSummarizeService.changeState(
                    event.studyMaterialId(),
                    DataState.EXTRACTING
            );

            // 2. 처리 중 SSE 전송
            sendStatus(
                    event,
                    DataState.EXTRACTING
            );

            // 3.  유저 프롬프트 만들기
            String userPrompt = uploadFilePromptBuilder.makeUserPrompt(event.studyMaterialId());
            // 4. 시스템 프롬프트 만들기
            String systemPrompt = uploadFilePromptBuilder.makeSystemPrompt(event.studyMaterialId());

            // 5. ai 요청.
            AiSummaryResult result = aiSummarizeService.requestSummary(userPrompt,systemPrompt);

            /* 6.
             * AI 응답을 DB에 저장하는 로직
             * aiResultService.saveResult(...);
             */

            // 4. 검토 필요 상태로 변경
            aiSummarizeService.changeState(
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
            aiSummarizeService.changeState(
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