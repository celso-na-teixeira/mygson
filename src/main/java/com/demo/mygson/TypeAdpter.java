package com.demo.mygson;

public abstract class TypeAdpter<T> {

  public TypeAdpter() {
  }

  public abstract void write(JsonWriter out, T value) throws Exception;

}
