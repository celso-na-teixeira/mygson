package com.demo.mygson;

import com.demo.mygson.model.JsonElement;
import java.lang.reflect.Type;

public interface JsonSerializer<T> {

  public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context);

}
