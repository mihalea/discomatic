package ro.mihalea.discomatic;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Mircea on 23-Nov-15.
 */
public class DiscoPanel extends JPanel {
    private BufferedImage circle;
    private BufferedImage sobel;

    @Override
    public void paint(Graphics g) {         
        if(sobel != null && circle != null) {
            g.drawImage(circle, 0, 0, null);
            g.drawImage(sobel, circle.getWidth(), 0, null);
        }
    }

    public void setCircle(BufferedImage circle) {
        this.circle = circle;
    }

    public void setSobel(BufferedImage sobel) {
        this.sobel = sobel;
    }
}
