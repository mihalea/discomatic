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
    //Timer used for measuring runtimes within methods
    private Timer timer = new Timer();

    //Original image with the circle drawn
    private BufferedImage original;

    //Image proccesed with different filters
    private BufferedImage processed;

    //Folder in which the image resides
    private String basePath;

    //Name of the image
    private String fileName;

    //Extension of the image
    //IMPORTANT: THIS MUST NOT CONTAIN A DOT
    private String extension;

    //Maximum X and Y, and value for the current radius
    private int mx, my, mv;

    //Radius of the optimum candidate and it's voting value
    private int maxRadius, maxValue;

    //Position of the optimum candidate
    private Point maxPos = new Point(-1, -1);

    //Voting matrix used in the hough transform
    private int[][] voting;

    //Scaling factor used to reduce the image
    private float scaling = 1;

    //Value between 0-255 below which edge pixels are ignore
    //Bigger values means less runtime, but more inaccurate results
    private final static int THRESHOLD = 100;

    //The number of radii to be searched by the hough transform
    private final static int HOUGH_RADII = 20;

    //Maximum size of the image
    private final static int MAX_SIZE = 500;

    //Convolution matrix for box blur
    private final static float[] BLUR_DATA = new float[] {
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f};

    //Convolution matrix for gaussian blur
    private final static float[] GAUSSIAN_DATA = new float[] {
            1/16f, 1/8f, 1/16f,
            1/8f,  1/4f, 1/8f,
            1/16f, 1/8f, 1/16f
    };

    //Convolution matrix for the sobel operator on the X axis
    private final static float[] SOBEL_X_DATA = new float[] {
            -1, 0, 1,
            -2, 0, 2,
            -1, 0, 1
    };

    //Convolution matrix for the sobel operator on the Y axis
    private final static float[] SOBEL_Y_DATA = new float[] {
            -1, -2, -1,
             0,  0,  0,
             1,  2,  1,
    };

    /**
     * Loads a BufferedImage in the image processor.
     * @param buffer Image to be processed.
     */
    public void loadBuffer(BufferedImage buffer) {
        original = buffer;

        int width = buffer.getWidth();
        int height = buffer.getHeight();

        /**
         * If the image is bigger than MAX_SIZE x MAX_SIZE scale it down so that it fits
         * inside the square defined by those variables
         */
        if(width > MAX_SIZE || height > MAX_SIZE) {
            scaling = Math.max(width * 1f / MAX_SIZE, height * 1f / MAX_SIZE);
            width /= scaling;
            height /= scaling;
        } else {
            scaling = 1;
        }

        processed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = processed.createGraphics();
        g.drawImage(buffer, 0, 0, width, height, null);
        g.dispose();

        System.out.println("Processed size: :" + width + "x" + height);


    }

    /**
     * Loads an image from a file
     * @param path File from which to load image
     * @throws Exception There is no such file
     */
    public void loadImage(String path) throws Exception {
        timer.start("load");

        if(!Files.isRegularFile(Paths.get(path)))
            throw new Exception("Image not found!");

        ImageIcon icon = new ImageIcon(path);

        original = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        icon.paintIcon(null, original.getGraphics(), 0, 0);

        loadBuffer(original);

        //Store the path of the image as separate components
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

    public BufferedImage getProcessed() {
        return processed;
    }

    /**
     * Finds the most prominent circle in the image
     * and draws it onto the original
     */
    public void findCircle() {
        this.filter(3, 3, GAUSSIAN_DATA);
        this.sobel();
        this.threshold();
        this.hough();
        this.drawCircle();
    }

    /**
     * Draw the edge and the center of the circle
     */
    private void drawCircle() {
        timer.start("circle");
        Graphics2D g = (Graphics2D) original.getGraphics();
        g.setColor(Color.GREEN);
        g.setStroke(new BasicStroke(3));

        //Scale up the coordinates so that they can be applied to the original image
        int x = (int) (maxPos.getX() * scaling);
        int y = (int) (maxPos.getY() * scaling);
        int r = (int) (maxRadius * scaling) ;
        int size = r / 10;

        //Draw the edge
        g.drawOval(x - r, y - r, r*2, r*2);

        //Draw an X at the center
        g.drawLine(x - size, y - size, x + size, y + size);
        g.drawLine(x - size, y + size, x + size, y - size);

        timer.stop();
    }

    private void hough() {
        timer.start("hough");

        final int step = Math.min(processed.getHeight(), processed.getWidth()) / 2 / HOUGH_RADII;
        int radius = step;

        //Reset the values used for storing the maximum instances
        maxValue = -1;
        mv = 0;

        //For every radius possible based on the fixed step
        for (int i=1 ; i<HOUGH_RADII ; i++) {
            voting = new int[processed.getWidth()][processed.getHeight()];

            //For every pixel in the image
            for (int ox = 0; ox < processed.getWidth() ; ox++) {
                for (int oy = 0; oy < processed.getHeight(); oy++) {

                    /**
                     * If the pixel is an edge and and above the threshold draw
                     * a circle around it using the midpoint circle algorithm
                     */
                    if (pixelToInt(processed.getRGB(ox, oy)) != 0) {
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

            /**
             * If the local maximum is bigger than the global maximum than
             * we have found a better candidate for the center
             */
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

    /**
     * Debug method used to print the voting matrix to an image
     * @param name Name of the file to be written
     */
    private void printVoting(String name) {
        BufferedImage tmp = new BufferedImage(processed.getWidth(), processed.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

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

    /**
     * Increase the value of a certain pixel on the voting matrix
     * and check against the local maximum
     * @param x Position on the X axis of the pixel
     * @param y Position on the Y axis of the pixel
     */
    private void checkPixel(int x, int y) {
        if(x >= 0 && x < processed.getWidth() &&
                y >= 0 && y < processed.getHeight()) {
            int v = ++voting[x][y];

            if (v > mv) {
                mx = x;
                my = y;
                mv = v;
            }
        }

    }

    /**
     * Apply a filter using a convolution matrix and predefined methods
     * @param sizeX Width of the kernel
     * @param sizeY Height of the kernel
     * @param data Kernel to be applied
     */
    private void filter(int sizeX, int sizeY, float[] data) {
        timer.start("filter");
        BufferedImageOp op = new ConvolveOp(new Kernel(sizeX, sizeY, data));
        processed = op.filter(processed, null);
        timer.stop();
    }

    /**
     * Apply the sobel operator to the processed image
     */
    private void sobel() {
        timer.start("sobel");

        int width = processed.getWidth();
        int height = processed.getHeight();

        //Convert to greyscale
        BufferedImage greyscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImageOp op = new ColorConvertOp(
                processed.getColorModel().getColorSpace(),
                greyscale.getColorModel().getColorSpace(), null);
        op.filter(processed, greyscale);

        /**
         * Hold the values in a matrix as their domain does not allow them to be applied
         * directly to a BufferedImage
         */
        double[][] gradient = new double[width][height];

        //Store the maximum value so we can scale it down to a pixel domain (0-255)
        int max = -5000;
        for (int x = 2 ; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {

                float sumX = 0;
                float sumY = 0;

                for (int ny = -1; ny <= 1; ny++) {
                    for (int nx = -1; nx <= 1; nx++) {
                        if (x + nx >= 0 && x + nx < width &&
                                y + ny >= 0 && y + ny < height) {
                            sumX += pixelToInt(greyscale.getRGB(x + nx, y + ny)) * SOBEL_X_DATA[nx + (ny + 1) * 3 + 1];
                            sumY += pixelToInt(greyscale.getRGB(x + nx, y + ny)) * SOBEL_Y_DATA[nx + (ny + 1) * 3 + 1];
                        }
                    }
                }

                /**
                 * Using pythagoras theorem we get that  G = sqrt(Gx^2 + Gy^2)
                  */
                int avg = (int) Math.sqrt(sumX * sumX + sumY * sumY);

                if(avg > max)
                    max = avg;

                gradient[x][y] = avg;
            }
        }

        //Construct the image based on the matrix
        BufferedImage sobel = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0 ; x < width; x++) {
            for (int y = 0; y < height; y++) {
                sobel.setRGB(x, y, intToPixel((int) (gradient[x][y] * 255 / max)));
            }
        }

        processed = sobel;

        timer.stop();
    }

    /**
     * Apply a threshold to the image to reduce noise and runtime
     */
    private void threshold() {
        timer.start("threshold");
        for (int x = 0; x < processed.getWidth(); x++)
            for (int y = 0; y < processed.getHeight() ; y++) {
                if(pixelToInt(processed.getRGB(x, y)) < THRESHOLD)
                    processed.setRGB(x, y, 0);
            }
        timer.stop();
    }

    /**
     * Transforms an 4 byte grayscale "color" to a 1byte int between 0-255
     * @param color 4 byte grayscale value
     * @return 1 byte grayscale value
     */
    private int pixelToInt(int color){
        return color & 0xff;
    }

    /**
     * Constructs a 4 byte grayscale color from a value between 0-255
     * @param integer Value between 0-255
     * @return 4 byte color
     */
    private int intToPixel(int integer) {
        return 0xff << 24 | integer << 16 | integer << 8 | integer;
    }

    /**
     * Save both BufferedImages to the disk
     */
    public void saveImage() {
        timer.start("save");
        try {
            File out = new File(basePath + "out/");
            if(!out.isDirectory())
                out.mkdirs();

            ImageIO.write(processed, extension, new File(basePath + "out/" + fileName + "_sobel." + extension));
            ImageIO.write(original, extension, new File(basePath + "out/" + fileName + "_circle." + extension));
        } catch (IOException e) {
            System.err.println("Could not save image");
        }
        timer.stop();
    }


}
