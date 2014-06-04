package rsma.impl;

import rsma.GUI;
import rsma.gui.Frame;

public class GUIImpl extends GUI {

    @Override
    protected void start() {
        super.start();

        Frame frame = new Frame();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        requires().guiWarehousePort().registerObserver(frame);
    }

}
