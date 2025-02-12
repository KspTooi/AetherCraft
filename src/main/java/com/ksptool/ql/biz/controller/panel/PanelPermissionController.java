package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.vo.SavePermissionVo;
import com.ksptool.ql.biz.service.panel.PanelPermissionService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.ksptool.entities.Entities.assign;

/**
 * 权限节点管理控制器
 */
@Controller
@RequestMapping("/ssr/system/permissions")
public class PanelPermissionController {

    @Autowired
    private PanelPermissionService panelPermissionService;

    /**
     * 权限管理页面
     */
    @GetMapping("")
    public ModelAndView permissionManager(@RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "size", defaultValue = "10") int size) {
        ModelAndView mav = new ModelAndView("panel-permission-manager");
        
        // 获取权限列表，按排序号升序
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        Page<PermissionPo> permissionPage = panelPermissionService.getPermissionList(PageRequest.of(page - 1, size, sort));
        
        // 添加数据到模型
        mav.addObject("permissions", permissionPage.getContent());
        mav.addObject("currentPage", page);
        mav.addObject("totalPages", permissionPage.getTotalPages());
        mav.addObject("totalElements", permissionPage.getTotalElements());
        
        return mav;
    }

    /**
     * 创建权限页面
     */
    @GetMapping("/create")
    public ModelAndView createPermission(@ModelAttribute("permission") PermissionPo permission) {
        ModelAndView mav = new ModelAndView("panel-permission-operator");
        
        // 如果Flash属性中有permission数据，使用它
        if (permission != null && permission.getCode() != null) {
            mav.addObject("permission", permission);
        } else {
            // 新建权限时，自动设置下一个可用的排序号
            PermissionPo newPermission = new PermissionPo();
            newPermission.setSortOrder(panelPermissionService.getNextSortOrder());
            mav.addObject("permission", newPermission);
        }
        
        return mav;
    }

    /**
     * 编辑权限页面
     */
    @GetMapping("/edit/{id}")
    public ModelAndView editPermission(@PathVariable(name = "id") Long id) {
        ModelAndView mav = new ModelAndView();
        
        try {
            PermissionPo permission = panelPermissionService.getPermission(id);
            mav.addObject("permission", permission);
            mav.setViewName("panel-permission-operator");
        } catch (BizException e) {
            mav.setViewName("redirect:/ssr/system/permissions");
        }
        
        return mav;
    }

    /**
     * 保存权限
     */
    @PostMapping("/save")
    public ModelAndView savePermission(SavePermissionVo saveVo, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        
        try {
            // 转换VO到PO
            PermissionPo permission = new PermissionPo();
            assign(saveVo, permission);
            
            // 判断是否为创建模式
            boolean isCreate = permission.getId() == null;
            
            // 保存权限
            panelPermissionService.savePermission(permission);
            
            // 设置成功消息
            if (isCreate) {
                redirectAttributes.addFlashAttribute("vo", Result.success(String.format("已创建权限节点:%s", permission.getCode()), null));
                // 创建模式：清空表单，返回创建页面继续创建
                PermissionPo newPermission = new PermissionPo();
                newPermission.setSortOrder(panelPermissionService.getNextSortOrder());
                redirectAttributes.addFlashAttribute("permission", newPermission);
                mav.setViewName("redirect:/ssr/system/permissions/create");
            } else {
                redirectAttributes.addFlashAttribute("vo", Result.success(String.format("已更新权限节点:%s", permission.getCode()), null));
                // 编辑模式：返回列表页
                mav.setViewName("redirect:/ssr/system/permissions");
            }
        } catch (BizException e) {
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
            // 保持原有数据，返回对应页面
            redirectAttributes.addFlashAttribute("permission", saveVo);
            if (saveVo.getId() != null) {
                mav.setViewName("redirect:/ssr/system/permissions/edit/" + saveVo.getId());
            } else {
                mav.setViewName("redirect:/ssr/system/permissions/create");
            }
        }
        
        return mav;
    }

    /**
     * 删除权限
     */
    @PostMapping("/delete/{id}")
    public ModelAndView deletePermission(@PathVariable(name = "id") Long id, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView("redirect:/ssr/system/permissions");
        
        try {
            PermissionPo permission = panelPermissionService.getPermission(id);
            panelPermissionService.deletePermission(id);
            redirectAttributes.addFlashAttribute("vo", Result.success(String.format("已删除权限节点:%s", permission.getCode()), null));
        } catch (BizException e) {
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        
        return mav;
    }

    /**
     * 获取权限信息
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Result<PermissionPo> getPermission(@PathVariable(name = "id") Long id) {
        try {
            PermissionPo permission = panelPermissionService.getPermission(id);
            return Result.success("获取成功", permission);
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }
} 