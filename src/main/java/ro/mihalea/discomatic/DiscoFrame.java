package ro.mihalea.discomatic;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Mircea on 23-Nov-15.
 */
public class DiscoFrame extends JFrame{
    public DiscoFrame() throws HeadlessException {
        this.setupFrame();
    }

    private void setupFrame() {
        this.setTitle("Discomatic 5000");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setSize(new Dimension(800, 600));
        this.setVisible(true);
    }
}
