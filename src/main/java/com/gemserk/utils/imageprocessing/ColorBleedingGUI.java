package com.gemserk.utils.imageprocessing;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ColorBleedingGUI extends JFrame {

	private static final long serialVersionUID = 6235616867066961378L;

	private JPanel contentPane;
	private Canvas mainCanvas;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					final ColorBleedingGUI frame = new ColorBleedingGUI();
					frame.setVisible(true);

					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								frame.update();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}).start();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private VolatileImage backBufferImage = null;

	private void createBackBufferImage(Canvas canvas, GraphicsConfiguration gc) {
		if (backBufferImage == null)
			backBufferImage = gc.createCompatibleVolatileImage(canvas.getWidth(), canvas.getHeight());
	}

	public void update() throws Exception {
		GraphicsConfiguration gc = mainCanvas.getGraphicsConfiguration();
		createBackBufferImage(mainCanvas, gc);

		String path = "/tmp/imagen.png";
		File inputFile = new File(path);

		BufferedImage image = ImageIO.read(inputFile);

		int width = image.getWidth();
		int height = image.getHeight();

		setBounds(100, 100, width * 3, height + 50);

		ColorBleedingEffect colorBleedingEffect = new ColorBleedingEffect();
		BufferedImage processedImage = colorBleedingEffect.processImage(image);

		BufferedImage processedImageOpaque = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int[] rgba = processedImage.getRGB(0, 0, width, height, null, 0, width);
		processedImageOpaque.setRGB(0, 0, width, height, rgba, 0, width);

		ImageIO.write(processedImage, "png", new File(inputFile.getAbsolutePath() + ".bleed"));

		long iniTime = System.currentTimeMillis();
		while (true) {
			Thread.sleep(33);

			long now = System.currentTimeMillis();
			long deltaTime = now - iniTime;
			iniTime = now;

			int validated = backBufferImage.validate(gc);
			if (validated == VolatileImage.IMAGE_INCOMPATIBLE) {
				createBackBufferImage(mainCanvas, gc);
			}

			Graphics2D graphics = (Graphics2D) backBufferImage.getGraphics();
			graphics.setBackground(Color.red);
			graphics.clearRect(0, 0, getWidth(), getHeight());

			graphics.drawImage(image, 0, 0, null);
			graphics.drawImage(processedImageOpaque, width, 0, null);
			graphics.drawImage(processedImage, 2 * width, 0, null);

			Graphics2D mainGraphics = (Graphics2D) mainCanvas.getGraphics();

			if (mainGraphics != null) {
				mainGraphics.drawImage(backBufferImage, 0, 0, null);
			}

		}
	}

	/**
	 * Create the frame.
	 */
	public ColorBleedingGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 386 * 2, 386 * 2);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		mainCanvas = new Canvas();

		contentPane.add(mainCanvas, BorderLayout.CENTER);

	}

}
