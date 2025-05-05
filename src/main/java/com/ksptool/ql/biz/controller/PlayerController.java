package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.CheckPlayerNameDto;
import com.ksptool.ql.biz.model.dto.CreatePlayerDto;
import com.ksptool.ql.biz.model.dto.GetPlayerListDto;
import com.ksptool.ql.biz.model.vo.GetPlayerListVo;
import com.ksptool.ql.biz.service.PlayerService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/player")
public class PlayerController {

    @Autowired
    private PlayerService service;

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

}
