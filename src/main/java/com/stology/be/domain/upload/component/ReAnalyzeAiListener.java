package com.stology.be.domain.upload.component;

import com.stology.be.domain.upload.dto.res.ReTaskSseRes;
import com.stology.be.domain.upload.dto.res.UploadSseRes;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.domain.upload.event.ReTaskEvent;
import com.stology.be.domain.upload.service.SseService;
import com.stology.be.domain.upload.service.SummarySaveService;
import com.stology.be.global.external.ai.AiSummarizeService;
import com.stology.be.global.external.ai.UploadFilePromptBuilder;
import com.stology.be.global.external.ai.dto.AiSummaryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReAnalyzeAiListener {


    private final AiSummarizeService aiSummarizeService;
    private final SseService sseService;
    private final UploadFilePromptBuilder uploadFilePromptBuilder;
    private final SummarySaveService summarySaveService;

    @Async("aiTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(ReTaskEvent event) {
        try {

            // 2. 처리 중 SSE 전송
            sendStatus(
                    event,
                    DataState.EXTRACTING
            );

            // 3.  유저 프롬프트 만들기
            String userPrompt = uploadFilePromptBuilder.makeUserPrompt(event.studyMaterialId());
            // 4. 시스템 프롬프트 만들기
            String systemPrompt = uploadFilePromptBuilder.makeSystemPrompt(event.studyId());

            // 5. ai 요청.
            AiSummaryResult result = aiSummarizeService.requestSummary(userPrompt,systemPrompt);

            //6. AI 응답을 DB에 저장하는 로직
            summarySaveService.saveResult(event.studyMaterialId(), result);

            // 7. 검토 필요 상태로 변경
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
            log.error(
                    "업로드 자료 AI 재추출 실패. studyMaterialId={}",
                    event.studyMaterialId(),
                    exception
            );
            handleFailure(event);
        }
    }

    private void handleFailure(ReTaskEvent event) {
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
            ReTaskEvent event,
            DataState dataState
    ) {
        ReTaskSseRes response =
                ReTaskSseRes.builder()
                        .studyId(event.studyId())
                        .studyMaterialId(event.studyMaterialId())
                        .uploaderMemberId(event.uploaderMemberId())
                        .dataState(dataState)
                        .build();


        sseService.sendToStudy(
                event.studyId(),
                "data-ai-status",
                response
        );
    }
}
