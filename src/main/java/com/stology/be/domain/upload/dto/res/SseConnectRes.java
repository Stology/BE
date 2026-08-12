package com.stology.be.domain.upload.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@AllArgsConstructor
@Builder
public class SseConnectRes {
    SseEmitter emitter;
}
