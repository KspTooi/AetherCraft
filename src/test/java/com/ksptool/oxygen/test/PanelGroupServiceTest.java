package com.ksptool.oxygen.test;

import com.ksptool.ql.QuickLauncher;

import com.ksptool.ql.biz.service.panel.PanelGroupService;
import com.ksptool.ql.commons.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = QuickLauncher.class)
public class PanelGroupServiceTest {

    @Autowired
    private PanelGroupService service;

    @Test
    public void testDetails() throws BizException {


        var groupDetails = service.getEditView(33L);

        System.out.println(groupDetails);

    }
} 