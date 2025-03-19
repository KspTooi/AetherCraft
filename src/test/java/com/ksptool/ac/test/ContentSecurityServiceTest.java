package com.ksptool.ac.test;

import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.ChaCha20Poly1305Cipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContentSecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContentSecurityService contentSecurityService;

    private final String testGlobalKek = "F6ztHzAFoYsldv+MepbyBp9hH/G8oG+08JmjLPh1Z5U=";
    private final Long testUserId = 1L;
    private final String testDek = "这是一个测试DEK值";
    private String encryptedDek;

    @BeforeEach
    void setUp() throws BizException {
        // 初始化Mock
        MockitoAnnotations.openMocks(this);
        
        // 注入全局KEK值
        ReflectionTestUtils.setField(contentSecurityService, "globalKek", testGlobalKek);
        
        // 加密测试DEK
        encryptedDek = ChaCha20Poly1305Cipher.encrypt(testDek, testGlobalKek);
        
        System.out.println("\n=== 测试环境初始化 ===");
        System.out.println("全局KEK: " + testGlobalKek);
        System.out.println("测试用户ID: " + testUserId);
        System.out.println("原始DEK: " + testDek);
        System.out.println("加密后的DEK: " + encryptedDek);
        System.out.println("===================\n");
    }

    @Test
    @DisplayName("测试成功获取用户DEK")
    void testGetPlainUserDekSuccess() throws BizException {
        System.out.println("\n=== 测试成功获取用户DEK ===");
        
        // 准备测试数据
        UserPo userPo = new UserPo();
        userPo.setId(testUserId);
        userPo.setEncryptedDek(encryptedDek);
        System.out.println("模拟用户数据: " + userPo);
        
        // 配置Mock行为
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(userPo));
        System.out.println("Mock设置: userRepository.findById(" + testUserId + ") 返回用户数据");
        
        // 执行测试
        String result = contentSecurityService.getPlainUserDek(testUserId);
        System.out.println("解密结果: " + result);
        
        // 验证结果
        assertEquals(testDek, result, "解密后的DEK应与原始值相同");
        System.out.println("测试通过: 解密结果与原始DEK匹配");
        
        // 验证Mock交互
        verify(userRepository, times(1)).findById(testUserId);
        System.out.println("验证通过: userRepository.findById 被调用1次");
        System.out.println("========================\n");
    }

    @Test
    @DisplayName("测试全局KEK为空的情况")
    void testGetPlainUserDekWithEmptyKek() {
        System.out.println("\n=== 测试全局KEK为空的情况 ===");
        
        // 设置全局KEK为空
        ReflectionTestUtils.setField(contentSecurityService, "globalKek", "");
        System.out.println("设置全局KEK为空字符串");
        
        // 执行测试并验证异常
        BizException exception = assertThrows(BizException.class, 
            () -> contentSecurityService.getPlainUserDek(testUserId),
            "KEK为空时应抛出异常");
        
        // 验证异常消息
        String expectedMessage = "获取用户Dek时出现错误,全局主密钥Kek无效!";
        assertEquals(expectedMessage, exception.getMessage());
        System.out.println("捕获到预期异常: " + exception.getMessage());
        
        // 验证Mock交互
        verify(userRepository, never()).findById(any());
        System.out.println("验证通过: userRepository.findById 未被调用");
        System.out.println("========================\n");
    }

    @Test
    @DisplayName("测试用户不存在的情况")
    void testGetPlainUserDekWithUserNotFound() {
        System.out.println("\n=== 测试用户不存在的情况 ===");
        
        // 配置Mock行为 - 用户不存在
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
        System.out.println("Mock设置: userRepository.findById(" + testUserId + ") 返回空");
        
        // 执行测试并验证异常
        BizException exception = assertThrows(BizException.class, 
            () -> contentSecurityService.getPlainUserDek(testUserId),
            "用户不存在时应抛出异常");
        
        // 验证异常消息
        String expectedMessage = "获取用户Dek时出现错误,用户不存在!";
        assertEquals(expectedMessage, exception.getMessage());
        System.out.println("捕获到预期异常: " + exception.getMessage());
        
        // 验证Mock交互
        verify(userRepository, times(1)).findById(testUserId);
        System.out.println("验证通过: userRepository.findById 被调用1次");
        System.out.println("========================\n");
    }

    @Test
    @DisplayName("测试解密DEK失败的情况")
    void testGetPlainUserDekWithDecryptionFailure() throws BizException {
        System.out.println("\n=== 测试解密DEK失败的情况 ===");
        
        // 准备测试数据 - 包含无效加密DEK的用户
        UserPo userPo = new UserPo();
        userPo.setId(testUserId);
        userPo.setEncryptedDek("无效的加密DEK");
        System.out.println("模拟用户数据: " + userPo);
        System.out.println("设置无效的加密DEK: " + userPo.getEncryptedDek());
        
        // 配置Mock行为
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(userPo));
        System.out.println("Mock设置: userRepository.findById(" + testUserId + ") 返回包含无效DEK的用户数据");
        
        // 执行测试并验证异常
        BizException exception = assertThrows(BizException.class, 
            () -> contentSecurityService.getPlainUserDek(testUserId),
            "解密失败时应抛出异常");
        
        // 验证异常消息应包含"解密失败"
        assertTrue(exception.getMessage().contains("解密失败"), 
            "异常消息应包含'解密失败'");
        System.out.println("捕获到预期异常: " + exception.getMessage());
        
        // 验证Mock交互
        verify(userRepository, times(1)).findById(testUserId);
        System.out.println("验证通过: userRepository.findById 被调用1次");
        System.out.println("========================\n");
    }
} 