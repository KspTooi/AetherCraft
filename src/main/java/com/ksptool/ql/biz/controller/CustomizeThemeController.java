package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.mapper.PlayerThemeRepository;
import com.ksptool.ql.biz.mapper.PlayerThemeValuesRepository;
import com.ksptool.ql.biz.model.dto.*;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.po.PlayerThemePo;
import com.ksptool.ql.biz.model.po.PlayerThemeValues;
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
    private PlayerThemeRepository themeRepository;

    @Autowired
    private PlayerThemeValuesRepository themeValuesRepository;
    
    @GetMapping("/view")
    public ModelAndView customizeView() {
        return new ModelAndView("user-customize/user-customize.html");
    }

    //获取当前玩家拥有的主题列表(不分页查全部)
    @PostMapping("/getThemeList")
    public Result<PageableView<GetUserThemeListVo>> getThemeList() {
        Long userId = AuthService.getCurrentUserId();
        
        // 查询当前玩家所有主题
        List<PlayerThemePo> themes = themeRepository.findByPlayerIdWithValues(userId);
        
        // 将PO转换为VO
        List<GetUserThemeListVo> voList = new ArrayList<>();
        for (PlayerThemePo theme : themes) {
            GetUserThemeListVo vo = new GetUserThemeListVo();
            assign(theme, vo);
            voList.add(vo);
        }
        
        // 返回分页视图(不分页，显示全部)
        return Result.success(new PageableView<>(voList, voList.size(), 1, voList.size()));
    }

    //移除主题
    @PostMapping("/removeTheme")
    @Transactional
    public Result<String> removeTheme(@RequestBody @Valid RemoveThemeDto dto) throws BizException {


        var query = new PlayerThemePo();
        var player = new PlayerPo();
        player.setId(AuthService.getCurrentPlayerId());
        query.setPlayer(player);
        query.setId(dto.getThemeId());

        PlayerThemePo po = themeRepository.findOne(Example.of(query)).orElseThrow(() -> new BizException("未找到主题"));

        if(po.getIsActive() == 1){
            return Result.error("主题正在使用,无法移除.");
        }

        themeRepository.delete(po);
        return Result.success("操作成功");
    }

    @PostMapping("/copyTheme")
    @Transactional
    public Result<String> copyTheme(@RequestBody @Valid GetThemeValuesDto dto) throws BizException {

        // 查找要复制的主题
        PlayerThemePo sourceTheme = themeRepository.findById(dto.getThemeId()).orElse(null);
        
        // 验证主题是否存在
        if (sourceTheme == null) {
            return Result.error("要复制的主题不存在");
        }
        
        // 验证主题是否属于当前玩家
        if (!sourceTheme.getPlayer().getId().equals(AuthService.getCurrentPlayerId())) {
            return Result.error("无权复制此主题");
        }
        
        // 创建新主题
        PlayerThemePo newTheme = new PlayerThemePo();
        // 复制原主题基本信息
        assign(sourceTheme, newTheme);
        // 设置新ID为null，使JPA生成新ID
        newTheme.setId(null);
        // 设置新名称
        newTheme.setThemeName(sourceTheme.getThemeName() + " - 副本");
        // 设置为非激活状态
        newTheme.setIsActive(0);
        // 设置所属玩家
        PlayerPo user = new PlayerPo();
        user.setId(AuthService.getCurrentPlayerId());
        newTheme.setPlayer(user);
        // 更新时间
        newTheme.setUpdateTime(new Date());
        // 清空主题值，后面会重新添加
        newTheme.setThemeValues(new ArrayList<>());
        
        // 保存新主题以获取ID
        themeRepository.save(newTheme);
        
        // 复制主题值
        if (sourceTheme.getThemeValues() != null) {
            List<PlayerThemeValues> newThemeValues = new ArrayList<>();
            
            for (PlayerThemeValues sourceValue : sourceTheme.getThemeValues()) {
                PlayerThemeValues newValue = new PlayerThemeValues();
                newValue.setTheme(newTheme);
                newValue.setThemeKey(sourceValue.getThemeKey());
                newValue.setThemeValue(sourceValue.getThemeValue());
                newThemeValues.add(newValue);
            }
            
            newTheme.setThemeValues(newThemeValues);
            themeRepository.save(newTheme);
        }
        
        return Result.success(newTheme.getId().toString());
    }

    //创建或者更新一个主题
    @PostMapping("/saveTheme")
    @Transactional
    public Result<String> saveTheme(@RequestBody @Valid SaveThemeDto dto) {

        PlayerThemePo theme;
        
        // 根据themeId判断是创建新主题还是更新现有主题
        if (dto.getThemeId() != null) {
            // 查找现有主题
            theme = themeRepository.findById(dto.getThemeId()).orElse(null);
            
            // 验证主题是否存在且属于当前玩家
            if (theme == null) {
                return Result.error("主题不存在");
            }
            
            if (!theme.getPlayer().getId().equals(AuthService.getCurrentPlayerId())) {
                return Result.error("无权操作此主题");
            }
        } else {
            // 创建新主题
            theme = new PlayerThemePo();
            PlayerPo player = new PlayerPo();
            player.setId(AuthService.getCurrentPlayerId());
            theme.setPlayer(player);
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
        List<PlayerThemeValues> themeValues = theme.getThemeValues();

        if (themeValues == null) {
            themeValues = new ArrayList<>();
            theme.setThemeValues(themeValues);
        }

        themeValues.clear();

        for(var item : dto.getThemeValues().entrySet()){
            var po = new PlayerThemeValues();
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

        // 参数校验
        if (dto.getThemeId() == null) {
            return Result.error("主题ID不能为空");
        }
        
        // 查找主题
        PlayerThemePo theme = themeRepository.findById(dto.getThemeId()).orElse(null);
        
        // 验证主题是否存在
        if (theme == null) {
            return Result.error("主题不存在");
        }
        
        // 验证主题是否属于当前玩家
        if (!theme.getPlayer().getId().equals(AuthService.getCurrentPlayerId())) {
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
        List<PlayerThemeValues> themeValues = themeValuesRepository.findByTheme_Id(theme.getId());
        Map<String, String> valueMap = new HashMap<>();
        for (PlayerThemeValues value : themeValues) {
            valueMap.put(value.getThemeKey(), value.getThemeValue());
        }
        vo.setThemeValues(valueMap);
        
        return Result.success(vo);
    }

    //获取当前正在使用的主题
    @PostMapping("getActiveTheme")
    public Result<GetActiveThemeVo> getActiveTheme() {

        // 查询当前玩家的激活主题
        PlayerThemePo activeTheme = themeRepository.findActiveThemeByPlayerId(AuthService.getCurrentPlayerId());
        
        // 如果玩家没有激活的主题，返回错误
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
            for (PlayerThemeValues value : activeTheme.getThemeValues()) {
                themeValues.put(value.getThemeKey(), value.getThemeValue());
            }
        }
        vo.setThemeValues(themeValues);
        
        return Result.success(vo);
    }
    
    /**
     * 激活指定的主题
     * 将当前玩家下的指定主题设置为默认，同时将其他主题设置为非默认
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
        PlayerThemePo themeToActive = themeRepository.findById(dto.getThemeId()).orElse(null);
        
        // 验证主题是否存在
        if (themeToActive == null) {
            return Result.error("要激活的主题不存在");
        }
        
        // 验证主题是否属于当前玩家
        if (!themeToActive.getPlayer().getId().equals(AuthService.getCurrentPlayerId())) {
            return Result.error("无权操作此主题");
        }
        
        // 查询当前玩家的所有主题
        List<PlayerThemePo> playerThemePos = themeRepository.findByPlayerIdWithValues(AuthService.getCurrentPlayerId());
        
        // 将所有主题设置为非激活状态
        for (PlayerThemePo theme : playerThemePos) {
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