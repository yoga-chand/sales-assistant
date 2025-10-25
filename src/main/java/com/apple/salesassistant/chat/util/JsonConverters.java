//package com.apple.salesassistant.chat.util;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.persistence.AttributeConverter;
//
//import java.util.List;
//import java.util.Map;
//
//public final class JsonConverters {
//    private static final ObjectMapper M = new ObjectMapper();
//
//    public static class MapJsonConverter implements AttributeConverter<Map<String,Object>, String> {
//        @Override public String convertToDatabaseColumn(Map<String,Object> attribute) {
//            try { return attribute == null ? null : M.writeValueAsString(attribute); }
//            catch (Exception e) { throw new IllegalStateException("Map->json", e); }
//        }
//        @Override public Map<String,Object> convertToEntityAttribute(String dbData) {
//            try { return dbData == null ? null : M.readValue(dbData, new TypeReference<>(){}); }
//            catch (Exception e) { throw new IllegalStateException("json->Map", e); }
//        }
//    }
//
//    public static class CitationsJsonConverter implements AttributeConverter<List<Citation>, String> {
//        @Override public String convertToDatabaseColumn(List<Citation> attribute) {
//            try { return attribute == null ? null : M.writeValueAsString(attribute); }
//            catch (Exception e) { throw new IllegalStateException("Citations->json", e); }
//        }
//        @Override public List<Citation> convertToEntityAttribute(String dbData) {
//            try { return dbData == null ? null : M.readValue(dbData, new TypeReference<>(){}); }
//            catch (Exception e) { throw new IllegalStateException("json->Citations", e); }
//        }
//    }
//
//    // Simple DTO for citations
//    public static record Citation(String chunkId, String title, List<String> tags) {}
//}
