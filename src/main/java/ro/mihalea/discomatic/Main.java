package ro.mihalea.discomatic;

import java.io.File;
/**
 * Main entry point for the program
 */
public class Main {
    public static void main(String[] args){
        if(args.length <= 0) {
            DiscoCam cam = new DiscoCam();
        } else {
            ImageProcessor proc = new ImageProcessor();

            for (String path : args) {
                try {
                    File file = new File(path);
                    if (file.isDirectory()) {
                        for (File f : file.listFiles())
                            if (f.isFile()) {
                                proc.loadImage(f.getAbsolutePath());
                                proc.findCircles();
                                proc.saveImage();
                            }
                    } else {
                        proc.loadImage(path);
                        proc.findCircles();
                        proc.saveImage();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
