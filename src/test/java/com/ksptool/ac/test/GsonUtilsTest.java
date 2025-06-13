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

    @Test
    public void testRemoveContentWithSimplePath() {
        String json = """
                {
                    "name": "张三",
                    "age": 25,
                    "city": "北京"
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("移除简单路径前:");
        System.out.println(gson.toJson(jsonElement));
        
        JsonElement result = GsonUtils.removeContent(jsonElement, "age");
        
        System.out.println("移除简单路径后:");
        System.out.println(gson.toJson(result));
        
        JsonObject resultObj = result.getAsJsonObject();
        Assertions.assertTrue(resultObj.has("name"));
        Assertions.assertTrue(resultObj.has("city"));
        Assertions.assertFalse(resultObj.has("age"));
    }

    @Test
    public void testRemoveContentWithNestedPath() {
        String json = """
                {
                    "user": {
                        "name": "李四",
                        "address": {
                            "city": "上海",
                            "street": "浦东新区"
                        }
                    }
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("移除嵌套路径前:");
        System.out.println(gson.toJson(jsonElement));
        
        JsonElement result = GsonUtils.removeContent(jsonElement, "user.address.city");
        
        System.out.println("移除嵌套路径后:");
        System.out.println(gson.toJson(result));
        
        JsonObject resultObj = result.getAsJsonObject();
        JsonObject user = resultObj.getAsJsonObject("user");
        JsonObject address = user.getAsJsonObject("address");
        Assertions.assertTrue(address.has("street"));
        Assertions.assertFalse(address.has("city"));
    }

    @Test
    public void testRemoveContentWithNonExistentPath() {
        String json = """
                {
                    "user": {
                        "name": "王五"
                    }
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("移除不存在路径前:");
        System.out.println(gson.toJson(jsonElement));
        
        JsonElement result = GsonUtils.removeContent(jsonElement, "user.address.city");
        
        System.out.println("移除不存在路径后:");
        System.out.println(gson.toJson(result));
        
        JsonObject resultObj = result.getAsJsonObject();
        JsonObject user = resultObj.getAsJsonObject("user");
        Assertions.assertTrue(user.has("name"));
        Assertions.assertEquals("王五", user.get("name").getAsString());
    }

    @Test
    public void testRemoveContentWithNullAndInvalidInputs() {
        String json = """
                {
                    "name": "赵六",
                    "age": 30
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        // 测试null json
        JsonElement result1 = GsonUtils.removeContent(null, "name");
        Assertions.assertNull(result1);
        
        // 测试null path
        JsonElement result2 = GsonUtils.removeContent(jsonElement, null);
        Assertions.assertEquals(jsonElement, result2);
        
        // 测试空path
        JsonElement result3 = GsonUtils.removeContent(jsonElement, "");
        Assertions.assertEquals(jsonElement, result3);
        
        // 测试非对象json
        JsonElement primitiveJson = JsonParser.parseString("123");
        JsonElement result4 = GsonUtils.removeContent(primitiveJson, "name");
        Assertions.assertEquals(primitiveJson, result4);
        
        System.out.println("无效输入测试通过");
    }

    @Test
    public void testRemoveContentWithTopLevelNestedStructure() {
        String json = """
                {
                    "user": {
                        "profile": {
                            "personal": {
                                "name": "钱七",
                                "age": 28
                            },
                            "contact": {
                                "phone": "13800138000",
                                "email": "qianqi@example.com"
                            }
                        },
                        "settings": {
                            "theme": "dark",
                            "language": "zh-CN"
                        }
                    },
                    "system": {
                        "version": "1.0.0"
                    }
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("移除最外层嵌套结构前:");
        System.out.println(gson.toJson(jsonElement));
        
        JsonElement result = GsonUtils.removeContent(jsonElement, "user");
        
        System.out.println("移除最外层嵌套结构后:");
        System.out.println(gson.toJson(result));
        
        JsonObject resultObj = result.getAsJsonObject();
        Assertions.assertFalse(resultObj.has("user"));
        Assertions.assertTrue(resultObj.has("system"));
        JsonObject system = resultObj.getAsJsonObject("system");
        Assertions.assertEquals("1.0.0", system.get("version").getAsString());
    }

    @Test
    public void testRemoveContentWithArray() {
        String json = """
                {
                    "users": [
                        {
                            "name": "张三",
                            "age": 25
                        },
                        {
                            "name": "李四",
                            "age": 30
                        }
                    ],
                    "config": {
                        "permissions": ["read", "write", "delete"],
                        "settings": {
                            "notifications": ["email", "sms"]
                        }
                    },
                    "version": "2.0.0"
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("移除数组前:");
        System.out.println(gson.toJson(jsonElement));
        
        // 移除顶级数组
        JsonElement result1 = GsonUtils.removeContent(jsonElement, "users");
        
        System.out.println("移除顶级数组后:");
        System.out.println(gson.toJson(result1));
        
        JsonObject resultObj1 = result1.getAsJsonObject();
        Assertions.assertFalse(resultObj1.has("users"));
        Assertions.assertTrue(resultObj1.has("config"));
        Assertions.assertTrue(resultObj1.has("version"));
        
        // 移除嵌套数组
        JsonElement result2 = GsonUtils.removeContent(jsonElement, "config.permissions");
        
        System.out.println("移除嵌套数组后:");
        System.out.println(gson.toJson(result2));
        
        JsonObject resultObj2 = result2.getAsJsonObject();
        JsonObject config = resultObj2.getAsJsonObject("config");
        Assertions.assertFalse(config.has("permissions"));
        Assertions.assertTrue(config.has("settings"));
        JsonObject settings = config.getAsJsonObject("settings");
        Assertions.assertTrue(settings.has("notifications"));
    }

    @Test
    public void testReplaceContentWithSimplePath() {
        String json = """
                {
                    "username": "张三",
                    "password": "123456",
                    "email": "zhangsan@example.com"
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("替换简单路径前:");
        System.out.println(gson.toJson(jsonElement));
        
        JsonElement result = GsonUtils.replaceContent(jsonElement, "password", "***");
        
        System.out.println("替换简单路径后:");
        System.out.println(gson.toJson(result));
        
        JsonObject resultObj = result.getAsJsonObject();
        Assertions.assertEquals("张三", resultObj.get("username").getAsString());
        Assertions.assertEquals("***", resultObj.get("password").getAsString());
        Assertions.assertEquals("zhangsan@example.com", resultObj.get("email").getAsString());
    }

    @Test
    public void testReplaceContentWithNestedPath() {
        String json = """
                {
                    "user": {
                        "profile": {
                            "name": "李四",
                            "phone": "13800138000",
                            "idCard": "110101199001011234"
                        }
                    }
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("替换嵌套路径前:");
        System.out.println(gson.toJson(jsonElement));
        
        JsonElement result = GsonUtils.replaceContent(jsonElement, "user.profile.phone", "***");
        
        System.out.println("替换嵌套路径后:");
        System.out.println(gson.toJson(result));
        
        JsonObject resultObj = result.getAsJsonObject();
        JsonObject user = resultObj.getAsJsonObject("user");
        JsonObject profile = user.getAsJsonObject("profile");
        Assertions.assertEquals("李四", profile.get("name").getAsString());
        Assertions.assertEquals("***", profile.get("phone").getAsString());
        Assertions.assertEquals("110101199001011234", profile.get("idCard").getAsString());
    }

    @Test
    public void testReplaceContentWithNonExistentPath() {
        String json = """
                {
                    "user": {
                        "name": "王五"
                    }
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("替换不存在路径前:");
        System.out.println(gson.toJson(jsonElement));
        
        JsonElement result = GsonUtils.replaceContent(jsonElement, "user.password", "***");
        
        System.out.println("替换不存在路径后:");
        System.out.println(gson.toJson(result));
        
        JsonObject resultObj = result.getAsJsonObject();
        JsonObject user = resultObj.getAsJsonObject("user");
        Assertions.assertEquals("王五", user.get("name").getAsString());
        Assertions.assertFalse(user.has("password"));
    }

    @Test
    public void testReplaceContentWithMultipleFields() {
        String json = """
                {
                    "account": {
                        "username": "admin",
                        "password": "secret123",
                        "email": "admin@example.com",
                        "profile": {
                            "realName": "管理员",
                            "phone": "13900139000",
                            "address": "北京市朝阳区"
                        }
                    }
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("替换多个字段前:");
        System.out.println(gson.toJson(jsonElement));
        
        JsonElement result1 = GsonUtils.replaceContent(jsonElement, "account.password", "***");
        JsonElement result2 = GsonUtils.replaceContent(result1, "account.profile.phone", "***");
        
        System.out.println("替换多个字段后:");
        System.out.println(gson.toJson(result2));
        
        JsonObject resultObj = result2.getAsJsonObject();
        JsonObject account = resultObj.getAsJsonObject("account");
        Assertions.assertEquals("admin", account.get("username").getAsString());
        Assertions.assertEquals("***", account.get("password").getAsString());
        
        JsonObject profile = account.getAsJsonObject("profile");
        Assertions.assertEquals("管理员", profile.get("realName").getAsString());
        Assertions.assertEquals("***", profile.get("phone").getAsString());
        Assertions.assertEquals("北京市朝阳区", profile.get("address").getAsString());
    }

    @Test
    public void testReplaceContentWithNullAndInvalidInputs() {
        String json = """
                {
                    "data": "test"
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        // 测试null json
        JsonElement result1 = GsonUtils.replaceContent(null, "data", "***");
        Assertions.assertNull(result1);
        
        // 测试null path
        JsonElement result2 = GsonUtils.replaceContent(jsonElement, null, "***");
        Assertions.assertEquals(jsonElement, result2);
        
        // 测试空path
        JsonElement result3 = GsonUtils.replaceContent(jsonElement, "", "***");
        Assertions.assertEquals(jsonElement, result3);
        
        // 测试非对象json
        JsonElement primitiveJson = JsonParser.parseString("\"simple string\"");
        JsonElement result4 = GsonUtils.replaceContent(primitiveJson, "field", "***");
        Assertions.assertEquals(primitiveJson, result4);
        
        System.out.println("无效输入测试通过");
    }

    @Test
    public void testReplaceContentWithDifferentDataTypes() {
        String json = """
                {
                    "stringField": "原始字符串",
                    "numberField": 12345,
                    "booleanField": true,
                    "arrayField": ["item1", "item2", "item3"],
                    "objectField": {
                        "nestedString": "嵌套值",
                        "nestedNumber": 999
                    }
                }
                """;
        JsonElement jsonElement = JsonParser.parseString(json);
        
        System.out.println("替换不同数据类型前:");
        System.out.println(gson.toJson(jsonElement));
        
        JsonElement result1 = GsonUtils.replaceContent(jsonElement, "stringField", "***");
        JsonElement result2 = GsonUtils.replaceContent(result1, "numberField", "***");
        JsonElement result3 = GsonUtils.replaceContent(result2, "booleanField", "***");
        JsonElement result4 = GsonUtils.replaceContent(result3, "arrayField", "***");
        JsonElement result5 = GsonUtils.replaceContent(result4, "objectField.nestedString", "***");
        
        System.out.println("替换不同数据类型后:");
        System.out.println(gson.toJson(result5));
        
        JsonObject resultObj = result5.getAsJsonObject();
        Assertions.assertEquals("***", resultObj.get("stringField").getAsString());
        Assertions.assertEquals("***", resultObj.get("numberField").getAsString());
        Assertions.assertEquals("***", resultObj.get("booleanField").getAsString());
        Assertions.assertEquals("***", resultObj.get("arrayField").getAsString());
        
        JsonObject objectField = resultObj.getAsJsonObject("objectField");
        Assertions.assertEquals("***", objectField.get("nestedString").getAsString());
        Assertions.assertEquals(999, objectField.get("nestedNumber").getAsInt());
    }
} 