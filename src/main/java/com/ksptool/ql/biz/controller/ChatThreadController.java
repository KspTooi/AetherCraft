package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.po.ChatThreadPo;
import com.ksptool.ql.biz.model.schema.ModelVariantSchema;
import com.ksptool.ql.biz.model.vo.CreateThreadVo;
import com.ksptool.ql.biz.model.vo.SelectThreadVo;
import com.ksptool.ql.biz.model.vo.GetThreadListVo;
import com.ksptool.ql.biz.service.ChatThreadService;
import com.ksptool.ql.biz.service.ModelVariantService;
import com.ksptool.ql.commons.annotation.PrintLog;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrintLog
@Slf4j
@RestController
@RequestMapping("/thread")
public class ChatThreadController {

    @Autowired
    private ChatThreadService service;

    @Autowired
    private ModelVariantService modelVariantService;

    //创建新的空NPC对话
    @PostMapping("/createThread")
    public Result<CreateThreadVo> createThread(@RequestBody @Valid CreateThreadDto dto) throws BizException {

        if(dto.getType() != 1){
            throw new BizException("type错误");
        }

        ModelVariantSchema model = modelVariantService.requireModelSchema(dto.getModelCode());
        ChatThreadPo threadPo = service.createSelfNpcThread(model, dto.getNpcId());

        var vo = new CreateThreadVo();
        vo.setThreadId(threadPo.getId());
        return Result.success(vo);
    }


    @PrintLog(sensitiveFields = "data.messages")
    @PostMapping("/selectThread")
    public Result<SelectThreadVo> selectThread(@RequestBody @Valid SelectThreadDto dto) throws BizException {

        if(dto.getThreadId() != null && dto.getNpcId() != null){
            return Result.error("参数过多, threadId与npcId仅可选填一项.");
        }
        if(dto.getThreadId() == null && dto.getNpcId() == null){
            return Result.error("参数不足, threadId与npcId必填一项");
        }

        return Result.success(service.selectThread(dto));
    }

    @PrintLog(sensitiveFields = {"title","data.rows"})
    @PostMapping("/getThreadList")
    public Result<RestPageableView<GetThreadListVo>> getThreadList(@RequestBody @Valid GetThreadListDto dto) throws BizException {

        //Thread类型 0:标准会话 1:RP会话 2:标准增强会话
        if(dto.getType() == 1 && dto.getNpcId() == null){
            return Result.error("NpcId不可为空");
        }

        return Result.success(service.getThreadList(dto));
    }

    @PrintLog(sensitiveFields = "title")
    @PostMapping("/editThreadTitle")
    public Result<String> editThreadTitle(@RequestBody @Valid EditThreadTitleDto dto) throws BizException {
        service.editThreadTitle(dto.getThreadId(),dto.getTitle());
        return Result.success("操作成功");
    }


    @PostMapping("/removeThread")
    public Result<String> removeThread(@RequestBody @Valid CommonIdDto dto) throws BizException {
        service.removeThread(dto.getId());
        return Result.success("操作成功");
    }


}
