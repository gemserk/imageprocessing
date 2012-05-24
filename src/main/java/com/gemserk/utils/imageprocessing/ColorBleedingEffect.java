package com.gemserk.utils.imageprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ColorBleedingEffect {

	private int TOPROCESS = 0;
	private int INPROCESS = 1;
	private int REALDATA = 2;

	public BufferedImage processImage(BufferedImage image) {
		return processImage(image,Integer.MAX_VALUE);
	}
	public BufferedImage processImage(BufferedImage image, int maxIterations) {

		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
		int[] mask = new int[rgb.length];

		int pending = 0;
		for (int row = 0; row < height; row++) {
			for (int column = 0; column < width; column++) {
				int pixelIndex = getPixelIndex(width, column, row);
				int pixelData = rgb[pixelIndex];
				Color color = new Color(pixelData, true);
				if (color.getAlpha() == 0) {
					mask[pixelIndex] = TOPROCESS;
					pending++;
				} else {
					mask[pixelIndex] = REALDATA;
				}
			}
		}

		int iterations = 0;
		int lastPending = -1;
		while (pending > 0) {
			if(iterations >= maxIterations){
				System.out.println("DEBUG: Reached max iterations");
				break;
			}
			
			lastPending = pending;
			pending = 0;
			for (int row = 0; row < height; row++) {
				for (int column = 0; column < width; column++) {
					getAverageColor(rgb, mask, width, height, column, row);
				}
			}

			for (int i = 0; i < mask.length; i++) {
				if (mask[i] == INPROCESS)
					mask[i] = REALDATA;

				if (mask[i] == TOPROCESS)
					pending++;
			}
			iterations++;

			if (pending == lastPending) {
				System.out.println("WARN: Infinite loop detected ABORT ABORT ABORT EXTERMINATE");
				break;
			}
		}

		System.out.println("Procesed image in " + iterations + " iterations");

		processedImage.setRGB(0, 0, width, height, rgb, 0, width);

		return processedImage;
	}

	private int getPixelIndex(int width, int x, int y) {
		return y * width + x;
	}

	SimpleColor simpleColor = new SimpleColor(0);
	
	private Color getAverageColor(int[] rgb, int[] mask, int width, int height, int x, int y) {
		int[][] offsets = { { -1, -1 }, { 0, -1 }, { 1, -1 }, { -1, 0 }, { 1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };

		int pixelIndex = getPixelIndex(width, x, y);
		if (mask[pixelIndex] != TOPROCESS)
			return null;

		int r = 0;
		int g = 0;
		int b = 0;
		int cant = 0;

		for (int i = 0; i < offsets.length; i++) {
			int[] offset = offsets[i];
			int column = x + offset[0];
			int row = y + offset[1];

			if (column < 0 || column >= width || row < 0 || row >= height)
				continue;

			int currentPixelIndes = getPixelIndex(width, column, row);
			// System.out.println("" + column + "," + row);
			int pixelData = rgb[currentPixelIndes];
			simpleColor.setRGB(pixelData);
			if (mask[currentPixelIndes] == REALDATA) {
				r += simpleColor.getRed();
				g += simpleColor.getGreen();
				b += simpleColor.getBlue();
				cant++;
			}
		}

		if (cant != 0) {
			simpleColor.setRGB(r / cant, g / cant, b / cant, 0);
			rgb[pixelIndex] = simpleColor.getRGB();
			mask[pixelIndex] = INPROCESS;
		}

		return null;

	}

}
