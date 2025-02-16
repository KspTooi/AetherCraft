package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.service.panel.PanelGroupService;
import com.ksptool.ql.biz.service.panel.PanelPermissionService;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.biz.model.vo.EditPanelGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/panel/group")
public class PanelGroupController {

    @Autowired
    private PanelGroupService groupService;

    @Autowired
    private PanelPermissionService permissionService;

    @GetMapping("/list")
    public ModelAndView list(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        ModelAndView mv = new ModelAndView("panel-group-manager");
        mv.addObject("data", groupService.findAll(page, size));
        return mv;
    }

    @GetMapping("/create")
    public ModelAndView create(@ModelAttribute("group") GroupPo group) {
        ModelAndView mv = new ModelAndView("panel-group-operator");
        
        // 如果Flash属性中有group数据，使用它
        if (group != null && group.getName() != null) {
            mv.addObject("group", group);
        }
        
        //mv.addObject("permissions", permissionService.getPermissionList(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "sortOrder"))).getContent());
        return mv;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable(name = "id") Long id) {
        ModelAndView mv = new ModelAndView();
        
        try {
            EditPanelGroupVo vo = groupService.getGroupDetails(id);
            mv.addObject("group", vo);
            mv.setViewName("panel-group-operator");
        } catch (BizException e) {
            mv.setViewName("redirect:/panel/group/list");
        }
        
        return mv;
    }

    @PostMapping("/save")
    public ModelAndView save(GroupPo group, @RequestParam(name = "permissionIds", required = false) Long[] permissionIds, RedirectAttributes ra) {
        ModelAndView mv = new ModelAndView();
        
        try {
            // 判断是否为创建模式
            boolean isCreate = group.getId() == null;
            
            // 保存用户组
            groupService.save(group, permissionIds);
            
            // 设置成功消息
            if (isCreate) {
                ra.addFlashAttribute("vo", Result.success(String.format("已创建用户组:%s", group.getName()), null));
                // 创建模式：清空表单，返回创建页面继续创建
                ra.addFlashAttribute("group", new GroupPo());
                mv.setViewName("redirect:/panel/group/create");
            } else {
                ra.addFlashAttribute("vo", Result.success(String.format("已更新用户组:%s", group.getName()), null));
                // 编辑模式：返回列表页
                mv.setViewName("redirect:/panel/group/list");
            }
        } catch (BizException e) {
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
            // 保持原有数据，返回对应页面
            ra.addFlashAttribute("group", group);
            
            if (group.getId() != null) {
                mv.setViewName("redirect:/panel/group/edit/" + group.getId());
            } else {
                mv.setViewName("redirect:/panel/group/create");
            }
        }
        
        return mv;
    }

    @PostMapping("/remove/{id}")
    public ModelAndView remove(@PathVariable(name = "id") Long id, RedirectAttributes redirectAttributes) {
        ModelAndView mv = new ModelAndView("redirect:/panel/group/list");
        
        try {
            GroupPo group = groupService.findById(id);
            if (group == null) {
                throw new BizException("用户组不存在");
            }
            groupService.remove(id);
            redirectAttributes.addFlashAttribute("vo", Result.success(String.format("已删除用户组:%s", group.getName()), null));
        } catch (BizException e) {
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        
        return mv;
    }
} 