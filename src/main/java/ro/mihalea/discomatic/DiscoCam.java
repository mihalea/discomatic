package ro.mihalea.discomatic;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.sun.javafx.logging.JFRInputEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Mircea on 23-Nov-15.
 */
public class DiscoCam extends JFrame{
    private ImageProcessor processor = new ImageProcessor();
    private JPanel container;
    private Webcam cam;
    private WebcamPanel panel;

    public DiscoCam() {
        this.setupCam();
        this.setupFrame();
    }

    private void setupFrame() {
        this.setResizable(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void setupCam() {
        container = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;

        cam = Webcam.getDefault();
        cam.setViewSize(WebcamResolution.VGA.getSize());
        cam.open(true);

        panel = new WebcamPanel(cam);
        panel.setFPSDisplayed(true);
        //panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);

        panel.setPainter(new CustomPainer());

        container.add(panel, c);
        this.setContentPane(container);
    }

    private class CustomPainer implements WebcamPanel.Painter {

        @Override
        public void paintPanel(WebcamPanel panel, Graphics2D g2) {

        }

        @Override
        public void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2) {
            processor.loadBuffer(image);
            processor.findCircles();
            g2.drawImage(processor.getOriginal(), 0, 0, null);
            //g2.drawImage(processor.getImage(), 0, 0, null);
        }
    }
}
