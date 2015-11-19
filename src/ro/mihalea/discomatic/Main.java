package ro.mihalea.discomatic;

/**
 * Main entry point for the program
 */
public class Main {
    public static void main(String[] args){
        if(args.length <= 0)
            System.out.println("No image provided");
        else {
            ImageProcessor proc = new ImageProcessor();

            for (String path : args) {
                try {
                    proc.loadImage(path);
                    proc.presetFilter(ImageProcessor.FILTER_GAUSSIAN);
                    proc.presetFilter(ImageProcessor.FILTER_SOBEL);
                    proc.presetFilter(ImageProcessor.FILTER_THRESHOLD);
                    proc.saveImage();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
