package com.ksptool.oxygen.test;

import com.ksptool.ql.AetherLauncher;

import com.ksptool.ql.biz.mapper.GroupRepository;
import com.ksptool.ql.biz.service.panel.PanelGroupService;
import com.ksptool.ql.commons.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AetherLauncher.class)
public class PanelGroupServiceTest {

    @Autowired
    private PanelGroupService service;

    @Autowired
    private GroupRepository gr;

    @Test
    public void testDetails() throws BizException {


    }
} 