package com.ksptool.ql.commons.utils;

import org.apache.commons.lang3.StringUtils;
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
    
    // 匹配条件表达式 #{?xxx} 的开始标记
    private static final Pattern CONDITION_START_PATTERN = Pattern.compile("#\\{\\?(\\w+)\\}");
    
    // 匹配条件表达式 #{?xxx} 的结束标记
    private static final Pattern CONDITION_END_PATTERN = Pattern.compile("#\\{\\?(\\w+)\\}");
    
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
     * 执行替换并返回最终的prompt
     * @param strictMode 严格模式，如果为true则在有未设置的参数时抛出异常
     * @return 替换后的prompt
     * @throws IllegalStateException 如果strictMode为true且存在未设置的参数
     */
    public String execute(boolean strictMode) {
        // 首先处理条件表达式
        String processedTemplate = processConditionalBlocks(this.template);
        
        // 然后处理普通占位符
        String[] unsetParams = getUnsetParameters(processedTemplate);
        if (strictMode && unsetParams.length > 0) {
            throw new IllegalStateException("存在未设置的参数: " + String.join(", ", unsetParams));
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(processedTemplate);
        
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
     * 检查模板中是否存在未设置的参数
     * @return 未设置的参数列表，如果所有参数都已设置则返回空数组
     */
    public String[] getUnsetParameters() {
        return getUnsetParameters(processConditionalBlocks(this.template));
    }
    
    /**
     * 检查处理后的模板中是否存在未设置的参数
     * @param processedTemplate 经过条件处理后的模板
     * @return 未设置的参数列表，如果所有参数都已设置则返回空数组
     */
    private String[] getUnsetParameters(String processedTemplate) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(processedTemplate);
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
     * 处理条件块，支持嵌套的条件表达式
     * @param input 输入的模板字符串
     * @return 处理后的模板字符串
     */
    private String processConditionalBlocks(String input) {
        StringBuilder result = new StringBuilder();
        int position = 0;
        
        while (position < input.length()) {
            // 查找条件开始标记
            int startTagPos = input.indexOf("#{?", position);
            
            if (startTagPos == -1) {
                // 没有找到条件开始标记，将剩余部分添加到结果中
                result.append(input.substring(position));
                break;
            }
            
            // 添加条件开始标记之前的内容
            result.append(input.substring(position, startTagPos));
            
            // 提取条件名称
            int nameStart = startTagPos + 3; // "#{?".length()
            int nameEnd = input.indexOf("}", nameStart);
            
            if (nameEnd == -1) {
                // 找不到结束括号，直接添加原始内容并继续
                result.append(input.substring(startTagPos));
                break;
            }
            
            String conditionName = input.substring(nameStart, nameEnd);
            String endTag = "#{?" + conditionName + "}";
            
            // 查找对应的结束标记，考虑到嵌套情况
            int endTagPos = findMatchingEndTag(input, nameEnd + 1, conditionName);
            
            if (endTagPos == -1) {
                // 找不到匹配的结束标记，直接添加原始内容并继续
                result.append(input.substring(startTagPos));
                break;
            }
            
            // 提取条件块内容（不包括开始和结束标记）
            String blockContent = input.substring(nameEnd + 1, endTagPos);
            
            // 检查条件是否满足
            String paramValue = parameters.get(conditionName);
            if (paramValue != null && !StringUtils.isBlank(paramValue)) {
                // 条件满足，递归处理条件块内容
                result.append(processConditionalBlocks(blockContent));
            }
            
            // 更新位置到结束标记之后
            position = endTagPos + endTag.length();
        }
        
        return result.toString();
    }
    
    /**
     * 查找匹配的结束标记，处理嵌套情况
     * @param input 输入字符串
     * @param startPos 开始查找的位置
     * @param conditionName 条件名称
     * @return 匹配的结束标记位置，如果未找到则返回-1
     */
    private int findMatchingEndTag(String input, int startPos, String conditionName) {
        int nestLevel = 1;
        int pos = startPos;
        String startTag = "#{?" + conditionName + "}";
        String endTag = "#{?" + conditionName + "}";
        
        while (pos < input.length()) {
            int nextStartTagPos = input.indexOf(startTag, pos);
            int nextEndTagPos = input.indexOf(endTag, pos);
            
            if (nextEndTagPos == -1) {
                // 找不到结束标记
                return -1;
            }
            
            if (nextStartTagPos != -1 && nextStartTagPos < nextEndTagPos) {
                // 找到嵌套的开始标记
                nestLevel++;
                pos = nextStartTagPos + startTag.length();
            } else {
                // 找到结束标记
                nestLevel--;
                if (nestLevel == 0) {
                    return nextEndTagPos;
                }
                pos = nextEndTagPos + endTag.length();
            }
        }
        
        return -1;
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
     * 先解析参数值中的模板占位符，然后再解析主模板
     * 这允许参数值本身包含模板表达式，实现嵌套解析
     * @param strictMode 严格模式，如果为true则在有未设置的参数时抛出异常
     * @return 替换后的prompt
     * @throws IllegalStateException 如果strictMode为true且存在未设置的参数
     */
    public String executeNested(boolean strictMode) {
        // 多次递归处理参数值，以支持多层嵌套
        Map<String, String> processedParams = new HashMap<>(this.parameters);
        boolean hasChanges;
        
        // 递归处理，直到所有嵌套参数都被解析完成
        do {
            hasChanges = false;
            
            // 创建一个临时的PreparedPrompt用于解析当前层级的参数
            PreparedPrompt paramProcessor = new PreparedPrompt("");
            paramProcessor.parameters.putAll(processedParams);
            paramProcessor.enableXssFilter = this.enableXssFilter;
            
            // 处理每个参数值，将其视为模板进行解析
            for (Map.Entry<String, String> entry : processedParams.entrySet()) {
                String paramName = entry.getKey();
                String paramValue = entry.getValue();
                
                if (paramValue != null && paramValue.contains("#{")) {
                    // 将参数值作为模板进行解析
                    String processedValue = PreparedPrompt.execute(paramValue, paramProcessor);
                    
                    // 如果解析后的值与原值不同，标记有变化
                    if (!processedValue.equals(paramValue)) {
                        processedParams.put(paramName, processedValue);
                        hasChanges = true;
                    }
                }
            }
        } while (hasChanges); // 如果有变化，继续处理
        
        // 创建一个新的PreparedPrompt实例，使用原模板和处理后的参数
        PreparedPrompt finalProcessor = new PreparedPrompt(this.template);
        finalProcessor.parameters.putAll(processedParams);
        finalProcessor.enableXssFilter = this.enableXssFilter;
        
        // 执行最终的模板替换
        return finalProcessor.execute(strictMode);
    }
    
    /**
     * 先解析参数值中的模板占位符，然后再解析主模板（默认严格模式）
     * @return 替换后的prompt
     * @throws IllegalStateException 如果存在未设置的参数
     */
    public String executeNested() {
        return executeNested(true);
    }

    /**
     * 使用新模板和已有的PreparedPrompt实例中的参数执行替换
     * @param template 新的模板
     * @param preparedPrompt 已有的PreparedPrompt实例，提供参数值
     * @return 替换后的结果
     */
    public static String execute(String template, PreparedPrompt preparedPrompt) {
        if (template == null) {
            throw new IllegalArgumentException("模板不能为null");
        }
        if (preparedPrompt == null) {
            throw new IllegalArgumentException("PreparedPrompt实例不能为null");
        }
        
        // 创建一个新的PreparedPrompt实例，使用提供的模板
        PreparedPrompt newPrompt = new PreparedPrompt(template);
        
        // 复制参数
        newPrompt.parameters.putAll(preparedPrompt.parameters);
        newPrompt.enableXssFilter = preparedPrompt.enableXssFilter;
        
        // 执行并返回结果
        return newPrompt.execute(false); // 使用非严格模式，因为新模板可能含有原PreparedPrompt中没有的参数
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