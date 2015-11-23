package ro.mihalea.discomatic;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class which handles the applying of  filters on images
 */
public class ImageProcessor {
    private Timer timer = new Timer();

    private BufferedImage original;
    private BufferedImage image;
    private String basePath;
    private String fileName;
    private String extension;

    private int mx, my, mv;
    private int maxRadius, maxValue;
    private Point maxPos = new Point(-1, -1);
    private int[][] voting;

    private float scaling = 1;

    private final static int THRESHOLD = 50;

    private final static int HOUGH_RADII = 50;

    private final static int MAX_WIDTH = 500;
    private final static int MAX_HEIGHT = 500;

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

    public void loadBuffer(BufferedImage buffer) {
        original = buffer;

        int width = buffer.getWidth();
        int height = buffer.getHeight();
        if(width > MAX_WIDTH || height > MAX_HEIGHT) {
            scaling = Math.max(width * 1f / MAX_WIDTH, height * 1f / MAX_HEIGHT);
            width /= scaling;
            height /= scaling;
        } else {
            scaling = 1;
        }

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(buffer, 0, 0, width, height, null);
        g.dispose();

        System.out.println("Processed size: :" + width + "x" + height);


    }

    public void loadImage(String path) throws Exception {
        timer.start("load");

        if(!Files.isRegularFile(Paths.get(path)))
            throw new Exception("Image not found!");

        ImageIcon icon = new ImageIcon(path);


        original = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        icon.paintIcon(null, original.getGraphics(), 0, 0);

        loadBuffer(original);

        int separator = path.lastIndexOf(".");
        int fileSeparator = path.lastIndexOf("\\") + 1;
        extension = path.substring(separator+1);
        basePath = path.substring(0, fileSeparator);
        fileName = path.substring(fileSeparator, separator);

        timer.stop();
    }

    public BufferedImage getOriginal() {
        return original;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void findCircles() {
        this.filter(3, 3, GAUSSIAN_DATA);
        this.sobel();
        this.threshold();
        this.hough();
        this.drawCircle();
    }

    private void drawCircle() {
        timer.start("circle");
        Graphics2D g = (Graphics2D) original.getGraphics();
        g.setColor(Color.GREEN);
        g.setStroke(new BasicStroke(3));

        int size = 5;
        int x = (int) (maxPos.getX() * scaling);
        int y = (int) (maxPos.getY() * scaling);
        int r = (int) (maxRadius * scaling) ;
        g.drawOval(x - r, y - r, r*2, r*2);
        g.drawLine(x - size, y - size, x + size, y + size);
        g.drawLine(x - size, y + size, x + size, y - size);
        timer.stop();
    }

    private void hough() {
        timer.start("hough");
        final int step = Math.min(image.getHeight(), image.getWidth()) / 2 / HOUGH_RADII;

        int radius = step;


        maxValue = -1;
        mv = 0;

        for (int i=1 ; i<HOUGH_RADII ; i++) {
            voting = new int[image.getWidth()][image.getHeight()];

            for (int ox=0 ; ox < image.getWidth() ; ox++) {
                for (int oy = 0; oy < image.getHeight(); oy++) {
                    if (pixelToInt(image.getRGB(ox, oy)) != 0) {
                        int x = radius;
                        int y = 0;
                        int d = 1 - x;

                        while (y <= x) {
                            checkPixel(x + ox, y + oy);
                            checkPixel(y + ox, x + oy);
                            checkPixel(-x + ox, y + oy);
                            checkPixel(-y + ox, x + oy);
                            checkPixel(-x + ox, -y + oy);
                            checkPixel(-y + ox, -x + oy);
                            checkPixel(x + ox, -y + oy);
                            checkPixel(y + ox, -x + oy);
                            y++;

                            if (d <= 0)
                                d += 2 * y + 1;
                            else {
                                x--;
                                d += 2 * (y - x) + 1;
                            }
                        }
                    }
                }
            }

            if(mv > maxValue) {
                maxRadius = radius;
                maxValue = mv;
                maxPos.setLocation(mx, my);
            }

            //printVoting("radius" + radius + "." + extension);
            radius += step;
        }

        //System.out.println(maxPos.getX() + " " + maxPos.getY() + " " + maxRadius);
        System.out.println("Radius: " + maxRadius);
        timer.stop();
    }

    private void printVoting(String name) {
        BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        for (int x=0 ; x<tmp.getWidth() ; x++)
            for (int y=0 ; y<tmp.getHeight() ; y++)
                tmp.setRGB(x, y, voting[x][y] != 0 ? 255 : 0);
                //tmp.setRGB(x, y, intToPixel(voting[x][y]));

        try {
            ImageIO.write(tmp, extension, new File(name));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void checkPixel(int x, int y) {
        if(x >= 0 && x < image.getWidth() &&
                y >= 0 && y < image.getHeight()) {
            int v = ++voting[x][y];

            if (v > mv) {
                mx = x;
                my = y;
                mv = v;
            }
        }

    }


    private void filter(int sizeX, int sizeY, float[] data) {
        timer.start("filter");
        BufferedImageOp op = new ConvolveOp(new Kernel(sizeX, sizeY, data));
        image = op.filter(image, null);
        timer.stop();
    }

    private void sobel() {
        timer.start("sobel");

        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageOp op = new ColorConvertOp(
                image.getColorModel().getColorSpace(),
                grayscale.getColorModel().getColorSpace(), null);
        op.filter(image, grayscale);

        double[][] gradient = new double[width][height];
        int max = -5000;
        for (int x = 2 ; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {

                float sumX = 0;
                float sumY = 0;

                for (int ny = -1; ny <= 1; ny++) {
                    for (int nx = -1; nx <= 1; nx++) {
                        if (x + nx >= 0 && x + nx < width &&
                                y + ny >= 0 && y + ny < height) {
                            sumX += pixelToInt(grayscale.getRGB(x + nx, y + ny)) * SOBEL_X_DATA[nx + (ny + 1) * 3 + 1];
                            sumY += pixelToInt(grayscale.getRGB(x + nx, y + ny)) * SOBEL_Y_DATA[nx + (ny + 1) * 3 + 1];
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
                sobel.setRGB(x, y, intToPixel((int) (gradient[x][y] * 255 / max)));
            }
        }

        image = sobel;

        timer.stop();
    }

    private void threshold() {
        timer.start("threshold");
        for (int x = 0 ; x < image.getWidth(); x++)
            for (int y = 0 ; y < image.getHeight() ; y++) {
                int color = image.getRGB(x, y) & 0xff;
                if(color < THRESHOLD)
                    image.setRGB(x, y, 0);
            }
        timer.stop();
    }

    private int pixelToInt(int color){
        return color & 0xff;
    }

    private int intToPixel(int integer) {
        return 0xff << 24 | integer << 16 | integer << 8 | integer;
    }

    public void saveImage() {
        timer.start("save");
        try {
            File out = new File(basePath + "out/");
            if(!out.isDirectory())
                out.mkdirs();
            ImageIO.write(image, extension, new File(basePath + "out/" + fileName + "_sobel." + extension));
            ImageIO.write(original, extension, new File(basePath + "out/" + fileName + "_circle." + extension));
        } catch (IOException e) {
            System.err.println("Could not save image");
        }
        timer.stop();
    }


}
