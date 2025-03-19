package com.ksptool.ql.commons.utils;

import com.ksptool.ql.commons.exception.BizException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * ChaCha20-Poly1305加密解密工具类
 */
public class ChaCha20Poly1305Cipher {

    private static final String CHACHA20_POLY1305 = "ChaCha20-Poly1305";
    private static final int NONCE_SIZE = 12;
    private static final int KEY_SIZE = 256;

    /**
     * 根据用户名生成安全的加密密钥
     * 确保相同用户名每次生成不同的密钥
     * 
     * @param string 字符串
     * @return 加密密钥(Base64编码)
     * @throws BizException 生成密钥失败时抛出异常
     */
    public static String generateKeyFromString(String string) throws BizException {
        if (string == null || string.isEmpty()) {
            throw new BizException("用户名不能为空");
        }

        try {
            // 使用当前时间戳作为额外熵源
            long timestamp = Instant.now().toEpochMilli();
            String seedMaterial = string + timestamp;
            
            // 使用SHA-256创建混合摘要作为种子
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] mixedSeed = digest.digest(seedMaterial.getBytes(StandardCharsets.UTF_8));
            
            // 使用混合种子初始化安全随机数生成器
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.setSeed(mixedSeed);
            
            // 创建密钥生成器
            KeyGenerator keyGenerator = KeyGenerator.getInstance("ChaCha20");
            keyGenerator.init(KEY_SIZE, secureRandom);
            
            // 生成秘钥
            SecretKey secretKey = keyGenerator.generateKey();
            
            // 将密钥转换为Base64编码的字符串
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
            
        } catch (NoSuchAlgorithmException e) {
            throw new BizException("生成密钥失败: " + e.getMessage());
        }
    }

    /**
     * 使用ChaCha20-Poly1305加密字符串
     * 
     * @param plainText 明文
     * @param key 密钥(Base64编码)
     * @return Base64编码的密文
     * @throws BizException 加密失败时抛出异常
     */
    public static String encrypt(String plainText, String key) throws BizException {
        if (plainText == null || plainText.isEmpty()) {
            return "";
        }

        if (key == null || key.isEmpty()) {
            throw new BizException("密钥不能为空");
        }

        try {
            // 解码Base64密钥
            byte[] keyBytes = Base64.getDecoder().decode(key);
            
            // 生成随机nonce (IV)
            byte[] nonce = new byte[NONCE_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(nonce);
            
            // 创建IV参数规范
            IvParameterSpec ivSpec = new IvParameterSpec(nonce);
            
            // 创建密钥规范
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "ChaCha20");
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(CHACHA20_POLY1305);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            
            // 加密
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // 将nonce和密文合并，并进行Base64编码
            ByteBuffer buffer = ByteBuffer.allocate(nonce.length + encryptedBytes.length);
            buffer.put(nonce);
            buffer.put(encryptedBytes);
            
            return Base64.getEncoder().encodeToString(buffer.array());
            
        } catch (Exception e) {
            throw new BizException("加密失败: " + e.getMessage());
        }
    }

    /**
     * 使用ChaCha20-Poly1305解密字符串
     * 
     * @param cipherText Base64编码的密文
     * @param key 密钥(Base64编码)
     * @return 解密后的明文
     * @throws BizException 解密失败时抛出异常
     */
    public static String decrypt(String cipherText, String key) throws BizException {
        if (cipherText == null || cipherText.isEmpty()) {
            return "";
        }

        if (key == null || key.isEmpty()) {
            throw new BizException("密钥不能为空");
        }

        try {
            // 解码Base64密钥
            byte[] keyBytes = Base64.getDecoder().decode(key);
            
            // 解码Base64
            byte[] cipherData = Base64.getDecoder().decode(cipherText);
            
            // 提取nonce (IV)
            ByteBuffer buffer = ByteBuffer.wrap(cipherData);
            byte[] nonce = new byte[NONCE_SIZE];
            buffer.get(nonce);
            
            // 提取密文
            byte[] encryptedBytes = new byte[cipherData.length - NONCE_SIZE];
            buffer.get(encryptedBytes);
            
            // 创建IV参数规范
            IvParameterSpec ivSpec = new IvParameterSpec(nonce);
            
            // 创建密钥规范
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "ChaCha20");
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(CHACHA20_POLY1305);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            // 解密
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new BizException("解密失败: " + e.getMessage());
        }
    }
} 