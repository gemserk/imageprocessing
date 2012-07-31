package com.gemserk.utils.imageprocessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class ColorBleedingCLI {

	static Logger logger = LoggerFactory.getLogger(ColorBleedingCLI.class);

	public static class Options {
		@Parameter
		List<String> parameters = new ArrayList<String>();

		@Parameter(names = "-dir", description = "Directory containing the images", required = true)
		public String dir;

		@Parameter(names = "-overwrite", description = "Overwrite Source Images")
		public boolean overwrite = false;

		@Parameter(names = "-debug", description = "Writes debug images without alpha")
		public boolean debug = false;

		@Parameter(names = "-outdir", description = "Directory to use for the output (copy if not overriding and debug image if debug enabled)")
		public String outdir = null;

		@Parameter(names = "-maxiterations", description = "Max iterations to run the algorithm")
		public int maxiterations = Integer.MAX_VALUE;
		
		@Parameter(names = "-numthreads", description = "Number of threads to use")
		public int numthreads = 8;

	}

	public static void main(String[] args) {
		final Options options = new Options();
		JCommander jCommander = new JCommander(options);
		jCommander.setProgramName("java -jar ColorBleeding.jar");
		try {
			String inPath = "/tmp/images";
			String[] argv = { /* "-overwrite", */"-dir", inPath, "-debug", "-maxiterations", "2" , "-outdir","/tmp/result", "-numthreads", "8" };

			if (System.getProperty("overrideArgs", "false").equals("true")) {
				args = argv;
			}
			
			jCommander.parse(args);

			execute(options);

		} catch (ParameterException e) {
			logger.error(e.getMessage());
			if (jCommander != null) {
				jCommander.usage();
			}
		} catch (IOException e) {
			logger.error("Error while processing images", e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void execute(final Options options) throws IOException, InterruptedException {
		String dirPath = options.dir;
		logger.info("Path: " + dirPath);
		logger.info("Overwrite: " + options.overwrite);

		final File dir = new File(dirPath);

		if (!isDirectoryValid("input", dir))
			return;

		final File outDir = options.outdir == null ? dir : new File(options.outdir);

		if (!isDirectoryValid("output", outDir))
			return;

		final ArrayList<File> images = new ArrayList<File>();

		FileTraversal fileTraversal = new FileTraversal() {
			@Override
			public void onFile(File f) {
				String name = f.getName();
				if (name.endsWith(".png"))
					images.add(f);
			}
		};

		fileTraversal.traverse(dir);

		ExecutorService executorService = Executors.newFixedThreadPool(options.numthreads);

		long globalIniTime = System.currentTimeMillis();

		for (int i = 0; i < images.size(); i++) {
			final File imageFile = images.get(i);
			final int index = i;
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						ColorBleedingEffect colorBleedingEffect = new ColorBleedingEffect();
						long iniTime = System.currentTimeMillis();

						logger.info("{} - Processing image {}", index, imageFile);
						BufferedImage image = ImageIO.read(imageFile);
						BufferedImage processedImage = colorBleedingEffect.processImage(image, options.maxiterations);
						File destinationFile = imageFile;
						File destinationFolder = destinationFolder(dir, imageFile, outDir);
						if (dir != outDir)
							destinationFolder.mkdirs();
						if (!options.overwrite) {
							destinationFile = new File(destinationFolder, imageFile.getName() + ".bleed");
						}
						ImageIO.write(processedImage, "png", destinationFile);

						if (options.debug) {
							int width = image.getWidth();
							int height = image.getHeight();

							BufferedImage processedImageOpaque = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

							int[] rgba = processedImage.getRGB(0, 0, width, height, null, 0, width);
							processedImageOpaque.setRGB(0, 0, width, height, rgba, 0, width);

							ImageIO.write(processedImageOpaque, "png", new File(destinationFolder, imageFile.getName() + ".debug"));
						}

						long now = System.currentTimeMillis();
						float time = (now - iniTime) / 1000f;
						logger.debug("{} - Time to process {} seconds", index, time);
					} catch (IOException e) {
						logger.error("{} - Error while processing images", index, e);
					} catch (Exception e) {
						logger.error("{} - Error while processing images", index, e);
					}
				}
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.HOURS);

		long globalNow = System.currentTimeMillis();
		float globalTime = (globalNow - globalIniTime) / 1000f;
		logger.info("Time to process {} images - {} seconds", images.size(), globalTime);
	}

	private static File destinationFolder(File originalDir, File originalFile, File destinationRoot) {
		File parentFile = originalFile.getParentFile();
		String relativePath = parentFile.getAbsolutePath().substring(originalDir.getAbsolutePath().length());
		return new File(destinationRoot, relativePath);
	}

	private static boolean isDirectoryValid(String use, File dir) {
		if (!dir.exists()) {
			logger.error("The path for {} - \"{}\" doesn't exist", use, dir);
			return false;
		}

		if (!dir.isDirectory()) {
			logger.error("The path for {} \"{}\" isn't a directory", use, dir);
			return false;
		}
		return true;
	}
}
