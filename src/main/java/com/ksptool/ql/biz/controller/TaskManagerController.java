package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.TerminateProcessDto;
import com.ksptool.ql.biz.model.vo.ProcessInfoVo;
import com.ksptool.ql.biz.service.TaskManagerService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ssr")
public class TaskManagerController {

    @Autowired
    private TaskManagerService taskManagerService;

    /**
     * 获取进程列表
     * @param keyword 搜索关键字（可选）
     */
    @GetMapping("/getProcessList")
    public Result<List<ProcessInfoVo>> getProcessList(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            return Result.success(taskManagerService.getProcessList(keyword));
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 终止进程
     */
    @PostMapping("/terminateProcess")
    public Result<String> terminateProcess(@RequestBody @Valid TerminateProcessDto dto) {
        try {
            taskManagerService.terminateProcess(dto.getPid());
            return Result.success("进程已终止");
        } catch (BizException e) {
            return Result.error(e);
        }
    }
} 