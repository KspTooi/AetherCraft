package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.vo.GetAttachPlayerDetailsVo;
import com.ksptool.ql.biz.model.vo.GetCurrentPlayerVo;
import com.ksptool.ql.biz.model.vo.GetPlayerListVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.GlobalConfigService;
import com.ksptool.ql.biz.service.PlayerService;
import com.ksptool.ql.biz.service.UserFileService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/player")
public class PlayerController {

    @Autowired
    private PlayerService service;

    @Autowired
    private UserFileService userFileService;

    @Autowired
    private GlobalConfigService globalConfigService;

    //获取当前登录人物快照
    @PostMapping("/getCurrentPlayer")
    public Result<GetCurrentPlayerVo> getCurrentPlayer() {

        Long playerId = AuthService.getCurrentPlayerId();
        String playerName = AuthService.getCurrentPlayerName();

        if(playerId == null){
            return Result.error("Player is not logged in");
        }

        GetCurrentPlayerVo vo = new GetCurrentPlayerVo();
        vo.setId(playerId);
        vo.setName(playerName);
        vo.setAvatarUrl(AuthService.getCurrentUserSession().getPlayerAvatarUrl());
        return Result.success(vo);
    }

    //获取当前登录人物详细信息
    @PostMapping("/getAttachPlayerDetails")
    public Result<GetAttachPlayerDetailsVo> getAttachPlayerDetails() {
        try{
            return Result.success(service.getAttachPlayerDetails());
        }catch (BizException ex){
            return Result.error(ex.getMessage());
        }
    }

    //编辑当前登录人物详细信息
    @PostMapping("/editAttachPlayerDetails")
    public Result<String> editAttachPlayerDetails(@RequestBody @Valid EditAttachPlayerDetailsDto dto){

        //自定义性别种类 gender为4 5 6时必填
        if(dto.getGender() == 4 || dto.getGender() == 5 || dto.getGender() == 6){
            if(StringUtils.isBlank(dto.getGenderData())){
                return Result.error("需填写自定义性别类型");
            }
        }

        try{
            service.editAttachPlayerDetails(dto);
            return Result.success("success");
        }catch (BizException ex){
            return Result.error(ex.getMessage());
        }
    }

    //用户从人物选择界面选择一个人物
    @PostMapping("/attachPlayer")
    public Result<String> attachPlayer(@RequestBody @Valid CommonIdDto dto) {
        try {
            service.attachPlayer(dto.getId());
            return Result.success("success");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    //取消激活全部人物并退回到人物选择界面
    @PostMapping("/detachPlayer")
    public Result<String> detachPlayer() {
        try {
            service.detachPlayer();
            return Result.success("success");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    //获取用户的人物列表
    @PostMapping("/getPlayerList")
    public Result<RestPageableView<GetPlayerListVo>> getPlayerList(@RequestBody @Valid GetPlayerListDto dto) {
        try {
            RestPageableView<GetPlayerListVo> result = service.getPlayerList(dto);
            return Result.success(result);
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    //创建人物
    @PostMapping("/createPlayer")
    public Result<String> createPlayer(@RequestBody @Valid CreatePlayerDto dto) {

        //自定义性别种类 gender为4 5 6时必填
        if(dto.getGender() == 4 || dto.getGender() == 5 || dto.getGender() == 6){
            if(StringUtils.isBlank(dto.getGenderData())){
                return Result.error("需填写自定义性别类型");
            }
        }

        try {
            String result = service.createPlayer(dto);
            return Result.success(result);
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    //检查人物名字是否可用
    @PostMapping("/checkName")
    public Result<String> checkPlayerName(@RequestBody @Valid CheckPlayerNameDto dto) {

        boolean pass = service.checkPlayerName(dto.getName());

        if (pass) {
            return Result.success("该名字可用");
        }

        return Result.error("该名字不可用");
    }

    //发送删除请求
    @PostMapping("/sendRemoveRequest")
    public Result<String> sendRemoveRequest(@RequestBody @Valid CommonIdDto dto){
        try {
            service.sendRemoveRequest(dto.getId());
            String defaultHoursStr = GlobalConfigEnum.PLAYER_REMOVE_WAITING_PERIOD_HOURS.getDefaultValue();
            int defaultHours = Integer.parseInt(defaultHoursStr);
            int waitingHours = globalConfigService.getInt(GlobalConfigEnum.PLAYER_REMOVE_WAITING_PERIOD_HOURS.getKey(), defaultHours);
            return Result.success("删除请求已提交，请在 " + waitingHours + " 小时后在人物管理界面确认删除。");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    //删除人物
    @PostMapping("/removePlayer")
    public Result<String> removePlayer(@RequestBody @Valid CommonIdDto dto){
        try {
            service.removePlayer(dto.getId());
            return Result.success("操作成功");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    //取消删除人物
    @PostMapping("/cancelRemovePlayer")
    public Result<String> cancelRemovePlayer(@RequestBody @Valid CommonIdDto dto){
        try {
            service.cancelRemovePlayer(dto.getId());
            return Result.success("操作成功");
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    //上传人物头像
    @PostMapping("/uploadAvatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file){
        try {
            // 使用统一文件服务保存头像
            var userFile = userFileService.receive(file);
            return Result.success(userFile.getFilepath());
        } catch (Exception e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
}
