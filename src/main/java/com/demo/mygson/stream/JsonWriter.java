package com.demo.mygson.stream;

import static com.demo.mygson.stream.JsonScope.DANGLING_NAME;
import static com.demo.mygson.stream.JsonScope.EMPTY_ARRAY;
import static com.demo.mygson.stream.JsonScope.EMPTY_DOCUMENT;
import static com.demo.mygson.stream.JsonScope.EMPTY_OBJECT;
import static com.demo.mygson.stream.JsonScope.NONEMPTY_ARRAY;
import static com.demo.mygson.stream.JsonScope.NONEMPTY_DOCUMENT;
import static com.demo.mygson.stream.JsonScope.NONEMPTY_OBJECT;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class JsonWriter implements Closeable, Flushable {

  private static final Pattern VALID_JSON_NUMBER_PATTERN = Pattern.compile("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?");

  private static final String[] REPLACEMENT_CHARS;

  private static final String[] HTML_SAFE_REPLACEMENT_CHARS;

  static {
    REPLACEMENT_CHARS = new String[128];
    for (int i = 0; i < 0x1f; i++){
      REPLACEMENT_CHARS[i] = String.format("\\u%04x", i);
    }
    REPLACEMENT_CHARS['"'] = "\\\"";
    REPLACEMENT_CHARS['\\'] = "\\\\";
    REPLACEMENT_CHARS['\t'] = "\\t";
    REPLACEMENT_CHARS['\b'] = "\\b";
    REPLACEMENT_CHARS['\n'] = "\\n";
    REPLACEMENT_CHARS['\r'] = "\\r";
    REPLACEMENT_CHARS['\f'] = "\\f";
    HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
    HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
    HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
    HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
    HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
    HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
  }

  private final Writer out;

  private int[] stack = new int[32];

  private int stacksize = 0;

  {
    push(EMPTY_DOCUMENT);
  }

  private String indent;

  private String separator;

  private boolean lenient;

  private  boolean htmlSafe;

  private String deferredName;

  private boolean serializeNulls = true;

  public JsonWriter(Writer out){
    this.out = Objects.requireNonNull(out, "out == null");
  }

  public final void setIndent(String indent){
    if (indent.length() == 0){
      this.indent = null;
      this.separator = ":";
    }else {
      this.indent = indent;
      this.separator = ": ";
    }
  }

  public final void setLenient(boolean lenient){
    this.lenient = lenient;
  }

  public boolean isLenient(){
    return lenient;
  }

  public final boolean isHtmlSafe(){
    return htmlSafe;
  }

  public final void setSerializeNulls(boolean serializeNulls){
    this.serializeNulls = serializeNulls;
  }

  public final boolean getSerializeNulls(){
    return serializeNulls;
  }

  public JsonWriter beginArray() throws IOException{
    writeDeferredName();
    return open(EMPTY_ARRAY, '[');
  }

  public JsonWriter endArray() throws IOException{
    return close(EMPTY_ARRAY, NONEMPTY_ARRAY, ']');
  }

  public JsonWriter beginObject() throws IOException{
    writeDeferredName();
    return open(EMPTY_OBJECT, '{');
  }

  public JsonWriter endObject() throws IOException{
    return close(EMPTY_OBJECT, NONEMPTY_OBJECT, '}');
  }

  private JsonWriter open(int empty, char openBracket) throws  IOException{
    beforeValue();
    push(empty);
    out.write(openBracket);
    return this;
  }

  private JsonWriter close(int empty, int nonempty, char closeBracket) throws IOException{
    int context = peek();
    if (context != nonempty && context != empty){
      throw new InvalidObjectException("Nesting problem");
    }
    if (deferredName != null){
      throw new IllegalStateException("Danglin name: " + deferredName);
    }

    stacksize--;
    if (context == nonempty){
      newline();
    }
    out.write(closeBracket);
    return this;
  }

  private void push(int newtop){
    if (stacksize == stack.length){
      stack = Arrays.copyOf(stack, stacksize*2);
    }
    stack[stacksize] = newtop;
  }

  private int peek(){
    if (stacksize == 0){
      throw  new IllegalStateException("JsonWriter is closed.");
    }
    return stack[stacksize - 1];
  }

  private void replaceTop(int topOfStack){
    stack[stacksize -1] = topOfStack;
  }

  public JsonWriter name(String name) throws IOException{
    Objects.requireNonNull(name, "name == null");
    if (deferredName != null){
      throw new IllegalStateException();
    }
    if (stacksize == 0){
      throw new IllegalStateException("JsonWriter is closed.");
    }
    deferredName = name;
    return this;
  }

  private void writeDeferredName() throws IOException{
    if (deferredName != null){
      beforeName();
      string(deferredName);
      deferredName = null;
    }
  }

  public JsonWriter value(String value) throws IOException{
    if (value == null){
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    string(value);
    return this;
  }

  public JsonWriter jsonValue(String value) throws IOException{
    if (value == null){
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    out.append(value);
    return this;
  }

  public JsonWriter value(boolean value) throws IOException{
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    return this;
  }

  public JsonWriter value(Boolean value) throws IOException{
    if (value == null){
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    return this;
  }

  public JsonWriter value(float value) throws IOException{
    writeDeferredName();
    if (!lenient && (Float.isNaN(value) || Float.isInfinite(value))){
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    beforeValue();
    out.append(Float.toString(value));
    return this;
  }

  public JsonWriter value(long value) throws IOException{
    writeDeferredName();
    beforeValue();
    out.write(Long.toString(value));
    return this;
  }

  private static boolean isTrustedNumberType(Class<? extends Number> c){
    return c == Integer.class || c == Long.class || c == Double.class || c == Float.class ||
        c == Byte.class || c == BigDecimal.class || c == BigInteger.class || c == AtomicInteger.class ||
        c == AtomicLong.class;
  }
  public JsonWriter value(Number value){

    return this;
  }

  private void beforeValue() throws IOException{
    switch (peek()){
      case NONEMPTY_DOCUMENT:
        if (!lenient){
          throw new IllegalStateException("JSON must have only one top-level value. ");
        }

      case EMPTY_DOCUMENT:
        replaceTop(NONEMPTY_DOCUMENT);
        break;

      case EMPTY_ARRAY:
        replaceTop(NONEMPTY_ARRAY);
        newline();
        break;
      case NONEMPTY_ARRAY:
        out.append(',');
        newline();
        break;
      case DANGLING_NAME:
        out.append(separator);
        replaceTop(NONEMPTY_OBJECT);
        break;

      default:
        throw new IllegalStateException("Nesting problem.");

    }
  }

  private void newline() throws IOException{
    if (indent == null){
      return;
    }

    out.write('\n');
    for (int i = 1, size = stacksize; i < size; i++){
      out.write(indent);
    }
  }

  private void  beforeName() throws IOException{
    int context = peek();
    if (context == NONEMPTY_OBJECT){
      out.write(',');
    } else if (context != EMPTY_OBJECT) {
      throw new IllegalStateException("Nesting problem");
    }
    newline();
    replaceTop(DANGLING_NAME);
  }

  private void string(String value) throws IOException{
    String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
    out.write('\"');
    int last = 0;
    int length = value.length();
    for (int i = 0; i < length; i++){
      char c = value.charAt(i);
      String replacement;
      if (c < 128){
        replacement = replacements[c];
        if (replacement == null){
          continue;
        }
      } else if (c == '\u2028') {
        replacement = "\\u2028";
      } else if (c == '\u2029') {
        replacement = "\\u2029";
      }else {
        continue;
      }
      if (last < i){
        out.write(value, last, i - last);
      }
      out.write(replacement);
      last = i + 1;
    }
    if (last < length){
      out.write(value, last, length - last);
    }
    out.write('\"');
  }

  public JsonWriter nullValue() throws  IOException{
    if (deferredName != null){
      if (serializeNulls){
        writeDeferredName();
      }else {
        deferredName = null;
        return this;
      }
    }
    beforeValue();
    out.write("null");
    return this;
  }

  @Override
  public void flush() throws IOException {
    if (stacksize == 0){
      throw new IllegalStateException("JsonWriter is closed.");
    }
    out.flush();
  }


  @Override
  public void close() throws IOException {
    out.close();

    int size = stacksize;
    if (size > 1 || size == 1 && stack[size - 1] != NONEMPTY_DOCUMENT){
      throw new IOException("Incomplte document");
    }
    stacksize = 0;
  }


}
