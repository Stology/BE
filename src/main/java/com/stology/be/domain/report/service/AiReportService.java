package com.stology.be.domain.report.service;

import com.stology.be.domain.report.dto.AiReportOutputDto;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.global.apiPayload.code.GeneralErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiReportService {

    private final ChatClient chatClient;

    public AiReportService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public AiReportOutputDto generateNewReport(Study study, String dbStatsContent) {
        String systemPrompt = "당신은 IT 스터디를 분석하고 피드백을 제공하는 AI 멘토입니다.\n" +
                "모든 응답 내용은 반드시 '한국어(Korean)'로만 작성해야 합니다.\n" +
                "제공된 데이터를 기반으로 아래 조건에 맞게 분석 리포트를 작성해주세요.\n\n" +
                "[분석 조건]\n" +
                "1. 진행 상황 요약 (aiReviewContent): 스터디의 전반적인 활동량과 다루어진 주제를 바탕으로 2~3문장으로 짧게 요약.\n" +
                "2. 노드 추천 (recommendedNodeList):\n" +
                "   - 다음 3가지 카테고리 중 적절한 것을 골라 추천합니다: '놓친' (자료가 부족하거나 빠진 개념), '심화' (더 깊게 공부해야 할 내용), '연결' (현재 개념과 이어지는 다음 개념).\n" +
                "   - 동일한 노드라도 추천 사유가 다르면 중복해서 배열에 담을 수 있습니다 (개수 제한 없음).\n" +
                "   - 'badge' 필드는 반드시 '놓친', '심화', '연결' 중 하나여야 합니다.\n" +
                "3. 팀원 개별 코멘트 (memberActivityStatisticsList):\n" +
                "   - 멤버별로 제공된 '자료 업로드 개수'와 '질문 개수' 데이터(숫자)만을 근거로 짧은 1:1 격려/조언 코멘트를 작성하세요.\n" +
                "   - 구체적인 자료의 내용은 코멘트에 언급하지 마세요.\n";

        String userPrompt = "스터디 이름: {studyName}\n" +
                "스터디 설명: {studyDesc}\n" +
                "---------------------\n" +
                "{dbStatsContent}";

        BeanOutputConverter<AiReportOutputDto> converter = new BeanOutputConverter<>(AiReportOutputDto.class);
        String format = converter.getFormat();

        PromptTemplate template = new PromptTemplate(userPrompt);
        template.add("studyName", study.getName());
        template.add("studyDesc", study.getDescription());
        template.add("dbStatsContent", dbStatsContent);
        
        String fullSystemPrompt = systemPrompt + "\n" + format;

        try {
            return chatClient.prompt()
                    .system(fullSystemPrompt)
                    .user(template.render())
                    .call()
                    .entity(converter);

        } catch (Exception e) {
            log.error("AI Report generation failed", e);
            throw new com.stology.be.domain.report.exception.ReportException(com.stology.be.domain.report.exception.ReportErrorCode.REPORT_GENERATION_FAILED);
        }
    }
}
