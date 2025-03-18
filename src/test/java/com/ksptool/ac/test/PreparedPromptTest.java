package com.ksptool.ac.test;

import com.ksptool.ql.commons.utils.PreparedPrompt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

/**
 * PreparedPrompt单元测试
 */
public class PreparedPromptTest {

    @Test
    @DisplayName("测试非空表达式 - 空字符串")
    public void testBlankExpress(){

        PreparedPrompt prompt = PreparedPrompt.prepare("你好#{user},今天是#{date}#{?game}你现在正在玩:#{game}#{?game}");

        prompt.setParameter("user","张三");
        prompt.setParameter("date","3月9日");

        System.out.println(prompt.execute());
        Assertions.assertEquals("你好张三,今天是3月9日", prompt.execute());
    }


    @Test
    @DisplayName("测试多个模板组合")
    public void testUnionTemplate(){

        PreparedPrompt prompt = PreparedPrompt.prepare("你好#{user}")
                .union(",","今天是#{date}");

        prompt.setParameter("user","张三");
        prompt.setParameter("date","3月9日");

        System.out.println(prompt.execute());
        Assertions.assertEquals("你好张三,今天是3月9日", prompt.execute());
    }


    @Test
    @DisplayName("测试基本替换功能")
    public void testBasicReplacement() {
        String template = "你好，#{name}！今天是#{date}。";
        PreparedPrompt prompt = new PreparedPrompt(template);
        
        prompt.setParameter("name", "张三");
        prompt.setParameter("date", "2023年5月1日");
        
        String result = prompt.execute();
        System.out.println(result);
        Assertions.assertEquals("你好，张三！今天是2023年5月1日。", result);
    }
    
    @Test
    @DisplayName("测试链式调用")
    public void testChainedCalls() {
        String template = "#{greeting}，#{name}！";
        String result = PreparedPrompt.prepare(template)
                .setParameter("greeting", "你好")
                .setParameter("name", "李四")
                .execute();
        
        System.out.println(result);        
        Assertions.assertEquals("你好，李四！", result);
    }
    
    @Test
    @DisplayName("测试批量设置参数")
    public void testBatchParameters() {
        String template = "#{company}的#{product}非常好用。";
        
        Map<String, String> params = new HashMap<>();
        params.put("company", "科技公司");
        params.put("product", "AI助手");
        
        PreparedPrompt prompt = new PreparedPrompt(template);
        prompt.setParameters(params);
        
        String result = prompt.execute();
        System.out.println(result);
        Assertions.assertEquals("科技公司的AI助手非常好用。", result);
    }
    
    @Test
    @DisplayName("测试清除参数")
    public void testClearParameters() {
        String template = "#{greeting}，#{name}！";
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("greeting", "你好")
                .setParameter("name", "王五");
                
        // 清除参数前
        String[] unsetParamsBefore = prompt.getUnsetParameters();
        System.out.println("未设置参数数量(清除前): " + unsetParamsBefore.length);
        Assertions.assertEquals(0, unsetParamsBefore.length);
        
        // 清除参数后
        prompt.clearParameters();
        String[] unsetParamsAfter = prompt.getUnsetParameters();
        System.out.println("未设置参数数量(清除后): " + unsetParamsAfter.length);
        Assertions.assertEquals(2, unsetParamsAfter.length);
    }
    
    @Test
    @DisplayName("测试严格模式下未设置参数抛出异常")
    public void testStrictModeException() {
        String template = "#{greeting}，#{name}！";
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("greeting", "你好");
        
        System.out.println("未设置参数: " + String.join(", ", prompt.getUnsetParameters()));
                
        // 严格模式下，未设置name参数应该抛出异常
        Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            prompt.execute(true);
        });
        
        System.out.println(exception.getMessage());
        Assertions.assertTrue(exception.getMessage().contains("name"));
    }
    
    @Test
    @DisplayName("测试非严格模式下未设置参数保留原样")
    public void testNonStrictMode() {
        String template = "#{greeting}，#{name}！";
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("greeting", "你好");
                
        // 非严格模式下，未设置的参数应保持原样
        String result = prompt.execute(false);
        System.out.println(result);
        Assertions.assertEquals("你好，#{name}！", result);
    }
    
    @Test
    @DisplayName("测试特殊字符处理")
    public void testSpecialCharacters() {
        String template = "#{message}";
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("message", "包含特殊字符: $, \\, &");
                
        String result = prompt.execute();
        System.out.println(result);
        Assertions.assertEquals("包含特殊字符: $, &#x5C;, &amp;", result);
    }
    
    @Test
    @DisplayName("测试多次出现的参数")
    public void testRepeatedParameters() {
        String template = "#{name}说：'我是#{name}'";
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("name", "赵六");
                
        String result = prompt.execute();
        System.out.println(result);
        Assertions.assertEquals("赵六说：'我是赵六'", result);
    }
    
    @Test
    @DisplayName("测试无占位符的模板")
    public void testNoPlaceholders() {
        String template = "这是一个没有占位符的模板。";
        PreparedPrompt prompt = PreparedPrompt.prepare(template);
        
        String result = prompt.execute();
        System.out.println(result);
        Assertions.assertEquals(template, result);
    }
    
    @Test
    @DisplayName("测试获取未设置的参数")
    public void testGetUnsetParameters() {
        String template = "#{a}, #{b}, #{c}, #{d}";
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("a", "A值")
                .setParameter("c", "C值");
                
        String[] unsetParams = prompt.getUnsetParameters();
        System.out.println("未设置参数: " + String.join(", ", unsetParams));
        Assertions.assertEquals(2, unsetParams.length);
        
        // 由于HashMap的无序性，我们不能确定数组中元素的顺序
        // 所以我们检查数组中是否包含这些元素
        boolean containsB = false;
        boolean containsD = false;
        
        for (String param : unsetParams) {
            if (param.equals("b")) containsB = true;
            if (param.equals("d")) containsD = true;
        }
        
        Assertions.assertTrue(containsB && containsD);
    }
    
    @Test
    @DisplayName("测试空模板")
    public void testEmptyTemplate() {
        String template = "";
        PreparedPrompt prompt = PreparedPrompt.prepare(template);
        
        String result = prompt.execute();
        System.out.println("[" + result + "]");
        Assertions.assertEquals("", result);
    }
    
    @Test
    @DisplayName("测试null参数值")
    public void testNullParameterValue() {
        String template = "#{param}是null值";
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("param", null);
                
        String result = prompt.execute();
        System.out.println(result);
        Assertions.assertEquals("null是null值", result);
    }
    
    @Test
    @DisplayName("测试XSS过滤 - HTML标签")
    public void testXssFilterHtmlTags() {
        String template = "用户输入：#{input}";
        String maliciousInput = "<script>alert('XSS攻击')</script>";
        
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("input", maliciousInput);
                
        String result = prompt.execute();
        System.out.println(result);
        Assertions.assertEquals("用户输入：&lt;script&gt;alert&#40;&#x27;XSS攻击&#x27;&#41;&lt;&#x2F;script&gt;", result);
    }
    
    @Test
    @DisplayName("测试XSS过滤 - JavaScript事件")
    public void testXssFilterJavaScriptEvents() {
        String template = "用户输入：#{input}";
        String maliciousInput = "<img src='x' onerror='alert(\"XSS\")' />";
        
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("input", maliciousInput);
                
        String result = prompt.execute();
        System.out.println(result);
        
        Assertions.assertTrue(
            result.contains("&lt;img src=&#x27;x&#x27;") && 
            !result.contains("onerror=&#x27;alert")
        );
    }
    
    @Test
    @DisplayName("测试XSS过滤 - JavaScript协议")
    public void testXssFilterJavaScriptProtocol() {
        String template = "用户输入：#{input}";
        String maliciousInput = "<a href='javascript:alert(\"XSS\")'>点击我</a>";
        
        PreparedPrompt prompt = PreparedPrompt.prepare(template)
                .setParameter("input", maliciousInput);
                
        String result = prompt.execute();
        System.out.println(result);
        
        Assertions.assertTrue(
            result.contains("&lt;a href=&#x27;") && 
            !result.contains("javascript:alert")
        );
    }
    
    @Test
    @DisplayName("测试禁用XSS过滤")
    public void testDisableXssFilter() {
        String template = "用户输入：#{input}";
        String htmlInput = "<b>粗体文本</b>";
        
        // 启用XSS过滤（默认）
        String filteredResult = PreparedPrompt.prepare(template)
                .setParameter("input", htmlInput)
                .execute();
        
        // 禁用XSS过滤
        String unfilteredResult = PreparedPrompt.prepare(template)
                .setParameter("input", htmlInput)
                .enableXssFilter(false)
                .execute();
        
        System.out.println("启用过滤: " + filteredResult);
        System.out.println("禁用过滤: " + unfilteredResult);
                
        Assertions.assertEquals("用户输入：&lt;b&gt;粗体文本&lt;&#x2F;b&gt;", filteredResult);
        Assertions.assertEquals("用户输入：<b>粗体文本</b>", unfilteredResult);
    }

    @Test
    @DisplayName("测试XSS注入")
    public void testXssFilter() {

        String name = PreparedPrompt.prepare("你好，#{name}")
                .setParameter("name", "<a>ABCD<a>")
                .execute();

        System.out.println(name);
    }
} 