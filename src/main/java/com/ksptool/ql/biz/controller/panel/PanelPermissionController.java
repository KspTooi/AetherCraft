package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.CreatePermissionDto;
import com.ksptool.ql.biz.model.po.PermissionPo;
import com.ksptool.ql.biz.model.vo.EditPanelPermissionVo;
import com.ksptool.ql.biz.model.vo.ListPanelPermissionVo;
import com.ksptool.ql.biz.service.panel.PanelPermissionService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.ksptool.entities.Entities.assign;

/**
 * 权限节点管理控制器
 */
@Controller
@RequestMapping("/panel/permission")
public class PanelPermissionController {

    @Autowired
    private PanelPermissionService panelPermissionService;

    /**
     * 权限管理列表页面
     */
    @GetMapping("/list")
    public ModelAndView permissionManager(@RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "size", defaultValue = "10") int size) {
        ModelAndView mav = new ModelAndView("panel-permission-manager");
        
        // 获取权限列表，按排序号升序
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        PageableView<ListPanelPermissionVo> pageableView = panelPermissionService.getPermissionList(PageRequest.of(page - 1, size, sort));
        mav.addObject("data", pageableView);
        
        return mav;
    }

    /**
     * 创建权限页面
     */
    @GetMapping("/create")
    public ModelAndView createPermission(@ModelAttribute("data") CreatePermissionDto dto) {
        ModelAndView mav = new ModelAndView("panel-permission-operator");
        
        if (dto == null || dto.getCode() == null) {
            CreatePermissionDto newDto = new CreatePermissionDto();
            newDto.setSortOrder(panelPermissionService.getNextSortOrder());
            mav.addObject("data", newDto);
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
            EditPanelPermissionVo vo = panelPermissionService.getPermissionForEdit(id);
            mav.addObject("data", vo);
            mav.setViewName("panel-permission-operator");
        } catch (BizException e) {
            mav.setViewName("redirect:/panel/permission/list");
        }
        
        return mav;
    }

    /**
     * 保存权限
     */
    @PostMapping("/save")
    public ModelAndView savePermission(@Valid @ModelAttribute("data") CreatePermissionDto dto, 
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        
        // 如果有验证错误，返回表单页面
        if (bindingResult.hasErrors()) {
            mav.addObject("data", dto);
            if (dto.getId() != null) {
                mav.setViewName("redirect:/panel/permission/edit/" + dto.getId());
            } else {
                mav.setViewName("redirect:/panel/permission/create");
            }
            return mav;
        }
        
        try {
            PermissionPo permission = new PermissionPo();
            assign(dto, permission);
            
            boolean isCreate = permission.getId() == null;
            
            panelPermissionService.savePermission(permission);
            
            if (isCreate) {
                redirectAttributes.addFlashAttribute("vo", Result.success(String.format("已创建权限节点:%s", permission.getCode()), null));
                CreatePermissionDto newDto = new CreatePermissionDto();
                newDto.setSortOrder(panelPermissionService.getNextSortOrder());
                redirectAttributes.addFlashAttribute("data", newDto);
                mav.setViewName("redirect:/panel/permission/create");
            } else {
                redirectAttributes.addFlashAttribute("vo", Result.success(String.format("已更新权限节点:%s", permission.getCode()), null));
                mav.setViewName("redirect:/panel/permission/list");
            }
        } catch (BizException e) {
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
            redirectAttributes.addFlashAttribute("data", dto);

            if (dto.getId() != null) {
                mav.setViewName("redirect:/panel/permission/edit/" + dto.getId());
            } else {
                mav.setViewName("redirect:/panel/permission/create");
            }
        }
        
        return mav;
    }

    /**
     * 删除权限
     */
    @PostMapping("/remove/{id}")
    public ModelAndView deletePermission(@PathVariable(name = "id") Long id, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView("redirect:/panel/permission/list");
        
        try {
            PermissionPo permission = panelPermissionService.getPermission(id);
            panelPermissionService.deletePermission(id);
            redirectAttributes.addFlashAttribute("vo", Result.success(String.format("已删除权限节点:%s", permission.getCode()), null));
        } catch (BizException e) {
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        
        return mav;
    }
} 