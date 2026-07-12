package com.stology.be.domain.report.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stology.be.domain.report.dto.MemberActivityStatisticsDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class MemberActivityStatisticsListConverter implements AttributeConverter<List<MemberActivityStatisticsDto>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<MemberActivityStatisticsDto> attribute) {
        if (attribute == null) {
            return "[]";
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("MemberActivityStatisticsList JSON writing error", e);
        }
    }

    @Override
    public List<MemberActivityStatisticsDto> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(dbData, new TypeReference<List<MemberActivityStatisticsDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("MemberActivityStatisticsList JSON reading error", e);
        }
    }
}
