package com.adobe.epubcheck.api;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public final class Option
{
  public static enum Key
  {
    ACCESSIBILITY;
  }

  public final Key key;
  public final Optional<Object> data;
  public final Optional<String> value;

  public Option(Key key)
  {
    this(key, null);
  }

  public Option(Key key, String value)
  {
    this(key, (Object) value);
  }

  public Option(Key key, Object data)
  {
    this.key = Preconditions.checkNotNull(key);
    this.data = Optional.fromNullable(data);
    this.value = data == null ? Optional.<String> absent() : Optional.of(data.toString());
  }

}
