package com.gemserk.utils.imageprocessing;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import com.gemserk.utils.imageprocessing.ColorBleedingEffect.Mask.MaskIterator;

public class ColorBleedingEffect {

	public static int TOPROCESS = 0;
	public static int INPROCESS = 1;
	public static int REALDATA = 2;
	
	public static int[][] offsets = { { -1, -1 }, { 0, -1 }, { 1, -1 }, { -1, 0 }, { 1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };

	public BufferedImage processImage(BufferedImage image) {
		return processImage(image, Integer.MAX_VALUE);
	}

	public BufferedImage processImage(BufferedImage image, int maxIterations) {

		int width = image.getWidth();
		int height = image.getHeight();
			
		int[] rgb;
		if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
			// fast shortcut
			rgb = (int[]) image.getRaster().getDataElements(0, 0, width, height, null);
		} else {
			rgb = image.getRGB(0, 0, width, height, null, 0, width);        	
		}
        
		Mask mask = new Mask(rgb, width, height);

		int iterations = 0;
		while (mask.getPendingSize() > 0) {
			if (iterations >= maxIterations) {
				System.out.println("DEBUG: Reached max iterations");
				break;
			}
			
			executeIteration(rgb, mask, width, height);

			iterations++;
		}

		System.out.println("Processed image in " + iterations + " iterations");

		BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		// as the array matches the return image type, we can write directly (and faster)
		processedImage.getRaster().setDataElements(0, 0, width, height, rgb);
		
		return processedImage;
	}

	private static int getPixelIndex(int width, int x, int y) {
		return y * width + x;
	}

	SimpleColor simpleColor = new SimpleColor(0);

	public static class Mask {
		int[] rgb;
		int[] data;
		int[] pending;
		int pendingSize = 0;
		int[] changing;
		int changingSize;
		SimpleColor simpleColor = new SimpleColor(0);
		
		final int width, height;

		public Mask(int[] rgb, int width, int height) {
			this.rgb = rgb;
			this.width = width;
			this.height = height;			
			data = new int[rgb.length];
			pending = new int[rgb.length];
			changing = new int[rgb.length];

			for (int i = 0; i < rgb.length; i++) {
				int pixel = rgb[i];
				simpleColor.setRGB(pixel);
				if (simpleColor.getAlpha() == 0) {
					data[i] = TOPROCESS;
					
					int x = i % width;
					int y = i / width;
					
					for (int j = 0; j < offsets.length; j++) {
						int[] offset = offsets[j];
						int column = x + offset[0];
						int row = y + offset[1];

						if (column < 0 || column >= width || row < 0 || row >= height)
							continue;

						int currentPixelIndex = getPixelIndex(width, column, row);
						int pixelData = rgb[currentPixelIndex];
						simpleColor.setRGB(pixelData);
						if (simpleColor.getAlpha() != 0) {
							// only add to pending if at least one opaque pixel as neighbor
							pending[pendingSize] = i;
							pendingSize++;
							break;
						}
					}

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

		private int removeFromPending(int index) {
			int value = pending[index];
			pendingSize--;
			pending[index] = pending[pendingSize];
			return value;
		}

		public MaskIterator iterator() {
			return new MaskIterator(this);
		}

		public static class MaskIterator {
			int index;
			private final Mask mask;
			
			public MaskIterator(Mask mask) {
				this.mask = mask;
			}

			public boolean hasNext() {
				return index < mask.pendingSize;
			}

			public int next() {
				return mask.pending[index++];
			}

			public void markAsInProgress() {
				index--;
				int removed = mask.removeFromPending(index);
				mask.changing[mask.changingSize] = removed;
				mask.changingSize++;
			}

			public void reset() {
				assert mask.pendingSize == 0;
				
				// used for duplicate checking of new border pixels
				Set<Integer> pending = new HashSet<Integer>(mask.changingSize * 2);

				index = 0;
				for (int i = 0; i < mask.changingSize; i++) {
					int index = mask.changing[i];
					mask.data[index] = REALDATA;
					
					int x = index % mask.width;
					int y = index / mask.width;
					
					// find neighbors and add to pending
					for (int j = 0; j < offsets.length; j++) {
						int[] offset = offsets[j];
						int column = x + offset[0];
						int row = y + offset[1];

						if (column < 0 || column >= mask.width || row < 0 || row >= mask.height)
							continue;

						int currentPixelIndex = getPixelIndex(mask.width, column, row);
						if (mask.getMask(currentPixelIndex) == TOPROCESS) {
							if (pending.add(currentPixelIndex)) {
								mask.pending[mask.pendingSize] = currentPixelIndex;
								mask.pendingSize++;
							}
						}
					}
				}
				mask.changingSize = 0;
			}
		}

	}

	private void executeIteration(int[] rgb, Mask mask, int width, int height) {

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
