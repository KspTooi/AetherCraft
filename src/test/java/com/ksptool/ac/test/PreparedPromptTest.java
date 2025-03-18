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
    @DisplayName("嵌套条件块")
    public void testExtremeConditions() {

        var t1 = "#{?start}故事开始了...#{?start}";  // 使用 #{?start} 关闭
        var t2 = "在一个#{place}里#{?nestedOuter}，住着#{?nestedInner}一个#{creature}，它的名字叫#{name}#{?nestedInner}#{?nestedOuter}。"; // 使用 #{?nestedInner} 和 #{?nestedOuter} 关闭
        var t3 = "#{?empty}#{?empty}"; // 空条件块，使用 #{?empty} 关闭
        var t4 = "它喜欢#{?food}吃#{food}#{?food}#{?treasure}和#{treasure}#{?treasure}。"; // 使用 #{?food} 和 #{?treasure} 关闭
        var t5 = "#{undefined}"; //undefined 仅在t5中被引用
        var t6 = "#{?sameName}sameName变量的值是：#{sameName}#{?sameName}";//条件变量和普通变量重名, 使用 #{?sameName} 关闭
        var t7 = "#{?end}故事结束了。#{?end}";// 使用 #{?end} 关闭
        var t8 = "";//没有内容的文本块
        PreparedPrompt prompt = PreparedPrompt.prepare(t1)
                .union(t2)
                .union(t3)
                .union(t4)
                .union(t5)
                .union(t6)
                .union(t7)
                .union(t8);

        // 设置部分参数，故意遗漏一些
        prompt.setParameter("place", "森林");
        prompt.setParameter("nestedOuter", "true"); // 激活外层条件
        // 不设置 nestedInner，内部的 creature 和 name 不会被渲染
        prompt.setParameter("food", "苹果");
        // 不设置 treasure
        prompt.setParameter("undefined","未定义变量");
        prompt.setParameter("sameName","相同的名字");
        prompt.setParameter("start","true");
        prompt.setParameter("end","true");

        //Assertions.assertThrows(IllegalStateException.class, prompt::execute);

        //如果设置了所有参数，则正常执行。
        prompt.setParameter("nestedInner", "true");
        prompt.setParameter("creature", "小精灵");
        prompt.setParameter("name", "闪闪");
        prompt.setParameter("treasure","宝石");
        var executed = prompt.execute().trim();
        System.out.println(executed);
        Assertions.assertEquals("故事开始了...在一个森林里，住着一个小精灵，它的名字叫闪闪。它喜欢吃苹果和宝石。未定义变量sameName变量的值是：相同的名字故事结束了。",executed);
    }

    @Test
    @DisplayName("测试非空表达式 - 组合多个模板")
    public void testNonBlankExpressMultiBlockAndMultiTemplate(){

        var t1 = "很久很久以前，在一个遥远的#{country}国度里，住着一位年轻的公主，名叫#{princessName}。";
        var t2 = "她拥有一只神奇的宠物#{?pet}，是一只会说话的#{pet}#{?pet}。";
        var t3 = "公主每天都会和她的宠物一起在#{place}玩耍，他们过着无忧无虑的生活。";
        var t4 = "直到有一天，一个邪恶的巫师出现，打破了这份平静，他想要夺走公主的#{treasure}。";

        PreparedPrompt prompt = PreparedPrompt.prepare(t1).union(t2).union(t3).union(t4);

        prompt.setParameter("country","天鹅");
        prompt.setParameter("princessName","爱丽丝");
        //prompt.setParameter("pet","猫咪");
        prompt.setParameter("place","后花园");
        prompt.setParameter("treasure","魔法项链");

        var executed = prompt.execute().trim();

        System.out.println(executed);
        Assertions.assertEquals("很久很久以前，在一个遥远的天鹅国度里，住着一位年轻的公主，名叫爱丽丝。她拥有一只神奇的宠物。公主每天都会和她的宠物一起在后花园玩耍，他们过着无忧无虑的生活。直到有一天，一个邪恶的巫师出现，打破了这份平静，他想要夺走公主的魔法项链。", executed);
    }


    @Test
    @DisplayName("测试非空表达式 - 多个表达式块")
    public void testNonBlankExpressMultiBlock(){

        var template = """ 
                你好#{user},今天是#{date}#{?game},你现在正在玩:#{game}。#{?game}#{?game}这是一款优秀的游戏,祝你玩得开心#{user}#{?game}
                """.trim();

        PreparedPrompt prompt = PreparedPrompt.prepare(template);

        prompt.setParameter("user","张三");
        prompt.setParameter("date","3月9日");
        prompt.setParameter("game","GTA5");

        System.out.println(prompt.execute());
        Assertions.assertEquals("你好张三,今天是3月9日,你现在正在玩:GTA5。这是一款优秀的游戏,祝你玩得开心张三", prompt.execute());
    }


    @Test
    @DisplayName("测试非空表达式 - 非字符串")
    public void testNonBlankExpress(){

        PreparedPrompt prompt = PreparedPrompt.prepare("你好#{user},今天是#{date}#{?game},你现在正在玩:#{game}#{?game}");

        prompt.setParameter("user","张三");
        prompt.setParameter("date","3月9日");
        prompt.setParameter("game","GTA5");

        System.out.println(prompt.execute());
        Assertions.assertEquals("你好张三,今天是3月9日,你现在正在玩:GTA5", prompt.execute());
    }

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

    @Test
    @DisplayName("测试静态execute方法复用已有参数")
    public void testStaticExecuteWithExistingPrompt() {
        // 创建原始PreparedPrompt并设置参数
        PreparedPrompt originalPrompt = PreparedPrompt.prepare("这是原始模板#{param1}");
        originalPrompt.setParameter("param1", "参数1");
        originalPrompt.setParameter("param2", "参数2");
        originalPrompt.setParameter("param3", "参数3");
        
        // 使用静态execute方法和新模板
        String newTemplate = "新模板使用已有参数：#{param1}, #{param2}, #{param3}";
        String result = PreparedPrompt.execute(newTemplate, originalPrompt);
        
        System.out.println(result);
        Assertions.assertEquals("新模板使用已有参数：参数1, 参数2, 参数3", result);
        
        // 测试原始PreparedPrompt不受影响
        Assertions.assertEquals("这是原始模板参数1", originalPrompt.execute());
    }

    @Test
    @DisplayName("测试静态execute方法支持条件表达式")
    public void testStaticExecuteWithConditions() {
        // 创建原始PreparedPrompt并设置参数
        PreparedPrompt originalPrompt = PreparedPrompt.prepare("原始模板");
        originalPrompt.setParameter("name", "小明");
        originalPrompt.setParameter("age", "18");
        // 故意不设置hobby参数
        
        // 使用静态execute方法和带条件表达式的新模板
        String newTemplate = "姓名: #{name}, 年龄: #{age} #{?hobby}爱好: #{hobby}#{?hobby}";
        String result = PreparedPrompt.execute(newTemplate, originalPrompt);
        
        System.out.println(result);
        // 由于hobby参数未设置，条件块不应该渲染
        Assertions.assertEquals("姓名: 小明, 年龄: 18 ", result);
        
        // 设置hobby参数后再次测试
        originalPrompt.setParameter("hobby", "编程");
        result = PreparedPrompt.execute(newTemplate, originalPrompt);
        
        System.out.println(result);
        // 此时hobby参数已设置，条件块应该渲染
        Assertions.assertEquals("姓名: 小明, 年龄: 18 爱好: 编程", result);
    }

    @Test
    @DisplayName("测试嵌套参数解析")
    public void testExecuteNested() {
        // 创建PreparedPrompt并设置嵌套参数
        PreparedPrompt prompt = PreparedPrompt.prepare("用户信息：#{userInfo}");
        
        // 参数值中包含模板表达式
        prompt.setParameter("username", "张三");
        prompt.setParameter("age", "25");
        prompt.setParameter("userInfo", "姓名：#{username}，年龄：#{age}岁");
        
        // 使用普通execute方法
        String normalResult = prompt.execute();
        System.out.println("普通执行结果: " + normalResult);
        // 普通执行只会替换userInfo，不会解析userInfo中的模板
        Assertions.assertEquals("用户信息：姓名：#{username}，年龄：#{age}岁", normalResult);
        
        // 使用嵌套参数解析
        String nestedResult = prompt.executeNested();
        System.out.println("嵌套执行结果: " + nestedResult);
        // 嵌套执行会先解析参数值中的模板，然后再解析主模板
        Assertions.assertEquals("用户信息：姓名：张三，年龄：25岁", nestedResult);
    }
    
    @Test
    @DisplayName("测试多层嵌套参数解析")
    public void testMultiLevelNestedParameters() {
        // 创建一个包含多层嵌套的PreparedPrompt
        PreparedPrompt prompt = PreparedPrompt.prepare("最终结果：#{level1}");
        
        prompt.setParameter("name", "王五");
        prompt.setParameter("level3", "值：#{name}");
        prompt.setParameter("level2", "内层：#{level3}");
        prompt.setParameter("level1", "外层：#{level2}");
        
        // 使用嵌套参数解析
        String result = prompt.executeNested();
        System.out.println("多层嵌套结果: " + result);
        // 解析应该从内到外，逐层处理
        Assertions.assertEquals("最终结果：外层：内层：值：王五", result);
    }

    @Test
    @DisplayName("测试嵌套参数解析与条件表达式结合")
    public void testNestedParametersWithConditions() {
        // 创建PreparedPrompt并设置嵌套参数和条件表达式
        PreparedPrompt prompt = PreparedPrompt.prepare("用户档案：#{profile}");
        
        // 设置基本信息
        prompt.setParameter("name", "李四");
        prompt.setParameter("age", "30");
        
        // 设置有条件表达式的嵌套参数
        prompt.setParameter("profile", 
            "姓名：#{name}，年龄：#{age}岁" +
            "#{?hobby}，爱好：#{hobby}#{?hobby}" +
            "#{?job}，职业：#{job}#{?job}"
        );
        
        // 不设置hobby和job参数，测试条件表达式
        String result1 = prompt.executeNested();
        System.out.println("无条件参数结果: " + result1);
        Assertions.assertEquals("用户档案：姓名：李四，年龄：30岁", result1);
        
        // 设置hobby参数，测试条件表达式
        prompt.setParameter("hobby", "阅读");
        String result2 = prompt.executeNested();
        System.out.println("设置hobby结果: " + result2);
        Assertions.assertEquals("用户档案：姓名：李四，年龄：30岁，爱好：阅读", result2);
        
        // 设置job参数，测试条件表达式
        prompt.setParameter("job", "工程师");
        String result3 = prompt.executeNested();
        System.out.println("设置hobby和job结果: " + result3);
        Assertions.assertEquals("用户档案：姓名：李四，年龄：30岁，爱好：阅读，职业：工程师", result3);
    }
} 