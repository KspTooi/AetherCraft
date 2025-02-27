package com.ksptool.ql.commons.dialect;

import com.ksptool.ql.biz.service.AuthService;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

public class HasPermissionProcessor extends AbstractAttributeTagProcessor {
    
    private static final String ATTR_NAME = "authorize";
    private static final int PRECEDENCE = 300;

    public HasPermissionProcessor(final String dialectPrefix) {
        super(
            TemplateMode.HTML,
            dialectPrefix,
            null,
            false,
            ATTR_NAME,
            true,
            PRECEDENCE,
            true
        );
    }

    @Override
    protected void doProcess(
            ITemplateContext context,
            IProcessableElementTag tag,
            AttributeName attributeName,
            String attributeValue,
            IElementTagStructureHandler structureHandler) {
        
        boolean hasPermission = AuthService.hasPermission(attributeValue);
        
        if (!hasPermission) {
            structureHandler.removeElement();
        }
    }
} 