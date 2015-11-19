package ro.mihalea.discomatic;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class which handles the applying of  filters on images
 */
public class ImageProcessor {
    private BufferedImage image;
    private File savePath;
    private String extension;

    public final static int FILTER_BOX = 0;
    public final static int FILTER_GAUSSIAN = 1;
    public final static int FILTER_SOBEL = 2;
    public final static int FILTER_THRESHOLD = 3;

    private final static int THRESHOLD = 200;

    private final static float[] BLUR_DATA = new float[] {
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f};
    private final static float[] GAUSSIAN_DATA = new float[] {
            1/16f, 1/8f, 1/16f,
            1/8f,  1/4f, 1/8f,
            1/16f, 1/8f, 1/16f
    };

    private final static float[] SOBEL_X_DATA = new float[] {
            -1, 0, 1,
            -2, 0, 2,
            -1, 0, 1
    };

    private final static float[] SOBEL_Y_DATA = new float[] {
            -1, -2, -1,
             0,  0,  0,
             1,  2,  1,
    };



    public void loadImage(String path) throws Exception {
        if(!Files.isRegularFile(Paths.get(path)))
            throw new Exception("Image not found!");

        ImageIcon icon = new ImageIcon(path);
        image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        icon.paintIcon(null, image.getGraphics(), 0, 0);

        int separator = path.lastIndexOf(".");
        extension = path.substring(separator + 1);
        savePath = new File(path.substring(0, separator) + "_out." + extension);
    }

    public void presetFilter(int filter) {
        switch (filter) {
            case FILTER_BOX:
                filter(3, 3, BLUR_DATA);
                break;
            case FILTER_GAUSSIAN:
                filter(3, 3, GAUSSIAN_DATA);
                break;
            case FILTER_SOBEL:
                sobel();
                break;
            case FILTER_THRESHOLD:
                threshold();
                break;
        }
    }

    private void filter(int sizeX, int sizeY, float[] data) {
        BufferedImageOp op = new ConvolveOp(new Kernel(sizeX, sizeY, data));
        image = op.filter(image, null);
    }

    private void sobel() {
        BufferedImage grayscale = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageOp op = new ColorConvertOp(
                image.getColorModel().getColorSpace(),
                grayscale.getColorModel().getColorSpace(), null);
        op.filter(image, grayscale);

        op = new ConvolveOp(new Kernel(3, 3, SOBEL_X_DATA));
        BufferedImage sobelX = op.filter(grayscale, null);

        op = new ConvolveOp(new Kernel(3, 3, SOBEL_Y_DATA));
        BufferedImage sobelY = op.filter(grayscale, null);

        for (int x = 0 ; x < image.getWidth(); x++)
            for (int y = 0 ; y < image.getHeight() ; y++) {
                int cx = sobelX.getRGB(x, y);
                int cy = sobelY.getRGB(x, y);
                image.setRGB(x, y, avgColors(cx, cy));
            }
    }

    private void threshold() {
        for (int x = 0 ; x < image.getWidth(); x++)
            for (int y = 0 ; y < image.getHeight() ; y++) {
                int color = image.getRGB(x, y) & 0xff;
                if(color < THRESHOLD)
                    image.setRGB(x, y, 0);
            }
    }

    private int avgColors(int a, int b) {
        int color = (int) Math.sqrt(Math.pow(a & 0xff, 2) + Math.pow(b & 0xff, 2));
        return 0xff << 24 | color << 16 | color << 8 | color;
    }

    public void saveImage() {
        try {
            ImageIO.write(image, extension, savePath);
        } catch (IOException e) {
            System.err.println("Could not save image");
        }
    }
}
