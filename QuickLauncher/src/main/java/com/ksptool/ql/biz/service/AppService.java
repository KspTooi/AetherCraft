package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.AppRepository;
import com.ksptool.ql.biz.model.dto.CreateAppDto;
import com.ksptool.ql.biz.model.po.AppPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AppService {

    @Autowired
    private AppRepository appRepository;


    public List<AppPo> getAppListByUserId(long userId) {
        AppPo query = new AppPo();
        query.setUserId(userId);
        Example<AppPo> example = Example.of(query);
        // 按 updateTime 降序排序，最新的排在前面
        return appRepository.findAll(example, Sort.by(Sort.Direction.DESC, "updateTime"));
    }


    public int getAppCountByUser(UserPo user) {
        AppPo query = new AppPo();
        query.setUserId(user.getId());
        long count = appRepository.count(Example.of(query));
        return (int) count;
    }

    // 检查用户下是否存在同名应用
    public boolean existsAppByNameForUser(UserPo user, String name) {
        AppPo query = new AppPo();
        query.setUserId(user.getId());
        query.setName(name);
        long count = appRepository.count(Example.of(query));
        return count > 0;
    }

    // 创建新的应用
    public AppPo createApp(UserPo user, CreateAppDto dto) throws BizException {

        AppPo query = new AppPo();
        query.setUserId(user.getId());
        query.setName(dto.getName());

        if(appRepository.count(Example.of(query)) > 0){
            throw new BizException("应用名称已存在，请使用其他名称 " + dto.getName());
        }

        AppPo app = new AppPo();
        app.setUserId(user.getId());
        app.setName(dto.getName());
        app.setKind(dto.getKind());
        app.setExecPath(dto.getExecPath());
        app.setLaunchCount(0);
        Date now = new Date();
        app.setCreateTime(now);
        app.setUpdateTime(now);
        return appRepository.save(app);
    }
}
