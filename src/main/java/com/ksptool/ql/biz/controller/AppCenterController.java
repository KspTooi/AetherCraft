package com.ksptool.ql.biz.controller;

import com.ksptool.ql.biz.model.dto.CreateAppDto;
import com.ksptool.ql.biz.model.dto.RunAppDto;
import com.ksptool.ql.biz.model.dto.EditAppDto;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.po.AppPo;
import com.ksptool.ql.biz.model.vo.AppCenterVo;
import com.ksptool.ql.biz.model.vo.AppItemVo;
import com.ksptool.ql.biz.model.vo.GetAppDetailsVo;
import com.ksptool.ql.biz.service.AppService;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.service.UserService;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Controller
@RequestMapping("/ssr")
public class AppCenterController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppService appService;

    @RequirePermission("app:view")
    @GetMapping("/appCenter")
    public ModelAndView appCenter(HttpServletRequest hsr, @RequestParam(value = "keyword", required = false) String keyword) {
        UserPo userPo = authService.verifyUser(hsr);
        ModelAndView mav = new ModelAndView("app-center");

        var data = new HashMap<String,Object>();
        List<AppPo> appList;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 如果有关键字，执行搜索
            appList = appService.searchApps(userPo.getId(), keyword.trim());
        } else {
            // 否则获取所有应用
            appList = appService.getAppListByUserId(userPo.getId());
        }
        
        data.put("appCount", appList.size());
        data.put("appList", as(appList, AppItemVo.class));

        mav.addObject("data", data);
        return mav;
    }

    @RequirePermission("app:add")
    @PostMapping("/createApp")
    public String createApp(@Valid CreateAppDto dto, BindingResult br, HttpServletRequest hsr, RedirectAttributes ra) {

        // 当表单数据校验失败时返回创建应用页面
        if (br.hasErrors()) {
            ra.addFlashAttribute("vo", Result.error("表单校验失败!"));
            return "redirect:/appCenter";
        }

        // 创建应用
        try{
            appService.createApp(authService.verifyUser(hsr), dto);
            ra.addFlashAttribute("vo", Result.success("已创建新应用:"+dto.getName(),null));
        }catch (BizException ex){
            ra.addFlashAttribute("vo", Result.error(ex));
        }

        return "redirect:/appCenter";
    }

    @RequirePermission("app:remove")
    @PostMapping("/removeApp")
    public String removeApp(@RequestParam("appId") Long appId, HttpServletRequest hsr, RedirectAttributes ra) {
        if (appId == null) {
            ra.addFlashAttribute("vo", Result.error("缺少应用ID！"));
            return "redirect:/appCenter";
        }
        try {
            // 根据当前用户和应用 ID 移除指定的应用
            appService.removeApp(authService.verifyUser(hsr), appId);
            ra.addFlashAttribute("vo", Result.success("已移除应用", null));
        } catch (BizException ex) {
            ra.addFlashAttribute("vo", Result.error(ex));
        }
        return "redirect:/appCenter";
    }

    @RequirePermission("app:launch")
    @PostMapping("/runApp")
    @ResponseBody
    public Result<String> runApp(@RequestBody @Valid RunAppDto dto, HttpServletRequest hsr) {
        try {
            UserPo user = authService.verifyUser(hsr);
            String message = appService.runApp(user, dto);
            return Result.success(message, null);
        } catch (BizException ex) {
            return Result.error(ex);
        }
    }

    @RequirePermission("app:edit")
    @GetMapping("/getAppDetails")
    @ResponseBody
    public Result<GetAppDetailsVo> getAppDetails(@RequestParam("appId") Long appId, HttpServletRequest hsr) {
        try {
            UserPo user = authService.verifyUser(hsr);
            AppPo app = appService.getAppById(user, appId);
            GetAppDetailsVo vo = new GetAppDetailsVo();
            assign(app, vo);
            return Result.success(vo);
        } catch (BizException ex) {
            return Result.error(ex);
        }
    }

    @RequirePermission("app:edit")
    @PostMapping("/editApp")
    public String editApp(@Valid EditAppDto dto, BindingResult br, HttpServletRequest hsr, RedirectAttributes ra) {
        // 当表单数据校验失败时返回应用中心页面
        if (br.hasErrors()) {
            ra.addFlashAttribute("vo", Result.error("表单校验失败!"));
            return "redirect:/appCenter";
        }

        try {
            UserPo user = authService.verifyUser(hsr);
            appService.editApp(user, dto);
            ra.addFlashAttribute("vo", Result.success("应用更新成功：" + dto.getName(), null));
        } catch (BizException ex) {
            ra.addFlashAttribute("vo", Result.error(ex));
        }
        return "redirect:/appCenter";
    }

}
