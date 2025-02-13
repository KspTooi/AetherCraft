package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.SaveUserDto;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.PanelUserVo;
import com.ksptool.ql.biz.service.panel.PanelUserService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.ksptool.entities.Entities.assign;

@Controller
@RequestMapping("/ssr/system/users")
public class PanelUserController {

    @Autowired
    private PanelUserService panelUserService;

    /**
     * 用户管理页面
     */
    @GetMapping("")
    public ModelAndView userManager(@RequestParam(name = "page", defaultValue = "1") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size) {
        ModelAndView mav = new ModelAndView("panel-user-manager");
        
        // 获取用户列表
        Page<PanelUserVo> userPage = panelUserService.getUserList(PageRequest.of(page - 1, size));
        
        // 添加数据到模型
        mav.addObject("users", userPage.getContent());
        mav.addObject("currentPage", page);
        mav.addObject("totalPages", userPage.getTotalPages());
        mav.addObject("totalElements", userPage.getTotalElements());
        
        return mav;
    }

    /**
     * 用户操作页面（创建/编辑）
     */
    @GetMapping("/operator")
    public ModelAndView userOperator(@RequestParam(name = "id", required = false) Long id) {
        ModelAndView mav = new ModelAndView("panel-user-operator");
        
        if (id != null) {
            // 编辑模式：获取用户信息
            try {
                UserPo user = panelUserService.getUserPo(id);
                user.setPassword(null);
                mav.addObject("user", user);
            } catch (BizException e) {
                // 用户不存在时返回列表页
                return new ModelAndView("redirect:/ssr/system/users");
            }
        }
        return mav;
    }

    /**
     * 保存用户
     */
    @PostMapping("/save")
    public ModelAndView saveUser(@Valid SaveUserDto dto, BindingResult bindingResult, RedirectAttributes ra) {
        ModelAndView mav = new ModelAndView();

        try {
            // 处理验证错误
            if (bindingResult.hasErrors()) {
                mav.setViewName("panel-user-operator");
                mav.addObject("user", dto);
                mav.addObject("vo", Result.error(bindingResult.getFieldError().getDefaultMessage()));
                return mav;
            }
            
            // 验证新建用户时密码必填
            if (dto.getId() == null && (dto.getPassword() == null || dto.getPassword().trim().isEmpty())) {
                mav.setViewName("panel-user-operator");
                mav.addObject("user", dto);
                mav.addObject("vo", Result.error("新建用户时密码不能为空"));
                return mav;
            }
            
            // 获取或创建UserPo
            UserPo user;
            boolean isCreate = dto.getId() == null;
            
            if (isCreate) {
                user = new UserPo();
            } else {
                user = panelUserService.getUserPo(dto.getId());
                // 编辑时，如果密码为空，保持原密码不变
                if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                    dto.setPassword(user.getPassword());
                }
            }
            
            // 合并DTO数据到PO
            assign(dto, user);
            
            // 保存用户
            panelUserService.saveUser(user);
            
            if (isCreate) {
                // 创建成功：显示成功消息，清空表单，返回创建页面继续创建
                mav.setViewName("panel-user-operator");
                mav.addObject("vo", Result.success("已创建用户:" + user.getUsername(), null));
            } else {
                // 编辑成功：显示成功消息，返回列表页
                mav.setViewName("redirect:/ssr/system/users");
                ra.addFlashAttribute("vo", Result.success("已更新用户:" + user.getUsername(), null));
            }
        } catch (BizException e) {
            // 保存失败，返回表单页面并显示错误信息
            mav.setViewName("panel-user-operator");
            mav.addObject("user", dto);
            mav.addObject("vo", Result.error(e.getMessage()));
        }
        
        return mav;
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete/{id}")
    public ModelAndView deleteUser(@PathVariable(name = "id") Long id) {
        ModelAndView mav = new ModelAndView("panel-user-manager");
        
        try {
            panelUserService.deleteUser(id);
            mav.addObject("vo", Result.success("删除成功", null));
        } catch (Exception e) {
            mav.addObject("vo", Result.error("删除失败：" + e.getMessage()));
        }
        
        // 重新加载用户列表
        Page<PanelUserVo> userPage = panelUserService.getUserList(PageRequest.of(0, 10));
        mav.addObject("users", userPage.getContent());
        mav.addObject("currentPage", 1);
        mav.addObject("totalPages", userPage.getTotalPages());
        mav.addObject("totalElements", userPage.getTotalElements());
        
        return mav;
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Result<PanelUserVo> getUser(@PathVariable Long id) {
        try {
            PanelUserVo user = panelUserService.getUser(id);
            return Result.success(user);
        } catch (BizException e) {
            return Result.error(e);
        }
    }
} 