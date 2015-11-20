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

    public void sobelize() {
        this.filter(3, 3, GAUSSIAN_DATA);
        this.applySobel();
    }


    private void filter(int sizeX, int sizeY, float[] data) {
        BufferedImageOp op = new ConvolveOp(new Kernel(sizeX, sizeY, data));
        image = op.filter(image, null);
    }

    private void applySobel() {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageOp op = new ColorConvertOp(
                image.getColorModel().getColorSpace(),
                grayscale.getColorModel().getColorSpace(), null);
        op.filter(image, grayscale);

        double[][] gradient = new double[width][height];
        int max = -5000;
        for (int x = 0 ; x < width; x++) {
            for (int y = 0; y < height; y++) {

                float sumX = 0;
                float sumY = 0;

                for (int ny = -1; ny <= 1; ny++) {
                    for (int nx = -1; nx <= 1; nx++) {
                        if (x + nx >= 0 && x + nx < width &&
                                y + ny >= 0 && y + ny < height) {
                            sumX += colorToInt(grayscale.getRGB(x + nx, y + ny)) * SOBEL_X_DATA[nx + ny + 2];
                            sumY += colorToInt(grayscale.getRGB(x + nx, y + ny)) * SOBEL_Y_DATA[nx + ny + 2];
                        }
                    }
                }

                int avg = (int) Math.sqrt(sumX * sumX + sumY * sumY);

                if(avg > max)
                    max = avg;

                gradient[x][y] = avg;
            }
        }

        BufferedImage sobel = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0 ; x < width; x++) {
            for (int y = 0; y < height; y++) {
                sobel.setRGB(x, y, intToColor((int) (gradient[x][y] * max / 100)));
            }
        }

        image = sobel;
    }

    private void threshold() {
        for (int x = 0 ; x < image.getWidth(); x++)
            for (int y = 0 ; y < image.getHeight() ; y++) {
                int color = image.getRGB(x, y) & 0xff;
                if(color < THRESHOLD)
                    image.setRGB(x, y, 0);
            }
    }

    private int colorToInt(int color){
        return color & 0xff;
    }

    private int intToColor(int integer) {
        return 0xff << 24 | integer << 16 | integer << 8 | integer;
    }

    public void saveImage() {
        try {
            ImageIO.write(image, extension, savePath);
        } catch (IOException e) {
            System.err.println("Could not save image");
        }
    }


}
