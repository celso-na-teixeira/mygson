package com.demo.mygson.model;

public class JsonNull extends JsonElement{

  public static final JsonNull INSTANCE = new JsonNull();

  @Override
  public JsonElement deepCopy() {
    return INSTANCE;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof JsonNull;
  }
}
