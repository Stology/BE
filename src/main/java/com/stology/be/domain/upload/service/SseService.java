package com.stology.be.domain.upload.service;

import com.stology.be.domain.upload.Component.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SseService {

    private static final long TIMEOUT = 60L * 60L * 1000L;

    private final SseEmitterRepository repository;

    public SseEmitter subscribe(
            Long studyId,
            Long memberId
    ) {
        //이미터 고유 ID 생성
        String emitterId = createEmitterId(studyId, memberId);

        //이미터 생성
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        //이미터 정보 서버에 저장
        repository.save(studyId, emitterId, emitter);

        //프론트로 보낼 이미터 정보 설정
        setEmitterConfig(emitter,studyId,emitterId);
        //프론트 와연동 되었는지 실험
        testSseEmitterConnect(emitter,studyId,emitterId);


        return emitter;
    }














    /*
    객체 내부 매서드

     */
    private String createEmitterId(Long studyId,Long memberId){
        return studyId + "_" + memberId +"_" +
                System.currentTimeMillis();
    }
    private void setEmitterConfig(SseEmitter emitter,Long studyId,String emitterId){
        emitter.onCompletion(
                () -> repository.delete(studyId, emitterId)
        );

        emitter.onTimeout(
                () -> repository.delete(studyId, emitterId)
        );

        emitter.onError(
                exception -> repository.delete(studyId, emitterId)
        );
    };
    private void testSseEmitterConnect(SseEmitter emitter,Long studyId,String emitterId) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connect")
                            .data("SSE connection completed")
            );
        } catch (IOException exception) {
            repository.delete(studyId, emitterId);
            emitter.completeWithError(exception);
        }
    }

}