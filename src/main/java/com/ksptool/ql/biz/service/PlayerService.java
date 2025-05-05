package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.PlayerRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.dto.CreatePlayerDto;
import com.ksptool.ql.biz.model.dto.GetPlayerListDto;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.GetPlayerListVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private AuthService authService;

    @Transactional
    public void attachPlayer(Long id) throws BizException{

        if(AuthService.isLoginPlayer()){
            throw new BizException("状态异常：当前已存在一个活跃会话，您无法再继续激活");
        }

        var userId = AuthService.getCurrentUserId();

        PlayerPo query = new PlayerPo();
        query.setId(id);
        query.setUser(Any.of().val("id", userId).as(UserPo.class));

        PlayerPo player = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("未能找到指定的人物或该人物不属于您"));

        player.setStatus(0); // 0:正在使用
        repository.save(player);

        //刷新用户会话
        authService.refreshUserSession(userId);
    }

    @Transactional
    public void detachPlayer() throws BizException{
        if(!AuthService.isLoginPlayer()){
            throw new BizException("状态异常：当前没有活跃会话，无法执行断开操作。");
        }
        Long userId = AuthService.getCurrentUserId();
        repository.detachAllActivePlayersByUserId(userId);
        //刷新用户会话
        authService.refreshUserSession(userId);
    }

    /**
     * 获取玩家列表
     * @param dto 查询条件
     * @return 玩家列表
     */
    public RestPageableView<GetPlayerListVo> getPlayerList(GetPlayerListDto dto) throws BizException{
        Page<PlayerPo> playerList = repository.getPlayerList(dto.getKeyword(), AuthService.getCurrentUserId(), dto.pageRequest());
        return new RestPageableView<>(playerList,GetPlayerListVo.class);
    }
    
    /**
     * 创建玩家
     * @param dto 玩家信息
     * @return 创建结果
     */
    @Transactional
    public String createPlayer(CreatePlayerDto dto) throws BizException {
        
        if(AuthService.isLoginPlayer()){
            throw new BizException("状态异常：当前存在一个到活跃会话，无法启动新的实体创建进程。");
        }

        //检查名称是否可用
        if(!this.checkPlayerName(dto.getName())){
            throw new BizException("玩家人物名称不可用");
        }

        if(!css.isInternalPath(dto.getAvatarUrl())){
            throw new BizException("玩家人物头像路径不合法");
        }

        //查询用户
        Long userId = AuthService.getCurrentUserId();

        if(userId == null){
            throw new BizException("未能找到用户");
        }

        UserPo user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException("未能找到用户"));

        //创建新玩家实体
        PlayerPo create = new PlayerPo();
        create.setName(dto.getName());
        create.setUser(user);
        create.setAvatarUrl(dto.getAvatarUrl());
        create.setPublicInfo(dto.getPublicInfo());
        create.setDescription(dto.getDescription());
        create.setBalance(BigDecimal.ZERO); // 设置初始余额为0
        create.setLanguage(dto.getLanguage());
        create.setEra(dto.getEra());
        create.setContentFilterLevel(dto.getContentFilterLevel()); // 设置默认内容过滤等级为普通
        create.setStatus(1); //初始状态为不活跃
        css.encryptEntity(create);

        //保存新玩家
        repository.save(create);
        return create.getId() + "";
    }

    public boolean checkPlayerName(String name) {
        // 1. 检查长度
        if (name == null || name.length() > 24) {
            return false;
        }

        // 2. 检查是否只包含字母、数字与中文
        //    使用正则表达式匹配，^表示开始，$表示结束，[a-zA-Z0-9\u4e00-\u9fa5]+表示一个或多个字母、数字或中文
        if (!name.matches("^[a-zA-Z0-9一-龥]+$")) {
            return false;
        }

        // 3. 检查是否以数字开头
        //    Character.isDigit() 检查第一个字符是否为数字
        if (Character.isDigit(name.charAt(0))) {
            return false;
        }

        // 4. 检查名称是否重复
        if (repository.existsByName(name)) {
            return false;
        }

        // 所有检查通过
        return true;
    }

}
