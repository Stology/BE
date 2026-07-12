package com.stology.be.domain.upload.component;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*

구조
studyId
  └─ memberId
       └─ emitterId
            └─ SseEmitter
스터디 ID -> 맴버 ID -> 이미터 ID -> 해당 객체.
memberId 하나당 브라우저 탭이나 기기가 여러 개일 수 있기에 고려 힌 구조.

 */


@Component
public class SseEmitterRepository {

    private final Map<Long, Map<String, SseEmitter>> emitters =
            new ConcurrentHashMap<>();

    public void save(
            Long studyId,
            String emitterId,
            SseEmitter emitter
    ) {
        emitters
                .computeIfAbsent(
                        studyId,
                        key -> new ConcurrentHashMap<>()
                )
                .put(emitterId, emitter);
    }

    public Map<String, SseEmitter> findAllByStudyId(Long studyId) {
        Map<String, SseEmitter> studyEmitters = emitters.get(studyId);

        if (studyEmitters == null) {
            return Map.of();
        }

        return Map.copyOf(studyEmitters);
    }

    public void delete(
            Long studyId,
            String emitterId
    ) {
        Map<String, SseEmitter> studyEmitters =
                emitters.get(studyId);

        if (studyEmitters == null) {
            return;
        }

        studyEmitters.remove(emitterId);

        if (studyEmitters.isEmpty()) {
            emitters.remove(studyId);
        }
    }
}