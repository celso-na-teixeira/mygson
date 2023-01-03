package com.demo.mygson.model;

import com.demo.mygson.internal.LazilyParsedNumber;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class JsonPrimitive extends JsonElement {

  private final Object value;

  public JsonPrimitive(Boolean bool) {
    value = Objects.requireNonNull(bool);
  }

  public JsonPrimitive(Number number ){
    value = Objects.requireNonNull(number);
  }

  public JsonPrimitive(String string){
    value = Objects.requireNonNull(string);
  }

  public JsonPrimitive(Character value) {
    this.value = Objects.requireNonNull(value).toString();
  }

  @Override
  public JsonPrimitive deepCopy() {
    return this;
  }

  public boolean isBoolean(){
    return value instanceof Boolean;
  }

  public boolean isNumber(){
    return value instanceof Number;
  }

  public boolean isString(){
    return value instanceof String;
  }

  @Override
  public JsonObject getAsJsonObject() {
    return super.getAsJsonObject();
  }

  @Override
  public JsonArray getAsJsonArray() {
    return super.getAsJsonArray();
  }

  @Override
  public JsonPrimitive getAsJsonPrimitive() {
    return super.getAsJsonPrimitive();
  }

  @Override
  public JsonNull getAsJsonNull() {
    return super.getAsJsonNull();
  }

  @Override
  public boolean getAsBoolean() {
    if (isBoolean()){
      return (Boolean) value;
    }
    return Boolean.parseBoolean(getAsString());
  }

  @Override
  public Number getAsNumber() {
    if (value instanceof Number){
      return (Number) value;
    }else if(value instanceof String){
      return new LazilyParsedNumber((String) value);
    }
    throw new UnsupportedOperationException("Primitive is neither a number nor a string");
  }

  @Override
  public String getAsString() {
    if (value instanceof String){
      return (String) value;
    }else if (isNumber()){
      return getAsNumber().toString();
    } else if (isBoolean()) {
      return ((Boolean) value).toString();
    }
    throw new AssertionError("Unexpected value type: " + value.getClass());
  }

  @Override
  public double getAsADouble() {
    return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(getAsString());
  }

  @Override
  public float getAsAFloat() {
    return super.getAsAFloat();
  }

  @Override
  public int getAsAnInt() {
    return super.getAsAnInt();
  }

  @Override
  public byte getAsAByte() {
    return super.getAsAByte();
  }

  @Override
  public BigDecimal getAsBigDecimal() {
    return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(getAsString());
  }

  @Override
  public BigInteger getAsBigInteger() {
    return super.getAsBigInteger();
  }

  @Override
  public short getAsShort() {
    return super.getAsShort();
  }
}
