package com.demo.mygson.internal;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.math.BigDecimal;
import java.util.Objects;

public class LazilyParsedNumber extends Number{

  private final String value;

  public LazilyParsedNumber(String value) {
    this.value = value;
  }

  @Override
  public int intValue() {
    try{
      return Integer.parseInt(value);
    }catch (NumberFormatException e){
      try{
        return (int) Long.parseLong(value);
      }catch (NumberFormatException nfe){
        return new BigDecimal(value).intValue();
      }
    }
  }

  @Override
  public long longValue() {
    try{
      return Long.parseLong(value);
    }catch (NumberFormatException nfe){
      return new BigDecimal(value).intValue();
    }

  }

  @Override
  public float floatValue() {
    return Float.parseFloat(value);
  }

  @Override
  public double doubleValue() {
    return Double.parseDouble(value);
  }

  @Override
  public String toString() {
    return value;
  }

  private Object writeReplace() throws ObjectStreamException{
    return new BigDecimal(value);
  }

  private void readObect(ObjectInputStream in) throws IOException{
    throw new InvalidObjectException("Deserialization is unsupported");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof LazilyParsedNumber) {
      LazilyParsedNumber other = (LazilyParsedNumber) o;
      return value == other.value || value.equals(other.value);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
