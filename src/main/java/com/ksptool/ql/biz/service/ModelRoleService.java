package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.ModelRoleChatExampleRepository;
import com.ksptool.ql.biz.mapper.ModelRoleRepository;
import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.po.ModelRolePo;
import com.ksptool.ql.biz.model.po.ModelRoleChatExamplePo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.vo.GetModelRoleDetailsVo;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.web.Result;
import com.ksptool.ql.commons.web.SimpleExample;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.ksptool.entities.Entities.assign;

@Service
public class ModelRoleService {

    @Autowired
    private ModelRoleRepository repository;

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private ModelRoleChatExampleRepository chatExampleRepository;


    public PageableView<GetModelRoleListVo> getModelRoleList(GetModelRoleListDto dto){

        var query = new ModelRolePo();
        query.setName(dto.getKeyword()); // 设置名称关键字查询条件
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class)); // 按当前人物过滤

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

    public GetModelRoleDetailsVo getModelRoleDetails(long id) throws BizException {
        
        // 创建查询条件
        ModelRolePo query = new ModelRolePo();
        query.setId(id);
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class)); // 确保只能查询当前玩家的角色
        
        // 创建Example查询
        SimpleExample<ModelRolePo> example = SimpleExample.of(query);
        
        // 执行查询
        ModelRolePo modelRole = repository.findOne(example.get())
                .orElseThrow(() -> new BizException("角色不存在或无权限访问"));
        
        // 创建VO对象并映射属性
        GetModelRoleDetailsVo vo = new GetModelRoleDetailsVo();
        assign(modelRole, vo);
        
        // 解密需要解密的字段
        vo.setAvatarPath(css.decryptForCurUser(vo.getAvatarPath()));
        vo.setDescription(css.decryptForCurUser(vo.getDescription()));
        vo.setRoleSummary(css.decryptForCurUser(vo.getRoleSummary()));
        vo.setScenario(css.decryptForCurUser(vo.getScenario()));
        vo.setFirstMessage(css.decryptForCurUser(vo.getFirstMessage()));
        vo.setTags(css.decryptForCurUser(vo.getTags()));
        
        // 处理头像路径
        if (StringUtils.isNotBlank(vo.getAvatarPath())) {
            vo.setAvatarPath("/res/" + vo.getAvatarPath());
        }
        
        return vo;
    }

    public void copyModelRole(long sourceId) throws BizException{

        // 查询要复制的角色
        ModelRolePo query = new ModelRolePo();
        query.setId(sourceId);
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class)); // 确保只能复制当前人物的角色
        
        // 创建Example查询
        SimpleExample<ModelRolePo> example = SimpleExample.of(query);
        
        // 执行查询
        ModelRolePo sourceRole = repository.findOne(example.get())
                .orElseThrow(() -> new BizException("要复制的角色不存在或无权限访问"));
        
        // 创建新角色并复制属性
        ModelRolePo newRole = new ModelRolePo();
        assign(sourceRole, newRole);
        
        // 设置新角色的基本属性
        newRole.setId(null); // 清空ID，让数据库自动生成
        
        // 生成不冲突的角色名称
        String baseName = sourceRole.getName() + " 副本";
        String newName = baseName;
        int suffix = 1;
        
        // 检查名称是否存在，如果存在则添加数字后缀
        while (isRoleNameExists(newName, AuthService.getCurrentUserId())) {
            newName = baseName + " " + suffix;
            suffix++;
        }
        
        newRole.setName(newName);
        newRole.setCreateTime(null); // 清空创建时间，让数据库自动设置
        newRole.setUpdateTime(null); // 清空更新时间，让数据库自动设置
        
        // 保存新角色 (不需要再次加密，因为查询出的数据并没有解密)
        ModelRolePo savedRole = repository.save(newRole);
        
        // 查询并复制对话示例
        var chatExampleQuery = new ModelRoleChatExamplePo();
        chatExampleQuery.setModelRoleId(sourceId);
        
        SimpleExample<ModelRoleChatExamplePo> chatExample = SimpleExample.of(chatExampleQuery);
        List<ModelRoleChatExamplePo> examples = chatExampleRepository.findAll(chatExample.get());
        
        if (!examples.isEmpty()) {
            for (ModelRoleChatExamplePo exampleItem : examples) {
                // 创建新的对话示例并复制属性
                ModelRoleChatExamplePo newExample = new ModelRoleChatExamplePo();
                assign(exampleItem, newExample);
                
                // 设置新对话示例的基本属性
                newExample.setId(null);
                newExample.setModelRoleId(savedRole.getId());
                newExample.setCreateTime(null);
                newExample.setUpdateTime(null);
                
                // 保存新对话示例
                chatExampleRepository.save(newExample);
            }
        }
    }
    
    /**
     * 检查角色名称是否已存在
     * @param roleName 角色名称
     * @param userId 用户ID
     * @return 如果名称已存在返回true，否则返回false
     */
    private boolean isRoleNameExists(String roleName, Long userId) {
        ModelRolePo query = new ModelRolePo();
        query.setName(roleName);
        query.setPlayer(Any.of().val("id",userId).as(PlayerPo.class));
        
        SimpleExample<ModelRolePo> example = SimpleExample.of(query);
        // 使用精确匹配，SimpleExample默认就是精确匹配
        
        return repository.exists(example.get());
    }

}
