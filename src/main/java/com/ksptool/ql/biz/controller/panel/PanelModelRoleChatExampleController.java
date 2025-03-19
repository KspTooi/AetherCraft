package com.ksptool.ql.biz.controller.panel;


import com.ksptool.ql.biz.model.dto.SaveModelRoleChatExampleDto;
import com.ksptool.ql.biz.model.vo.EditModelRoleChatExampleVo;
import com.ksptool.ql.biz.service.panel.PanelModelRoleChatExampleService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/panel/model/role/chatExample")
public class PanelModelRoleChatExampleController {

    @Autowired
    private PanelModelRoleChatExampleService service;

    /**
     * 获取对话示例编辑视图
     * @param modelRoleId 模型角色ID
     * @return 包含对话示例数据的ModelAndView
     */
    @GetMapping("/edit")
    public ModelAndView getEditView(@RequestParam("modelRoleId") Long modelRoleId) throws BizException {
        ModelAndView mav = new ModelAndView("panel-model-role-chat-example-edit");
        List<EditModelRoleChatExampleVo> examples = service.getExamplesByRoleId(modelRoleId);
        mav.addObject("data", examples);
        mav.addObject("modelRoleId", modelRoleId);
        return mav;
    }

    /**
     * 保存对话示例（支持新增、修改和删除）
     * @param dto 保存请求数据
     * @return 重定向到编辑页面
     */
    @PostMapping("/save")
    public ModelAndView save(SaveModelRoleChatExampleDto dto, RedirectAttributes ra){
        ModelAndView mav = new ModelAndView();
        try {
            service.save(dto);
            ra.addFlashAttribute("vo", Result.success("保存成功",null));
        } catch (Exception e) {
            ra.addFlashAttribute("vo", Result.error("保存失败：" + e.getMessage()));
        }
        
        mav.setViewName("redirect:/panel/model/role/chatExample/edit?modelRoleId=" + dto.getModelRoleId());
        return mav;
    }
    
    /**
     * 删除对话示例
     * @param id 示例ID
     * @param modelRoleId 模型角色ID
     * @return 重定向到编辑页面
     */
    @PostMapping("/remove")
    public ModelAndView remove(@RequestParam("id") Long id, 
                              @RequestParam("modelRoleId") Long modelRoleId, 
                              RedirectAttributes ra){
        ModelAndView mav = new ModelAndView();
        try {
            service.removeById(id);
            ra.addFlashAttribute("vo", Result.success("删除成功", null));
        } catch (Exception e) {
            ra.addFlashAttribute("vo", Result.error("删除失败：" + e.getMessage()));
        }
        
        mav.setViewName("redirect:/panel/model/role/chatExample/edit?modelRoleId=" + modelRoleId);
        return mav;
    }

}
