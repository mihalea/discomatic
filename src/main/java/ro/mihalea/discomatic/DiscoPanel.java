package ro.mihalea.discomatic;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Panel handling the drawing of the webcam feed
 */
public class DiscoPanel extends JPanel {
    private BufferedImage webcamImage;
    private BufferedImage sobelFiltered;

    @Override
    public void paint(Graphics g) {
        if(sobelFiltered != null && webcamImage != null) {
            g.drawImage(webcamImage, 0, 0, null);
            g.drawImage(sobelFiltered, webcamImage.getWidth(), 0, null);
        }
    }

    public void setWebcamImage(BufferedImage webcamImage) {
        this.webcamImage = webcamImage;
    }

    public void setSobelFiltered(BufferedImage sobelFiltered) {
        this.sobelFiltered = sobelFiltered;
    }
}
