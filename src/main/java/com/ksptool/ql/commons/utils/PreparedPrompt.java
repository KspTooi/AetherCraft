package com.ksptool.ql.commons.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PreparedPrompt - 用于替换Prompt中的#{XXX}格式的占位符
 * 类似于JDBC中的PreparedStatement，用于防止注入和提高可读性
 */
public class PreparedPrompt {
    
    // 原始prompt模板
    private final String template;
    
    // 参数映射
    private final Map<String, String> parameters = new HashMap<>();
    
    // 匹配#{xxx}格式的正则表达式
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("#\\{([^{}]+)\\}");
    
    // 是否启用XSS过滤
    private boolean enableXssFilter = true;
    
    /**
     * 构造函数
     * @param template 包含#{xxx}格式占位符的prompt模板
     */
    public PreparedPrompt(String template) {
        if (template == null) {
            throw new IllegalArgumentException("Prompt模板不能为null");
        }
        this.template = template;
    }

    /**
     * 合并另一个模板，使用指定的分隔符
     * @param separatorStr 分隔符
     * @param template 要合并的模板
     * @return 合并后的新PreparedPrompt实例
     */
    public PreparedPrompt union(String separatorStr, String template) {
        if (template == null) {
            throw new IllegalArgumentException("要合并的模板不能为null");
        }
        
        // 创建新的PreparedPrompt实例，传入合并后的模板
        PreparedPrompt newPrompt = new PreparedPrompt(this.template + separatorStr + template);
        
        // 复制原有参数
        newPrompt.parameters.putAll(this.parameters);
        newPrompt.enableXssFilter = this.enableXssFilter;
        
        return newPrompt;
    }

    /**
     * 合并另一个模板，无分隔符
     * @param template 要合并的模板
     * @return 合并后的新PreparedPrompt实例
     */
    public PreparedPrompt union(String template) {
        return union("", template);
    }
    
    /**
     * 设置参数值
     * @param name 参数名称
     * @param value 参数值
     * @return 当前PreparedPrompt实例，支持链式调用
     */
    public PreparedPrompt setParameter(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("参数名不能为null");
        }
        parameters.put(name, value);
        return this;
    }
    
    /**
     * 批量设置参数值
     * @param params 参数映射
     * @return 当前PreparedPrompt实例，支持链式调用
     */
    public PreparedPrompt setParameters(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("参数映射不能为null");
        }
        parameters.putAll(params);
        return this;
    }
    
    /**
     * 清除所有参数
     * @return 当前PreparedPrompt实例，支持链式调用
     */
    public PreparedPrompt clearParameters() {
        parameters.clear();
        return this;
    }
    
    /**
     * 设置是否启用XSS过滤
     * @param enable 是否启用
     * @return 当前PreparedPrompt实例，支持链式调用
     */
    public PreparedPrompt enableXssFilter(boolean enable) {
        this.enableXssFilter = enable;
        return this;
    }
    
    /**
     * 获取原始模板
     * @return 原始prompt模板
     */
    public String getTemplate() {
        return template;
    }
    
    /**
     * 获取已设置的参数
     * @return 参数映射的副本
     */
    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }
    
    /**
     * 检查模板中是否存在未设置的参数
     * @return 未设置的参数列表，如果所有参数都已设置则返回空数组
     */
    public String[] getUnsetParameters() {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        Map<String, Boolean> unsetParams = new HashMap<>();
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            if (!parameters.containsKey(paramName)) {
                unsetParams.put(paramName, true);
            }
        }
        
        return unsetParams.keySet().toArray(new String[0]);
    }
    
    /**
     * 执行替换并返回最终的prompt
     * @param strictMode 严格模式，如果为true则在有未设置的参数时抛出异常
     * @return 替换后的prompt
     * @throws IllegalStateException 如果strictMode为true且存在未设置的参数
     */
    public String execute(boolean strictMode) {
        String[] unsetParams = getUnsetParameters();
        if (strictMode && unsetParams.length > 0) {
            throw new IllegalStateException("存在未设置的参数: " + String.join(", ", unsetParams));
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            String replacement = parameters.getOrDefault(paramName, "#{" + paramName + "}");
            
            // 处理null值
            if (replacement == null) {
                replacement = "null";
            }
            
            // 应用XSS过滤
            if (enableXssFilter) {
                replacement = escapeXss(replacement);
            }
            
            // 需要处理replacement中的特殊字符，如$和\
            replacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 执行替换并返回最终的prompt（默认严格模式）
     * @return 替换后的prompt
     * @throws IllegalStateException 如果存在未设置的参数
     */
    public String execute() {
        return execute(true);
    }
    
    /**
     * 静态工厂方法，创建PreparedPrompt实例
     * @param template prompt模板
     * @return 新的PreparedPrompt实例
     */
    public static PreparedPrompt prepare(String template) {
        return new PreparedPrompt(template);
    }
    
    /**
     * 对字符串进行XSS过滤
     * @param input 输入字符串
     * @return 过滤后的字符串
     */
    private String escapeXss(String input) {
        if (input == null) {
            return "null";
        }
        
        // 替换HTML特殊字符
        String result = input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
            .replace("\\", "&#x5C;")
            .replace("(", "&#40;")
            .replace(")", "&#41;");
        
        // 过滤常见的XSS攻击向量
        result = result
            .replaceAll("(?i)javascript:", "")
            .replaceAll("(?i)data:", "")
            .replaceAll("(?i)vbscript:", "")
            .replaceAll("(?i)expression\\s*\\(", "")
            .replaceAll("(?i)eval\\s*\\(", "")
            .replaceAll("(?i)on\\w+\\s*=", "");
        
        return result;
    }
} 