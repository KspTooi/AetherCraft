package com.ksptool.ql.biz.controller.panel;

import com.ksptool.ql.biz.model.dto.ListModelRoleDto;
import com.ksptool.ql.biz.model.vo.ListModelRoleVo;
import com.ksptool.ql.biz.service.panel.PanelModelRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 模型角色管理控制器
 * 用于管理AI模型的角色信息和对话设置
 */
@Controller
@RequestMapping("/panel/model/role")
public class PanelModelRoleController {

    @Autowired
    private PanelModelRoleService panelModelRoleService;

    /**
     * 获取模型角色列表视图
     * @param dto 查询参数
     * @return 模型角色列表视图
     */
    @GetMapping("/list")
    public ModelAndView getListView(ListModelRoleDto dto) {

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
        
        // 创建ModelAndView并设置数据
        ModelAndView mv = new ModelAndView("panel-model-role");
        mv.addObject("title", "模型角色管理");
        mv.addObject("data", vo);
        return mv;
    }
}