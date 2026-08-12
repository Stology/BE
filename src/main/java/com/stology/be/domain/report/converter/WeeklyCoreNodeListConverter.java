package com.stology.be.domain.report.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stology.be.domain.report.dto.WeeklyCoreNodeDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class WeeklyCoreNodeListConverter implements AttributeConverter<List<WeeklyCoreNodeDto>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<WeeklyCoreNodeDto> attribute) {
        if (attribute == null) {
            return "[]";
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("WeeklyCoreNodeList JSON writing error", e);
        }
    }

    @Override
    public List<WeeklyCoreNodeDto> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(dbData, new TypeReference<List<WeeklyCoreNodeDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("WeeklyCoreNodeList JSON reading error", e);
        }
    }
}
