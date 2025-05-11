package com.ksptool.ql.biz.service;

import com.ksptool.entities.Any;
import com.ksptool.ql.biz.mapper.*;
import com.ksptool.ql.biz.model.dto.RecoverChatDto;
import com.ksptool.ql.biz.model.po.*;
import com.ksptool.ql.biz.model.vo.RecoverChatVo;
import com.ksptool.ql.biz.model.vo.RecoverChatHistoryVo;
import com.ksptool.ql.biz.service.contentsecurity.ContentSecurityService;
import com.ksptool.ql.commons.enums.GlobalConfigEnum;
import com.ksptool.ql.commons.enums.UserConfigEnum;
import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.enums.AIModelEnum;
import com.ksptool.ql.commons.utils.HttpClientUtils;
import com.ksptool.ql.commons.utils.PreparedPrompt;
import com.ksptool.ql.commons.utils.ThreadStatusTrack;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import static com.ksptool.entities.Entities.as;
import java.util.function.Consumer;
import com.ksptool.ql.biz.model.vo.ChatSegmentVo;
import com.ksptool.ql.biz.model.dto.BatchChatCompleteDto;
import com.ksptool.ql.biz.model.dto.ModelChatParam;
import com.ksptool.ql.biz.model.dto.ModelChatParamHistory;
import com.ksptool.ql.biz.model.vo.ModelChatContext;
import com.ksptool.ql.biz.model.dto.CreateEmptyThreadDto;
import com.ksptool.ql.biz.model.vo.CreateEmptyThreadVo;

@Slf4j
@Service
public class ModelChatService {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GROK_BASE_URL = "https://api.x.ai/v1/chat/completions";

    //线程会话状态跟踪
    private final ThreadStatusTrack track = new ThreadStatusTrack();

    @Autowired
    private PlayerConfigService playerConfigService;
    
    @Autowired
    private GlobalConfigService globalConfigService;
    
    @Autowired
    private ModelChatThreadRepository modalThreadRepository;
    
    @Autowired
    private ModelChatHistoryRepository modelHistoryRepository;
    
    @Autowired
    private ModelChatSegmentRepository modelSegmentRepository;
    
    @Autowired
    private ModelGeminiService modelGeminiService;

    @Autowired
    private ModelGrokService modelGrokService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ContentSecurityService css;

    @Autowired
    private ChatThreadRepository threadRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private ChatSegmentRepository segmentRepository;


    public ModelChatThreadPo createOrRetrieveThread(Long threadId, Long playerId, String modelCode) throws BizException {

        if (threadId == null || threadId == -1) {
            // 创建新的会话
            long count = modalThreadRepository.countByPlayerId(playerId);
            
            ModelChatThreadPo thread = new ModelChatThreadPo();
            thread.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));
            thread.setTitle("新对话" + (count + 1));
            thread.setModelCode(modelCode);
            css.encryptEntity(thread);
            return modalThreadRepository.save(thread);
        }
        
        // 获取已有会话
        ModelChatThreadPo thread = modalThreadRepository.findByIdWithHistories(threadId);
        if (thread == null) {
            throw new BizException("会话不存在");
        }
        
        return thread;
    }
    
    private ModelChatHistoryPo createHistory(ModelChatThreadPo thread, String content, Integer role) throws BizException {

        ModelChatHistoryPo history = new ModelChatHistoryPo();
        history.setThread(thread);
        history.setPlayer(thread.getPlayer());
        history.setContent(content);
        history.setRole(role);
        
        // 获取当前最大序号并加1
        int nextSequence = modelHistoryRepository.findMaxSequenceByThreadId(thread.getId()) + 1;
        history.setSequence(nextSequence);
        css.encryptEntity(history,thread.getPlayer().getUser().getId());
        return modelHistoryRepository.save(history);
    }

    /**
     * 创建ModelChatDto并预先填充配置信息
     * @param modelCode 模型代码
     * @param playerId 人物ID
     * @return 预填充配置的ModelChatDto
     * @throws BizException 业务异常
     */
    public ModelChatParam createModelChatDto(String modelCode, Long playerId) throws BizException {
        // 验证模型代码
        AIModelEnum modelEnum = AIModelEnum.getByCode(modelCode);
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        // 创建并填充DTO
        ModelChatParam param = new ModelChatParam();
        param.setModelCode(modelEnum.getCode());
        playerConfigService.readPlayerModelParam(param,playerId);
        return param;
    }

    /**
     * 发送批量聊天消息
     * @param dto 批量聊天请求参数
     * @return 聊天片段VO
     * @throws BizException 业务异常
     */
    public ChatSegmentVo chatCompleteSendBatch(BatchChatCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();

        // 检查该会话是否正在处理中
        if (!threadId.equals(-1L) && track.isLocked(threadId)) {
            throw new BizException("该会话正在处理中，请等待AI响应完成");
        }

        try {
            // 获取并验证模型配置
            AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
            if (modelEnum == null) {
                throw new BizException("无效的模型代码");
            }

            // 获取当前用户ID
            Long userId = AuthService.getCurrentUserId();

            //获取当前玩家ID
            var playerId = AuthService.getCurrentPlayerId();

            String apiKey = apiKeyService.getApiKey(modelEnum.getCode(), playerId);

            if (StringUtils.isBlank(apiKey)) {
                throw new BizException("未配置API Key");
            }

            // 获取或创建会话
            ModelChatThreadPo thread = createOrRetrieveThread(threadId, userId, modelEnum.getCode());

            // 创建新会话
            track.newSession(thread.getId());

            // 先获取全部消息历史
            List<ModelChatHistoryPo> historyPos = new ArrayList<>();

            // 处理重新生成逻辑
            Long userHistoryId = null;

            // 保存用户消息
            if(dto.getQueryKind() == 0){

                //获取用户加密聊天记录
                historyPos.addAll(modelHistoryRepository.getByThreadId(thread.getId()));

                ModelChatHistoryPo userHistory = createHistory(thread, dto.getMessage(), 0);
                userHistoryId = userHistory.getId();
            }

            if (dto.getQueryKind() == 3 && dto.getRegenerateRootHistoryId() != null) {

                var query = new ModelChatHistoryPo();
                query.setId(dto.getRegenerateRootHistoryId());
                query.setThread(Any.of().val("id",threadId).as(ModelChatThreadPo.class));
                query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

                ModelChatHistoryPo rootHistory = modelHistoryRepository.findOne(Example.of(query))
                        .orElseThrow(() -> new BizException("指定的根消息记录不存在或无权访问"));

                // 检查消息类型，只能是用户消息
                if (rootHistory.getRole() != 0) {
                    throw new BizException("只能以用户消息作为重新生成的起点");
                }
                
                // 删除根消息之后的所有消息
                modelHistoryRepository.removeHistoryAfter(threadId, rootHistory.getSequence());
                
                // 使用根消息的ID作为当前用户消息ID
                userHistoryId = rootHistory.getId();
                thread = createOrRetrieveThread(threadId, userId, modelEnum.getCode());

                //获取用户加密聊天记录
                historyPos.addAll(modelHistoryRepository.getByThreadId(thread.getId()));

                //需要移除最后一条消息(防止历史记录中的消息与当前要发送的消息出现重复)
                if(!historyPos.isEmpty()){
                    if(historyPos.getLast().getRole() == 0){
                        historyPos.removeLast();
                    }
                }
            }

            // 清理之前的片段（如果有）
            modelSegmentRepository.deleteByThreadId(thread.getId());

            // 创建开始片段并返回
            ModelChatSegmentPo startSegment = new ModelChatSegmentPo();
            startSegment.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
            startSegment.setThread(thread);
            startSegment.setSequence(1);
            startSegment.setContent(null);
            startSegment.setStatus(0); // 未读状态
            startSegment.setType(0); // 开始类型
            modelSegmentRepository.save(startSegment);

            // 获取代理配置
            String proxyConfig = getProxyConfig(playerId);

            // 创建HTTP客户端
            OkHttpClient client = HttpClientUtils.createHttpClient(proxyConfig, 60);

            // 创建请求DTO
            ModelChatParam modelChatParam = createModelChatDto(modelEnum.getCode(), playerId);
            modelChatParam.setMessage(dto.getMessage());
            modelChatParam.setApiKey(apiKey);

            // 根据模型类型设置不同的URL
            if (dto.getModel().contains("grok")) {
                modelChatParam.setUrl(GROK_BASE_URL);
            } else {
                modelChatParam.setUrl(GEMINI_BASE_URL + modelEnum.getCode() + ":streamGenerateContent");
            }

            //将新模型选项保存到Thread
            thread.setModelCode(modelEnum.getCode());
            modalThreadRepository.save(thread);

            modelChatParam.setHistories(as(historyPos, ModelChatParamHistory.class));

            //解密记录内容
            for(var item : modelChatParam.getHistories()){
                item.setContent(css.decryptForCurUser(item.getContent()));
            }

            // 根据模型类型选择不同的服务发送请求
            if (dto.getModel().contains("grok")) {
                // 使用GROK服务
                modelGrokService.sendMessageStream(
                        client,
                        modelChatParam,
                        onModelMessageRcv(thread, userId,playerId)
                );
            }
            if(dto.getModel().contains("gemini")){
                // 使用Gemini服务
                modelGeminiService.sendMessageStream(
                        client,
                        modelChatParam,
                        onModelMessageRcv(thread, userId,playerId)
                );
            }

            // 返回用户消息作为第一次响应
            ChatSegmentVo vo = new ChatSegmentVo();
            vo.setThreadId(thread.getId());
            vo.setHistoryId(userHistoryId);
            vo.setRole(0); // 用户角色
            vo.setSequence(startSegment.getSequence());
            vo.setContent(dto.getMessage()); // 返回用户的消息内容
            vo.setType(0); // 起始类型，而不是数据类型
            vo.setName("User");
            vo.setAvatarPath(null);
            return vo;

        } catch (Exception e) {
            // 发生异常时通知会话失败
            if (!threadId.equals(-1L)) {
                track.notifyFailed(threadId);
                track.closeSession(threadId);
            }

            // 清理所有片段
            try {
                if (!threadId.equals(-1L)) {
                    modelSegmentRepository.deleteByThreadId(threadId);
                }
            } catch (Exception ex) {
                log.error("清理聊天片段失败", ex);
            }

            throw new BizException("发送批量聊天消息失败: " + e.getMessage());
        }
    }


    /**
     * 查询批量聊天响应
     * @param dto 批量聊天请求参数
     * @return 聊天片段VO
     * @throws BizException 业务异常
     */
    public ChatSegmentVo chatCompleteQueryBatch(BatchChatCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();
        Long playerId = AuthService.getCurrentPlayerId();

        // 最大等待次数和等待时间
        final int MAX_WAIT_TIMES = 3;
        final long WAIT_INTERVAL_MS = 300;
        
        // 尝试获取所有未读片段，最多等待3次
        List<ModelChatSegmentPo> unreadSegments = null;
        int waitTimes = 0;
        
        while (waitTimes < MAX_WAIT_TIMES) {
            // 检查会话状态
            if(track.getStatus(threadId) == 0){
                continue;
            }

            // 一次性查询所有未读片段，按sequence排序
            unreadSegments = modelSegmentRepository.findAllUnreadByThreadIdOrderBySequence(threadId,playerId);
            
            // 如果有未读片段，跳出循环
            if (!unreadSegments.isEmpty()) {
                break;
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

        // 优先处理错误片段(type=10)和开始片段(type=0)
        for (ModelChatSegmentPo segment : unreadSegments) {
            // 开始片段，只标记该片段为已读并返回
            if (segment.getType() == 0) {
                segment.setStatus(1);
                modelSegmentRepository.save(segment);
                ChatSegmentVo vo = new ChatSegmentVo();
                vo.setThreadId(threadId);
                vo.setHistoryId(segment.getHistoryId());
                vo.setSequence(segment.getSequence());
                vo.setContent(segment.getContent());
                vo.setType(segment.getType());
                vo.setRole(1); // AI助手角色
                return vo;
            }

            // 错误片段，标记所有片段为已读并返回错误信息
            if (segment.getType() == 10) {
                for (ModelChatSegmentPo seg : unreadSegments) {
                    seg.setStatus(1);
                }
                modelSegmentRepository.saveAll(unreadSegments);
                ChatSegmentVo vo = new ChatSegmentVo();
                vo.setThreadId(threadId);
                vo.setSequence(segment.getSequence());
                vo.setContent(segment.getContent() != null ? segment.getContent() : "AI响应出错");
                vo.setType(segment.getType());
                vo.setRole(1); // AI助手角色
                return vo;
            }
        }

        // 获取第一个片段
        ModelChatSegmentPo firstSegment = unreadSegments.getFirst();
        ModelChatThreadPo thread = firstSegment.getThread();
        AIModelEnum modelEnum = AIModelEnum.getByCode(thread.getModelCode());
        String modelName = modelEnum != null ? modelEnum.getSeries() : "AI助手";

        // 如果第一个片段是数据片段(type=1)，则合并后续的数据片段
        if (firstSegment.getType() == 1) {
            StringBuilder combinedContent = new StringBuilder();
            List<ModelChatSegmentPo> segmentsToMark = new ArrayList<>();
            
            // 遍历所有片段，合并type=1的片段，直到遇到非type=1的片段
            for (ModelChatSegmentPo segment : unreadSegments) {
                if (segment.getType() != 1) {
                    break;
                }
                
                combinedContent.append(css.decryptForCurUser(segment.getContent()));
                segment.setStatus(1); // 标记为已读
                segmentsToMark.add(segment);
            }
            
            modelSegmentRepository.saveAll(segmentsToMark);
            
            // 返回合并后的数据片段
            ChatSegmentVo vo = new ChatSegmentVo();
            vo.setThreadId(threadId);
            vo.setHistoryId(null);
            vo.setSequence(firstSegment.getSequence());
            vo.setContent(combinedContent.toString());
            vo.setType(1); // 数据类型
            vo.setRole(1); // AI助手角色
            vo.setName(modelName);
            vo.setAvatarPath(null);
            return vo;
        }
        
        // 如果第一个片段是type=2结束片段
        firstSegment.setStatus(1); // 标记为已读
        modelSegmentRepository.save(firstSegment);
        
        ChatSegmentVo vo = new ChatSegmentVo();
        vo.setThreadId(threadId);
        vo.setHistoryId(firstSegment.getHistoryId());
        vo.setSequence(firstSegment.getSequence());
        vo.setContent(firstSegment.getContent());
        vo.setType(firstSegment.getType());
        vo.setRole(1); // AI助手角色
        return vo;
    }

    /**
     * 终止批量聊天响应
     * @param dto 批量聊天请求参数
     * @throws BizException 业务异常
     */
    public void chatCompleteTerminateBatch(BatchChatCompleteDto dto) throws BizException {
        Long threadId = dto.getThreadId();

        var query = new ModelChatThreadPo();
        query.setId(threadId);
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        ModelChatThreadPo thread = modalThreadRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("会话不存在或不属于您"));

        // 检查会话状态
        int status = track.getStatus(threadId);
        if(status != 0 && status != 1){
            throw new BizException("该会话未在进行中或已经终止");
        }

        // 通知会话已终止
        track.notifyTerminated(threadId);

        // 获取当前最大序号
        int nextSequence = modelSegmentRepository.findMaxSequenceByThreadId(threadId) + 1;

        // 创建终止片段
        ModelChatSegmentPo endSegment = new ModelChatSegmentPo();
        endSegment.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));
        endSegment.setThread(thread);
        endSegment.setSequence(nextSequence);
        endSegment.setContent("用户终止了AI响应");
        endSegment.setStatus(0); // 未读状态
        endSegment.setType(2); // 结束类型
        modelSegmentRepository.save(endSegment);
    }

    /**
     * 编辑会话标题
     *
     * @param threadId 会话ID
     * @param newTitle 新标题
     * @throws BizException 业务异常
     */
    public void editThreadTitle(Long threadId, String newTitle) throws BizException {
        if (threadId == null) {
            throw new BizException("会话ID不能为空");
        }
        
        if (StringUtils.isBlank(newTitle)) {
            throw new BizException("标题不能为空");
        }
        
        // 限制标题长度
        if (newTitle.length() > 100) {
            newTitle = newTitle.substring(0, 97) + "...";
        }

        var query = new ModelChatThreadPo();
        query.setId(threadId);
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        ModelChatThreadPo thread = modalThreadRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("会话不存在或不属于您"));

        // 更新会话标题
        thread.setTitle(newTitle);
        // 标记为手动编辑的标题
        thread.setTitleGenerated(1);
        css.encryptEntity(thread);
        modalThreadRepository.save(thread);
    }

    /**
     * 生成会话标题
     * @param threadId 会话ID
     * @param model 模型代码
     */
    public void generateThreadTitle(Long threadId, String model,Long uid,Long playerId) {
        try {
            // 检查是否需要生成标题
            boolean shouldGenerateTitle = globalConfigService.getBoolean("model.chat.gen.thread.title", true);
            if (!shouldGenerateTitle) {
                return; // 配置为不生成标题，直接返回
            }
            
            // 获取会话
            ModelChatThreadPo thread = modalThreadRepository.findByIdWithHistories(threadId);
            if (thread == null) {
                throw new BizException("会话不存在");
            }
            
            // 检查是否已生成过标题
            if (thread.getTitleGenerated() != null && thread.getTitleGenerated() == 1) {
                return; // 已生成过标题，直接返回
            }
            
            // 获取第一条用户消息和对应的AI回复
            List<ModelChatHistoryPo> histories = thread.getHistories();
            if (histories == null || histories.isEmpty()) {
                return; // 没有历史记录，无法生成标题
            }
            
            // 查找第一条用户消息和对应的AI回复
            ModelChatHistoryPo firstUserMessage = null;
            ModelChatHistoryPo firstAIResponse = null;
            boolean foundUser = false;
            
            for (ModelChatHistoryPo history : histories) {
                if (!foundUser && history.getRole() == 0) { // 用户消息
                    firstUserMessage = history;
                    foundUser = true;
                    continue;
                }
                
                if (foundUser && history.getRole() == 1) { // AI回复
                    firstAIResponse = history;
                    break;
                }
            }
            
            if (firstUserMessage == null || firstAIResponse == null || 
                StringUtils.isBlank(firstUserMessage.getContent()) || 
                StringUtils.isBlank(firstAIResponse.getContent())) {
                return; // 必须同时有用户消息和AI回复才生成标题
            }

            // 从配置获取提示语模板
            String promptTemplate = globalConfigService.get(GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_PROMPT.getKey(),
                    GlobalConfigEnum.MODEL_CHAT_GEN_THREAD_PROMPT.getDefaultValue());

            String dekPt = css.getPlainUserDek(uid);

            PreparedPrompt prompt = PreparedPrompt.prepare(promptTemplate);
            prompt.setParameter("userContent", css.decryptForCurUser(firstUserMessage.getContent(),dekPt));
            prompt.setParameter("modelContent", css.decryptForCurUser(firstAIResponse.getContent(),dekPt));

            // 创建请求DTO
            ModelChatParam modelChatParam = createModelChatDto(model, playerId);
            modelChatParam.setMessage(prompt.execute());
                  
            // 获取代理配置
            String proxyUrl = getProxyConfig(playerId);
            
            String apiKey = apiKeyService.getApiKey(model, playerId);
            
            if (StringUtils.isBlank(apiKey)) {
                throw new BizException("未配置API Key");
            }
            
            // 根据模型类型设置不同的URL
            if (model.contains("grok")) {
                modelChatParam.setUrl(GROK_BASE_URL);
            } else {
                modelChatParam.setUrl(GEMINI_BASE_URL + model + ":generateContent");
            }
            
            modelChatParam.setApiKey(apiKey);
            
            // 根据模型类型选择不同的服务发送请求
            String title= "";

            if (model.contains("grok")) {
                // 使用GROK服务
                title = modelGrokService.sendMessageSync(HttpClientUtils.createHttpClient(proxyUrl, 30), modelChatParam);
            }
            if(model.contains("gemini")){
                // 使用Gemini服务
                title = modelGeminiService.sendMessageSync(HttpClientUtils.createHttpClient(proxyUrl, 30), modelChatParam);
            }
            
            // 处理标题（去除引号和多余空格，限制长度）
            title = title.replaceAll("^\"|\"$", "").trim();
            if (title.length() > 100) {
                title = title.substring(0, 97) + "...";
            }
            
            // 更新会话标题
            thread.setTitle(title);
            thread.setTitleGenerated(1); // 标记为已生成标题
            css.encryptEntity(thread,uid);
            modalThreadRepository.save(thread);
            
            log.info("已为会话 {} 生成标题: {}", threadId, title);
        } catch (Exception e) {
            log.error("生成会话标题失败", e);
            // 生成标题失败不抛出异常，不影响主流程
        }
    }

    /**
     * 重新生成AI最后一条回复
     * @param dto 批量聊天请求参数
     * @return 聊天片段VO
     * @throws BizException 业务异常
     */
    public ChatSegmentVo chatCompleteRegenerateBatch(BatchChatCompleteDto dto) throws BizException {
        // 检查该会话是否正在处理中
        if (track.isLocked(dto.getThreadId())) {
            throw new BizException("该会话正在处理中，请等待AI响应完成");
        }

        // 获取并验证模型配置
        AIModelEnum modelEnum = AIModelEnum.getByCode(dto.getModel());
        if (modelEnum == null) {
            throw new BizException("无效的模型代码");
        }

        var query = new ModelChatThreadPo();
        query.setId(dto.getThreadId());
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        if(modalThreadRepository.count(Example.of(query)) < 1){
            throw new BizException("会话不存在或不可用!");
        }

        // 查找最后一条用户消息
        ModelChatHistoryPo lastUserMessage = modelHistoryRepository.getLastMessage(dto.getThreadId(), 0);
        if (lastUserMessage == null) {
            throw new BizException("未找到任何用户消息");
        }

        // 使用找到的最后一条用户消息作为重新生成的起点
        BatchChatCompleteDto batchSendDto = new BatchChatCompleteDto();
        batchSendDto.setThreadId(dto.getThreadId());
        batchSendDto.setModel(dto.getModel());
        batchSendDto.setMessage(css.decryptForCurUser(lastUserMessage.getContent()));
        batchSendDto.setQueryKind(3); // 重新生成AI最后一条回复
        batchSendDto.setRegenerateRootHistoryId(lastUserMessage.getId());
        return chatCompleteSendBatch(batchSendDto);
    }

    /**
     * 恢复会话
     * @param dto 包含threadId参数
     * @return 返回会话信息和历史消息
     * @throws BizException 业务异常
     */
    public RecoverChatVo recoverChat(RecoverChatDto dto) throws BizException {

        var query = new ChatThreadPo();
        query.setId(dto.getThreadId());
        query.setUser(Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class));
        query.setPlayer(Any.of().val("id",AuthService.getCurrentPlayerId()).as(PlayerPo.class));

        ChatThreadPo thread = threadRepository.findOne(Example.of(query))
                .orElseThrow(() -> new BizException("会话不存在或无权限访问"));

        playerConfigService.put(UserConfigEnum.MODEL_CHAT_CURRENT_THREAD.key(), thread.getId());

        // 获取会话历史记录
        List<ChatMessagePo> messages = thread.getMessages();
        
        // 创建响应VO
        RecoverChatVo vo = new RecoverChatVo();
        vo.setThreadId(thread.getId());
        vo.setModelCode(thread.getModelCode());
        
        // 转换历史记录
        List<RecoverChatHistoryVo> messageVos = new ArrayList<>();
        if (messages != null && !messages.isEmpty()) {
            for (ChatMessagePo history : messages) {
                RecoverChatHistoryVo messageVo = new RecoverChatHistoryVo();
                
                // 手动映射特定字段
                messageVo.setId(history.getId());
                messageVo.setContent(css.decryptForCurUser(history.getContent()));
                messageVo.setCreateTime(history.getCreateTime());
                
                //发送人角色 0:Player 1:Model
                messageVo.setRole(history.getSenderRole());
                
                // 根据角色设置名称和头像
                if (history.getSenderRole() == 0) {
                    // 用户消息
                    messageVo.setName("User");
                    messageVo.setAvatarPath("");
                }
                
                if (history.getSenderRole() == 1) {
                    // AI消息 - 设置为模型系列名称
                    AIModelEnum modelEnum = AIModelEnum.getByCode(thread.getModelCode());
                    String aiName = modelEnum != null ? modelEnum.getSeries() : "AI助手";
                    messageVo.setName(aiName);
                    messageVo.setAvatarPath("");
                }
                
                messageVos.add(messageVo);
            }
        }
        
        vo.setMessages(messageVos);
        return vo;
    }

    /**
     * 获取用户空间或全局的代理配置
     * @param playerId 玩家ID
     * @return 代理url
     */
    public String getProxyConfig(Long playerId){
        // 获取代理配置 - 首先检查用户级别的代理配置
        String proxyConfig = playerConfigService.getString("model.proxy.config", null,playerId);

        // 如果用户未配置代理，则使用全局代理配置
        if (StringUtils.isBlank(proxyConfig)) {
            proxyConfig = globalConfigService.get("model.proxy.config");
        }
        return proxyConfig;
    }

    /**
     * 创建处理模型消息回调的Consumer
     *
     * @param thread 聊天线程
     * @param userId 用户ID
     * @return 处理模型消息的Consumer
     */
    private Consumer<ModelChatContext> onModelMessageRcv(ModelChatThreadPo thread, Long userId,Long playerId) {
        return context -> {
            try {
                Long threadId = thread.getId();
                String modelCode = context.getModelCode();
                AIModelEnum modelEnum = AIModelEnum.getByCode(modelCode);

                PlayerPo currentPlayer = Any.of().val("id", playerId).as(PlayerPo.class);

                //检查该线程是否被终止 0:正在等待响应(Pending) 1:正在回复(Receive) 2:已结束(Finished) 10:已失败(Failed) 11:已终止(Terminated)
                if(track.getStatus(threadId) == 11){
                    // 如果是完成或错误回调，需要关闭Session
                    if (context.getType() == 1 || context.getType() == 2) {
                        track.closeSession(threadId);
                    }
                    return;
                }

                //当为正在等待响应状态 首次收到消息时需要转换状态
                if(track.getStatus(threadId) == 0){
                    track.notifyReceive(threadId);
                }

                // 获取当前最大序号
                int nextSequence = modelSegmentRepository.findMaxSequenceByThreadId(threadId) + 1;

                // 根据context.type处理不同类型的消息
                if (context.getType() == 0) {
                    // 数据类型 - 创建数据片段
                    ModelChatSegmentPo dataSegment = new ModelChatSegmentPo();
                    dataSegment.setPlayer(currentPlayer);
                    dataSegment.setThread(thread);
                    dataSegment.setSequence(nextSequence);
                    dataSegment.setContent(context.getContent());
                    dataSegment.setStatus(0); // 未读状态
                    dataSegment.setType(1); // 数据类型
                    css.encryptEntity(dataSegment,userId);
                    modelSegmentRepository.save(dataSegment);
                    return;
                }

                if (context.getType() == 1) {

                    // 保存AI响应到历史记录
                    ModelChatHistoryPo history = new ModelChatHistoryPo();
                    history.setThread(thread);
                    history.setPlayer(thread.getPlayer());
                    history.setContent(context.getContent());
                    history.setRole(1);
                    // 获取当前最大序号并加1
                    history.setSequence(modelHistoryRepository.findMaxSequenceByThreadId(thread.getId()) + 1);
                    css.encryptEntity(history,userId);
                    ModelChatHistoryPo aiHistory = modelHistoryRepository.save(history);

                    // 完成类型 - 创建结束片段
                    ModelChatSegmentPo endSegment = new ModelChatSegmentPo();
                    endSegment.setPlayer(currentPlayer);
                    endSegment.setThread(thread);
                    endSegment.setSequence(nextSequence);
                    endSegment.setContent(null);
                    endSegment.setStatus(0); // 未读状态
                    endSegment.setType(2); // 结束类型
                    endSegment.setHistoryId(aiHistory.getId()); // 设置关联的历史记录ID
                    css.encryptEntity(endSegment,userId);
                    modelSegmentRepository.save(endSegment);

                    // 通知会话已结束
                    track.notifyFinished(threadId);
                    track.closeSession(threadId);

                    if (modelEnum != null) {
                        //异步生成会话标题
                        Thread.ofVirtual().start(()->{
                            try {
                                generateThreadTitle(thread.getId(), modelEnum.getCode(),userId,playerId);
                            } catch (Exception e) {
                                log.error("生成会话标题失败", e);
                                // 生成标题失败不影响主流程
                            }
                        });
                    }
                    return;
                }

                if (context.getType() == 2) {
                    // 错误类型 - 创建错误片段
                    ModelChatSegmentPo errorSegment = new ModelChatSegmentPo();
                    errorSegment.setPlayer(currentPlayer);
                    errorSegment.setThread(thread);
                    errorSegment.setSequence(nextSequence);
                    errorSegment.setContent(context.getException() != null ? "AI响应错误: " + context.getException().getMessage() : "AI响应错误");
                    errorSegment.setStatus(0); // 未读状态
                    errorSegment.setType(10); // 错误类型
                    modelSegmentRepository.save(errorSegment);

                    // 通知会话已失败
                    track.notifyFailed(threadId);
                    track.closeSession(threadId);
                }
            } catch (Exception e) {
                log.error("处理聊天片段失败", e);
            }
        };
    }

    /**
     * 创建空会话
     * @param dto 创建空会话请求参数
     * @return 新创建的会话ID
     * @throws BizException 业务异常
     */
    public CreateEmptyThreadVo createEmptyThread(CreateEmptyThreadDto dto) throws BizException {

        //获取当前玩家ID
        Long playerId = AuthService.getCurrentPlayerId();
        
        //获取当前玩家已有的会话数量
        long threadCount = threadRepository.getPlayerThreadCount(playerId);

        //生成新会话标题
        String title = String.format("新会话#%d", threadCount + 1);
        
        //创建新会话
        ChatThreadPo thread = new ChatThreadPo();
        thread.setType(0);//Thread类型 0:标准会话 1:RP会话 2:标准增强会话
        thread.setUser(Any.of().val("id",AuthService.getCurrentUserId()).as(UserPo.class));
        thread.setPlayer(Any.of().val("id",playerId).as(PlayerPo.class));
        thread.setNpc(null);
        thread.setTitle(title);
        thread.setPublicInfo(null);
        thread.setDescription(null);
        thread.setTitleGenerated(0);
        thread.setModelCode(dto.getModel());
        thread.setActive(-1);
        thread.setDescription(css.encryptForCurUser(thread.getDescription()));
        threadRepository.save(thread);
        // 返回新创建的会话ID
        return CreateEmptyThreadVo.of(thread.getId());
    }

}