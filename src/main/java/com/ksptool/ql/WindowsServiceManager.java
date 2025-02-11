package com.ksptool.ql;

import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.util.ArrayList;
import java.util.List;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import java.util.Arrays;
import com.sun.jna.Memory;

public class WindowsServiceManager {

    public List<WindowsService> getWindowsServices() {
        List<WindowsService> services = new ArrayList<>();
        
        // 打开服务控制管理器
        Winsvc.SC_HANDLE scManager = Advapi32.INSTANCE.OpenSCManager(null, null, 
                Winsvc.SC_MANAGER_ENUMERATE_SERVICE);
        
        if (scManager == null) {
            throw new RuntimeException("打开服务管理器失败，错误码：" + Native.getLastError());
        }
        
        try {
            // 首先获取需要的缓冲区大小
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
            
            // 分配内存
            int bytesNeeded = pcbBytesNeeded.getValue();
            Winsvc.ENUM_SERVICE_STATUS_PROCESS[] serviceStatus = 
                    (Winsvc.ENUM_SERVICE_STATUS_PROCESS[]) new Winsvc.ENUM_SERVICE_STATUS_PROCESS()
                    .toArray(bytesNeeded / new Winsvc.ENUM_SERVICE_STATUS_PROCESS().size());
            
            // 再次调用以获取实际的服务信息
            boolean success = Advapi32.INSTANCE.EnumServicesStatusEx(
                    scManager,
                    Winsvc.SC_ENUM_PROCESS_INFO,
                    WinNT.SERVICE_WIN32,
                    Winsvc.SERVICE_STATE_ALL,
                    serviceStatus[0].getPointer(),
                    bytesNeeded,
                    pcbBytesNeeded,
                    lpServicesReturned,
                    lpResumeHandle,
                    null);
            
            if (!success) {
                throw new RuntimeException("枚举服务失败，错误码：" + Native.getLastError());
            }
            
            // 处理返回的服务信息
            int numServices = lpServicesReturned.getValue();
            Pointer pointer = serviceStatus[0].getPointer();
            
            for (int i = 0; i < numServices; i++) {
                Winsvc.ENUM_SERVICE_STATUS_PROCESS service = Structure.newInstance(
                    Winsvc.ENUM_SERVICE_STATUS_PROCESS.class, 
                    pointer.share(i * new Winsvc.ENUM_SERVICE_STATUS_PROCESS().size())
                );
                service.read();
                
                services.add(new WindowsService(
                        service.lpServiceName != null ? service.lpServiceName.toString() : "",
                        service.lpDisplayName != null ? service.lpDisplayName.toString() : "",
                        service.ServiceStatusProcess.dwCurrentState
                ));
            }
            
        } finally {
            // 关闭服务管理器句柄
            Advapi32.INSTANCE.CloseServiceHandle(scManager);
        }
        
        return services;
    }
    
    // 用于存储服务信息的内部类
    public static class WindowsService {
        private final String serviceName;
        private final String displayName;
        private final int state;
        
        public WindowsService(String serviceName, String displayName, int state) {
            this.serviceName = serviceName;
            this.displayName = displayName;
            this.state = state;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getState() {
            return state;
        }
        
        @Override
        public String toString() {
            return String.format("服务名称: %s, 显示名称: %s, 状态: %d", 
                    serviceName, displayName, state);
        }
    }

    // 测试方法
    public static void main(String[] args) {
        WindowsServiceManager manager = new WindowsServiceManager();
        List<WindowsService> services = manager.getWindowsServices();
        
        System.out.println("Windows服务列表：");
        for (WindowsService service : services) {
            System.out.println(service);
        }
    }
}