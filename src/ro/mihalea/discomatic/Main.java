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
                    proc.sobelize();
                    proc.saveImage();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
