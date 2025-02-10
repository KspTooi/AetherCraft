package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.AppRepository;
import com.ksptool.ql.biz.model.dto.CreateAppDto;
import com.ksptool.ql.biz.model.dto.EditAppDto;
import com.ksptool.ql.biz.model.dto.RunAppDto;
import com.ksptool.ql.biz.model.po.AppPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.commons.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ksptool.entities.Entities.assign;

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

    // 根据文件路径判断应用类型
    private String determineAppKind(String execPath) {
        File file = new File(execPath);
        
        // 检查是否为文件夹
        if (file.isDirectory()) {
            return "文件夹";
        }
        
        // 获取文件名
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        
        // 如果没有后缀名
        if (lastDotIndex == -1) {
            return "未知";
        }
        
        // 返回大写的后缀名
        return fileName.substring(lastDotIndex + 1).toUpperCase();
    }

    // 创建新的应用
    public AppPo createApp(UserPo user, CreateAppDto dto) throws BizException {
        // 检查应用名称是否重复
        AppPo query = new AppPo();
        query.setUserId(user.getId());
        query.setName(dto.getName());

        if(appRepository.count(Example.of(query)) > 0){
            throw new BizException("应用名称已存在，请使用其他名称 " + dto.getName());
        }

        // 处理command字段
        String command = dto.getCommand();
        if (!StringUtils.isBlank(command)) {
            command = command.trim();
            // 如果指定了command,检查是否重复
            AppPo commandQuery = new AppPo();
            commandQuery.setCommand(command);
            if(appRepository.count(Example.of(commandQuery)) > 0) {
                throw new BizException("快捷启动命令已存在，请使用其他命令: " + command);
            }
        } else {
            command = null;
        }

        AppPo app = new AppPo();
        app.setUserId(user.getId());
        app.setName(dto.getName());
        app.setExecPath(dto.getExecPath());
        app.setDescription(dto.getDescription());
        app.setCommand(command);
        // 如果用户指定了类型就使用用户指定的，否则自动推断
        app.setKind(StringUtils.isBlank(dto.getKind()) 
            ? determineAppKind(dto.getExecPath())
            : dto.getKind().trim());
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

        // 检查文件路径是否存在
        File file = new File(app.getExecPath());
        if (!file.exists()) {
            throw new BizException("应用路径不存在: " + app.getExecPath());
        }

        try {
            // 统一使用explorer启动所有应用
            String[] command = {"explorer", app.getExecPath()};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

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

    // 根据用户和应用ID获取应用详情
    public AppPo getAppById(UserPo user, Long appId) throws BizException {
        if (user == null || appId == null) {
            throw new BizException("参数无效");
        }
        AppPo app = appRepository.findByIdAndUserId(appId, user.getId())
                .orElseThrow(() -> new BizException("应用不存在或无权限访问"));
        return app;
    }

    // 更新应用信息
    public AppPo editApp(UserPo user, EditAppDto dto) throws BizException {
        // 检查应用是否存在且属于当前用户
        AppPo app = getAppById(user, dto.getId());
        
        // 检查新名称是否与其他应用重复
        if (!app.getName().equals(dto.getName()) && existsAppByNameForUser(user, dto.getName())) {
            throw new BizException("应用名称已存在，请使用其他名称: " + dto.getName());
        }

        // 处理command字段
        String newCommand = dto.getCommand();
        if (!StringUtils.isBlank(newCommand)) {
            newCommand = newCommand.trim();
            if (!newCommand.equals(app.getCommand())) {
                // 检查新command是否与其他应用重复
                AppPo commandQuery = new AppPo();
                commandQuery.setCommand(newCommand);
                if(appRepository.count(Example.of(commandQuery)) > 0) {
                    throw new BizException("快捷启动命令已存在，请使用其他命令: " + newCommand);
                }
            }
        } else {
            newCommand = null;
        }

        assign(dto, app);
        app.setCommand(newCommand);
        // 如果用户指定了类型就使用用户指定的，否则自动推断
        app.setKind(StringUtils.isBlank(dto.getKind())
            ? determineAppKind(dto.getExecPath())
            : dto.getKind().trim());
        app.setUpdateTime(new Date());
        
        return appRepository.save(app);
    }

    // 根据关键字搜索应用
    public List<AppPo> searchApps(Long userId, String keyword) {
        AppPo query = new AppPo();
        query.setUserId(userId);
        Example<AppPo> example = Example.of(query);
        
        // 获取所有用户的应用，然后在内存中过滤
        return appRepository.findAll(example, Sort.by(Sort.Direction.DESC, "updateTime"))
                .stream()
                .filter(app -> matchesKeyword(app, keyword))
                .collect(Collectors.toList());
    }

    // 检查应用是否匹配关键字
    private boolean matchesKeyword(AppPo app, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return true;
        }
        keyword = keyword.toLowerCase();
        return app.getName().toLowerCase().contains(keyword) ||
               app.getExecPath().toLowerCase().contains(keyword);
    }
}
