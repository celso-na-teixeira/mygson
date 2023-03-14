package com.demo.mygson;

import com.demo.mygson.model.JsonElement;
import java.lang.reflect.Type;

public interface JsonSerializationContext {

  public JsonElement serialize(Object src);
  public JsonElement serialize(Object src, Type typeOfSrc);

}
