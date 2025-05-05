package com.ksptool.ac.test;

import com.ksptool.ql.AetherLauncher;
import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.mapper.UserRepository;
import com.ksptool.ql.biz.mapper.UserSessionRepository;
import com.ksptool.ql.biz.model.po.GroupPo;
import com.ksptool.ql.biz.model.po.UserPo;
import com.ksptool.ql.biz.model.po.UserSessionPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AetherLauncher.class)
//@Transactional
public class UserSessionRepositoryTest {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private GroupPo testGroup;
    private UserPo testUser1;
    private UserPo testUser2;
    private UserPo testUser3;
    private UserSessionPo activeSession1;
    private UserSessionPo activeSession2;
    private UserSessionPo expiredSession;

    @BeforeEach
    public void setup() {
        // 创建测试用户组
        testGroup = new GroupPo();
        testGroup.setCode("test-group");
        testGroup.setName("测试用户组");
        testGroup.setDescription("用于测试的用户组");
        testGroup.setIsSystem(false);
        testGroup.setStatus(1);
        testGroup.setSortOrder(999);
        testGroup = groupRepository.save(testGroup);

        // 创建测试用户1（在组内，有活跃会话）
        testUser1 = new UserPo();
        testUser1.setUsername("test-user1");
        testUser1.setPassword("password");
        testUser1.setNickname("测试用户1");
        Set<GroupPo> groups1 = new HashSet<>();
        groups1.add(testGroup);
        testUser1.setGroups(groups1);
        testUser1 = userRepository.save(testUser1);

        // 创建测试用户2（在组内，有活跃会话）
        testUser2 = new UserPo();
        testUser2.setUsername("test-user2");
        testUser2.setPassword("password");
        testUser2.setNickname("测试用户2");
        Set<GroupPo> groups2 = new HashSet<>();
        groups2.add(testGroup);
        testUser2.setGroups(groups2);
        testUser2 = userRepository.save(testUser2);

        // 创建测试用户3（在组内，会话已过期）
        testUser3 = new UserPo();
        testUser3.setUsername("test-user3");
        testUser3.setPassword("password");
        testUser3.setNickname("测试用户3");
        Set<GroupPo> groups3 = new HashSet<>();
        groups3.add(testGroup);
        testUser3.setGroups(groups3);
        testUser3 = userRepository.save(testUser3);

        // 创建活跃会话1
        activeSession1 = new UserSessionPo();
        activeSession1.setUserId(testUser1.getId());
        activeSession1.setToken("token-user1");
        activeSession1.setPermissions("{}");
        // 设置过期时间为当前时间后1小时
        activeSession1.setExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000));
        userSessionRepository.save(activeSession1);

        // 创建活跃会话2
        activeSession2 = new UserSessionPo();
        activeSession2.setUserId(testUser2.getId());
        activeSession2.setToken("token-user2");
        activeSession2.setPermissions("{}");
        // 设置过期时间为当前时间后1小时
        activeSession2.setExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000));
        userSessionRepository.save(activeSession2);

        // 创建过期会话
        expiredSession = new UserSessionPo();
        expiredSession.setUserId(testUser3.getId());
        expiredSession.setToken("token-user3");
        expiredSession.setPermissions("{}");
        // 设置过期时间为当前时间前1小时
        expiredSession.setExpiresAt(new Date(System.currentTimeMillis() - 3600 * 1000));
        userSessionRepository.save(expiredSession);
    }

    @Test
    public void testGetUserSessionByGroupId() {
        // 测试查询指定用户组中的在线用户
        List<UserSessionPo> activeSessions = userSessionRepository.getUserSessionByGroupId(testGroup.getId());
        
        // 验证结果
        assertNotNull(activeSessions);
        assertEquals(2, activeSessions.size());
        
        // 验证返回的是活跃会话
        boolean containsUser1 = false;
        boolean containsUser2 = false;
        
        for (UserSessionPo session : activeSessions) {
            if (session.getUserId().equals(testUser1.getId())) {
                containsUser1 = true;
            } else if (session.getUserId().equals(testUser2.getId())) {
                containsUser2 = true;
            }
        }
        
        assertTrue(containsUser1, "结果应包含用户1的活跃会话");
        assertTrue(containsUser2, "结果应包含用户2的活跃会话");
        
        // 验证不包含过期会话
        boolean containsUser3 = false;
        for (UserSessionPo session : activeSessions) {
            if (session.getUserId().equals(testUser3.getId())) {
                containsUser3 = true;
                break;
            }
        }
        
        assertFalse(containsUser3, "结果不应包含用户3的过期会话");
    }
    
    @Test
    public void testGetUserSessionByNonExistentGroupId() {
        // 测试查询不存在的用户组
        List<UserSessionPo> sessions = userSessionRepository.getUserSessionByGroupId(999999L);
        
        // 验证结果为空列表
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }

    @Test
    public void getPlayer(){


        List<UserSessionPo> userSessionByGroupId = userSessionRepository.getUserSessionByGroupId(132L);

    }
} 