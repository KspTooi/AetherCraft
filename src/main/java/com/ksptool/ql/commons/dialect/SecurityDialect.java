package com.ksptool.ql.commons.dialect;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import java.util.HashSet;
import java.util.Set;

public class SecurityDialect extends AbstractProcessorDialect {
    
    private static final String DIALECT_NAME = "Security Dialect";
    private static final String PREFIX = "sec";
    
    public SecurityDialect() {
        super(DIALECT_NAME, PREFIX, 1000);
    }
    
    @Override
    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<>();
        processors.add(new HasPermissionProcessor(dialectPrefix));
        return processors;
    }
} 