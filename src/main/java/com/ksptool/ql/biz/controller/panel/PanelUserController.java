package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.service.panel.PanelUserService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ssr/system/users")
public class PanelUserController {

    @Autowired
    private PanelUserService panelUserService;

    /**
     * 用户管理页面
     */
    @GetMapping("")
    public String userManager(Model model,
                            @RequestParam(name = "page", defaultValue = "1") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size) {
        // 获取用户列表
        Page<UserPo> userPage = panelUserService.getUserList(PageRequest.of(page - 1, size));
        
        // 添加数据到模型
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        
        return "panel-user-manager";
    }

    /**
     * 保存用户
     */
    @PostMapping("/save")
    @ResponseBody
    public Result<String> saveUser(@RequestBody UserPo user) {
        try {
            panelUserService.saveUser(user);
            return Result.success("保存成功");
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Result<String> deleteUser(@PathVariable Long id) {
        try {
            panelUserService.deleteUser(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Result<UserPo> getUser(@PathVariable Long id) {
        try {
            UserPo user = panelUserService.getUser(id);
            return Result.success(user);
        } catch (BizException e) {
            return Result.error(e);
        }
    }
} 