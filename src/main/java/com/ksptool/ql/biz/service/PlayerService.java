package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.PlayerDefaultGroupRepository;
import com.ksptool.ql.biz.mapper.PlayerRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.model.dto.CommonIdDto;
import com.ksptool.ql.biz.model.dto.CreatePlayerDto;
import com.ksptool.ql.biz.model.dto.EditAttachPlayerDetailsDto;
import com.ksptool.ql.biz.model.dto.GetPlayerListDto;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.PlayerDefaultGroupPo;
import com.ksptool.ql.biz.model.po.PlayerPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.vo.GetAttachPlayerDetailsVo;
import com.ksptool.ql.biz.model.vo.GetPlayerListVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.web.RestPageableView;
import com.ksptool.ql.commons.web.Result;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

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

    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private PlayerDefaultGroupRepository playerDefaultGroupRepository;

    public GetAttachPlayerDetailsVo getAttachPlayerDetails() throws BizException {

        Long playerId = AuthService.getCurrentPlayerId();

        if(playerId == null){
            throw new BizException("Player is not logged in");
        }

        PlayerPo query = new PlayerPo();
        query.setId(playerId);
        query.setUser(Any.of().val("id", AuthService.getCurrentUserId()).as(UserPo.class));
        query.setStatus(0);

        PlayerPo playerPo = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("未能找到指定的人物或该人物不属于您"));

        GetAttachPlayerDetailsVo vo = as(playerPo, GetAttachPlayerDetailsVo.class);
        vo.setDescription(css.decryptForCurUser(playerPo.getDescription()));
        return vo;
    }

    public void editAttachPlayerDetails(EditAttachPlayerDetailsDto dto) throws BizException {

        if(!AuthService.isLoginPlayer()){
            throw new BizException("状态异常：当前没有活跃会话，无法执行修改操作。");
        }

        PlayerPo query = new PlayerPo();
        query.setId(dto.getId());
        query.setUser(Any.of().val("id", AuthService.getCurrentUserId()).as(UserPo.class));
        query.setStatus(0);

        PlayerPo playerPo = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("未能找到指定的人物或该人物不属于您"));

        assign(dto,playerPo);
        css.encryptEntity(playerPo);
        repository.save(playerPo);
    }


    @Transactional
    public void attachPlayer(Long id) throws BizException{

        if(AuthService.isLoginPlayer()){
            throw new BizException("状态异常：当前已存在一个活跃会话，您无法再继续激活");
        }

        var userId = AuthService.getCurrentUserId();

        PlayerPo query = new PlayerPo();
        query.setId(id);
        query.setUser(Any.of().val("id", userId).as(UserPo.class));
        query.setStatus(1);

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
        
        // 获取配置的等待时间（小时）
        long waitingHours = globalConfigService.getLong(
                GlobalConfigEnum.PLAYER_REMOVE_WAITING_PERIOD_HOURS.getKey(),
                Long.parseLong(GlobalConfigEnum.PLAYER_REMOVE_WAITING_PERIOD_HOURS.getDefaultValue()) // 使用枚举中的默认值
        );
        long waitingMillis = waitingHours * 60 * 60 * 1000;
        Date now = new Date();
        long nowMillis = now.getTime();

        List<GetPlayerListVo> vos = new ArrayList<>();
        for (PlayerPo po : playerList.getContent()) {
            GetPlayerListVo vo = as(po, GetPlayerListVo.class); // 先进行基础映射
            vo.setRemainingRemoveTime(-1); // 初始化为 -1

            if (po.getStatus() == 2) { // 检查 PO 的状态
                Date removalRequestTime = po.getRemovalRequestTime(); // 从 PO 获取时间
                if (removalRequestTime != null) {
                    long removalRequestMillis = removalRequestTime.getTime();
                    long earliestRemovalTimeMillis = removalRequestMillis + waitingMillis;
                    long remainingMillis = earliestRemovalTimeMillis - nowMillis;

                    if (remainingMillis > 0) {
                        long remainingHours = remainingMillis / (60 * 60 * 1000);
                        // 转换为 int 并处理可能的溢出（虽然小时数不太可能溢出int）
                        vo.setRemainingRemoveTime((int) remainingHours);
                    } else {
                        vo.setRemainingRemoveTime(0); // 时间已到
                    }
                }
                 // 如果 removalRequestTime 为 null 但状态为 2，保持 -1
            }
            vos.add(vo);
        }

        return new RestPageableView<>(vos, playerList.getTotalElements());
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

        // 获取默认访问组
        Set<GroupPo> defaultGroupList = playerDefaultGroupRepository.getDefaultGroupList();

        // 将默认访问组关联到新玩家
        create.setGroups(defaultGroupList);

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


    /**
     * 提交人物删除请求
     * @param playerId 要删除的人物ID
     * @throws BizException 业务异常
     */
    @Transactional
    public void sendRemoveRequest(Long playerId) throws BizException {

        if(AuthService.isLoginPlayer()){
            throw new BizException("状态异常：当前已存在一个活跃会话,您无法进行此操作!");
        }

        var userId = AuthService.getCurrentUserId();

        PlayerPo query = new PlayerPo();
        query.setId(playerId);
        query.setUser(Any.of().val("id", userId).as(UserPo.class));
        query.setStatus(1);

        PlayerPo player = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("未能找到指定的人物或该人物不属于您"));

        //0:正在使用 1:不活跃 2:等待删除 3:已删除
        player.setStatus(2);
        player.setRemovalRequestTime(new Date());
        repository.save(player);
    }

    /**
     * 确认删除人物 (需要后续实现)
     * @param playerId 要删除的人物ID
     * @throws BizException 业务异常
     */
    @Transactional
    public void removePlayer(Long playerId) throws BizException{

        if(AuthService.isLoginPlayer()){
            throw new BizException("状态异常：当前已存在一个活跃会话,您无法进行此操作!");
        }

        var userId = AuthService.getCurrentUserId();

        PlayerPo query = new PlayerPo();
        query.setId(playerId);
        query.setUser(Any.of().val("id", userId).as(UserPo.class));
        query.setStatus(2); // 状态必须是等待删除

        PlayerPo player = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("未能找到指定的人物、该人物不属于您或当前状态无法执行删除操作"));

        // 获取配置的等待时间（小时）
        long waitingHours = globalConfigService.getLong(
                GlobalConfigEnum.PLAYER_REMOVE_WAITING_PERIOD_HOURS.getKey(),
                Long.parseLong(GlobalConfigEnum.PLAYER_REMOVE_WAITING_PERIOD_HOURS.getDefaultValue()) // 使用枚举中的默认值
        );

        // 计算可以删除的时间点
        Date removalRequestTime = player.getRemovalRequestTime();
        if (removalRequestTime == null) {
            //按理说status=2这个时间不应该为空,加个保护
             throw new BizException("人物删除请求时间异常，无法确认删除");
        }
        
        long removalRequestMillis = removalRequestTime.getTime();
        long waitingMillis = waitingHours * 60 * 60 * 1000; // 小时转换为毫秒
        long earliestRemovalTimeMillis = removalRequestMillis + waitingMillis;

        Date now = new Date();

        // 检查当前时间是否已经过了等待期
        if (now.getTime() < earliestRemovalTimeMillis) {
             long remainingMillis = earliestRemovalTimeMillis - now.getTime();
             long remainingHours = remainingMillis / (60 * 60 * 1000);
             long remainingMinutes = (remainingMillis % (60 * 60 * 1000)) / (60 * 1000);
             throw new BizException(String.format("删除操作需要等待 %d 小时 %d 分钟", remainingHours, remainingMinutes));
        }

        // 更新状态为已删除
        player.setStatus(3);
        player.setRemovedTime(now);
        repository.save(player);
    }

    /**
     * 取消删除人物请求
     * @param playerId 要取消删除的人物ID
     * @throws BizException 业务异常
     */
    @Transactional
    public void cancelRemovePlayer(Long playerId) throws BizException {

        if(AuthService.isLoginPlayer()){
            throw new BizException("状态异常：当前已存在一个活跃会话,您无法进行此操作!");
        }

        var userId = AuthService.getCurrentUserId();

        PlayerPo query = new PlayerPo();
        query.setId(playerId);
        query.setUser(Any.of().val("id", userId).as(UserPo.class));
        query.setStatus(2); // 状态必须是等待删除

        PlayerPo player = repository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("未能找到指定的人物、该人物不属于您或当前状态无法执行取消删除操作"));

        // 将状态改回不活跃，并清除删除请求时间
        player.setStatus(1);
        player.setRemovalRequestTime(null);
        repository.save(player);
    }

}
