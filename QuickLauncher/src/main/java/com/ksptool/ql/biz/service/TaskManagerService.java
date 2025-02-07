package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.model.vo.ProcessInfoVo;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.*;
import java.lang.ProcessHandle;
import java.time.Duration;
import java.time.Instant;

@Service
public class TaskManagerService {
    
    /**
     * 获取系统当前运行的进程列表
     */
    public List<ProcessInfoVo> getProcessList(String keyword) throws BizException {
        try {
            List<ProcessInfoVo> processes = new ArrayList<>();
            
            // 使用 Java ProcessHandle API 获取所有进程
            ProcessHandle.allProcesses()
                .filter(handle -> {
                    if (keyword == null || keyword.isEmpty()) {
                        return true;
                    }
                    return handle.info().command().orElse("")
                        .toLowerCase()
                        .contains(keyword.toLowerCase());
                })
                .forEach(handle -> {
                    try {
                        ProcessInfoVo vo = new ProcessInfoVo();
                        
                        // 设置进程ID
                        vo.setPid(handle.pid());
                        
                        // 设置进程名称
                        String name = handle.info().command()
                            .orElse("N/A");
                        vo.setName(name);
                        
                        // 设置进程状态
                        vo.setStatus(handle.isAlive() ? "运行中" : "已终止");
                        
                        // 尝试获取进程的启动时间
                        handle.info().startInstant().ifPresent(startTime -> {
                            Duration uptime = Duration.between(startTime, Instant.now());
                            long hours = uptime.toHours();
                            long minutes = uptime.toMinutesPart();
                            if (hours > 0) {
                                vo.setStatus(String.format("运行中 (%d小时%d分钟)", hours, minutes));
                            } else {
                                vo.setStatus(String.format("运行中 (%d分钟)", minutes));
                            }
                        });
                        
                        processes.add(vo);
                    } catch (Exception e) {
                        // 忽略单个进程的处理错误
                    }
                });
            
            return processes;
            
        } catch (Exception e) {
            throw new BizException("获取进程列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 终止指定的进程
     */
    public void terminateProcess(long pid) throws BizException {
        try {
            Optional<ProcessHandle> processHandle = ProcessHandle.of(pid);
            if (processHandle.isPresent()) {
                if (!processHandle.get().destroy()) {
                    // 如果正常终止失败，尝试强制终止
                    if (!processHandle.get().destroyForcibly()) {
                        throw new BizException("无法终止进程");
                    }
                }
                // 等待进程终止
                processHandle.get().onExit().get();
            } else {
                throw new BizException("找不到指定的进程");
            }
        } catch (Exception e) {
            throw new BizException("终止进程时发生错误: " + e.getMessage());
        }
    }
} 