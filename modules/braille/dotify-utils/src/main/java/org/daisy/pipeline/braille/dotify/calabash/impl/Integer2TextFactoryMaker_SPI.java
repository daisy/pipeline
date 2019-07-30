package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.daisy.dotify.api.text.Integer2TextFactoryMaker;
import org.daisy.dotify.api.text.Integer2TextFactoryService;

// wrapper class for org.daisy.dotify.api.text.Integer2TextFactoryMaker that can be instantiated using SPI
public class Integer2TextFactoryMaker_SPI extends Integer2TextFactoryMaker {
	
	public Integer2TextFactoryMaker_SPI() {
		super();
		// copied from Integer2TextFactoryMaker.newInstance()
		Iterator<Integer2TextFactoryService> i = ServiceLoader.load(Integer2TextFactoryService.class).iterator();
		while (i.hasNext()) {
			addFactory(i.next());
		}
	}
}
