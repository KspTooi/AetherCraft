package com.ksptool.ql.biz.service.contentsecurity;

import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.ChaCha20Poly1305Cipher;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 内容安全服务
 */
@Service
public class ContentSecurityService {

    @Autowired
    private UserRepository userRepository;

    @Value("${encrypt.kek}")
    private String globalKek;


    public void process(ModelRpSegmentPo po,boolean encrypt) throws BizException{

    }

    public void process(ModelRpHistoryPo po, boolean encrypt) throws BizException{

    }

    public void process(ModelRpThreadPo po, boolean encrypt) throws BizException{

    }

    public void process(ModelUserRolePo po, boolean encrypt) throws BizException{

    }

    public void process(ModelRolePo po, boolean encrypt) throws BizException{

    }


    public String getPlainUserDek(Long uid) throws BizException {

        if (StringUtils.isBlank(globalKek)) {
            throw new BizException("获取用户Dek时出现错误,全局主密钥Kek无效!");
        }

        UserPo userPo = userRepository.findById(uid).orElseThrow(() -> new BizException("获取用户Dek时出现错误,用户不存在!"));
        var dekCt = userPo.getEncryptedDek();
        return ChaCha20Poly1305Cipher.decrypt(dekCt,globalKek);
    }

}
