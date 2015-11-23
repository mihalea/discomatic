package ro.mihalea.discomatic;

import com.github.sarxos.webcam.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Mircea on 23-Nov-15.
 */
public class DiscoFrame extends JFrame{
    private ImageProcessor processor = new ImageProcessor();
    private Webcam cam;
    private DiscoPanel panel;

    public DiscoFrame() {
        this.setupCam();
        this.setupFrame();
        new Capture().run();
    }

    private void setupFrame() {
        this.setContentPane(panel = new DiscoPanel());
        this.setResizable(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setSize(new Dimension(1280, 480));
        this.setVisible(true);
    }

    private void setupCam() {
        cam = Webcam.getDefault();
        cam.setViewSize(WebcamResolution.VGA.getSize());
    }

    private class Capture extends Thread {
        @Override
        public void run() {
            cam.open();

            while(cam.isOpen()) {
                BufferedImage image = cam.getImage();
                if(image == null)
                    break;

                processor.loadBuffer(image);
                processor.findCircle();

                panel.setCircle(processor.getOriginal());
                panel.setSobel(processor.getImage());

                panel.repaint();
            }
        }
    }
}
