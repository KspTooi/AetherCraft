package com.ksptool.ql.biz.controller;

import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.biz.model.dto.ChatCompleteDto;
import com.ksptool.ql.biz.model.vo.ChatCompleteVo;
import com.ksptool.ql.biz.model.vo.ModelChatViewVo;
import com.ksptool.ql.biz.service.ModelChatService;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.web.SseResult;
import com.ksptool.ql.commons.AuthContext;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@RestController
@RequestMapping("/model")
public class ModelChatController {
    
    private static final long SSE_TIMEOUT = TimeUnit.MINUTES.toMillis(5); // 5分钟超时
    private static final MediaType APPLICATION_STREAM_JSON = MediaType.APPLICATION_JSON;
    
    @Autowired
    private ModelChatService modelChatService;

    @GetMapping("/chat/view")
    public ModelAndView chatView(@RequestParam(name = "threadId", required = false) Long threadId) {
        ModelAndView mav = new ModelAndView("model-chat");
        mav.addObject("data", modelChatService.getChatView(threadId));
        return mav;
    }

    @PostMapping("/chat/complete")
    public Result<ChatCompleteVo> chatComplete(@Valid @RequestBody ChatCompleteDto dto) {
        try {
            return Result.success(modelChatService.chatComplete(dto));
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 流式对话接口
     * @param dto 对话请求参数
     * @return SSE事件流
     */
    @PostMapping("/chat/complete/stream")
    public SseEmitter chatCompleteStream(@Valid @RequestBody ChatCompleteDto dto) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        // 获取用户ID和其他上下文信息 - 在主线程中获取
        Long userId = AuthContext.getCurrentUserId();
        
        // 设置SSE连接成功的回调
        emitter.onCompletion(() -> {
            log.debug("SSE连接完成");
        });

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onError(ex -> {
            log.error("SSE连接错误", ex);
            emitter.complete();
        });

        // 在主线程中获取必要的Spring上下文信息
        try {
            // 发送开始事件 - 在主线程发送
            emitter.send(SseResult.start("开始生成响应..."), APPLICATION_STREAM_JSON);

            // 使用虚拟线程处理SSE响应
            Thread.startVirtualThread(() -> {
                try {
                    // 调用流式对话服务，传入用户ID
                    modelChatService.chatCompleteSSE(dto, userId, text -> {
                        try {
                            // 发送文本片段
                            emitter.send(SseResult.data(text), APPLICATION_STREAM_JSON);
                        } catch (IOException e) {
                            log.error("发送SSE消息失败", e);
                            emitter.completeWithError(e);
                        }
                    });
                    
                    // 发送完成事件
                    emitter.send(SseResult.end("生成完成"), APPLICATION_STREAM_JSON);
                    
                    // 完成SSE连接
                    emitter.complete();
                    
                } catch (Exception e) {
                    log.error("处理流式对话失败", e);
                    try {
                        // 发送错误事件
                        emitter.send(SseResult.error(e.getMessage()), APPLICATION_STREAM_JSON);
                        emitter.complete();
                    } catch (IOException ex) {
                        emitter.completeWithError(ex);
                    }
                }
            });
        } catch (Exception e) {
            log.error("初始化SSE连接失败", e);
            try {
                emitter.send(SseResult.error(e.getMessage()), APPLICATION_STREAM_JSON);
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
        
        return emitter;
    }

    /**
     * 删除会话
     * @param threadId 会话ID
     * @return 重定向到聊天视图
     */
    @GetMapping("/chat/view/removeThread/{threadId}")
    public ModelAndView removeThread(@PathVariable(name = "threadId") Long threadId) {
        ModelAndView mav = new ModelAndView("redirect:/model/chat/view");
        try {
            modelChatService.removeThread(threadId);
            mav.addObject("vo", Result.success("会话删除成功", null));
        } catch (BizException e) {
            mav.addObject("vo", Result.error(e.getMessage()));
        }
        return mav;
    }
} 