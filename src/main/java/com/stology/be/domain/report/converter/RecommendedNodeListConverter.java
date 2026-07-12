package com.stology.be.domain.report.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stology.be.domain.report.dto.RecommendedNodeDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class RecommendedNodeListConverter implements AttributeConverter<List<RecommendedNodeDto>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<RecommendedNodeDto> attribute) {
        if (attribute == null) {
            return "[]";
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("RecommendedNodeList JSON writing error", e);
        }
    }

    @Override
    public List<RecommendedNodeDto> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(dbData, new TypeReference<List<RecommendedNodeDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("RecommendedNodeList JSON reading error", e);
        }
    }
}
