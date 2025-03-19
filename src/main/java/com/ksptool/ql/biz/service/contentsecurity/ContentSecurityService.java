package com.ksptool.ql.biz.service.contentsecurity;

import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.ChaCha20Poly1305Cipher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void process(ModelRpSegmentPo po, boolean encrypt) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(po.getUserId());
        if(encrypt) {
            po.setContent(encrypt(po.getContent(), dek));
            return;
        }
        po.setContent(decrypt(po.getContent(), dek));
    }

    public void process(ModelRpHistoryPo po, boolean encrypt) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(po.getThread().getUserId());
        if(encrypt) {
            po.setRawContent(encrypt(po.getRawContent(), dek));
            po.setRpContent(encrypt(po.getRpContent(), dek));
            return;
        }
        po.setRawContent(decrypt(po.getRawContent(), dek));
        po.setRpContent(decrypt(po.getRpContent(), dek));
    }

    public void process(ModelRpThreadPo po, boolean encrypt) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(po.getUserId());
        if(encrypt) {
            po.setTitle(encrypt(po.getTitle(), dek));
            po.setDescription(encrypt(po.getDescription(), dek));
            return;
        }
        po.setTitle(decrypt(po.getTitle(), dek));
        po.setDescription(decrypt(po.getDescription(), dek));
    }

    public void process(ModelUserRolePo po, boolean encrypt) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(po.getUserId());
        if(encrypt) {
            po.setAvatarPath(encrypt(po.getAvatarPath(), dek));
            po.setDescription(encrypt(po.getDescription(), dek));
            return;
        }
        po.setAvatarPath(decrypt(po.getAvatarPath(), dek));
        po.setDescription(decrypt(po.getDescription(), dek));
    }

    public void process(ModelRolePo po, boolean encrypt) throws BizException{
        if(po == null) {
            return;
        }
        String dek = getPlainUserDek(po.getUserId());
        if(encrypt) {
            po.setAvatarPath(encrypt(po.getAvatarPath(), dek));
            po.setDescription(encrypt(po.getDescription(), dek));
            po.setRoleSummary(encrypt(po.getRoleSummary(), dek));
            po.setScenario(encrypt(po.getScenario(), dek));
            po.setFirstMessage(encrypt(po.getFirstMessage(), dek));
            po.setTags(encrypt(po.getTags(), dek));
            return;
        }
        po.setAvatarPath(decrypt(po.getAvatarPath(), dek));
        po.setDescription(decrypt(po.getDescription(), dek));
        po.setRoleSummary(decrypt(po.getRoleSummary(), dek));
        po.setScenario(decrypt(po.getScenario(), dek));
        po.setFirstMessage(decrypt(po.getFirstMessage(), dek));
        po.setTags(decrypt(po.getTags(), dek));
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
            String dek = getPlainUserDek(roleList.getFirst().getUserId());
            
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
        }
    }

    public String getPlainUserDek(Long uid) throws BizException {

        if (StringUtils.isBlank(globalKek)) {
            throw new BizException("获取用户Dek时出现错误,全局主密钥Kek无效!");
        }

        UserPo userPo = userRepository.findById(uid).orElseThrow(() -> new BizException("获取用户Dek时出现错误,用户不存在!"));

        if(StringUtils.isBlank(userPo.getEncryptedDek())){
            var dekPt = ChaCha20Poly1305Cipher.generateKeyFromString(userPo.getUsername());
            userPo.setEncryptedDek(ChaCha20Poly1305Cipher.encrypt(dekPt,globalKek));
            log.info("为用户: {} 创建新的DEK", userPo.getUsername());
            userRepository.save(userPo);
        }

        var dekCt = userPo.getEncryptedDek();
        return decrypt(dekCt,globalKek);
    }

}
