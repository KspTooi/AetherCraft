package com.ksptool.ql.biz.service;

import com.ksptool.ql.commons.exception.BizException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

@Service
public class WindowsNativeService {

    /**
     * 运行指定路径的 exe 程序
     * @param execPath exe 程序的完整路径
     * @return 进程 ID (PID)
     * @throws BizException 如果启动失败则抛出异常
     */
    public long runExe(String execPath) throws BizException {
        try {
            // 检查文件是否存在
            File file = new File(execPath);
            if (!file.exists()) {
                throw new BizException("程序文件不存在: " + execPath);
            }
            if (!file.isFile()) {
                throw new BizException("指定路径不是一个文件: " + execPath);
            }
            if (!execPath.toLowerCase().endsWith(".exe")) {
                throw new BizException("不是可执行文件: " + execPath);
            }

            // 检查操作系统
            OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            if (!os.getName().toLowerCase().contains("windows")) {
                throw new BizException("当前系统不是 Windows 系统");
            }

            // 启动程序
            ProcessBuilder processBuilder = new ProcessBuilder(execPath);
            processBuilder.directory(file.getParentFile()); // 设置工作目录为程序所在目录
            Process process = processBuilder.start();

            // 获取进程 ID
            long pid = process.pid();
            
            // 检查进程是否立即退出
            if (!process.isAlive()) {
                int exitCode = process.exitValue();
                throw new BizException("程序启动后立即退出，退出码: " + exitCode);
            }

            return pid;

        } catch (IOException e) {
            throw new BizException("启动程序失败: " + e.getMessage(), e);
        } catch (SecurityException e) {
            throw new BizException("没有权限启动程序: " + e.getMessage(), e);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("启动程序时发生未知错误: " + e.getMessage(), e);
        }
    }
} 