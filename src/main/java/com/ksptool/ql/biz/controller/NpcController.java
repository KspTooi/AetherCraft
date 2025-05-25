package com.ksptool.ql.biz.controller;


import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetNpcListDto;
import com.ksptool.ql.biz.model.dto.SaveNpcDto;
import com.ksptool.ql.biz.model.po.NpcPo;
import com.ksptool.ql.biz.model.vo.GetNpcDetailsVo;
import com.ksptool.ql.biz.model.vo.GetNpcListVo;
import com.ksptool.ql.biz.service.NpcService;
import com.ksptool.ql.biz.service.UserFileService;
import com.ksptool.ql.biz.service.panel.PanelNpcService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/npc")
public class NpcController {

    @Autowired
    private UserFileService userFileService;

    @Autowired
    private NpcService service;

    @Autowired
    private PanelNpcService panelNpcService;


    @PostMapping("/getNpcListVo")
    public Result<RestPageableView<GetNpcListVo>> getNpcListVo(@RequestBody @Valid GetNpcListDto dto){
        return Result.success(service.getNpcList(dto));
    }

    @PostMapping("getNpcDetails")
    public Result<GetNpcDetailsVo> getNpcDetails(@RequestBody @Valid CommonIdDto dto) throws BizException {
        return Result.success(service.getNpcDetails(dto.getId()));
    }

    @PostMapping("copyNpc")
    public Result<String> copyNpc(@RequestBody @Valid CommonIdDto dto){

        try{
            service.copyNpc(dto.getId());
            return Result.success("success");
        }catch (BizException ex){
            return Result.error(ex.getMessage());
        }
    }


    @PostMapping("/saveNpc")
    public Result<String> saveNpc(@RequestBody @Valid SaveNpcDto dto){
        try{
            NpcPo npcPo = panelNpcService.saveNpc(dto);
            return Result.success(npcPo.getId()+"");
        }catch (BizException ex){
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/removeNpc")
    public Result<String> removeNpc(@RequestBody @Valid CommonIdDto dto){
        try{
            panelNpcService.removeNpc(dto.getId());
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
