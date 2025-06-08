package com.ksptool.ql.commons.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;

public class GsonUtils {

    public static JsonElement injectContent(JsonElement json, Map<String, String> map) {

        if (json == null) {
            json = new JsonObject();
        }

        if(map == null){
            return json;
        }

        if (!(json instanceof JsonObject)) {
            return json;
        }

        JsonObject jsonObject = (JsonObject) json;

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
