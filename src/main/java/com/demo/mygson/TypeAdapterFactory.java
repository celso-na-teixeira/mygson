package com.demo.mygson;

public interface TypeAdapterFactory {

  <T> TypeAdpter<T> create(Gson gson, TypeToken<T> type);
}
