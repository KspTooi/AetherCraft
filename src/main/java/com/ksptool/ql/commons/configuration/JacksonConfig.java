package com.ksptool.ql.commons.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Jackson全局配置
 * 主要用于处理Date类型的格式化
 * 以及Long类型转String
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Long类型序列化为字符串
     */
    public static class LongToStringSerializer extends JsonSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                gen.writeString(value.toString());
            }
        }
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // 设置日期格式
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
        
        // 处理Java 8日期时间类型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期转换为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 注册Long类型序列化器，将Long转为String
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, new LongToStringSerializer());
        simpleModule.addSerializer(Long.TYPE, new LongToStringSerializer());
        objectMapper.registerModule(simpleModule);
        
        return objectMapper;
    }
} 