package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListModelRoleDto;
import com.ksptool.ql.biz.model.dto.SaveModelRoleDto;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.vo.ListModelRoleVo;
import com.ksptool.ql.biz.service.UserFileService;
import com.ksptool.ql.biz.service.panel.PanelModelRoleService;
import com.ksptool.ql.commons.web.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.ksptool.entities.Entities.assign;

/**
 * 模型角色管理控制器
 * 用于管理AI模型的角色信息和对话设置
 */
@Controller
@RequestMapping("/panel/model/role")
public class PanelModelRoleController {

    @Autowired
    private PanelModelRoleService panelModelRoleService;
    
    @Autowired
    private UserFileService userFileService;

    /**
     * 获取模型角色列表视图
     * @param dto 查询参数
     * @return 模型角色列表视图
     */
    @GetMapping("/list")
    public ModelAndView getListView(ListModelRoleDto dto, @ModelAttribute("formData") SaveModelRoleDto formData, Model model) {
        // 调用服务获取视图数据
        ListModelRoleVo vo = panelModelRoleService.getListView(dto);
        
        // 如果是新建模式，设置isNew标志
        if (dto.getIsNew()) {
            ListModelRoleVo newVo = new ListModelRoleVo();
            newVo.setKeyword(dto.getKeyword());
            newVo.setRoleList(vo.getRoleList());
            newVo.setIsNew(true);
            newVo.setId(null);
            newVo.setStatus(1);
            vo = newVo;
        }
        
        // 检查是否有保存失败时的表单数据
        if (formData != null && formData.getName() != null) {
            // 将表单数据应用到视图对象
            vo.setIsNew(formData.getId() == null);
            assign(formData, vo);
        }
        
        // 创建ModelAndView并设置数据
        ModelAndView mv = new ModelAndView("panel-model-role");
        mv.addObject("title", "模型角色管理");
        mv.addObject("data", vo);
        return mv;
    }
    
    /**
     * 保存模型角色
     * @param dto 保存参数
     * @return 保存结果视图
     */
    @PostMapping("/save")
    public String saveModelRole(@Valid @ModelAttribute("formData") SaveModelRoleDto dto, BindingResult br, RedirectAttributes ra) {
        // 表单验证
        if (br.hasErrors()) {
            ra.addFlashAttribute("vo", Result.error(br.getAllErrors().get(0).getDefaultMessage()));
            ra.addFlashAttribute("formData", dto);
            return "redirect:/panel/model/role/list";
        }
        
        try {
            // 调用服务保存角色
            ModelRolePo po = panelModelRoleService.saveModelRole(dto);

            // 保存成功，添加成功消息
            ra.addFlashAttribute("vo", Result.success("角色已保存:"+po.getName(),null));
            
            // 重定向到列表页面，并选中刚保存的角色
            return "redirect:/panel/model/role/list?id=" + po.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("vo", Result.error(e.getMessage()));
            ra.addFlashAttribute("formData", dto);
            return "redirect:/panel/model/role/list";
        }
    }
    
    /**
     * 删除模型角色
     * @param id 角色ID
     * @param redirectAttributes 重定向属性
     * @return 重定向到列表页
     */
    @GetMapping("/remove")
    public String removeModelRole(@RequestParam(name = "id") Long id, RedirectAttributes redirectAttributes) {
        try {
            // 删除角色
            panelModelRoleService.removeModelRole(id);
            redirectAttributes.addFlashAttribute("vo", Result.success("角色删除成功", null));
        } catch (Exception e) {
            // 设置业务异常消息
            redirectAttributes.addFlashAttribute("vo", Result.error(e.getMessage()));
        }
        // 重定向到列表页
        return "redirect:/panel/model/role/list";
    }
    
    /**
     * 上传角色头像
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("/upload-avatar")
    @ResponseBody
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 使用统一文件服务保存头像
            var userFile = userFileService.receive(file);
            return Result.success(userFile.getFilepath());
        } catch (Exception e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
}