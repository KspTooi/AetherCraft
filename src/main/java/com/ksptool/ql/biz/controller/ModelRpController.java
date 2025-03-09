package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.dto.RecoverRpChatDto;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.biz.model.vo.RecoverRpChatVo;
import com.ksptool.ql.biz.service.ModelRpService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/model/rp")
public class ModelRpController {

    @Autowired
    private ModelRpService modelRpService;

    @PostMapping("/getRoleList")
    public Result<PageableView<GetModelRoleListVo>> getModelRoleList(@RequestBody @Valid GetModelRoleListDto queryDto) {
        return Result.success(modelRpService.getModelRoleList(queryDto));
    }

    /**
     * 恢复或创建RP对话
     * 如果存在激活的存档，则返回存档内容
     * 如果不存在，则创建新存档并返回首条消息
     */
    @PostMapping("/recoverRpChat")
    public Result<RecoverRpChatVo> recoverRpChat(@RequestBody @Valid RecoverRpChatDto dto) throws BizException {
        return Result.success(modelRpService.recoverRpChat(dto));
    }
} 