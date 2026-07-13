package com.stology.be.domain.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stology.be.domain.report.dto.GeminiRequest;
import com.stology.be.domain.report.dto.GeminiResponse;
import com.stology.be.domain.report.dto.MemberActivityStatisticsDto;
import com.stology.be.domain.report.dto.RecommendedNodeDto;
import com.stology.be.domain.report.dto.WeeklyCoreNodeDto;
import com.stology.be.domain.report.entity.Report;
import com.stology.be.domain.study.entity.Study;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public Report generateNewReport(Study study) {
        log.info("Gemini API 실전 통신 시작... (Study ID: {})", study.getId());

        String prompt = createPrompt(study);
        GeminiRequest requestDto = new GeminiRequest(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // URL 파라미터로 API 키 추가
        String requestUrl = geminiApiUrl + "?key=" + geminiApiKey;
        
        HttpEntity<GeminiRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        try {
            log.info("Gemini API 호출 중...");
            ResponseEntity<GeminiResponse> responseEntity = restTemplate.postForEntity(
                    requestUrl,
                    requestEntity,
                    GeminiResponse.class
            );

            GeminiResponse responseDto = responseEntity.getBody();
            if (responseDto == null || responseDto.getExtractText() == null) {
                log.error("Gemini API 응답이 비어있습니다.");
                return createFallbackReport(study);
            }

            String aiResponseText = responseDto.getExtractText();
            log.info("Gemini 응답 수신 성공! 응답 길이: {}", aiResponseText.length());
            
            return parseAiResponseToReport(study, aiResponseText);

        } catch (Exception e) {
            log.error("Gemini API 통신 중 에러 발생: {}", e.getMessage(), e);
            return createFallbackReport(study);
        }
    }

    private String createPrompt(Study study) {
        // TODO: 실제 DB에서 이번 주 생성된 노드 수, 멤버 활동 통계, 핵심 노드 등 데이터를 조회하여 프롬프트에 포함해야 합니다.
        // 현재는 스터디 이름과 설명만 전달하고 있습니다.
        return "당신은 IT 스터디를 분석하고 피드백을 제공하는 AI 멘토입니다.\n" +
               "현재 분석할 스터디의 이름은 '" + study.getName() + "' 이고, 설명은 '" + study.getDescription() + "' 입니다.\n" +
               "이 스터디의 활동 내역을 바탕으로 주간 리포트를 작성해 주세요.\n\n" +
               "반드시 아래의 JSON 포맷으로만 대답하세요. 마크다운 기호(```json)는 제외하고 순수 JSON 문자열만 출력하세요.\n" +
               "{\n" +
               "  \"totalNodeCount\": \"(정수) 누적 전체 노드 개수\",\n" +
               "  \"newActiveNodeCount\": \"(정수) 이번 주 신규 활성화 노드 개수\",\n" +
               "  \"reinforcedNodeCount\": \"(정수) 이번 주 보강된 노드 개수\",\n" +
               "  \"weeklyCoreNodeList\": [\n" +
               "    { \"nodeName\": \"(문자열) 노드 이름\", \"state\": \"(문자열) 신규 또는 보강\", \"materialCount\": \"(정수) 자료 수\" }\n" +
               "  ],\n" +
               "  \"aiReviewContent\": \"(문자열) AI의 분석 멘트 2~3문장\",\n" +
               "  \"recommendedNodeList\": [\n" +
               "    { \"nodeName\": \"(문자열) 추천 노드 이름\", \"reason\": \"(문자열) 추천 이유\", \"badge\": \"(문자열) 놓친/심화/연결 중 하나\" }\n" +
               "  ],\n" +
               "  \"followUpContent\": \"(문자열) 다음 학습을 위한 제안 멘트\",\n" +
               "  \"memberActivityStatisticsList\": [\n" +
               "    { \"memberName\": \"(문자열) 멤버 이름\", \"materialUploadCount\": \"(정수) 업로드 수\", \"questionCount\": \"(정수) 질문 수\", \"aiFeedback\": \"(문자열) 해당 멤버에 대한 한줄 피드백\" }\n" +
               "  ]\n" +
               "}";
    }

    private Report parseAiResponseToReport(Study study, String aiResponseText) {
        try {
            // 마크다운 코드 블록 제거 (혹시나 AI가 넣었을 경우)
            String cleanJson = aiResponseText.replace("```json", "").replace("```", "").trim();
            
            JsonNode rootNode = objectMapper.readTree(cleanJson);
            
            List<WeeklyCoreNodeDto> weeklyList = Collections.emptyList();
            if (rootNode.has("weeklyCoreNodeList")) {
                weeklyList = objectMapper.convertValue(rootNode.get("weeklyCoreNodeList"), new TypeReference<List<WeeklyCoreNodeDto>>() {});
            }

            List<RecommendedNodeDto> recommendedList = Collections.emptyList();
            if (rootNode.has("recommendedNodeList")) {
                recommendedList = objectMapper.convertValue(rootNode.get("recommendedNodeList"), new TypeReference<List<RecommendedNodeDto>>() {});
            }

            List<MemberActivityStatisticsDto> statsList = Collections.emptyList();
            if (rootNode.has("memberActivityStatisticsList")) {
                statsList = objectMapper.convertValue(rootNode.get("memberActivityStatisticsList"), new TypeReference<List<MemberActivityStatisticsDto>>() {});
            }
            
            return Report.builder()
                    .study(study)
                    .totalNodeCount(rootNode.path("totalNodeCount").asInt(0))
                    .newActiveNodeCount(rootNode.path("newActiveNodeCount").asInt(0))
                    .reinforcedNodeCount(rootNode.path("reinforcedNodeCount").asInt(0))
                    .weeklyCoreNodeList(weeklyList)
                    .recommendedNodeList(recommendedList)
                    .memberActivityStatisticsList(statsList)
                    .aiReviewContent(rootNode.path("aiReviewContent").asText("리뷰 내용이 없습니다."))
                    .followUpContent(rootNode.path("followUpContent").asText("제안 내용이 없습니다."))
                    .build();
        } catch (JsonProcessingException e) {
            log.error("AI 응답 JSON 파싱 실패! 응답 원본: {}", aiResponseText);
            return createFallbackReport(study);
        }
    }

    private Report createFallbackReport(Study study) {
        return Report.builder()
                .study(study)
                .totalNodeCount(0)
                .newActiveNodeCount(0)
                .reinforcedNodeCount(0)
                .weeklyCoreNodeList(Collections.emptyList())
                .recommendedNodeList(Collections.emptyList())
                .memberActivityStatisticsList(Collections.emptyList())
                .aiReviewContent("AI 통신에 실패하여 임시 리뷰를 제공합니다.")
                .followUpContent("통신이 복구된 후 다시 리포트를 생성해 보세요.")
                .build();
    }
}
