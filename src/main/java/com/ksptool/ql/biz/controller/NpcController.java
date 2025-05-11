package com.ksptool.ql.biz.controller;


import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.dto.SaveModelRoleDto;
import com.ksptool.ql.biz.model.po.NpcPo;
import com.ksptool.ql.biz.model.vo.GetModelRoleDetailsVo;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.biz.service.NpcService;
import com.ksptool.ql.biz.service.UserFileService;
import com.ksptool.ql.biz.service.panel.PanelNpcService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/model/role")
public class NpcController {

    @Autowired
    private UserFileService userFileService;

    @Autowired
    private NpcService service;

    @Autowired
    private PanelNpcService panelNpcService;


    @PostMapping("/getModelRoleList")
    public Result<PageableView<GetModelRoleListVo>> getModelRoleList(@RequestBody @Valid GetModelRoleListDto dto){
        return Result.success(service.getModelRoleList(dto));
    }

    @PostMapping("getModelRoleDetails")
    public Result<GetModelRoleDetailsVo> getModelRoleDetails(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(service.getModelRoleDetails(dto.getId()));
    }

    @PostMapping("copyModelRole")
    public Result<String> copyModelRole(@RequestBody @Valid CommonIdDto dto){

        try{
            service.copyModelRole(dto.getId());
            return Result.success("success");
        }catch (BizException ex){
            return Result.error(ex.getMessage());
        }
    }


    @PostMapping("/saveModelRole")
    public Result<String> saveModelRole(@RequestBody @Valid SaveModelRoleDto dto){
        try{
            NpcPo npcPo = panelNpcService.saveModelRole(dto);
            return Result.success(npcPo.getId()+"");
        }catch (BizException ex){
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/removeModelRole")
    public Result<String> removeModelRole(@RequestBody @Valid CommonIdDto dto){
        try{
            panelNpcService.removeModelRole(dto.getId());
            return Result.success("success");
        }catch (BizException ex){
            return Result.error(ex.getMessage());
        }
    }

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
