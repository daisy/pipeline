package com.adobe.epubcheck.api;

import java.util.List;

import com.adobe.epubcheck.api.Option.Key;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;

public final class OptionSet
{

  private final ListMultimap<Key, Option> options;

  private OptionSet(ListMultimap<Key, Option> options)
  {
    this.options = options;
  }

  public boolean has(Key key)
  {
    return options.containsKey(key);
  }

  public List<Option> getOptions(Key key)
  {
    return options.get(key);
  }

  public Optional<Option> getOption(Key key)
  {
    return Optional.fromNullable(Iterables.getFirst(options.get(key), null));
  }

  public static final class OptionSetBuilder
  {
    private final ImmutableListMultimap.Builder<Key, Option> options = new ImmutableListMultimap.Builder<Key, Option>();

    public OptionSetBuilder add(Option option)
    {
      if (option != null)
      {
        options.put(option.key, option);
      }
      return this;
    }

    public OptionSetBuilder add(Option.Key key)
    {
      if (key != null)
      {
        options.put(key, new Option(key));
      }
      return this;
    }

    public OptionSetBuilder add(Option.Key key, Object value)
    {
      if (key != null)
      {
        options.put(key, new Option(key, value));
      }
      return this;
    }

    public OptionSetBuilder add(Iterable<? extends Option> options)
    {
      return this;
    }

    public OptionSet build()
    {
      return new OptionSet(options.build());
    }
  }

}
