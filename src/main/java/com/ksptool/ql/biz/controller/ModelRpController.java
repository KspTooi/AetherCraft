package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.BatchRpCompleteDto;
import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.dto.RecoverRpChatDto;
import com.ksptool.ql.biz.model.dto.DeActiveThreadDto;
import com.ksptool.ql.biz.model.dto.RemoveRpHistoryDto;
import com.ksptool.ql.biz.model.dto.EditRpHistoryDto;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.biz.model.vo.RecoverRpChatVo;
import com.ksptool.ql.biz.model.vo.RpSegmentVo;
import com.ksptool.ql.biz.service.ModelRpService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.ksptool.ql.commons.enums.AIModelEnum;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/model/rp")
public class ModelRpController {

    @Autowired
    private ModelRpService modelRpService;

    @GetMapping("/view")
    public ModelAndView getModelRpView() {
        ModelAndView modelAndView = new ModelAndView("model-rp");
        
        // 从枚举中获取所有可用的模型列表
        List<AIModelEnum> models = new ArrayList<>();
        for (AIModelEnum model : AIModelEnum.values()) {
            models.add(model);
        }
        
        // 获取默认模型（枚举中的第一个）
        String defaultModel = AIModelEnum.values()[0].getCode();
        
        modelAndView.addObject("models", models);
        modelAndView.addObject("defaultModel", defaultModel);
        
        return modelAndView;
    }

    @PostMapping("/getRoleList")
    public Result<PageableView<GetModelRoleListVo>> getModelRoleList(@RequestBody @Valid GetModelRoleListDto queryDto) {
        return Result.success(modelRpService.getModelRoleList(queryDto));
    }

    /**
     * 恢复或创建RP对话
     * 如果存在激活的存档，则返回存档内容
     * 如果不存在，则创建新存档并返回首条消息
     * 
     * @param dto 包含 modelRoleId、modelCode 和 newThread 参数
     *            newThread=0：创建新会话并将已有激活的存档置为未激活
     *            newThread=1：优先尝试获取已有激活的存档，没有则创建新存档（默认行为）
     * @return 返回会话信息和历史消息
     * @throws BizException 业务异常
     */
    @PostMapping("/recoverRpChat")
    public Result<RecoverRpChatVo> recoverRpChat(@RequestBody @Valid RecoverRpChatDto dto) throws BizException {
        return Result.success(modelRpService.recoverRpChat(dto));
    }

    /**
     * 取消激活RP对话
     */
    @PostMapping("/deActiveThread")
    public Result<String> deActiveThread(@RequestBody @Valid DeActiveThreadDto dto) throws BizException {
        modelRpService.deActiveThread(dto);
        return Result.success("存档已取消激活");
    }

    /**
     * 批量完成RP对话 - 长轮询方式
     * 处理发送消息、查询响应流和终止AI响应等操作
     */
    @PostMapping("/rpCompleteBatch")
    public Result<RpSegmentVo> rpCompleteBatch(@RequestBody @Valid BatchRpCompleteDto dto) throws BizException {


        // 发送消息
        if (dto.getQueryKind() == 0) {
            if (StringUtils.isBlank(dto.getMessage())) {
                throw new BizException("发送消息时，消息内容不能为空");
            }

            if (StringUtils.isBlank(dto.getModel())) {
                throw new BizException("发送消息时，模型代码不能为空");
            }

            return Result.success(modelRpService.rpCompleteSendBatch(dto));
        }

        // 查询响应流
        if (dto.getQueryKind() == 1) {
            return Result.success(modelRpService.rpCompleteQueryBatch(dto));
        }

        // 终止AI响应
        if (dto.getQueryKind() == 2) {
            modelRpService.rpCompleteTerminateBatch(dto);
            return Result.success(null);
        }

        // 重新生成AI最后一条回复
        if (dto.getQueryKind() == 3) {
            return Result.success(modelRpService.rpCompleteRegenerateBatch(dto));
        }
        
        throw new BizException("无效的查询类型");
    }

    /**
     * 删除消息历史记录
     */
    @PostMapping("/removeHistory")
    public Result<String> removeRpHistory(@RequestBody @Valid RemoveRpHistoryDto dto) throws BizException {
        modelRpService.removeRpHistory(dto);
        return Result.success("已删除消息");
    }

    /**
     * 编辑消息历史记录
     */
    @PostMapping("/editHistory")
    public Result<String> editRpHistory(@RequestBody @Valid EditRpHistoryDto dto) throws BizException {
        modelRpService.editRpHistory(dto);
        return Result.success("已编辑消息");
    }

} 