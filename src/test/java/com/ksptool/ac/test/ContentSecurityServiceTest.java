package com.ksptool.ac.test;

import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.ChaCha20Poly1305Cipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ContentSecurityServiceTest {

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PLAIN_TEXT = "这是一段需要加密的测试文本，包含中文和特殊字符!@#$%^&*()";
    
    // private ChaCha20Poly1305Cipher contentSecurityService; // 不需要实例化 ChaCha20Poly1305Cipher


    @Test
    @DisplayName("测试根据用户名生成密钥")
    void testGenerateKeyFromUsername() throws BizException {
        // 测试生成密钥
        String key1 = ChaCha20Poly1305Cipher.generateKeyFromString(TEST_USERNAME);
        System.out.println("用户名: " + TEST_USERNAME);
        System.out.println("生成密钥1: " + key1);
        assertNotNull(key1, "生成的密钥不应为null");
        assertFalse(key1.isEmpty(), "生成的密钥不应为空");
        
        // 测试相同用户名生成不同密钥
        String key2 = ChaCha20Poly1305Cipher.generateKeyFromString(TEST_USERNAME);
        System.out.println("生成密钥2: " + key2);
        assertNotNull(key2, "生成的第二个密钥不应为null");
        assertNotEquals(key1, key2, "相同用户名应生成不同的密钥");
        
        // 测试空用户名异常
        BizException exception = assertThrows(BizException.class, () ->
                        ChaCha20Poly1305Cipher.generateKeyFromString(""),
            "空用户名应抛出BizException");
        assertEquals("用户名不能为空", exception.getMessage());
        
        // 测试null用户名异常
        exception = assertThrows(BizException.class, () ->
                        ChaCha20Poly1305Cipher.generateKeyFromString(null),
            "null用户名应抛出BizException");
        assertEquals("用户名不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("测试加密字符串")
    void testEncrypt() throws BizException {
        // 生成密钥
        String key = ChaCha20Poly1305Cipher.generateKeyFromString(TEST_USERNAME);
        System.out.println("\n测试加密字符串");
        System.out.println("密钥: " + key);
        System.out.println("明文: " + TEST_PLAIN_TEXT);
        
        // 测试加密
        String encrypted = ChaCha20Poly1305Cipher.encrypt(TEST_PLAIN_TEXT, key);
        System.out.println("密文: " + encrypted);
        assertNotNull(encrypted, "加密结果不应为null");
        assertFalse(encrypted.isEmpty(), "加密结果不应为空");
        assertFalse(TEST_PLAIN_TEXT.equals(encrypted), "加密结果不应与原文相同");
        
        // 测试相同文本每次加密结果不同
        String encrypted2 = ChaCha20Poly1305Cipher.encrypt(TEST_PLAIN_TEXT, key);
        System.out.println("相同明文再次加密: " + encrypted2);
        assertNotEquals(encrypted, encrypted2, "相同文本每次加密结果应不同");
        
        // 测试空文本加密
        String emptyEncrypted = ChaCha20Poly1305Cipher.encrypt("", key);
        System.out.println("空文本加密: " + emptyEncrypted);
        assertEquals("", emptyEncrypted, "空文本加密应返回空字符串");
        
        // 测试null文本加密
        String nullEncrypted = ChaCha20Poly1305Cipher.encrypt(null, key);
        System.out.println("null文本加密: " + nullEncrypted);
        assertEquals("", nullEncrypted, "null文本加密应返回空字符串");
        
        // 测试空密钥异常
        BizException exception = assertThrows(BizException.class, () -> 
            ChaCha20Poly1305Cipher.encrypt(TEST_PLAIN_TEXT, ""),
            "空密钥应抛出BizException");
        assertEquals("密钥不能为空", exception.getMessage());
        
        // 测试null密钥异常
        exception = assertThrows(BizException.class, () -> 
            ChaCha20Poly1305Cipher.encrypt(TEST_PLAIN_TEXT, null),
            "null密钥应抛出BizException");
        assertEquals("密钥不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("测试解密字符串")
    void testDecrypt() throws BizException {
        // 生成密钥
        String key = ChaCha20Poly1305Cipher.generateKeyFromString(TEST_USERNAME);
        System.out.println("\n测试解密字符串");
        System.out.println("密钥: " + key);
        System.out.println("明文: " + TEST_PLAIN_TEXT);
        
        // 加密文本
        String encrypted = ChaCha20Poly1305Cipher.encrypt(TEST_PLAIN_TEXT, key);
        System.out.println("密文: " + encrypted);
        
        // 测试解密
        String decrypted = ChaCha20Poly1305Cipher.decrypt(encrypted, key);
        System.out.println("解密结果: " + decrypted);
        assertNotNull(decrypted, "解密结果不应为null");
        assertEquals(TEST_PLAIN_TEXT, decrypted, "解密结果应与原文相同");
        
        // 测试空密文解密
        String emptyDecrypted = ChaCha20Poly1305Cipher.decrypt("", key);
        System.out.println("空密文解密: " + emptyDecrypted);
        assertEquals("", emptyDecrypted, "空密文解密应返回空字符串");
        
        // 测试null密文解密
        String nullDecrypted = ChaCha20Poly1305Cipher.decrypt(null, key);
        System.out.println("null密文解密: " + nullDecrypted);
        assertEquals("", nullDecrypted, "null密文解密应返回空字符串");
        
        // 测试空密钥异常
        BizException exception = assertThrows(BizException.class, () -> 
            ChaCha20Poly1305Cipher.decrypt(encrypted, ""),
            "空密钥应抛出BizException");
        assertEquals("密钥不能为空", exception.getMessage());
        
        // 测试null密钥异常
        exception = assertThrows(BizException.class, () -> 
            ChaCha20Poly1305Cipher.decrypt(encrypted, null),
            "null密钥应抛出BizException");
        assertEquals("密钥不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("测试加密解密完整流程")
    void testEncryptDecryptFlow() throws BizException {
        System.out.println("\n测试加密解密完整流程");
        
        // 测试多种不同类型的文本
        String[] testTexts = {
            "简单文本",
            "带有特殊字符的文本!@#$%^&*()",
            "包含\n换行\t制表符的文本",
            "非常长的文本" + "a".repeat(1000),
            "JSON格式文本: {\"name\":\"测试\", \"age\":30, \"data\":[1,2,3]}",
            "包含表情符号的文本😀🤔🎉💯"
        };
        
        for (int i = 0; i < testTexts.length; i++) {
            String text = testTexts[i];
            System.out.println("\n测试文本" + (i+1) + ": " + (text.length() > 50 ? text.substring(0, 47) + "..." : text));
            
            // 生成密钥
            String key = ChaCha20Poly1305Cipher.generateKeyFromString(TEST_USERNAME);
            System.out.println("密钥: " + key);
            
            // 加密
            String encrypted = ChaCha20Poly1305Cipher.encrypt(text, key);
            System.out.println("密文: " + encrypted);
            
            // 解密
            String decrypted = ChaCha20Poly1305Cipher.decrypt(encrypted, key);
            System.out.println("解密结果: " + (decrypted.length() > 50 ? decrypted.substring(0, 47) + "..." : decrypted));
            
            // 验证结果
            assertEquals(text, decrypted, "解密后的文本应与原文相同");
        }
    }
    
    @Test
    @DisplayName("测试使用错误密钥解密")
    void testDecryptWithWrongKey() throws BizException {
        System.out.println("\n测试使用错误密钥解密");
        
        // 生成两个不同的密钥
        String key1 = ChaCha20Poly1305Cipher.generateKeyFromString(TEST_USERNAME);
        String key2 = ChaCha20Poly1305Cipher.generateKeyFromString(TEST_USERNAME + "_different");
        System.out.println("密钥1: " + key1);
        System.out.println("密钥2: " + key2);
        System.out.println("明文: " + TEST_PLAIN_TEXT);
        
        // 使用key1加密
        String encrypted = ChaCha20Poly1305Cipher.encrypt(TEST_PLAIN_TEXT, key1);
        System.out.println("使用密钥1加密的密文: " + encrypted);
        
        // 尝试使用key2解密，应抛出异常
        try {
            String decrypted = ChaCha20Poly1305Cipher.decrypt(encrypted, key2);
            System.out.println("使用密钥2解密的结果(预期会抛出异常): " + decrypted);
        } catch (BizException e) {
            System.out.println("使用错误密钥解密出现预期异常: " + e.getMessage());
        }
        
        assertThrows(BizException.class, () -> 
            ChaCha20Poly1305Cipher.decrypt(encrypted, key2),
            "使用错误密钥解密应抛出异常");
    }
} 