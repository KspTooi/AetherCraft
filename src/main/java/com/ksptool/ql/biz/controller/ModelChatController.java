package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.biz.model.dto.ChatCompleteDto;
import com.ksptool.ql.biz.model.dto.BatchChatCompleteDto;
import com.ksptool.ql.biz.model.vo.ChatCompleteVo;
import com.ksptool.ql.biz.model.vo.ChatSegmentVo;
import com.ksptool.ql.biz.service.ModelChatService;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.web.SseResult;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.ksptool.ql.biz.service.UserConfigService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@RestController
@RequestMapping("/model")
public class ModelChatController {
    
    private static final long SSE_TIMEOUT = TimeUnit.MINUTES.toMillis(5); // 5分钟超时
    private static final MediaType APPLICATION_STREAM_JSON = MediaType.APPLICATION_JSON;
    
    @Autowired
    private ModelChatService modelChatService;

    @Autowired
    private UserConfigService userConfigService;

    @RequirePermission("model:chat:create:thread")
    @GetMapping("/chat/newThread")
    public ModelAndView newThread(@RequestParam(name = "modelCode") String modelCode, RedirectAttributes ra) {
        try {
            // 创建新会话
            Long threadId = modelChatService.createNewThread(modelCode);
            // 重定向到聊天视图
            return new ModelAndView("redirect:/model/chat/view?threadId=" + threadId);
        } catch (BizException e) {
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
            return new ModelAndView("redirect:/model/chat/view");
        }
    }

    @RequirePermission("model:chat:view")
    @GetMapping("/chat/view")
    public ModelAndView chatView(@RequestParam(name = "threadId", required = false) Long threadId) {
        ModelAndView mav = new ModelAndView("model-chat");
        
        // 如果threadId为空，尝试从缓存获取
        if (threadId == null) {
            String cachedThreadId = userConfigService.get("model.chat.current.thread");
            if (cachedThreadId != null) {
                try {
                    Long cachedId = Long.parseLong(cachedThreadId);
                    // 验证缓存的会话是否有效
                    if (modelChatService.isValidThread(cachedId)) {
                        threadId = cachedId;
                    }
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }
        
        // 如果是新对话，清除缓存
        if (threadId != null && threadId == -1) {
            userConfigService.setValue("model.chat.current.thread", null);
        }
        
        // 如果是有效的会话ID，更新缓存
        if (threadId != null && threadId != -1 && modelChatService.isValidThread(threadId)) {
            userConfigService.setValue("model.chat.current.thread", threadId.toString());
        }
        
        mav.addObject("data", modelChatService.getChatView(threadId));
        return mav;
    }

    @RequirePermissionRest("model:chat:message")
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
    @RequirePermissionRest("model:chat:message")
    @PostMapping("/chat/complete/stream")
    public SseEmitter chatCompleteStream(@Valid @RequestBody ChatCompleteDto dto) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        // 获取用户ID和其他上下文信息 - 在主线程中获取
        Long userId = AuthService.getCurrentUserId();
        
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
     * 批量聊天接口 - 长轮询方式
     * @param dto 批量聊天请求参数
     * @return 聊天片段
     */
    @RequirePermissionRest("model:chat:message")
    @PostMapping("/chat/complete/batch")
    public Result<ChatSegmentVo> chatCompleteBatch(@Valid @RequestBody BatchChatCompleteDto dto) {
        try {
            // 参数校验：只有queryKind=0时才需要model和message
            if (dto.getQueryKind() == 0) {
                if (dto.getModel() == null || dto.getModel().trim().isEmpty()) {
                    return Result.error("发送消息时，模型代码不能为空");
                }
                if (dto.getMessage() == null || dto.getMessage().trim().isEmpty()) {
                    return Result.error("发送消息时，消息内容不能为空");
                }
                
                // 发送消息
                return Result.success(modelChatService.chatCompleteSendBatch(dto));
            }
            
            // 查询响应
            if (dto.getQueryKind() == 1) {
                return Result.success(modelChatService.chatCompleteQueryBatch(dto));
            }
            
            // 处理终止操作的特殊情况
            if (dto.getQueryKind() == 2) {
                modelChatService.chatCompleteTerminateBatch(dto);
                return Result.success("AI响应已终止", null);
            }
            
            // 默认情况
            return Result.error("无效的查询类型");
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 编辑会话标题
     * @param threadId 会话ID
     * @param newTitle 新标题
     * @return 重定向到聊天视图
     */
    @RequirePermission("model:chat:edit:thread")
    @GetMapping("/chat/view/editThreadTitle")
    public ModelAndView editThreadTitle(
            @RequestParam(name = "threadId") Long threadId,
            @RequestParam(name = "newTitle") String newTitle,
            RedirectAttributes ra) {
        try {
            // 编辑会话标题
            Long updatedThreadId = modelChatService.editThreadTitle(threadId, newTitle);
            // 重定向到聊天视图
            return new ModelAndView("redirect:/model/chat/view?threadId=" + updatedThreadId);
        } catch (BizException e) {
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
            return new ModelAndView("redirect:/model/chat/view");
        }
    }

    /**
     * 删除会话
     * @param threadId 会话ID
     * @return 重定向到聊天视图
     */
    @RequirePermission("model:chat:remove:thread")
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

    /**
     * 删除指定的历史消息
     * @param threadId 会话ID
     * @param historyId 历史消息ID
     * @return 重定向到聊天视图
     */
    @RequirePermission("model:chat:remove:history")
    @GetMapping("/chat/view/removeHistory")
    public ModelAndView removeHistory(
            @RequestParam(name = "threadId") Long threadId,
            @RequestParam(name = "historyId") Long historyId,
            RedirectAttributes ra) {
        ModelAndView mav = new ModelAndView("redirect:/model/chat/view?threadId=" + threadId);
        try {
            modelChatService.removeHistory(threadId, historyId);
            ra.addFlashAttribute("vo", Result.success("历史消息已删除", null));
        } catch (BizException e) {
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        return mav;
    }

} 