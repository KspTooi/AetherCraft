package com.ksptool.ql.biz.service;

import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.WindowsServiceManager;
import com.ksptool.ql.biz.model.dto.ServiceQueryDto;
import com.ksptool.ql.biz.model.vo.WindowsServiceVo;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Winsvc;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import com.sun.jna.Memory;

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

    /**
     * 获取Windows服务列表
     * @param dto 查询条件
     * @return 服务列表
     * @throws BizException 如果获取服务列表失败
     */
    public List<WindowsServiceVo> getNativeServiceList(ServiceQueryDto dto) throws BizException {
        List<WindowsServiceVo> services = new ArrayList<>();
        Winsvc.SC_HANDLE scManager = null;
        Memory buffer = null;
        
        try {
            // 打开服务控制管理器
            scManager = Advapi32.INSTANCE.OpenSCManager(null, null, 
                    Winsvc.SC_MANAGER_ENUMERATE_SERVICE);
            
            if (scManager == null) {
                throw new BizException("打开服务管理器失败，错误码：" + Native.getLastError());
            }
            
            IntByReference pcbBytesNeeded = new IntByReference();
            IntByReference lpServicesReturned = new IntByReference();
            IntByReference lpResumeHandle = new IntByReference(0);
            
            // 第一次调用获取所需的内存大小
            Advapi32.INSTANCE.EnumServicesStatusEx(
                    scManager,
                    Winsvc.SC_ENUM_PROCESS_INFO,
                    WinNT.SERVICE_WIN32,
                    Winsvc.SERVICE_STATE_ALL,
                    null,
                    0,
                    pcbBytesNeeded,
                    lpServicesReturned,
                    lpResumeHandle,
                    null);
            
            int bytesNeeded = pcbBytesNeeded.getValue();
            buffer = new Memory(bytesNeeded);
            
            // 再次调用以获取实际的服务信息
            boolean success = Advapi32.INSTANCE.EnumServicesStatusEx(
                    scManager,
                    Winsvc.SC_ENUM_PROCESS_INFO,
                    WinNT.SERVICE_WIN32,
                    Winsvc.SERVICE_STATE_ALL,
                    buffer,
                    bytesNeeded,
                    pcbBytesNeeded,
                    lpServicesReturned,
                    lpResumeHandle,
                    null);
            
            if (!success) {
                throw new BizException("枚举服务失败，错误码：" + Native.getLastError());
            }
            
            // 处理返回的服务信息
            int numServices = lpServicesReturned.getValue();
            Winsvc.ENUM_SERVICE_STATUS_PROCESS service = new Winsvc.ENUM_SERVICE_STATUS_PROCESS();
            int serviceSize = service.size();
            
            // 检查查询条件
            String keyword = (dto != null && dto.getServiceName() != null) ? 
                           dto.getServiceName().toLowerCase().trim() : null;
            
            for (int i = 0; i < numServices; i++) {
                service = Structure.newInstance(
                    Winsvc.ENUM_SERVICE_STATUS_PROCESS.class,
                    buffer.share(i * serviceSize)
                );
                service.read();
                
                String serviceName = service.lpServiceName;
                String displayName = service.lpDisplayName;
                
                // 如果有关键字，进行过滤
                if (keyword != null && !keyword.isEmpty()) {
                    if (!serviceName.toLowerCase().contains(keyword) && 
                        !displayName.toLowerCase().contains(keyword)) {
                        continue;
                    }
                }
                
                services.add(new WindowsServiceVo(
                    serviceName,
                    displayName,
                    service.ServiceStatusProcess.dwCurrentState
                ));
            }
            
            return services;
            
        } finally {
            // 确保资源被正确释放
            if (scManager != null) {
                Advapi32.INSTANCE.CloseServiceHandle(scManager);
            }
            if (buffer != null) {
                buffer.clear();
            }
        }
    }
} 