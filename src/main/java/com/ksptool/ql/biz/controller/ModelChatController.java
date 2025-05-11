package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.vo.*;
import com.ksptool.ql.biz.service.ChatMessageService;
import com.ksptool.ql.biz.service.PlayerConfigService;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.biz.model.dto.BatchChatCompleteDto;
import com.ksptool.ql.biz.model.dto.RecoverChatDto;
import com.ksptool.ql.biz.model.dto.EditThreadDto;
import com.ksptool.ql.biz.model.dto.RemoveThreadDto;
import com.ksptool.ql.biz.model.dto.RemoveHistoryDto;
import com.ksptool.ql.biz.model.dto.CreateEmptyThreadDto;
import com.ksptool.ql.biz.model.dto.EditHistoryDto;
import com.ksptool.ql.biz.service.ModelChatService;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RestController
@RequestMapping("/model/chat")
public class ModelChatController {
    
    @Autowired
    private ModelChatService modelChatService;
    @Autowired
    private PlayerConfigService playerConfigService;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 恢复会话
     * @param dto 恢复会话请求参数
     * @return 会话信息和历史消息
     */
    @PostMapping("/recoverChat")
    public Result<RecoverChatVo> recoverChat(@Valid @RequestBody RecoverChatDto dto) {
        try {
            return Result.success(modelChatService.recoverChat(dto));
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 批量聊天接口 - 长轮询方式
     * @param dto 批量聊天请求参数
     * @return 聊天片段
     */
    @PostMapping("/completeBatch")
    public Result<ChatSegmentVo> chatCompleteBatch(@Valid @RequestBody BatchChatCompleteDto dto) {
        try {
            // 参数校验：只有queryKind=0时才需要model和message
            if (dto.getQueryKind() == 0) {

                if (StringUtils.isBlank(dto.getModel())) {
                    return Result.error("发送消息时，模型代码不能为空");
                }
                if (StringUtils.isBlank(dto.getMessage())) {
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

            //处理重新生成
            if(dto.getQueryKind() == 3) {
                return Result.success(modelChatService.chatCompleteRegenerateBatch(dto));
            }
            
            // 默认情况
            return Result.error("无效的查询类型");
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 编辑会话标题
     * @param dto 编辑会话请求参数
     * @return 编辑结果
     */
    @PostMapping("/editThread")
    public Result<String> editThread(@Valid @RequestBody EditThreadDto dto) {
        try {
            modelChatService.editThreadTitle(dto.getThreadId(), dto.getTitle());
            return Result.success("会话标题修改成功");
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 删除会话
     * @param dto 删除会话请求参数
     * @return 删除结果
     */
    @PostMapping("/removeThread")
    public Result<String> removeThread(@Valid @RequestBody RemoveThreadDto dto) {
        try {

            modelChatService.removeThread(dto.getThreadId());

            //如移除的角色是用户最后选择的那一个Thread 需清空用户保存的配置
            Long userLastSelect = playerConfigService.getLong(UserConfigEnum.MODEL_CHAT_CURRENT_THREAD.key(),-1L);

            if(userLastSelect.equals(dto.getThreadId())){
                playerConfigService.remove(UserConfigEnum.MODEL_CHAT_CURRENT_THREAD.key());
            }

            return Result.success("会话删除成功");
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 删除指定的历史消息
     * @param dto 删除历史消息请求参数
     * @return 删除结果
     */
    @PostMapping("/removeHistory")
    public Result<String> removeHistory(@Valid @RequestBody RemoveHistoryDto dto) {
        try {
            modelChatService.removeHistory(dto.getThreadId(), dto.getHistoryId());
            return Result.success("历史消息已删除");
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 编辑历史消息
     * @param dto 编辑历史消息请求参数
     * @return 编辑结果
     */
    @PostMapping("/editHistory")
    public Result<String> editMessage(@Valid @RequestBody EditHistoryDto dto) {
        try {
            chatMessageService.editMessage(dto.getHistoryId(),dto.getContent());
            return Result.success("历史消息已编辑");
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 获取当前用户的会话列表
     * @return 会话列表
     */
    @PostMapping("/getThreadList")
    public Result<List<ThreadListItemVo>> getThreadList() {
        try {
            return Result.success(modelChatService.getThreadList());
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 创建空会话
     * @param dto 创建空会话请求参数
     * @return 新创建的会话ID
     */
    @PostMapping("/createEmptyThread")
    public Result<CreateEmptyThreadVo> createEmptyThread(@Valid @RequestBody CreateEmptyThreadDto dto) {
        try {
            return Result.success(modelChatService.createEmptyThread(dto));
        } catch (BizException e) {
            return Result.error(e);
        }
    }
} 