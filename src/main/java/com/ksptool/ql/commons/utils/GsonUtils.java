package com.ksptool.ql.commons.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Map;

/**
 * JSON工具类
 */
public class GsonUtils {

    public static JsonElement replaceContent(JsonElement obj, String jsonPath, String content){
        if (obj == null || jsonPath == null || jsonPath.isEmpty()) {
            return obj;
        }

        if (!(obj instanceof JsonObject jsonObject)) {
            return obj;
        }

        String[] parts = jsonPath.split("\\.");
        JsonObject currentObj = jsonObject;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!currentObj.has(part)) {
                return obj;
            }
            JsonElement element = currentObj.get(part);
            if (!(element instanceof JsonObject)) {
                return obj;
            }
            currentObj = (JsonObject) element;
        }

        String lastPart = parts[parts.length - 1];
        if (currentObj.has(lastPart)) {
            currentObj.add(lastPart, new JsonPrimitive(content));
        }

        return obj;
    }

    public static JsonElement removeContent(JsonElement json, String jsonPath) {
        if (json == null || jsonPath == null || jsonPath.isEmpty()) {
            return json;
        }

        if (!(json instanceof JsonObject jsonObject)) {
            return json;
        }

        String[] parts = jsonPath.split("\\.");
        JsonObject currentObj = jsonObject;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!currentObj.has(part)) {
                return json;
            }
            JsonElement element = currentObj.get(part);
            if (!(element instanceof JsonObject)) {
                return json;
            }
            currentObj = (JsonObject) element;
        }

        String lastPart = parts[parts.length - 1];
        if (currentObj.has(lastPart)) {
            currentObj.remove(lastPart);
        }

        return json;
    }



    public static JsonElement injectContent(JsonElement json, Map<String, String> map) {

        if (json == null) {
            json = new JsonObject();
        }

        if(map == null){
            return json;
        }

        if (!(json instanceof JsonObject jsonObject)) {
            return json;
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.contains(".")) {
                String[] parts = key.split("\\.");
                JsonObject currentObj = jsonObject;

                for (int i = 0; i < parts.length - 1; i++) {
                    String part = parts[i];
                    
                    if (!currentObj.has(part)) {
                        currentObj.add(part, new JsonObject());
                    }
                    
                    JsonElement element = currentObj.get(part);
                    if (!(element instanceof JsonObject)) {
                        continue;
                    }
                    
                    currentObj = (JsonObject) element;
                }
                
                String lastPart = parts[parts.length - 1];
                if (!currentObj.has(lastPart)) {
                    currentObj.add(lastPart, new JsonPrimitive(value));
                }
            } else {
                if (!jsonObject.has(key)) {
                    jsonObject.add(key, new JsonPrimitive(value));
                }
            }
        }

        return jsonObject;
    }


}
