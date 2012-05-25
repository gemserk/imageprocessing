package com.gemserk.utils.imageprocessing;

import java.awt.image.BufferedImage;
import java.util.NoSuchElementException;

import com.gemserk.utils.imageprocessing.ColorBleedingEffect.Mask.MaskIterator;

public class ColorBleedingEffect {

	public static int TOPROCESS = 0;
	public static int INPROCESS = 1;
	public static int REALDATA = 2;

	public BufferedImage processImage(BufferedImage image) {
		return processImage(image, Integer.MAX_VALUE);
	}

	public BufferedImage processImage(BufferedImage image, int maxIterations) {

		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
		// int[] mask = new int[rgb.length];

		Mask mask = new Mask(rgb);

		int iterations = 0;
		int lastPending = -1;
		while (mask.getPendingSize() > 0) {
			if (iterations >= maxIterations) {
				System.out.println("DEBUG: Reached max iterations");
				break;
			}

			lastPending = mask.getPendingSize();
			executeIteration(rgb, mask, width, height);

			iterations++;

			if (mask.getPendingSize() == lastPending) {
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

	public static class Mask {
		int[] data;
		int[] pending;
		int pendingSize = 0;
		int[] changing;
		int changingSize;
		SimpleColor simpleColor = new SimpleColor(0);

		public Mask(int[] rgb) {
			data = new int[rgb.length];
			pending = new int[rgb.length];
			changing = new int[rgb.length];

			for (int i = 0; i < rgb.length; i++) {
				int pixel = rgb[i];
				simpleColor.setRGB(pixel);
				if (simpleColor.getAlpha() == 0) {
					data[i] = TOPROCESS;
					pending[pendingSize] = i;
					pendingSize++;
				} else {
					data[i] = REALDATA;
				}
			}
		}

		public int getPendingSize() {
			return pendingSize;
		}

		public int getMask(int index) {
			return data[index];
		}

		private int removeIndex(int index) {
			if (index >= pendingSize)
				throw new IndexOutOfBoundsException(String.valueOf(index));

			int value = pending[index];
			pendingSize--;
			pending[index] = pending[pendingSize];
			return value;
		}

		public MaskIterator iterator() {
			return new MaskIterator(this);
		}

		static public class MaskIterator {
			int index;
			private final Mask mask;

			public MaskIterator(Mask mask) {
				this.mask = mask;
			}

			public boolean hasNext() {
				return index < mask.pendingSize;
			}

			public int next() {
				if (index >= mask.pendingSize)
					throw new NoSuchElementException(String.valueOf(index));
				return mask.pending[index++];
			}

			public void markAsInProgress() {
				index--;
				int removed = mask.removeIndex(index);
				mask.changing[mask.changingSize] = removed;
				mask.changingSize++;
			}

			public void reset() {
				index = 0;
				for (int i = 0; i < mask.changingSize; i++) {
					int index = mask.changing[i];
					mask.data[index] = REALDATA;
				}
				mask.changingSize = 0;
			}
		}

	}

	private void executeIteration(int[] rgb, Mask mask, int width, int height) {
		int[][] offsets = { { -1, -1 }, { 0, -1 }, { 1, -1 }, { -1, 0 }, { 1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };

		MaskIterator iterator = mask.iterator();
		while (iterator.hasNext()) {
			int pixelIndex = iterator.next();

			int x = pixelIndex % width;
			int y = pixelIndex / width;

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
				if (mask.getMask(currentPixelIndes) == REALDATA) {
					r += simpleColor.getRed();
					g += simpleColor.getGreen();
					b += simpleColor.getBlue();
					cant++;
				}
			}

			if (cant != 0) {
				simpleColor.setRGB(r / cant, g / cant, b / cant, 0);
				rgb[pixelIndex] = simpleColor.getRGB();
				iterator.markAsInProgress();
			}
		}

		iterator.reset();
	}

}
