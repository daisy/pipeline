package org.daisy.impl;

import org.daisy.Foo;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "foo-service",
	service = { Foo.class }
)
public class FooImpl implements Foo {
	public void run() {
		System.out.print("hello world!");
	}
}
