package com.ksptool.ql.biz.controller;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.UserThemeRepository;
import com.ksptool.ql.biz.mapper.UserThemeValuesRepository;
import com.ksptool.ql.biz.model.dto.ActiveThemeDto;
import com.ksptool.ql.biz.model.dto.GetThemeValuesDto;
import com.ksptool.ql.biz.model.dto.RemoveThemeDto;
import com.ksptool.ql.biz.model.dto.SaveThemeDto;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.po.UserThemePo;
import com.ksptool.ql.biz.model.po.UserThemeValues;
import com.ksptool.ql.biz.model.vo.GetActiveThemeVo;
import com.ksptool.ql.biz.model.vo.GetThemeValuesVo;
import com.ksptool.ql.biz.model.vo.GetUserThemeListVo;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import static com.ksptool.entities.Entities.assign;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

@RestController
@RequestMapping("/customize/theme")
public class CustomizeThemeController {
    
    @Autowired
    private UserThemeRepository themeRepository;

    @Autowired
    private UserThemeValuesRepository themeValuesRepository;

    // 用户壁纸路径的配置键
    private static final String USER_WALLPAPER_PATH_KEY = "customize.wallpaper.path";
    
    @GetMapping("/view")
    public ModelAndView customizeView() {
        return new ModelAndView("user-customize/user-customize.html");
    }

    //获取当前用户拥有的主题列表(不分页查全部)
    @PostMapping("/getThemeList")
    public PageableView<GetUserThemeListVo> getThemeList() {
        Long userId = AuthService.getCurrentUserId();
        
        // 查询当前用户所有主题
        List<UserThemePo> themes = themeRepository.findByUserIdWithValues(userId);
        
        // 将PO转换为VO
        List<GetUserThemeListVo> voList = new ArrayList<>();
        for (UserThemePo theme : themes) {
            GetUserThemeListVo vo = new GetUserThemeListVo();
            assign(theme, vo);
            voList.add(vo);
        }
        
        // 返回分页视图(不分页，显示全部)
        return new PageableView<>(voList, voList.size(), 1, voList.size());
    }

    //移除主题
    @PostMapping("/removeTheme")
    @Transactional
    public Result<String> removeTheme(@RequestBody @Valid RemoveThemeDto dto) throws BizException {

        Long uid = AuthService.getCurrentUserId();

        var query = new UserThemePo();
        var user = new UserPo();
        user.setId(uid);
        query.setUser(user);
        query.setId(dto.getThemeId());

        UserThemePo po = themeRepository.findOne(Example.of(query)).orElseThrow(() -> new BizException("未找到主题"));

        if(po.getIsActive() == 1){
            return Result.error("主题正在使用,无法移除.");
        }

        themeRepository.delete(po);
        return Result.success("操作成功");
    }

    //创建或者更新一个主题
    @PostMapping("/saveTheme")
    @Transactional
    public Result<String> saveTheme(@RequestBody @Valid SaveThemeDto dto) {

        Long userId = AuthService.getCurrentUserId();
        UserThemePo theme;
        
        // 根据themeId判断是创建新主题还是更新现有主题
        if (dto.getThemeId() != null) {
            // 查找现有主题
            theme = themeRepository.findById(dto.getThemeId()).orElse(null);
            
            // 验证主题是否存在且属于当前用户
            if (theme == null) {
                return Result.error("主题不存在");
            }
            
            if (!theme.getUser().getId().equals(userId)) {
                return Result.error("无权操作此主题");
            }
        } else {
            // 创建新主题
            theme = new UserThemePo();
            UserPo user = new UserPo();
            user.setId(userId);
            theme.setUser(user);
            theme.setIsActive(0); // 默认非激活
            theme.setIsSystem(0); // 默认非系统主题
        }
        
        // 设置主题名称和描述
        if (StringUtils.isNotBlank(dto.getThemeName())) {
            theme.setThemeName(dto.getThemeName());
        } else if (theme.getId() == null) {
            // 如果是新主题且未提供名称，设置默认名称
            theme.setThemeName("我的主题 " + new Date().getTime());
        }
        
        if (StringUtils.isNotBlank(dto.getDescription())) {
            theme.setDescription(dto.getDescription());
        }
        
        // 保存主题基本信息
        theme.setUpdateTime(new Date());
        themeRepository.save(theme);

        //处理主题值合并
        List<UserThemeValues> themeValues = theme.getThemeValues();

        if (themeValues == null) {
            themeValues = new ArrayList<>();
            theme.setThemeValues(themeValues);
        }

        themeValues.clear();

        Map<String,String> map = Any.of(dto.getThemeValues()).as(Map.class);

        for(var item : map.entrySet()){
            var po = new UserThemeValues();
            po.setTheme(theme);
            po.setThemeKey(item.getKey());
            po.setThemeValue(item.getValue());
            themeValues.add(po);
        }

        themeRepository.save(theme);
        return Result.success(theme.getId().toString());
    }

    //获取主题值
    @PostMapping("/getThemeValues")
    public Result<GetThemeValuesVo> getThemeValuesDetails(@RequestBody @Valid GetThemeValuesDto dto) {
        Long userId = AuthService.getCurrentUserId();
        
        // 参数校验
        if (dto.getThemeId() == null) {
            return Result.error("主题ID不能为空");
        }
        
        // 查找主题
        UserThemePo theme = themeRepository.findById(dto.getThemeId()).orElse(null);
        
        // 验证主题是否存在
        if (theme == null) {
            return Result.error("主题不存在");
        }
        
        // 验证主题是否属于当前用户
        if (!theme.getUser().getId().equals(userId)) {
            return Result.error("无权查看此主题");
        }
        
        // 创建返回对象
        GetThemeValuesVo vo = new GetThemeValuesVo();
        vo.setId(theme.getId());
        vo.setThemeName(theme.getThemeName());
        vo.setDescription(theme.getDescription());
        vo.setIsActive(theme.getIsActive());
        vo.setIsSystem(theme.getIsSystem());
        
        // 获取主题值
        List<UserThemeValues> themeValues = themeValuesRepository.findByTheme_Id(theme.getId());
        Map<String, String> valueMap = new HashMap<>();
        for (UserThemeValues value : themeValues) {
            valueMap.put(value.getThemeKey(), value.getThemeValue());
        }
        vo.setThemeValues(valueMap);
        
        return Result.success(vo);
    }

    //获取当前正在使用的主题
    @PostMapping("getActiveTheme")
    public Result<GetActiveThemeVo> getActiveTheme() {
        Long userId = AuthService.getCurrentUserId();
        
        // 查询当前用户的激活主题
        UserThemePo activeTheme = themeRepository.findActiveThemeByUserId(userId);
        
        // 如果用户没有激活的主题，返回错误
        if (activeTheme == null) {
            return Result.error("您还没有激活的主题");
        }
        
        // 创建返回对象
        GetActiveThemeVo vo = new GetActiveThemeVo();
        vo.setId(activeTheme.getId());
        vo.setThemeName(activeTheme.getThemeName());
        vo.setDescription(activeTheme.getDescription());
        vo.setIsSystem(activeTheme.getIsSystem());
        
        // 获取主题值
        Map<String, String> themeValues = new HashMap<>();
        if (activeTheme.getThemeValues() != null) {
            for (UserThemeValues value : activeTheme.getThemeValues()) {
                themeValues.put(value.getThemeKey(), value.getThemeValue());
            }
        }
        vo.setThemeValues(themeValues);
        
        return Result.success(vo);
    }
    
    /**
     * 激活指定的主题
     * 将当前用户下的指定主题设置为默认，同时将其他主题设置为非默认
     */
    @PostMapping("/activeTheme")
    @Transactional
    public Result<String> activeTheme(@RequestBody @Valid ActiveThemeDto dto) {
        Long userId = AuthService.getCurrentUserId();
        
        // 参数校验
        if (dto.getThemeId() == null) {
            return Result.error("主题ID不能为空");
        }
        
        // 查找要激活的主题
        UserThemePo themeToActive = themeRepository.findById(dto.getThemeId()).orElse(null);
        
        // 验证主题是否存在
        if (themeToActive == null) {
            return Result.error("要激活的主题不存在");
        }
        
        // 验证主题是否属于当前用户
        if (!themeToActive.getUser().getId().equals(userId)) {
            return Result.error("无权操作此主题");
        }
        
        // 查询当前用户的所有主题
        List<UserThemePo> userThemePos = themeRepository.findByUserIdWithValues(userId);
        
        // 将所有主题设置为非激活状态
        for (UserThemePo theme : userThemePos) {
            // 只修改激活状态非当前值的主题
            if (theme.getIsActive() != 0) {
                theme.setIsActive(0);
                theme.setUpdateTime(new Date());
                themeRepository.save(theme);
            }
        }
        
        // 将目标主题设置为激活状态
        themeToActive.setIsActive(1);
        themeToActive.setUpdateTime(new Date());
        themeRepository.save(themeToActive);
        
        return Result.success("主题激活成功");
    }

} 