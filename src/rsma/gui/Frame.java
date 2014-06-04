package rsma.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.util.Position;
import rsma.util.WarehouseChangement;

public class Frame extends JFrame implements Observer {

    private static final String TITLE = "Multi-Agent System: A Warehouse Example";
    private static final int WIDTH = 750;
    private static final int HEIGHT = 400;
    
    private final JPanel content;
    private Set<Entry<Position, WORLD_ENTITY>> entities;
    
    public Frame() {
        super(TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        content = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
            
        };
        content.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setContentPane(content);
    }
    
    @Override
    public void update(Observable o, Object arg) {
        entities = ((WarehouseChangement) arg).getMap().entrySet();
        content.repaint();
    }
    
    private void draw(Graphics g) {
        // TODO
    }
    
}
