package com.ksptool.oxygen.test;

import com.ksptool.ql.QuickLauncher;
import com.ksptool.ql.biz.model.dto.ServiceQueryDto;
import com.ksptool.ql.biz.model.vo.EditPanelGroupVo;
import com.ksptool.ql.biz.model.vo.WindowsServiceVo;
import com.ksptool.ql.biz.service.WindowsNativeService;
import com.ksptool.ql.biz.service.panel.PanelGroupService;
import com.ksptool.ql.commons.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = QuickLauncher.class)
public class PanelGroupServiceTest {

    @Autowired
    private PanelGroupService service;

    @Test
    public void testDetails() throws BizException {


        var groupDetails = service.getGroupDetails(33L);

        System.out.println(groupDetails);

    }
} 