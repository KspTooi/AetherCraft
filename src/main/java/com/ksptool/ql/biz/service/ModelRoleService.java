package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.SimpleExample;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ModelRoleService {

    @Autowired
    private ModelRoleRepository repository;

    @Autowired
    private ContentSecurityService css;


    public PageableView<GetModelRoleListVo> getModelRoleList(GetModelRoleListDto dto){

        var query = new ModelRolePo();
        query.setName(dto.getKeyword()); // 设置名称关键字查询条件
        query.setStatus(1);              // 设置状态为 1 (通常表示有效或启用)
        query.setUserId(AuthService.getCurrentUserId()); // 按当前用户过滤

        // 创建 SimpleExample 用于构建查询条件
        SimpleExample<ModelRolePo> example = SimpleExample.of(query);
        example.like("name"); // 添加名称的模糊查询

        // 显式定义排序规则：按 "sortOrder" 字段升序排列
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");

        Pageable pageable = PageRequest.of(dto.getPage() - 1, dto.getPageSize(), sort);

        // 使用 example 条件和 pageable（包含排序）从 repository 获取分页数据
        Page<ModelRolePo> page = repository.findAll(example.get(), pageable);

        // 将查询结果 Page<ModelRolePo> 转换为 PageableView<GetModelRoleListVo>
        PageableView<GetModelRoleListVo> pageableView = new PageableView<>(page, GetModelRoleListVo.class);

        // 对结果列表中的每个 VO 对象进行后处理
        for (GetModelRoleListVo vo : pageableView.getRows()) {
            // 解密头像路径
            vo.setAvatarPath(css.decryptForCurUser(vo.getAvatarPath()));
            // 如果头像路径不为空，则添加资源访问前缀
            if (StringUtils.isNotBlank(vo.getAvatarPath())) {
                vo.setAvatarPath("/res/" + vo.getAvatarPath());
            }
        }

        // 返回处理后的分页视图
        return pageableView;
    }






}
