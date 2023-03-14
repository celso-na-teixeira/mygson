package com.demo.mygson.common;

import com.demo.mygson.JsonSerializationContext;
import com.demo.mygson.JsonSerializer;
import com.demo.mygson.model.JsonElement;
import com.demo.mygson.model.JsonObject;
import com.ericsson.oss.itpf.modeling.mdt.impl.app.TimeOfDeployment;
import java.lang.reflect.Type;
import java.util.Collection;

public class TestTypes {

  public static class Base {
    public static final String BASE_NAME = Base.class.getSimpleName();
    public static final String BASE_FIELD_KEY = "baseName";
    public static final String SERIALIZER_KEY = "SserializerName";
    public String baseName = BASE_NAME;
    public String serializerName;
  }

  public static class Sub extends Base {
    public static final String SUB_NAME = Sub.class.getSimpleName();
    public static final String SUB_FIELD_KEY = "subName";
    public final String subName = SUB_NAME;
  }

  public static class ClassWithBaseField {
    public static final String FIELD_KEY = "base";
    public final Base base;
    public ClassWithBaseField(Base base) {
      this.base = base;
    }
  }

  public static class ClassWithBaseArrayField {
    public static final String FIELD_KEY = "base";
    public final Base[] bases;

    public ClassWithBaseArrayField(Base[] bases) {
      this.bases = bases;
    }
  }

  public static class ClassWithBaseCollectionField {
    public static final String FIELD_KEY = "base";
    public final Collection<Base> base;

    public ClassWithBaseCollectionField(Collection<Base> base) {
      this.base = base;
    }
  }

  public static class BaseSerializer implements JsonSerializer<Base> {
    public static final String NAME = BaseSerializer.class.getSimpleName();

    @Override
    public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject object = new JsonObject();
      object.addProperty(Base.SERIALIZER_KEY, NAME);
      return object;
    }
  }

  public static class StringWrapper {
    public final String someConstantStringInstanceField;

    public StringWrapper(String someConstantStringInstanceField) {
      this.someConstantStringInstanceField = someConstantStringInstanceField;
    }
  }

  public static class BagOfPrimitives {
    public static final long DEFAULT_VALUE = 0;
    public long longValue;
    public int intValue;
    public boolean booleanValue;
    public String stringValue;

    public BagOfPrimitives() {
      this(DEFAULT_VALUE,0,false,"");
    }

    public BagOfPrimitives(long longValue, int intValue, boolean booleanValue, String stringValue) {
      this.longValue = longValue;
      this.intValue = intValue;
      this.booleanValue = booleanValue;
      this.stringValue = stringValue;
    }

    public int getIntValue(){return intValue;}

    public String getExpectedJson(){
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("\"longValue\":").append(longValue).append(",");
      sb.append("\"intValue\":").append(intValue).append(",");
      sb.append("\"booleanValue\":").append(booleanValue).append(",");
      sb.append("\"stringValue\":").append(stringValue).append(",");
      sb.append("}");
      return sb.toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (booleanValue ? 1231 : 1237);
      result = prime * result + intValue;
      result = prime * result + (int) (longValue ^ (longValue >>>32));
      result = prime + ((stringValue == null) ? 0 : stringValue.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      BagOfPrimitives primitives = (BagOfPrimitives) obj;
      if (booleanValue != primitives.booleanValue)
        return false;
      if (intValue != primitives.intValue)
        return false;
      if (longValue != primitives.longValue)
        return false;
      if (stringValue == null) {
        if (primitives.stringValue != null)
          return false;
      }else if (!stringValue.equals(primitives.stringValue))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return String.format("(longValue=%d, intValue=%d, booleanValue=%b, stringValue=%s)",
          longValue, intValue, booleanValue, stringValue);
    }
  }

  public static class BagOfPrimitivesWrappers{
    private final  Long longValue;
    private final Integer intValue;
    private final Boolean booleanValue;

    public BagOfPrimitivesWrappers(Long longValue, Integer intValue, Boolean booleanValue) {
      this.longValue = longValue;
      this.intValue = intValue;
      this.booleanValue = booleanValue;
    }

    public String getExpectedJson(){
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("{");
      stringBuilder.append("\"longValue\":").append(longValue).append(",");
      stringBuilder.append("\"intValue\":").append(intValue).append(",");
      stringBuilder.append("\"booleanValue\":").append(booleanValue).append(",");
      stringBuilder.append("}");
      return stringBuilder.toString();
    }
  }

  public static class PrimitiveArray{
    private final long[] longArray;

    public PrimitiveArray(long[] longArray) {
      this.longArray = longArray;
    }
    public String getExpectedJson(){
      StringBuilder sb = new StringBuilder();
      sb.append("{\"longArray\":[");

      boolean first = true;
      for (long l: longArray){
        if (!first){
          sb.append(",");
        }else{
          first = false;
        }
        sb.append(l);
      }
      sb.append("]}");
      return sb.toString();
    }
  }
  @SuppressWarnings("overrides")
  public static class ClassWithNoFields{

    @Override
    public boolean equals(Object obj) {
      return obj.getClass() == ClassWithNoFields.class;
    }
  }

  public static class Nested {
    private final BagOfPrimitives primitive;
    private final BagOfPrimitives primitive2;

    public Nested() {
      this(null,null);
    }

    public Nested(BagOfPrimitives primitive, BagOfPrimitives primitive2) {
      this.primitive = primitive;
      this.primitive2 = primitive2;
    }

    public String getExpectedJson(){
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      appendFields(sb);
      sb.append("}");
      return sb.toString();
    }

    public void appendFields(StringBuilder sb){
      if (primitive != null){
        sb.append("\"primitive\":").append(primitive.getExpectedJson());
      }
      if (primitive != null && primitive2 != null){
        sb.append(",");
      }
      if (primitive2 != null){
        sb.append("\"primitive2\":").append(primitive2.getExpectedJson())
      }
    }
  }

  public static class ClassWithTransientFields<T>{
    public transient T transientT;
    public final transient long transientLongValue;
    private final long[] longValue;

    public ClassWithTransientFields() {
      this(0L);
    }
    public ClassWithTransientFields(long value) {
      longValue = new long[] {value};
      transientLongValue = value + 1;
    }

    public String getExpectedJson(){
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("\"longValue\":[").append(longValue[0]).append("]");
      sb.append("}");
      return sb.toString();
    }
  }

  public static class ClassWithCustomTypeConverter {
    private final BagOfPrimitives bag;
    private final int value;

    public ClassWithCustomTypeConverter() {
      this(new BagOfPrimitives(), 10);
    }

    public ClassWithCustomTypeConverter(int value) {
      this(new BagOfPrimitives(value, value, false, ""), value);
    }

    public ClassWithCustomTypeConverter(BagOfPrimitives bag, int value) {
      this.bag = bag;
      this.value = value;
    }

    public BagOfPrimitives getBag() {
      return bag;
    }

    public int getValue() {
      return value;
    }

    public String getExpectedJson(){
      return "\"url\":" + bag.getExpectedJson() + "\",\"value\":" + value + "}";
    }
  }

  public static class ArrayOfObjects{

    private final BagOfPrimitives[] elements;

    public ArrayOfObjects(){
      elements = new BagOfPrimitives[3];
      for (int i = 0; i < elements.length; ++i){
        elements[i] = new BagOfPrimitives(i, i+2, false, "i" + i);
      }
    }

    public String getExpectedJson(){
      StringBuilder builder = new StringBuilder("{|\"elements\":[}");
      boolean first = true;
      for (BagOfPrimitives element : elements){
        if (first){
          first = false;
        }else {
          builder.append(",");
        }
        builder.append(element.getExpectedJson());
      }
      builder.append("]}");
      return builder.toString();
    }
  }
}
