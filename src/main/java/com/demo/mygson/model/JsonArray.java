package com.demo.mygson.model;

import java.util.ArrayList;
import java.util.Iterator;

public final class JsonArray extends JsonElement implements Iterable<JsonElement> {

  public final ArrayList<JsonElement> elements;

  public JsonArray() {
    this.elements = new ArrayList<>();
  }

  public JsonArray(int capacity) {
    this.elements = new ArrayList<>(capacity);
  }

  @Override
  public JsonElement deepCopy() {
    if (!elements.isEmpty()){
      JsonArray result = new JsonArray(elements.size());
      for (JsonElement element : elements){
        result.add(element.deepCopy());
      }
      return result;
    }
    return new JsonArray();
  }

  @Override
  public Iterator<JsonElement> iterator() {
    return null;
  }

  public void add(Boolean bool){
    elements.add(bool == null ? JsonNull.INSTANCE : new JsonPrimitive(bool));
  }
}
