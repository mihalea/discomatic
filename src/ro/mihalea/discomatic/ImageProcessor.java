package ro.mihalea.discomatic;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
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

    public final static float[] BLUR = new float[] {
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f};

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

    public void applyFilter(float[] filter) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0 ; x < width ; x++)
            for (int y = 0 ; y < height ; y++) {
                float sum = 0;
                for (int ny = -1 ; ny <= 1 ; ny++)
                    for (int nx = -1 ; nx <= 1 ; nx++)
                        if(x + nx >= 0 && x + nx < width &&
                                y + ny >= 0 && y + ny < height)
                            sum += image.getRGB(x + nx, y + ny) * filter[nx + ny + 2];
                buffer.setRGB(x, y, (int) sum);
            }

        image = buffer;
    }

    public void saveImage() {
        try {
            ImageIO.write(image, extension, savePath);
        } catch (IOException e) {
            System.err.println("Could not save image");
        }
    }
}
