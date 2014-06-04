package rsma.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

import rsma.interfaces.IEnvironnementAnalysis.WORLD_ENTITY;
import rsma.util.ConfigurationManager;
import rsma.util.Position;
import rsma.util.WarehouseChangement;

public class Frame extends JFrame implements Observer {

    private static final String TITLE = "Multi-Agent System: A Warehouse Example";
    private static final int ENTITY_SIZE = 10;
    
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
        
        int width = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_X_LENGHT"));
        int height = Integer.parseInt(ConfigurationManager.getProperty("WAREHOUSE_Y_LENGHT"));
        content.setPreferredSize(new Dimension(ENTITY_SIZE * width, ENTITY_SIZE * height));
        setContentPane(content);
    }
    
    @Override
    public void update(Observable o, Object arg) {
        entities = ((WarehouseChangement) arg).getMap().entrySet();
        content.repaint();
    }
    
    private void draw(Graphics g) {
        // Add anti-aliasing.
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw entities.
        g.setColor(Color.BLACK);
        for (Entry<Position, WORLD_ENTITY> entity : entities) {
            int x = ENTITY_SIZE * entity.getKey().getX();
            int y = ENTITY_SIZE * entity.getKey().getY();
            
            switch (entity.getValue()) {
            case EMPTY:
                break;
            case WALL:
                g.fillRect(x, y, ENTITY_SIZE, ENTITY_SIZE);
                break;
            case RESOURCE:
                g.fillOval(x, y, ENTITY_SIZE, ENTITY_SIZE);
                break;
            case ROBOT:
                g.fillRect(x, y, ENTITY_SIZE, ENTITY_SIZE);
                break;
            case ROBOT_AND_RESOURCE:
                g.fillRect(x, y, ENTITY_SIZE, ENTITY_SIZE);
                g.setColor(Color.RED);
                g.drawRect(x, y, ENTITY_SIZE, ENTITY_SIZE);
                break;
            default:
                break;
            }
        }
    }
    
}
