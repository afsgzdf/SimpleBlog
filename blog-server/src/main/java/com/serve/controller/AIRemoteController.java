package com.serve.controller;

import com.serve.context.BaseContext;
import com.serve.dto.RequestAIDTO;
import com.serve.service.AIRemoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/ai")
@Slf4j
@Tag(name = "AI访问管理", description = "AI相关操作")
@RequiredArgsConstructor
public class AIRemoteController {

    private final AIRemoteService aiRemoteService;
    private final Executor sseStreamExecutor;

    @Operation(summary = "DeepSeek ai接口")
    @PostMapping(value = "/deepseek", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter requestRemoteForDeepSeek(@RequestBody RequestAIDTO requestAIDTO) {
        SseEmitter emitter = new SseEmitter(0L);

        Long userId = BaseContext.getThreadLocal();
        if (userId == null) {
            throw new RuntimeException("请先登录再进行对话!");
        }

        sseStreamExecutor.execute(() -> {
            try {
                BaseContext.setThreadLocal(userId);
                aiRemoteService.remoteRequestAIStream(requestAIDTO, content->{
                    try {
                        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                        emitter.send(bytes, MediaType.TEXT_EVENT_STREAM);
                    } catch (Exception e) {
                        log.error("SSE连接已断开,停止发送: {}", e.getMessage());
                    }
                });
                String endEvent = "data:[DONE]";
                emitter.send(endEvent.getBytes(StandardCharsets.UTF_8), MediaType.TEXT_EVENT_STREAM);
                emitter.complete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                BaseContext.removeThreadLocal();
            }
        });
        return emitter;
    }
}
