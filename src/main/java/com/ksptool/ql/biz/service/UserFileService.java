package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.UserFileRepository;
import com.ksptool.ql.biz.model.po.UserFilePo;
import com.ksptool.ql.commons.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils;
import static org.springframework.util.StringUtils.getFilenameExtension;

import java.io.File;
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

    private final GlobalConfigService globalConfigService;
    private final UserFileRepository userFileRepository;

    /**
     * 获取文件存储的根目录
     */
    private Path getStorageBasePath() throws BizException {
        String storagePath = globalConfigService.get("user.file.storage.path");
        if (StringUtils.isBlank(storagePath)) {
            throw new BizException("未配置文件存储路径 它位于全局配置:user.file.storage.path");
        }
        String userDir = System.getProperty("user.dir");
        return Paths.get(userDir, storagePath);
    }

    /**
     * 获取文件的完整存储路径
     */
    private Path getFullPath(String filename) throws BizException {
        return getStorageBasePath().resolve(filename);
    }

    @Transactional
    public UserFilePo receive(MultipartFile file) throws BizException {
        if (file.isEmpty()) {
            throw new BizException("上传的文件不能为空");
        }

        try {
            // 确保存储目录存在
            Path storagePath = getStorageBasePath();
            Files.createDirectories(storagePath);

            Long userId = AuthService.getCurrentUserId();
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFilenameExtension(originalFilename);
            
            // 生成新文件名：userId_UUID.extension
            String newFilename = userId + "_" + UUID.randomUUID() +
                (fileExtension != null ? "." + fileExtension : "");

            // 保存文件
            Path targetPath = getFullPath(newFilename);
            Files.copy(file.getInputStream(), targetPath);

            // 计算SHA256
            String sha256 = calculateSHA256(file);

            // 创建文件记录
            UserFilePo userFile = new UserFilePo();
            userFile.setUserId(userId);
            userFile.setOriginalFilename(originalFilename);
            userFile.setFilepath(newFilename);  // 只存储文件名
            userFile.setFileType(file.getContentType());
            userFile.setFileSize(file.getSize());
            userFile.setSha256(sha256);

            return userFileRepository.save(userFile);

        } catch (IOException e) {
            throw new BizException("文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件名获取文件，并验证用户权限
     * @param filename 文件名
     * @param userId 用户ID
     * @return 文件对象，如果文件不存在或用户无权限则返回null
     */
    public File getUserFile(String filename, Long userId) {
        if (StringUtils.isBlank(filename) || userId == null) {
            return null;
        }

        // 验证文件是否属于该用户
        UserFilePo userFile = userFileRepository.findByFilepathAndUserId(filename, userId);
        if (userFile == null) {
            return null;
        }

        try {
            File file = getFullPath(filename).toFile();
            return file.exists() && file.isFile() ? file : null;
        } catch (BizException e) {
            return null;
        }
    }

    /**
     * 根据文件名获取当前用户的文件
     * @param filename 文件名
     * @return 文件对象，如果文件不存在或用户无权限则返回null
     */
    public File getUserFile(String filename) {
        if (StringUtils.isBlank(filename)) {
            return null;
        }
        return getUserFile(filename, AuthService.getCurrentUserId());
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