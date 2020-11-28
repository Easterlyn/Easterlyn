package com.easterlyn.util.reflection;

import java.lang.reflect.Field;
import org.jetbrains.annotations.Nullable;

public class ReflectionUtil {

  /**
   * Gets the specified Field from a Class, ensuring that the Field's type matches the expected
   * type.
   *
   * @param clazz the Class containing the Field
   * @param fieldName the name of the Field
   * @param fieldClass the Class of the Field
   * @return the Field
   * @throws NoSuchFieldException if the expected Field is not found or is of the wrong type
   */
  public static <T> Field getField(Class<?> clazz, String fieldName, Class<T> fieldClass)
      throws NoSuchFieldException {
    Field declaredField = clazz.getDeclaredField(fieldName);
    declaredField.setAccessible(true);
    if (!fieldClass.isAssignableFrom(declaredField.getType())) {
      throw new NoSuchFieldException(
          String.format(
              "Field %s has type %s in %s, expected %s",
              fieldName, declaredField.getType().getName(), clazz.getName(), fieldClass.getName()));
    }
    return declaredField;
  }

  /**
   * Gets the contents of the specified Field from an Object. While the underlying Field is
   * guaranteed to be of the correct type, the value of the Field may be null.
   *
   * <p>Since there's no reason to use reflection on accessible members, this method always searches
   * recursively for a matching declared field in superclasses. For cases where more generic
   * handling is not required, consider {@link #getField(Class, String, Class)} instead.
   *
   * @param object the Object containing the Field
   * @param fieldName the name of the Field
   * @param fieldClass the Class of the expected Field value
   * @return the value of the Field
   * @throws NoSuchFieldException if the expected Field is not found or is of the wrong type
   * @throws IllegalAccessException if the Field is not accessible
   */
  public static <T> @Nullable T getFieldValue(Object object, String fieldName, Class<T> fieldClass)
      throws NoSuchFieldException, IllegalAccessException {
    return getFieldValue(object, object.getClass(), fieldName, fieldClass);
  }

  private static <T> @Nullable T getFieldValue(
      Object object, Class<?> checkClazz, String fieldName, Class<T> fieldClass)
      throws NoSuchFieldException, IllegalAccessException {
    Class<?> superclass = checkClazz.getSuperclass();

    try {
      Field field = getField(checkClazz, fieldName, fieldClass);
      Object fieldObj = field.get(object);
      return fieldClass.cast(fieldObj);
    } catch (NoSuchFieldException exception) {
      if (superclass == null) {
        throw exception;
      }
    }

    return getFieldValue(object, superclass, fieldName, fieldClass);
  }
}
