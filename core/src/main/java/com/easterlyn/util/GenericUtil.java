package com.easterlyn.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility for useful methods involving generics.
 *
 * @author Jikoo
 */
public class GenericUtil {

  private GenericUtil() {}

  public @NotNull static <T> T orDefault(@Nullable T t, @NotNull T defaultT) {
    return t == null ? defaultT : t;
  }

  public @Nullable static <T, R> R functionAs(
      @NotNull Class<T> clazz, @Nullable Object obj, @NotNull Function<T, R> function) {
    if (!clazz.isInstance(obj)) {
      return null;
    }
    return function.apply(clazz.cast(obj));
  }

  public static <T> void consumeAs(
      @NotNull Class<T> clazz, @Nullable Object obj, @NotNull Consumer<T> consumer) {
    if (!clazz.isInstance(obj)) {
      return;
    }
    consumer.accept(clazz.cast(obj));
  }

  public static <T> void biConsumeAs(
      @NotNull Class<T> clazz,
      @Nullable Object obj1,
      Object obj2,
      @NotNull BiConsumer<T, T> consumer) {
    consumeAs(clazz, obj1, cast1 -> consumeAs(clazz, obj2, cast2 -> consumer.accept(cast1, cast2)));
  }
}
