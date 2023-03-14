package com.demo.mygson.common;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.Assert;

public class MoreAsserts {

  public static <T> void assertContains(Collection<T> collection, T value){
    for (T entry : collection){
      if (entry.equals(value)){
        return;
      }
    }
    Assert.fail(value + " not present in " + collection);
  }

  public static void assertEqualAndHashCode(Object a, Object b){
     Assert.assertTrue(a.equals(b));
     Assert.assertTrue(b.equals(a));
     Assert.assertEquals(a.hashCode(), b.hashCode());
     Assert.assertFalse(a.equals(null));
     Assert.assertFalse(a.equals(new Object()));
  }

  private static boolean isProtectedOrPublic(Method method){
    int modifiers = method.getModifiers();
    return Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers);
  }

  private static String getMethodSignature(Method method){
    StringBuilder builder = new StringBuilder(method.getName());
    builder.append('(');

    String sep = "";
    for (Class<?> paramType : method.getParameterTypes()){
      builder.append(sep).append(paramType.getName());
      sep = ",";
    }
    builder.append(')');
    return builder.toString();
  }

  public static void assertOverridesMethods(Class<?> baseClass, Class<?> subClass, List<String> ignoredMethods){
    Set<String> requiredOverriddenMethods = new LinkedHashSet<>();
    for (Method method : baseClass.getDeclaredMethods()){
      if (isProtectedOrPublic(method)){
        requiredOverriddenMethods.add(getMethodSignature(method));
      }
    }

    for (Method method : subClass.getDeclaredMethods()){
      requiredOverriddenMethods.remove(getMethodSignature(method));
    }

    for (String ignoredMethod : ignoredMethods){
      boolean foundIgnored = requiredOverriddenMethods.remove(ignoredMethod);
      if (!foundIgnored){
        throw new IllegalArgumentException("Method '" + ignoredMethod + "' does not exist or is already overridden");
      }
    }

    if (!requiredOverriddenMethods.isEmpty()){
      Assert.fail(subClass.getSimpleName() + "must override these methods: " + requiredOverriddenMethods);
    }
  }

}
