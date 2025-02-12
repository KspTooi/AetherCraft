package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.service.panel.PanelPermissionService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String permissionManager(Model model,
                                  @RequestParam(name = "page", defaultValue = "1") int page,
                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        // 获取权限列表，按排序号升序
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        Page<PermissionPo> permissionPage = panelPermissionService.getPermissionList(PageRequest.of(page - 1, size, sort));
        
        // 添加数据到模型
        model.addAttribute("permissions", permissionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", permissionPage.getTotalPages());
        model.addAttribute("totalElements", permissionPage.getTotalElements());
        
        return "panel-permission-manager";
    }

    /**
     * 创建权限页面
     */
    @GetMapping("/create")
    public String createPermission() {
        return "panel-permission-operator";
    }

    /**
     * 编辑权限页面
     */
    @GetMapping("/edit/{id}")
    public String editPermission(@PathVariable(name = "id") Long id, Model model) {
        try {
            PermissionPo permission = panelPermissionService.getPermission(id);
            model.addAttribute("permission", permission);
            return "panel-permission-operator";
        } catch (BizException e) {
            return "redirect:/ssr/system/permissions";
        }
    }

    /**
     * 保存权限
     */
    @PostMapping("/save")
    @ResponseBody
    public Result<String> savePermission(@RequestBody PermissionPo permission) {
        try {
            panelPermissionService.savePermission(permission);
            return Result.success("保存成功");
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 删除权限
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Result<String> deletePermission(@PathVariable(name = "id") Long id) {
        try {
            panelPermissionService.deletePermission(id);
            return Result.success("删除成功");
        } catch (BizException e) {
            return Result.error(e);
        }
    }

    /**
     * 获取权限信息
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Result<PermissionPo> getPermission(@PathVariable(name = "id") Long id) {
        try {
            PermissionPo permission = panelPermissionService.getPermission(id);
            return Result.success(permission);
        } catch (BizException e) {
            return Result.error(e);
        }
    }
} 