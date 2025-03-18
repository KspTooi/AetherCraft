package com.ksptool.ac.test;

import com.ksptool.ql.AetherLauncher;
import com.ksptool.ql.biz.model.dto.ServiceQueryDto;
import com.ksptool.ql.biz.model.vo.WindowsServiceVo;
import com.ksptool.ql.biz.service.WindowsNativeService;
import com.ksptool.ql.commons.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AetherLauncher.class)
public class WindowsNativeServiceTest {

    @Autowired
    private WindowsNativeService windowsNativeService;

    @Test
    public void testGetNativeServiceList_NoFilter() {
        try {
            // 不带过滤条件的测试
            List<WindowsServiceVo> services = windowsNativeService.getNativeServiceList(null);
            
            // 验证结果不为空
            assertNotNull(services);
            // 验证至少有一些系统服务
            assertFalse(services.isEmpty());
            
            // 验证每个服务的基本信息
            for (WindowsServiceVo service : services) {
                assertNotNull(service.getServiceName());
                assertNotNull(service.getDisplayName());
                assertNotNull(service.getStateDesc());
                assertTrue(service.getState() >= 1 && service.getState() <= 7);
            }
            
            // 输出一些服务信息用于调试
            System.out.println("找到服务数量: " + services.size());
            services.stream()
                .limit(5)
                .forEach(service -> System.out.println(
                    String.format("服务名: %s, 显示名: %s, 状态: %s",
                        service.getServiceName(),
                        service.getDisplayName(),
                        service.getStateDesc()
                    )
                ));
            
        } catch (BizException e) {
            fail("获取服务列表失败: " + e.getMessage());
        }
    }

    @Test
    public void testGetNativeServiceList_WithFilter() {
        try {
            // 创建查询条件，查找包含 "Windows" 的服务
            ServiceQueryDto dto = new ServiceQueryDto();
            dto.setServiceName("Windows");
            
            List<WindowsServiceVo> services = windowsNativeService.getNativeServiceList(dto);
            
            // 验证结果
            assertNotNull(services);
            assertFalse(services.isEmpty());
            
            // 验证过滤结果
            for (WindowsServiceVo service : services) {
                assertTrue(
                    service.getServiceName().toLowerCase().contains("windows") ||
                    service.getDisplayName().toLowerCase().contains("windows"),
                    "服务名称或显示名称应该包含 'Windows'"
                );
            }
            
            // 输出过滤结果
            System.out.println("找到包含 'Windows' 的服务数量: " + services.size());
            services.forEach(service -> System.out.println(
                String.format("服务名: %s, 显示名: %s, 状态: %s",
                    service.getServiceName(),
                    service.getDisplayName(),
                    service.getStateDesc()
                )
            ));
            
        } catch (BizException e) {
            fail("获取服务列表失败: " + e.getMessage());
        }
    }

    @Test
    public void testGetNativeServiceList_EmptyFilter() {
        try {
            // 测试空字符串过滤条件
            ServiceQueryDto dto = new ServiceQueryDto();
            dto.setServiceName("");
            
            List<WindowsServiceVo> servicesWithEmptyFilter = windowsNativeService.getNativeServiceList(dto);
            List<WindowsServiceVo> servicesWithNullFilter = windowsNativeService.getNativeServiceList(null);
            
            // 验证空字符串过滤和无过滤条件的结果应该相同
            assertEquals(
                servicesWithNullFilter.size(),
                servicesWithEmptyFilter.size(),
                "空字符串过滤和无过滤条件应返回相同数量的服务"
            );
            
        } catch (BizException e) {
            fail("获取服务列表失败: " + e.getMessage());
        }
    }

    @Test
    public void testRunExe_InvalidPath() {
        // 测试无效的可执行文件路径
        String invalidPath = "C:\\invalid\\path\\test.exe";
        BizException exception = assertThrows(
            BizException.class,
            () -> windowsNativeService.runExe(invalidPath)
        );
        assertTrue(exception.getMessage().contains("程序文件不存在"));
    }

    @Test
    public void testRunExe_NotExeFile() {
        // 测试非exe文件
        String notExePath = "C:\\Windows\\System32\\drivers\\etc\\hosts";
        BizException exception = assertThrows(
            BizException.class,
            () -> windowsNativeService.runExe(notExePath)
        );
        assertTrue(exception.getMessage().contains("不是可执行文件"));
    }
} 