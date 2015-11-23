package ro.mihalea.discomatic;

import com.github.sarxos.webcam.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Frame holding the live gui
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
        this.setSize(new Dimension(640 + 500, 480));
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

                panel.setWebcamImage(processor.getOriginal());
                panel.setSobelFiltered(processor.getProcessed());

                panel.repaint();
            }
        }
    }
}
