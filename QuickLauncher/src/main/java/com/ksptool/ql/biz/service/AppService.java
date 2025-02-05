package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.AppRepository;
import com.ksptool.ql.biz.model.dto.CreateAppDto;
import com.ksptool.ql.biz.model.dto.RunAppDto;
import com.ksptool.ql.biz.model.po.AppPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.commons.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    // 根据当前用户和应用 ID 移除应用，确保只能删除当前用户的应用
    public void removeApp(UserPo user, Long appId) throws BizException {
        int deleted = appRepository.deleteByIdAndUserId(appId, user.getId());
        if (deleted == 0) {
            throw new BizException("无法删除应用，可能不存在或没有删除权限");
        }
    }

    public String runApp(UserPo user, RunAppDto dto) throws BizException {
        // 使用 Repository 根据应用ID和用户ID查询应用记录
        Optional<AppPo> appOpt = appRepository.findByIdAndUserId(dto.getAppId(), user.getId());
        if (appOpt.isEmpty()) {
            throw new BizException("应用不存在或无权限启动");
        }
        AppPo app = appOpt.get();

        // 判断应用类型，0 表示 EXE 才支持启动
        if (app.getKind() != 0) {
            throw new BizException("仅支持EXE类型应用启动");
        }

        try {
            // 调用 Java Runtime 执行应用
            Process process = Runtime.getRuntime().exec(app.getExecPath());

            // 更新应用启动信息
            Integer launchCount = app.getLaunchCount();
            launchCount = (launchCount == null) ? 1 : launchCount + 1;
            app.setLaunchCount(launchCount);
            app.setLastLaunchTime(new Date());
            app.setUpdateTime(new Date());
            appRepository.save(app);

            return "应用启动成功";
        } catch (IOException e) {
            throw new BizException("启动应用失败: " + e.getMessage(), e);
        }
    }
}
