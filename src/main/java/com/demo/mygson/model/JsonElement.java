package com.demo.mygson.model;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class JsonElement {

  public abstract JsonElement deepCopy();

  private boolean isJasonArray(){
    return this instanceof JsonArray;
  }

  public boolean isJasonObject(){
    return this instanceof JsonObject;
  }

  public boolean isJsonPrimitive(){
    return this instanceof JsonPrimitive;
  }

  public boolean isJsonNull(){
    return this instanceof JsonNull;
  }

  public JsonObject getAsJsonObject(){
    if (isJasonObject()){
      return (JsonObject) this;
    }
    throw new IllegalStateException("Not a JSON Object: " + this);
  }

  public JsonArray getAsJsonArray(){
    if (isJasonArray()){
      return (JsonArray) this;
    }
    throw new IllegalStateException("Not a JSON Array: " + this);
  }

  public JsonPrimitive getAsJsonPrimitive(){
    if (isJsonPrimitive()){
      return (JsonPrimitive) this;
    }
    throw new IllegalStateException("Not a JSON Primitive: " + this);
  }

  public JsonNull getAsJsonNull(){
    if (isJsonNull()){
      return (JsonNull) this;
    }
    throw new IllegalStateException("Not a JSON Null: " + this);
  }

  public boolean getAsBoolean(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public Number getAsNumber(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public String getAsString(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public double getAsADouble(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public float getAsAFloat(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public int getAsAnInt(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public byte getAsAByte(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public BigDecimal getAsBigDecimal(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public BigInteger getAsBigInteger(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  public short getAsShort(){
    throw new UnsupportedOperationException(getClass().getSimpleName());
  }

  @Override
  public String toString() {
    StringWriter stringWriter = new StringWriter();
    return stringWriter.toString();
  }
}
