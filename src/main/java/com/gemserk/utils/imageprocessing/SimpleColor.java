/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.gemserk.utils.imageprocessing;

public class SimpleColor {

	int value;

	private static void testColorValueRange(int r, int g, int b, int a) {
		boolean rangeError = false;
		String badComponentString = "";

		if (a < 0 || a > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Alpha";
		}
		if (r < 0 || r > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Red";
		}
		if (g < 0 || g > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Green";
		}
		if (b < 0 || b > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Blue";
		}
		if (rangeError == true) {
			throw new IllegalArgumentException("Color parameter outside of expected range:" + badComponentString);
		}
	}

	public SimpleColor(int r, int g, int b) {
		this(r, g, b, 255);
	}

	public SimpleColor(int r, int g, int b, int a) {
		value = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		testColorValueRange(r, g, b, a);
	}

	public SimpleColor(int rgb) {
		value = 0xff000000 | rgb;
	}

	public SimpleColor(int rgba, boolean hasalpha) {
		if (hasalpha) {
			value = rgba;
		} else {
			value = 0xff000000 | rgba;
		}
	}
	

	public int getRed() {
		return (getRGB() >> 16) & 0xFF;
	}

	public int getGreen() {
		return (getRGB() >> 8) & 0xFF;
	}

	public int getBlue() {
		return (getRGB() >> 0) & 0xFF;
	}

	public int getAlpha() {
		return (getRGB() >> 24) & 0xff;
	}

	public int getRGB() {
		return value;
	}
	
	public void setRGB(int rgba){
		value = rgba;
	}
	
	public void setRGB(int r, int g, int b, int a) {
		value = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		testColorValueRange(r, g, b, a);
	}
}
