package com.stology.be.domain.upload.service;

import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.domain.upload.component.SseEmitterRepository;
import com.stology.be.domain.upload.exception.UploadException;
import com.stology.be.domain.upload.exception.code.UploadErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SseService {

    private static final long TIMEOUT = 60L * 60L * 1000L;

    private final SseEmitterRepository repository;
    private final MemberStudyRepository memberStudyRepository;

    public SseEmitter subscribe(
            Long studyId,
            Long memberId
    ) {
        //권한 검증 자신의 스터디로 가입할 떄
        getMemberStudy(studyId, memberId);
        //이미터 고유 ID 생성
        String emitterId = createEmitterId(studyId, memberId);

        //이미터 생성
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        //이미터 정보 서버에 저장
        repository.save(studyId, emitterId, emitter);

        //프론트로 보낼 이미터 정보 설정
        setEmitterConfig(emitter,studyId,emitterId);
        //프론트 와연동 되었는지 실험
        sendConnectEvent(emitter,studyId,emitterId);


        return emitter;
    }

    public void sendToStudy(
            Long studyId,
            String eventName,
            Object data
    ) {
        repository.findAllByStudyId(studyId)
                .forEach((emitterId, emitter) -> {
                    try {
                        emitter.send(
                                SseEmitter.event()
                                        .name(eventName)
                                        .data(data)
                                        .reconnectTime(3000L)
                        );

                    } catch (IOException | IllegalStateException exception) {
                        repository.delete(studyId, emitterId);
                        emitter.completeWithError(exception);
                    }
                });
    }
    @Scheduled(fixedRate = 25_000)
    public void sendHeartbeat() {
        repository.findAll()
                .forEach((studyId, studyEmitters) ->
                        studyEmitters.forEach((emitterId, emitter) -> {
                            try {
                                emitter.send(
                                        SseEmitter.event()
                                                .name("heartbeat")
                                                .data("ping")
                                );
                            } catch (IOException | IllegalStateException e) {
                                repository.delete(studyId, emitterId);
                                emitter.complete();
                            }
                        })
                );
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
    }
    private void sendConnectEvent(SseEmitter emitter,Long studyId,String emitterId) {
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

    private void getMemberStudy(
            Long studyId,
            Long memberId
    ) {
        MemberStudy memberStudy = memberStudyRepository
                .findByStudyIdAndMemberId(
                        studyId,
                        memberId
                )
                .orElseThrow(
                        () -> new UploadException(
                                UploadErrorCode
                                        .UPLOAD_MEMBER_NOT_IN_STUDY
                        )
                );
    }

}