package com.ksptool.ql.biz.service.contentsecurity;

import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.ChaCha20Poly1305Cipher;
import com.ksptool.ql.biz.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内容安全服务
 */
@Slf4j
@Service
public class ContentSecurityService {

    @Autowired
    private UserRepository userRepository;

    @Value("${encrypt.kek}")
    private String globalKek;

    /**
     * 用户DEK缓存
     * key: userId
     * value: 加密后的DEK
     */
    private final Map<Long, String> userDekCache = new ConcurrentHashMap<>();

    private String encrypt(String content, String key) throws BizException {
        return ChaCha20Poly1305Cipher.encrypt(content, key);
    }

    private String decrypt(String content, String key) {
        if(content == null) {
            return null;
        }
        try {
            return ChaCha20Poly1305Cipher.decrypt(content, key);
        } catch (Exception e) {
            return content;
        }
    }

    public void encryptEntity(ModelRpSegmentPo po) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(po.getUserId());
        po.setContent(encrypt(po.getContent(), dek));
    }

    public void encryptEntity(PlayerPo po) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(po.getUser().getId());
        po.setDescription(encrypt(po.getDescription(), dek));
        po.setGenderData(encrypt(po.getGenderData(), dek));
    }

    public void encryptEntity(ModelRpHistoryPo po) throws BizException{
        if(po == null) {
            return;
        }

        Long uid = null;

        if(po.getThread()!=null){
            uid = po.getThread().getUserId();
        }
        if(uid == null){
            uid = AuthService.getCurrentUserId();
        }
        String dek = getPlainUserDek(uid);
        po.setRawContent(encrypt(po.getRawContent(), dek));
        po.setRpContent(encrypt(po.getRpContent(), dek));
    }

    public void encryptEntity(ModelChatHistoryPo po,Long uid) throws BizException {
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(uid);
        po.setContent(encrypt(po.getContent(), dek));
    }

    public void encryptEntity(ModelChatSegmentPo po,Long uid) throws BizException {
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(uid);
        po.setContent(encrypt(po.getContent(), dek));
    }

    public void encryptEntity(ModelChatThreadPo po) throws BizException {
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(AuthService.getCurrentUserId());
        po.setTitle(encrypt(po.getTitle(), dek));
    }

    public void encryptEntity(ModelRpThreadPo po) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(po.getUserId());
        po.setTitle(encrypt(po.getTitle(), dek));
        po.setDescription(encrypt(po.getDescription(), dek));
    }

    public void encryptEntity(ModelUserRolePo po) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(po.getUserId());
        po.setAvatarPath(encrypt(po.getAvatarPath(), dek));
        po.setDescription(encrypt(po.getDescription(), dek));
    }

    public void encryptEntity(ModelRolePo po) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(AuthService.getCurrentUserId());
        po.setAvatarPath(encrypt(po.getAvatarPath(), dek));
        po.setDescription(encrypt(po.getDescription(), dek));
        po.setRoleSummary(encrypt(po.getRoleSummary(), dek));
        po.setScenario(encrypt(po.getScenario(), dek));
        po.setFirstMessage(encrypt(po.getFirstMessage(), dek));
        po.setTags(encrypt(po.getTags(), dek));
    }

    public void encryptEntity(ModelRoleChatExamplePo po) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(AuthService.getCurrentUserId());
        po.setContent(encrypt(po.getContent(), dek));
    }

    public void processList(List<?> poList, boolean encrypt) throws BizException {
        if(poList == null || poList.isEmpty()) {
            return;
        }

        Object firstElement = poList.getFirst();
        
        if(firstElement instanceof ModelUserRolePo) {
            @SuppressWarnings("unchecked")
            List<ModelUserRolePo> userRoleList = (List<ModelUserRolePo>) poList;
            String dek = getPlainUserDek(userRoleList.getFirst().getUserId());
            
            if(encrypt) {
                for(ModelUserRolePo po : userRoleList) {
                    po.setAvatarPath(encrypt(po.getAvatarPath(), dek));
                    po.setDescription(encrypt(po.getDescription(), dek));
                }
                return;
            }
            
            for(ModelUserRolePo po : userRoleList) {
                po.setAvatarPath(decrypt(po.getAvatarPath(), dek));
                po.setDescription(decrypt(po.getDescription(), dek));
            }
            return;
        }

        if(firstElement instanceof ModelRolePo) {
            @SuppressWarnings("unchecked")
            List<ModelRolePo> roleList = (List<ModelRolePo>) poList;
            String dek = getPlainUserDek(AuthService.getCurrentUserId());
            
            if(encrypt) {
                for(ModelRolePo po : roleList) {
                    po.setAvatarPath(encrypt(po.getAvatarPath(), dek));
                    po.setDescription(encrypt(po.getDescription(), dek));
                    po.setRoleSummary(encrypt(po.getRoleSummary(), dek));
                    po.setScenario(encrypt(po.getScenario(), dek));
                    po.setFirstMessage(encrypt(po.getFirstMessage(), dek));
                    po.setTags(encrypt(po.getTags(), dek));
                }
                return;
            }
            
            for(ModelRolePo po : roleList) {
                po.setAvatarPath(decrypt(po.getAvatarPath(), dek));
                po.setDescription(decrypt(po.getDescription(), dek));
                po.setRoleSummary(decrypt(po.getRoleSummary(), dek));
                po.setScenario(decrypt(po.getScenario(), dek));
                po.setFirstMessage(decrypt(po.getFirstMessage(), dek));
                po.setTags(decrypt(po.getTags(), dek));
            }
            return;
        }

        if(firstElement instanceof ModelRoleChatExamplePo) {
            @SuppressWarnings("unchecked")
            List<ModelRoleChatExamplePo> exampleList = (List<ModelRoleChatExamplePo>) poList;
            String dek = getPlainUserDek(AuthService.getCurrentUserId());
            
            if(encrypt) {
                for(ModelRoleChatExamplePo po : exampleList) {
                    po.setContent(encrypt(po.getContent(), dek));
                }
                return;
            }
            
            for(ModelRoleChatExamplePo po : exampleList) {
                po.setContent(decrypt(po.getContent(), dek));
            }
            return;
        }

        if(firstElement instanceof ModelRpHistoryPo) {
            @SuppressWarnings("unchecked")
            List<ModelRpHistoryPo> historyList = (List<ModelRpHistoryPo>) poList;
            if(historyList.isEmpty()) {
                return;
            }
            String dek = getPlainUserDek(AuthService.getCurrentUserId());
            
            if(encrypt) {
                for(ModelRpHistoryPo po : historyList) {
                    po.setRawContent(encrypt(po.getRawContent(), dek));
                    po.setRpContent(encrypt(po.getRpContent(), dek));
                }
                return;
            }
            
            for(ModelRpHistoryPo po : historyList) {
                po.setRawContent(decrypt(po.getRawContent(), dek));
                po.setRpContent(decrypt(po.getRpContent(), dek));
            }
            return;
        }

        if(firstElement instanceof ModelRpSegmentPo) {
            @SuppressWarnings("unchecked")
            List<ModelRpSegmentPo> segmentList = (List<ModelRpSegmentPo>) poList;
            if(segmentList.isEmpty()) {
                return;
            }
            String dek = getPlainUserDek(AuthService.getCurrentUserId());
            
            if(encrypt) {
                for(ModelRpSegmentPo po : segmentList) {
                    po.setContent(encrypt(po.getContent(), dek));
                }
                return;
            }
            
            for(ModelRpSegmentPo po : segmentList) {
                po.setContent(decrypt(po.getContent(), dek));
            }
        }
    }

    /**
     * 检查给定的路径是否为内部路径（非绝对URL）.
     * @param path 待检查的路径字符串
     * @return 如果是内部路径（不以 http://, https://, // 开头），则返回 true，否则返回 false。
     */
    public boolean isInternalPath(String path) {
        if (StringUtils.isBlank(path)) {
            return true; // null 或空字符串视为内部路径或无效路径
        }
        String lowerCasePath = path.trim().toLowerCase();
        if (lowerCasePath.startsWith("http://") || lowerCasePath.startsWith("https://") || lowerCasePath.startsWith("//")) {
            return false; // 以协议或协议相对路径开头的视为外部路径
        }
        return true; // 其他情况视为内部路径
    }

    public String decryptForCurUser(String content) {
        if(content == null) {
            return null;
        }
        if(StringUtils.isBlank(content)) {
            return "";
        }
        try {
            Long curUserId = AuthService.getCurrentUserId();
            if(curUserId == null) {
                log.warn("解密失败: 当前用户ID为空");
                return content;
            }
            String dek = getPlainUserDek(curUserId);
            return decrypt(content, dek);
        } catch (Exception e) {
            log.error("解密内容失败: {}", e.getMessage());
            return content;
        }
    }

    public String decryptForCurUser(String content,String dekPt) {
        if(content == null) {
            return null;
        }
        if(StringUtils.isBlank(content)) {
            return "";
        }
        try {
            return decrypt(content, dekPt);
        } catch (Exception e) {
            log.error("解密内容失败: {}", e.getMessage());
            return content;
        }
    }

    public String getPlainUserDek(Long uid) throws BizException {

        if (StringUtils.isBlank(globalKek)) {
            throw new BizException("获取用户Dek时出现错误,全局主密钥Kek无效!");
        }

        // 从缓存中获取加密的DEK
        String dekCt = userDekCache.get(uid);

        if(dekCt != null) {
            return decrypt(dekCt, globalKek);
        }

        UserPo userPo = userRepository.findById(uid).orElseThrow(() -> new BizException("获取用户Dek时出现错误,用户不存在!"));

        if(StringUtils.isBlank(userPo.getEncryptedDek())){
            var dekPt = ChaCha20Poly1305Cipher.generateKeyFromString(userPo.getUsername());
            dekCt = ChaCha20Poly1305Cipher.encrypt(dekPt,globalKek);
            userPo.setEncryptedDek(dekCt);
            log.info("为用户: {} 创建新的DEK", userPo.getUsername());
            userRepository.save(userPo);
        } else {
            dekCt = userPo.getEncryptedDek();
        }

        // 将加密的DEK存入缓存
        userDekCache.put(uid, dekCt);
        return decrypt(dekCt,globalKek);
    }

}
