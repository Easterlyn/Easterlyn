package com.easterlyn.util.tuple;

import org.jetbrains.annotations.NotNull;

/**
 * Tuple for three objects.
 *
 * @author Jikoo
 */
public class Triple<L, M, R> extends Pair<L, R> {

  private @NotNull M middle;

  public Triple(@NotNull L left, @NotNull M middle, @NotNull R right) {
    super(left, right);
    this.middle = middle;
  }

  public @NotNull M getMiddle() {
    return middle;
  }

  public void setMiddle(@NotNull M middle) {
    this.middle = middle;
  }

  @Override
  public int hashCode() {
    return super.hashCode() * 17 * middle.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && middle.equals(((Triple<?, ?, ?>) obj).middle);
  }
}
