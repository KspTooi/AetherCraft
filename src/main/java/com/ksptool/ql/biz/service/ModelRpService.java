package com.ksptool.ql.biz.service;

import com.ksptool.ql.biz.mapper.*;
import com.ksptool.ql.biz.model.dto.BatchRpCompleteDto;
import com.ksptool.ql.biz.model.dto.GetModelRoleListDto;
import com.ksptool.ql.biz.model.dto.RecoverRpChatDto;
import com.ksptool.ql.biz.model.dto.RemoveRpHistoryDto;
import com.ksptool.ql.biz.model.dto.EditRpHistoryDto;
import com.ksptool.ql.biz.model.dto.RemoveThreadDto;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.vo.GetModelRoleListVo;
import com.ksptool.ql.biz.model.vo.RecoverRpChatHistoryVo;
import com.ksptool.ql.biz.model.vo.RecoverRpChatVo;
import com.ksptool.ql.biz.model.vo.RpSegmentVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.biz.service.panel.PanelApiKeyService;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import com.ksptool.ql.commons.web.PageableView;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import com.ksptool.ql.commons.web.SimpleExample;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.biz.model.vo.ModelRoleThreadListVo;
import com.ksptool.ql.biz.model.dto.GetModelRoleThreadListDto;

import static com.ksptool.entities.Entities.as;

@Slf4j
@Service
public class ModelRpService {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GROK_BASE_URL = "https://api.x.ai/v1/chat/completions";

    // 线程安全的容器，记录RP聊天状态 <threadId, contextId>
    private final ConcurrentHashMap<Long, String> rpThreadToContextIdMap = new ConcurrentHashMap<>();
    
    // 终止列表，存储已经被终止的contextId
    private final ConcurrentHashMap<String, Boolean> terminatedContextIds = new ConcurrentHashMap<>();

    @Autowired
    private ModelRoleRepository modelRoleRepository;

    @Autowired
    private ModelRpThreadRepository threadRepository;

    @Autowired
    private ModelRpHistoryRepository historyRepository;
    
    @Autowired
    private ModelRpSegmentRepository segmentRepository;

    @Autowired
    private ModelGeminiService modelGeminiService;
    
    @Autowired
    private UserConfigService userConfigService;
    
    @Autowired
    private PanelApiKeyService panelApiKeyService;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private ModelGrokService modelGrokService;

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private ModelRpCastService castService;

    @Autowired
    private ModelRpScriptService scriptService;

    /**
     * 获取模型角色列表（按 sortOrder 升序排序）
     *
     * @param dto 查询参数 DTO
     * @return 分页视图对象
     * @throws BizException 业务异常
     */
    public PageableView<GetModelRoleListVo> getModelRoleList(GetModelRoleListDto dto) throws BizException {

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
        Page<ModelRolePo> page = modelRoleRepository.findAll(example.get(), pageable);

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


    /**
     * 恢复或创建RP对话
     */
    @Transactional
    public RecoverRpChatVo recoverRpChat(RecoverRpChatDto dto) throws BizException{

        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModelCode());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        //激活指定存档
        if(dto.getThreadId()!=null){
            scriptService.activeThread(dto.getThreadId());
        }

        //查询激活的存档
        ModelRpThreadPo threadCt = threadRepository.getActiveThreadWithRoleAndHistories(dto.getModelRoleId());

        //newThread=0 创建新存档
        if(dto.getNewThread() == 0 || threadCt == null) {

            //查询用户当前所扮演的角色(未解密)
            ModelUserRolePo userPlayRoleCt = castService.getUserPlayRole(AuthService.getCurrentUserId());

            if(userPlayRoleCt == null){
                throw new BizException("无法创建新存档:未找到用户所扮演的角色!");
            }

            //查询模型所扮演的角色(未解密)
            ModelRolePo modelPlayRoleCt = castService.getModelPlayRole(dto.getModelRoleId());

            if(modelPlayRoleCt == null){
                throw new BizException("无法创建新存档:未找到模型所扮演的角色!");
            }

            //调用剧本服务创建新存档
            scriptService.createNewThread(userPlayRoleCt,modelPlayRoleCt,dto.getModelCode());
            threadCt = threadRepository.getActiveThreadWithRoleAndHistories(dto.getModelRoleId());
        }

        //查询模型扮演的角色 + 用户扮演的角色
        ModelUserRolePo userPlayRoleCt = threadCt.getUserRole();
        ModelRolePo modelPlayRoleCt = threadCt.getModelRole();

        //查询历史记录
        List<ModelRpHistoryPo> historyPos = threadCt.getHistories();
        List<RecoverRpChatHistoryVo> historyVos = new ArrayList<>();

        //构建响应数据
        RecoverRpChatVo vo = new RecoverRpChatVo();
        vo.setThreadId(threadCt.getId());
        vo.setModelCode(threadCt.getModelCode());
        vo.setMessages(historyVos);

        if(historyPos == null){
            return vo;
        }

        for(ModelRpHistoryPo history : historyPos){

            RecoverRpChatHistoryVo hisVo = as(history,RecoverRpChatHistoryVo.class);

            //解密Vo中的消息
            hisVo.setRawContent(css.decryptForCurUser(hisVo.getRawContent()));

            //处理用户扮演角色消息
            if(history.getType() == 0){

                hisVo.setName("user");
                hisVo.setAvatarPath("");

                if(userPlayRoleCt != null){
                    hisVo.setName(userPlayRoleCt.getName());
                    if(StringUtils.isNotBlank(userPlayRoleCt.getAvatarPath())){
                        hisVo.setAvatarPath("/res/"+css.decryptForCurUser(userPlayRoleCt.getAvatarPath()));
                    }
                }
            }

            //处理AI消息
            if(history.getType() == 1){
                hisVo.setName(modelPlayRoleCt.getName());
                hisVo.setAvatarPath("");
                if(StringUtils.isNotBlank(modelPlayRoleCt.getAvatarPath())){
                    hisVo.setAvatarPath("/res/"+css.decryptForCurUser(modelPlayRoleCt.getAvatarPath()));
                }
            }

            historyVos.add(hisVo);
        }

        userConfigService.setValue(UserConfigEnum.MODEL_RP_CURRENT_THREAD.key(), threadCt.getId());
        userConfigService.setValue(UserConfigEnum.MODEL_RP_CURRENT_ROLE.key(), threadCt.getModelRole().getId());
        return vo;
    }


    /**
     * 发送RP对话消息
     * @param dto 批量完成RP对话的请求参数
     * @return 返回对话片段信息
     * @throws BizException 业务异常
     */
    @Transactional
    public RpSegmentVo rpCompleteSendBatch(BatchRpCompleteDto dto) throws BizException {

        // 检查该会话是否正在处理中
        if (rpThreadToContextIdMap.containsKey(dto.getThreadId())) {
            throw new BizException("该会话正在处理中，请等待AI响应完成");
        }

        //获取并验证模型配置
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        //获取当前用户的会话信息
        ModelRpThreadPo threadCt = scriptService.getCurUserActiveThread(dto.getThreadId());

        if(threadCt == null){
            throw new BizException("会话不存在或不可用");
        }

        String apiKey = panelApiKeyService.getApiKey(modelEnum.getCode(), threadCt.getUserId());
        if (StringUtils.isBlank(apiKey)) {
            throw new BizException("未配置API Key");
        }

        //获取用户与模型扮演的角色
        ModelUserRolePo userPlayRoleCt = threadCt.getUserRole();
        ModelRolePo modelPlayRoleCt = threadCt.getModelRole();

        if (modelPlayRoleCt == null) {
            throw new BizException("模型角色信息不存在");
        }

        // 先获取全部历史记录
        List<ModelRpHistoryPo> historyPos = new ArrayList<>();

        Long userHistoryId = null;

        if(dto.getQueryKind() == 0){

            //获取用户之前的加密聊天记录
            historyPos.addAll(historyRepository.findByThreadIdOrderBySequence(threadCt.getId()));

            //创建加密的用户的历史消息
            ModelRpHistoryPo userHistoryCt = scriptService.createNewRpUserHistory(dto.getThreadId(), dto.getMessage());
            userHistoryId = userHistoryCt.getId();
        }

        // 处理重新生成逻辑
        if (dto.getQueryKind() ==3 && dto.getRegenerateRootHistoryId()!=null) {
            // 获取指定的根消息记录
            var rootHistoryQuery = new ModelRpHistoryPo();
            rootHistoryQuery.setId(dto.getRegenerateRootHistoryId());
            rootHistoryQuery.setThread(threadCt);
            rootHistoryQuery.setType(0); //用户消息

            ModelRpHistoryPo rootHistory = historyRepository.findOne(Example.of(rootHistoryQuery))
                .orElseThrow(() -> new BizException("指定的根消息记录不存在"));
            
            // 删除根消息之后的所有消息
            historyRepository.removeHistoryAfter(dto.getThreadId(), rootHistory.getSequence());
            
            // 使用根消息的内容作为当前要发送的消息
            dto.setMessage(css.decryptForCurUser(rootHistory.getRawContent()));
            userHistoryId = rootHistory.getId();

            //获取用户加密聊天记录
            historyPos.addAll(historyRepository.findByThreadIdOrderBySequence(threadCt.getId()));

            //需要移除最后一条消息(防止历史记录中的消息与当前要发送的消息出现重复)
            if(!historyPos.isEmpty()){
                if(historyPos.getLast().getType() == 0){
                    historyPos.removeLast();
                }
            }

        }

        //创建主Prompt
        PreparedPrompt prompt = scriptService.createSystemPrompt(userPlayRoleCt, modelPlayRoleCt);

        //解析模型示例对话
        prompt = scriptService.appendExamplePrompt(modelPlayRoleCt.getId(),prompt);

        String finalPrompt = prompt.executeNested();

        // 清理之前的消息片段(如果有)
        segmentRepository.deleteByThreadId(threadCt.getId());

        //为消息创建起始片段
        scriptService.createStartSegment(threadCt);

        OkHttpClient client = HttpClientUtils.createHttpClient(getProxyConfig(threadCt.getUserId()), 60);

        // 创建请求参数
        ModelChatParam param = new ModelChatParam();
        param.setMessage(dto.getMessage());
        param.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent");
        param.setApiKey(apiKey);
        param.setModelCode(dto.getModel());
        param.setSystemPrompt(finalPrompt);


        List<ModelChatParamHistory> paramHistories = new ArrayList<>();
        param.setHistories(paramHistories);

        for (ModelRpHistoryPo history : historyPos) {
            ModelChatParamHistory paramHistory = new ModelChatParamHistory();
            paramHistory.setRole(history.getType()); // 设置角色类型：0-用户 1-AI助手
            paramHistory.setContent(css.decryptForCurUser(history.getRawContent()));
            paramHistories.add(paramHistory);
        }

        //将新模型选项保存到Thread
        threadCt.setModelCode(modelEnum.getCode());
        threadRepository.save(threadCt);

        userConfigService.readUserModelParam(param,null);

        try {

            if(dto.getModel().contains("grok")){
                param.setUrl(GROK_BASE_URL);
                // 异步调用ModelGrokService发送流式请求
                modelGrokService.sendMessageStream(client, param,
                        onModelRpMessageRcv(threadCt, threadCt.getUserId())
                );
            }

            if(dto.getModel().contains("gemini")){
                // 异步调用ModelGeminiService发送流式请求
                modelGeminiService.sendMessageStream(client, param,
                        onModelRpMessageRcv(threadCt, threadCt.getUserId())
                );
            }

            // 返回用户消息作为第一次响应
            RpSegmentVo vo = new RpSegmentVo();
            vo.setThreadId(threadCt.getId());
            vo.setHistoryId(userHistoryId);
            vo.setRole(0); // 用户角色
            vo.setSequence(0);
            vo.setContent(dto.getMessage()); // 返回用户的消息内容
            vo.setType(0); // 起始类型
            
            // 设置角色信息
            vo.setRoleId(null);
            vo.setRoleName("user");
            vo.setRoleAvatarPath(null);
            if(userPlayRoleCt != null){
                vo.setRoleId(userPlayRoleCt.getId());
                vo.setRoleName(userPlayRoleCt.getName());

                var pathPt = css.decryptForCurUser(userPlayRoleCt.getAvatarPath());

                if(StringUtils.isNotBlank(pathPt)){
                    vo.setRoleAvatarPath("/res/"+pathPt);
                }
            }

            return vo;

        } catch (Exception e) {
            // 发生异常时清理会话状态
            rpThreadToContextIdMap.remove(threadCt.getId());

            // 清理所有片段
            try {
                segmentRepository.deleteByThreadId(threadCt.getId());
            } catch (Exception ex) {
                log.error("清理RP对话片段失败", ex);
            }

            throw new BizException("发送RP对话消息失败: " + e.getMessage());
        }
    }

    /**
     * 重新生成AI最后一条回复
     * @param dto 批量完成RP对话的请求参数
     * @return 返回对话片段信息
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public RpSegmentVo rpCompleteRegenerateBatch(BatchRpCompleteDto dto) throws BizException {
        // 检查该会话是否正在处理中
        if (rpThreadToContextIdMap.containsKey(dto.getThreadId())) {
            throw new BizException("该会话正在处理中，请等待AI响应完成");
        }

        //获取并验证模型配置
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        //获取当前用户的会话信息
        ModelRpThreadPo threadCt = scriptService.getCurUserActiveThread(dto.getThreadId());

        if(threadCt == null){
            throw new BizException("会话不存在或不可用");
        }

        // 查找最后一条用户消息
        ModelRpHistoryPo lastUserMessage = historyRepository.getLastMessage(dto.getThreadId(), 0);
        if (lastUserMessage == null) {
            throw new BizException("未找到任何用户消息");
        }

        // 使用找到的最后一条用户消息作为重新生成的起点
        var batchSendDto = new BatchRpCompleteDto();
        batchSendDto.setThreadId(dto.getThreadId());
        batchSendDto.setModel(dto.getModel());
        batchSendDto.setQueryKind(3); //重新生成AI最后一条回复
        batchSendDto.setRegenerateRootHistoryId(lastUserMessage.getId());
        return rpCompleteSendBatch(batchSendDto);
    }
    
    /**
     * 查询RP对话响应流
     * @param dto 批量完成RP对话的请求参数
     * @return 返回对话片段信息
     * @throws BizException 业务异常
     */
    public RpSegmentVo rpCompleteQueryBatch(BatchRpCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();
        Long userId = AuthService.getCurrentUserId();
        
        // 最大等待次数和等待时间
        final int MAX_WAIT_TIMES = 3;
        final long WAIT_INTERVAL_MS = 300;
        
        // 尝试获取所有未读片段，最多等待3次
        List<ModelRpSegmentPo> unreadSegments = null;
        int waitTimes = 0;
        
        while (waitTimes < MAX_WAIT_TIMES) {
            // 一次性查询所有未读片段，按sequence排序
            unreadSegments = segmentRepository.findAllUnreadByThreadIdOrderBySequence(threadId);

            // 如果有未读片段，跳出循环
            if (!unreadSegments.isEmpty()) {
                break;
            }
            
            // 如果没有未读片段，且会话不在处理中，直接返回null
            if (!rpThreadToContextIdMap.containsKey(threadId)) {
                return null;
            }
            
            // 等待一段时间后再次尝试
            try {
                Thread.sleep(WAIT_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BizException("等待片段时被中断");
            }
            
            waitTimes++;
        }
        
        // 如果等待超时仍未获取到片段
        if (unreadSegments.isEmpty()) {
            return null;
        }
        
        // 检查权限
        if (!unreadSegments.getFirst().getUserId().equals(userId)) {
            throw new BizException("无权访问该会话");
        }

        // 优先处理错误片段(type=10)和开始片段(type=0)
        for (ModelRpSegmentPo segment : unreadSegments) {
            //开始片段，只标记该片段为已读并返回
            if (segment.getType() == 0) {
                segment.setStatus(1);
                segmentRepository.save(segment);
                RpSegmentVo vo = new RpSegmentVo();
                vo.setThreadId(threadId);
                vo.setHistoryId(segment.getHistoryId());
                vo.setSequence(segment.getSequence());
                vo.setContent(segment.getContent());
                vo.setType(segment.getType());
                vo.setRole(1);
                return vo;
            }

            //错误片段，标记所有片段为已读并返回错误信息
            if (segment.getType() == 10) {
                for (ModelRpSegmentPo seg : unreadSegments) {
                    seg.setStatus(1);
                }
                segmentRepository.saveAll(unreadSegments);
                RpSegmentVo vo = new RpSegmentVo();
                vo.setThreadId(threadId);
                vo.setSequence(segment.getSequence());
                vo.setContent(segment.getContent() != null ? segment.getContent() : "AI响应出错");
                vo.setType(segment.getType());
                vo.setRole(1);
                return vo;
            }
        }

        // 获取第一个片段
        ModelRpSegmentPo firstSegment = unreadSegments.getFirst();
        
        // 如果第一个片段是数据片段(type=1)，则合并后续的数据片段
        if (firstSegment.getType() == 1) {
            StringBuilder combinedContent = new StringBuilder();
            List<ModelRpSegmentPo> segmentsToMark = new ArrayList<>();
            
            // 遍历所有片段，合并type=1的片段，直到遇到非type=1的片段
            for (ModelRpSegmentPo segment : unreadSegments) {

                if (segment.getType() != 1) {
                    break;
                }

                //合并时解密
                combinedContent.append(css.decryptForCurUser(segment.getContent()));
                segment.setStatus(1);
                segmentsToMark.add(segment);
            }

            segmentRepository.saveAll(segmentsToMark);
            
            // 返回合并后的数据片段
            RpSegmentVo vo = new RpSegmentVo();
            vo.setThreadId(threadId);
            vo.setHistoryId(null);
            vo.setSequence(firstSegment.getSequence());
            vo.setContent(combinedContent.toString());
            vo.setType(1);
            vo.setRole(1);
            return vo;
        }
        
        // 如果第一个片段是type=2结束片段
        firstSegment.setStatus(1);
        segmentRepository.save(firstSegment);
        
        RpSegmentVo vo = new RpSegmentVo();
        vo.setThreadId(threadId);
        vo.setHistoryId(firstSegment.getHistoryId());
        vo.setSequence(firstSegment.getSequence());
        vo.setContent(firstSegment.getContent());
        vo.setType(firstSegment.getType());
        vo.setRole(1);
        return vo;
    }
    
    /**
     * 终止RP对话响应
     * @param dto 批量完成RP对话的请求参数
     * @throws BizException 业务异常
     */
    public void rpCompleteTerminateBatch(BatchRpCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();
        Long userId = AuthService.getCurrentUserId();

        // 检查会话是否存在
        ModelRpThreadPo thread = threadRepository.findById(threadId).orElse(null);
        if (thread == null) {
            throw new BizException("会话不存在");
        }

        // 检查权限
        if (!thread.getUserId().equals(userId)) {
            throw new BizException("无权访问该会话");
        }

        // 获取当前正在进行的contextId
        String contextId = rpThreadToContextIdMap.get(threadId);
        if (contextId == null) {
            throw new BizException("该会话未在进行中或已经终止");
        }

        // 将contextId加入终止列表
        terminatedContextIds.put(contextId, true);
        
        // 清理会话状态
        rpThreadToContextIdMap.remove(threadId);

        // 获取当前最大序号
        int nextSequence = segmentRepository.findMaxSequenceByThreadId(threadId) + 1;

        // 创建终止片段
        ModelRpSegmentPo endSegment = new ModelRpSegmentPo();
        endSegment.setUserId(userId);
        endSegment.setThread(thread);
        endSegment.setSequence(nextSequence);
        endSegment.setContent("用户终止了AI响应");
        endSegment.setStatus(0); // 未读状态
        endSegment.setType(2); // 结束类型
        segmentRepository.save(endSegment);
    }

    /**
     * 删除指定的消息历史记录
     * @param dto 包含要删除的消息ID
     * @throws BizException 如果消息不存在或不属于当前用户
     */
    @Transactional
    public void removeRpHistory(RemoveRpHistoryDto dto) throws BizException {
        Long currentUserId = AuthService.getCurrentUserId();
        
        // 查询消息记录
        ModelRpHistoryPo history = historyRepository.findById(dto.getHistoryId())
            .orElseThrow(() -> new BizException("消息不存在"));
        
        // 验证消息所属的会话是否属于当前用户
        ModelRpThreadPo thread = history.getThread();
        if (!thread.getUserId().equals(currentUserId)) {
            throw new BizException("无权删除此消息");
        }
        
        // 删除消息
        historyRepository.delete(history);
    }

    /**
     * 编辑RP对话历史记录
     * @param dto 编辑RP对话历史记录的请求参数
     * @throws BizException 业务异常
     */
    @Transactional
    public void editRpHistory(EditRpHistoryDto dto) throws BizException {
        //查询历史记录
        ModelRpHistoryPo history = historyRepository.findById(dto.getHistoryId())
                .orElseThrow(() -> new BizException("历史记录不存在"));

        //验证用户权限
        if (!history.getThread().getUserId().equals(AuthService.getCurrentUserId())) {
            throw new BizException("无权编辑此消息");
        }

        //更新消息内容
        history.setRawContent(dto.getContent());
        history.setRpContent(dto.getContent());
        css.encryptEntity(history);
        historyRepository.save(history);
    }

    /**
     * 获取指定模型角色的所有会话列表
     * 用于用户管理和选择不同的会话
     * 
     * @param dto 包含模型角色ID的DTO
     * @return 会话列表
     * @throws BizException 业务异常
     */
    public List<ModelRoleThreadListVo> getModelRoleThreadList(GetModelRoleThreadListDto dto) throws BizException {
        // 构建查询条件
        ModelRpThreadPo query = new ModelRpThreadPo();
        query.setUserId(AuthService.getCurrentUserId()); // 获取当前用户ID
        
        // 使用Example构建查询
        SimpleExample<ModelRpThreadPo> example = SimpleExample.of(query);
        
        // 添加排序（按更新时间倒序）
        example.orderByDesc("updateTime");
        
        // 执行查询，查询指定modelRoleId的记录
        List<ModelRpThreadPo> threads = threadRepository.findAll(example.get(),example.getSort());

        //解密全部会话
        css.processList(threads,false);

        // 过滤出指定modelRoleId的记录，同时获取每个线程的最后一条消息作为预览
        List<ModelRoleThreadListVo> result = new ArrayList<>();
        
        for (ModelRpThreadPo thread : threads) {
            // 判断是否是指定角色的线程
            if (thread.getModelRole() != null && thread.getModelRole().getId().equals(dto.getModelRoleId())) {
                ModelRoleThreadListVo vo = new ModelRoleThreadListVo();
                
                // 将PO的基本字段映射到VO
                vo.setId(thread.getId());
                vo.setTitle(css.decryptForCurUser(thread.getTitle()));
                vo.setDescription(css.decryptForCurUser(thread.getDescription()));
                vo.setModelCode(thread.getModelCode());
                vo.setActive(thread.getActive());
                vo.setCreateTime(thread.getCreateTime());
                vo.setUpdateTime(thread.getUpdateTime());
                
                // 获取最后一条消息（如果有）作为预览
                String lastMessage = "";
                if (thread.getHistories() != null && !thread.getHistories().isEmpty()) {
                    // 找出序号最大的那条消息
                    ModelRpHistoryPo lastHistoryItem = thread.getHistories().stream()
                        .max(Comparator.comparing(ModelRpHistoryPo::getSequence))
                        .orElse(null);

                    // 截取前50个字符作为预览
                    String content = css.decryptForCurUser(lastHistoryItem.getRawContent());
                    lastMessage = content.length() > 50 ?
                        content.substring(0, 50) + "..." :
                        content;
                }
                vo.setLastMessage(lastMessage);
                result.add(vo);
            }
        }
        
        return result;
    }

    /**
     * 删除指定的会话及其相关历史记录
     * 
     * @param dto 包含threadId参数
     * @throws BizException 业务异常
     */
    @Transactional
    public void removeThread(RemoveThreadDto dto) throws BizException {
        Long currentUserId = AuthService.getCurrentUserId();
        Long threadId = dto.getThreadId();
        
        // 1. 根据用户ID+ThreadID查询是否有该Thread
        ModelRpThreadPo query = new ModelRpThreadPo();
        query.setId(threadId);
        query.setUserId(currentUserId);
        
        ModelRpThreadPo thread = threadRepository.findOne(Example.of(query))
            .orElseThrow(() -> new BizException("会话不存在或无权删除"));
        
        // 2. 判断该thread是否为激活的
        if (thread.getActive() == 1) {
            // 如果当前是激活的，需要查询updateTime最新的一个thread将其设置为激活
            ModelRpThreadPo newActiveThread = threadRepository.findTopByUserIdAndModelRoleAndIdNotOrderByUpdateTimeDesc(
                currentUserId, 
                thread.getModelRole(), 
                threadId
            );
            
            if (newActiveThread != null) {
                // 将找到的最新线程设置为激活状态
                newActiveThread.setActive(1);
                threadRepository.save(newActiveThread);
            }
        }
        
        // 3. 删除该thread下的所有history
        historyRepository.deleteByThreadId(threadId);
        
        // 4. 删除该thread下的所有segment
        segmentRepository.deleteByThreadId(threadId);
        
        // 5. 最后删除thread本身
        threadRepository.delete(thread);
    }

    /**
     * 获取用户空间或全局的代理配置
     * @return 代理url
     */
    public String getProxyConfig(Long uid){
        // 获取代理配置 - 首先检查用户级别的代理配置
        String proxyConfig = userConfigService.get("model.proxy.config", uid);

        // 如果用户未配置代理，则使用全局代理配置
        if (StringUtils.isBlank(proxyConfig)) {
            proxyConfig = globalConfigService.get("model.proxy.config");
        }
        return proxyConfig;
    }

    /**
     * 创建处理模型消息回调的Consumer
     * @param thread RP聊天线程
     * @param userId 用户ID
     * @return 处理模型消息的Consumer
     */
    private Consumer<ModelChatContext> onModelRpMessageRcv(ModelRpThreadPo thread, Long userId) {
        return context -> {
            try {
                // 从ModelChatContext中获取contextId
                String contextId = context.getContextId();
                Long threadId = thread.getId();

                // 检查该contextId是否已被终止
                if (terminatedContextIds.containsKey(contextId)) {
                    // 如果是完成或错误回调，从终止列表中移除
                    if (context.getType() == 1 || context.getType() == 2) {
                        terminatedContextIds.remove(contextId);
                    }
                    return;
                }

                // 首次收到消息时，将contextId存入映射表
                if (!rpThreadToContextIdMap.containsValue(contextId)) {
                    rpThreadToContextIdMap.put(threadId, contextId);
                }

                // 获取当前最大序号
                int nextSequence = segmentRepository.findMaxSequenceByThreadId(threadId) + 1;

                // 根据context.type处理不同类型的消息
                if (context.getType() == 0) {
                    // 数据类型 - 创建数据片段
                    scriptService.createDataSegment(userId,threadId,context.getContent(),nextSequence);
                    return;
                }

                //响应已结束
                if (context.getType() == 1) {

                    // 保存加密的AI响应到历史记录
                    ModelRpHistoryPo modelHistory = new ModelRpHistoryPo();
                    modelHistory.setThread(thread);
                    modelHistory.setType(1); //Model消息
                    modelHistory.setRawContent(context.getContent());
                    modelHistory.setRpContent(context.getContent());
                    modelHistory.setSequence(historyRepository.findMaxSequenceByThreadId(threadId) + 1);
                    css.encryptEntity(modelHistory);
                    historyRepository.save(modelHistory);

                    // 完成类型 - 创建结束片段
                    ModelRpSegmentPo endSegment = new ModelRpSegmentPo();
                    endSegment.setUserId(userId);
                    endSegment.setThread(thread);
                    endSegment.setSequence(nextSequence);
                    endSegment.setContent(null);
                    endSegment.setStatus(0); // 未读状态
                    endSegment.setType(2); // 结束类型
                    endSegment.setHistoryId(modelHistory.getId()); // 设置关联的历史记录ID
                    segmentRepository.save(endSegment);

                    // 清理会话状态
                    rpThreadToContextIdMap.remove(threadId);
                    return;
                }

                if (context.getType() == 2) {
                    // 错误类型 - 创建错误片段
                    ModelRpSegmentPo errorSegment = new ModelRpSegmentPo();
                    errorSegment.setUserId(userId);
                    errorSegment.setThread(thread);
                    errorSegment.setSequence(nextSequence);
                    errorSegment.setContent(context.getException() != null ? "AI响应错误: " + context.getException().getMessage() : "AI响应错误");
                    errorSegment.setStatus(0); // 未读状态
                    errorSegment.setType(10); // 错误类型
                    css.encryptEntity(errorSegment);
                    segmentRepository.save(errorSegment);

                    // 清理会话状态
                    rpThreadToContextIdMap.remove(threadId);
                }
            } catch (Exception e) {
                log.error("处理RP对话片段失败", e);
            }
        };
    }
}