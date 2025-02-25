package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.UserFileRepository;
import com.ksptool.ql.biz.model.po.UserFilePo;
import com.ksptool.ql.commons.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import jakarta.transaction.Transactional;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class UserFileService {

    private final ConfigService configService;
    private final UserFileRepository userFileRepository;

    @Transactional
    public UserFilePo receive(MultipartFile file) throws BizException {
        if (file.isEmpty()) {
            throw new BizException("上传的文件不能为空");
        }

        String storagePath = configService.get("user.file.storage.path");
        if (!StringUtils.hasText(storagePath)) {
            throw new BizException("未配置文件存储路径");
        }

        // 获取程序运行路径并拼接存储路径
        String userDir = System.getProperty("user.dir");
        Path absoluteStoragePath = Paths.get(userDir, storagePath);

        try {
            // 确保存储目录存在
            Files.createDirectories(absoluteStoragePath);

            Long userId = AuthService.getCurrentUserId();
            String originalFilename = file.getOriginalFilename();
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            
            // 生成新文件名：userId_UUID.extension
            String newFilename = userId + "_" + UUID.randomUUID().toString() + 
                (fileExtension != null ? "." + fileExtension : "");
            
            Path targetPath = absoluteStoragePath.resolve(newFilename);

            // 保存文件
            Files.copy(file.getInputStream(), targetPath);

            // 计算SHA256
            String sha256 = calculateSHA256(file);

            // 创建文件记录
            UserFilePo userFile = new UserFilePo();
            userFile.setUserId(userId);
            userFile.setOriginalFilename(originalFilename);
            userFile.setFilepath(targetPath.toString());
            userFile.setFileType(file.getContentType());
            userFile.setFileSize(file.getSize());
            userFile.setSha256(sha256);

            return userFileRepository.save(userFile);

        } catch (IOException e) {
            throw new BizException("文件保存失败: " + e.getMessage());
        }
    }

    private String calculateSHA256(MultipartFile file) throws BizException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new BizException("文件哈希计算失败: " + e.getMessage());
        }
    }
} 