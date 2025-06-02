package com.ksptool.ac.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ksptool.ql.commons.utils.GsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class GsonUtilsTest {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void testInjectContentWithFlatMap() {
        JsonObject jsonObject = new JsonObject();
        Map<String, String> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", "25");
        
        JsonElement result = GsonUtils.injectContent(jsonObject, map);
        
        System.out.println("简单键值对测试结果:");
        System.out.println(gson.toJson(result));
        
        Assertions.assertTrue(result.isJsonObject());
        JsonObject resultObj = result.getAsJsonObject();
        Assertions.assertEquals("张三", resultObj.get("name").getAsString());
        Assertions.assertEquals("25", resultObj.get("age").getAsString());
    }
    
    @Test
    public void testInjectContentWithNestedMap() {
        JsonObject jsonObject = new JsonObject();
        Map<String, String> map = new HashMap<>();
        map.put("user.name", "李四");
        map.put("user.address.city", "北京");
        map.put("user.address.street", "朝阳区");
        
        JsonElement result = GsonUtils.injectContent(jsonObject, map);
        
        System.out.println("嵌套对象测试结果:");
        System.out.println(gson.toJson(result));
        
        Assertions.assertTrue(result.isJsonObject());
        JsonObject resultObj = result.getAsJsonObject();
        JsonObject user = resultObj.getAsJsonObject("user");
        Assertions.assertEquals("李四", user.get("name").getAsString());
        
        JsonObject address = user.getAsJsonObject("address");
        Assertions.assertEquals("北京", address.get("city").getAsString());
        Assertions.assertEquals("朝阳区", address.get("street").getAsString());
    }
    
    @Test
    public void testInjectContentWithExistingValues() {
        String json = """
                {
                    "user": {
                        "name": "王五",
                        "address": {
                            "city": "上海"
                        }
                    }
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("原始JSON:");
        System.out.println(gson.toJson(jsonElement));
        
        Map<String, String> map = new HashMap<>();
        map.put("user.age", "30");
        map.put("user.address.city", "广州"); // 已存在，不应覆盖
        map.put("user.address.street", "天河区");
        
        JsonElement result = GsonUtils.injectContent(jsonElement, map);
        
        System.out.println("保留已存在值的测试结果:");
        System.out.println(gson.toJson(result));
        
        JsonObject resultObj = result.getAsJsonObject();
        JsonObject user = resultObj.getAsJsonObject("user");
        Assertions.assertEquals("王五", user.get("name").getAsString());
        Assertions.assertEquals("30", user.get("age").getAsString());
        
        JsonObject address = user.getAsJsonObject("address");
        Assertions.assertEquals("上海", address.get("city").getAsString()); // 应保持原值
        Assertions.assertEquals("天河区", address.get("street").getAsString());
    }
    
    @Test
    public void testInjectContentWithNullJson() {
        Map<String, String> map = new HashMap<>();
        map.put("country.state.city", "重庆");
        
        JsonElement result = GsonUtils.injectContent(null, map);
        
        System.out.println("Null JSON测试结果:");
        System.out.println(gson.toJson(result));
        
        JsonObject resultObj = result.getAsJsonObject();
        JsonObject country = resultObj.getAsJsonObject("country");
        JsonObject state = country.getAsJsonObject("state");
        Assertions.assertEquals("重庆", state.get("city").getAsString());
    }
    
    @Test
    public void testInjectContentWithNonObjectJson() {
        JsonElement jsonElement = JsonParser.parseString("123");
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        
        JsonElement result = GsonUtils.injectContent(jsonElement, map);
        
        System.out.println("非对象JSON测试结果:");
        System.out.println(gson.toJson(result));
        
        Assertions.assertTrue(result.isJsonPrimitive());
        Assertions.assertEquals(123, result.getAsInt());
    }
} 