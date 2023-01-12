package com.demo.mygson.model;

import com.demo.mygson.internal.NonNullElementWrapperList;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public final class JsonArray extends JsonElement implements Iterable<JsonElement> {

  public final ArrayList<JsonElement> elements;

  public JsonArray() {
    this.elements = new ArrayList<>();
  }

  public JsonArray(int capacity) {
    this.elements = new ArrayList<>(capacity);
  }

  @Override
  public JsonArray deepCopy() {
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
    return elements.iterator();
  }

  public void add(Boolean bool){
    elements.add(bool == null ? JsonNull.INSTANCE : new JsonPrimitive(bool));
  }

  public  void add(Number number){
    elements.add(number == null ? JsonNull.INSTANCE : new JsonPrimitive(number));
  }

  public void add(String string){
    elements.add(string == null ? JsonNull.INSTANCE : new JsonPrimitive(string));
  }

  public void add(Character character){
    elements.add(character == null ? JsonNull.INSTANCE : new JsonPrimitive(character));
  }

  public void add(JsonElement element){
    elements.add(element == null ? JsonNull.INSTANCE : element);
  }

  public void addAll(JsonArray array){
    elements.addAll(array.elements);
  }

  public JsonElement set(int index, JsonElement element){
    return elements.set(index, element == null ? JsonNull.INSTANCE : element);
  }

  public boolean remove(JsonElement element){
    return elements.remove(element);
  }

  public JsonElement remove(int index){
    return elements.remove(index);
  }
  public boolean contains(JsonElement element){
    return elements.contains(element);
  }
  public int size(){
    return elements.size();
  }

  public boolean isEmpty(){
    return elements.isEmpty();
  }

  public JsonElement get(int index){
    return elements.get(index);
  }

  private JsonElement getAsSingleElement(){
    int size = elements.size();
    if (size == 1){
      return elements.get(0);
    }
    throw new IllegalStateException("Array must have size 1, but has size " + size);
  }

  @Override
  public Number getAsNumber() {
    return getAsSingleElement().getAsNumber();
  }

  @Override
  public String getAsString() {
    return getAsSingleElement().getAsString();
  }

  @Override
  public double getAsDouble() {
    return getAsSingleElement().getAsDouble();
  }

  @Override
  public float getAsFloat() {
    return getAsSingleElement().getAsFloat();
  }

  @Override
  public long getAsLong() {
    return getAsSingleElement().getAsLong();
  }

  @Override
  public int getAsInt() {
    return getAsSingleElement().getAsInt();
  }

  @Override
  public byte getAsAByte() {
    return getAsSingleElement().getAsAByte();
  }

  @Override
  public BigDecimal getAsBigDecimal() {
    return getAsSingleElement().getAsBigDecimal();
  }

  @Override
  public BigInteger getAsBigInteger() {
    return getAsSingleElement().getAsBigInteger();
  }

  @Override
  public short getAsShort() {
    return getAsSingleElement().getAsShort();
  }

  @Override
  public boolean getAsBoolean() {
    return getAsSingleElement().getAsBoolean();
  }

  @Override
  public char getAsCharacter() {
    return getAsSingleElement().getAsCharacter();
  }

  public List<JsonElement> asList(){
    return new NonNullElementWrapperList<>(elements);
  }

  @Override
  public boolean equals(Object o) {
    return (this == o) || (o instanceof JsonArray && ((JsonArray) o).elements.equals(elements));
  }

  @Override
  public int hashCode() {
    return elements.hashCode();
  }
}
