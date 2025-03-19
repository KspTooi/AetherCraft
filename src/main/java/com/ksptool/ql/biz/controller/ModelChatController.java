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
    

    @Autowired
    private ModelChatService modelChatService;

    @Autowired
    private UserConfigService userConfigService;












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