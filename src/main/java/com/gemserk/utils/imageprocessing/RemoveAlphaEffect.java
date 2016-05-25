package com.gemserk.utils.imageprocessing;

import java.awt.image.BufferedImage;

/**
 * Created by ruben on 5/25/16.
 */
public class RemoveAlphaEffect {

    public BufferedImage processImage(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
        // int[] mask = new int[rgb.length];

        SimpleColor simpleColor = new SimpleColor(0);

        for(int i = 0; i < rgb.length; i++) {
            int color = rgb[i];
            simpleColor.setRGB(color);
            simpleColor.setRGB(simpleColor.getRed(), simpleColor.getGreen(), simpleColor.getBlue(), 255);
            rgb[i] = simpleColor.getRGB();
        }

        System.out.println("Removed Alpha from image");

        processedImage.setRGB(0, 0, width, height, rgb, 0, width);

        return processedImage;
    }
}
