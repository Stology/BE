package com.stology.be.domain.report.converter;

import com.stology.be.domain.report.dto.MemberActivityStatisticsDto;
import com.stology.be.domain.report.dto.RecommendedNodeDto;
import com.stology.be.domain.report.dto.WeeklyCoreNodeDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportJsonConvertersTest {

    @Test
    void recommendedNodeConverter_shouldRoundTripJson() {
        RecommendedNodeListConverter converter = new RecommendedNodeListConverter();
        List<RecommendedNodeDto> payload = List.of(new RecommendedNodeDto("node-a", "reason-a", "badge-a"));

        String json = converter.convertToDatabaseColumn(payload);
        List<RecommendedNodeDto> restored = converter.convertToEntityAttribute(json);

        assertNotNull(json);
        assertEquals(1, restored.size());
        assertEquals("node-a", restored.get(0).getNodeName());
        assertEquals("reason-a", restored.get(0).getReason());
        assertEquals("badge-a", restored.get(0).getBadge());
    }

    @Test
    void weeklyCoreNodeConverter_shouldReturnEmptyListForNull() {
        WeeklyCoreNodeListConverter converter = new WeeklyCoreNodeListConverter();

        List<WeeklyCoreNodeDto> restored = converter.convertToEntityAttribute(null);

        assertNotNull(restored);
        assertTrue(restored.isEmpty());
    }

    @Test
    void memberActivityStatisticsConverter_shouldRoundTripJson() {
        MemberActivityStatisticsListConverter converter = new MemberActivityStatisticsListConverter();
        List<MemberActivityStatisticsDto> payload = List.of(
                new MemberActivityStatisticsDto("홍길동", 2, 1, "good")
        );

        String json = converter.convertToDatabaseColumn(payload);
        List<MemberActivityStatisticsDto> restored = converter.convertToEntityAttribute(json);

        assertNotNull(json);
        assertEquals(1, restored.size());
        assertEquals("홍길동", restored.get(0).getMemberName());
        assertEquals(2, restored.get(0).getMaterialUploadCount());
        assertEquals(1, restored.get(0).getQuestionCount());
        assertEquals("good", restored.get(0).getAiFeedback());
    }
}
