package com.demo.mygson.model;

import com.demo.mygson.internal.LinkedTreeMap;
import java.util.Map;

public class JsonObject extends JsonElement {

  private final LinkedTreeMap<String, JsonElement> members = new LinkedTreeMap<>(false);

  public JsonObject() {
  }

  @Override
  public JsonElement deepCopy() {
    JsonObject result = new JsonObject();
    for (Map.Entry<String, JsonElement> entry : members.entrySet()){
      result.add(entry.getKey(), entry.getValue().deepCopy());
    }
    return result;
  }

  public void add(String property, JsonElement value){
    members.put(property, value == null ? JsonNull.INSTANCE : value);
  }

  public JsonElement remove(String property){
    return members.remove(property);
  }

  public void addProperty(String property, String value){
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  public void addProperty(String property, Number value){
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  public void addProperty(String property, Boolean value){
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

  public void addProperty(String property, Character value){
    add(property, value == null ? JsonNull.INSTANCE : new JsonPrimitive(value));
  }

}
