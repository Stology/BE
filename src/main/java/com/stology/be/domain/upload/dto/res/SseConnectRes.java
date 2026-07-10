package com.stology.be.domain.upload.dto.res;

import lombok.AllArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@AllArgsConstructor
public class SseConnectRes {
    SseEmitter emitter;
}
