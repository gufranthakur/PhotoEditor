package main;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Launcher extends JFrame{
    private JPanel rootPanel;
    private JButton importImageButton;
    private JButton exitButton;

    private BufferedImage image;

    public Launcher() {
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setTitle("Photo Editor");
        this.setContentPane(rootPanel);
        init();
        this.setVisible(true);  
    }
    
    public void init() {
        importImageButton.addActionListener(e -> {
            
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                
                String projectName = JOptionPane.showInputDialog("Enter project name");
                
                try {
                    if (projectName.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Enter project name", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        PhotoEditor photoEditor = new PhotoEditor();
                        
                        image = ImageIO.read(file);
                        photoEditor.image = image;

                        photoEditor.setTitle(projectName);
                        photoEditor.setImage(image);
                        photoEditor.image = image;
                        photoEditor.refreshTimer.start();
                        photoEditor.imagePanel = new ImagePanel(photoEditor, photoEditor.image, image.getWidth(), image.getHeight());
                        photoEditor.rightPanel.add(photoEditor.imagePanel);
                        photoEditor.setDefaultValues();
                        dispose();
                    }
                    
                    
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error loading image: " + ex.getMessage());
                }
            }
        });
        
        exitButton.addActionListener(e -> System.exit(0));
    }
    
    public static void main(String[] args) throws UnsupportedLookAndFeelException{
        UIManager.setLookAndFeel(new FlatMacDarkLaf());
        
        Color theme = new Color(49, 49, 49);
        UIManager.put("Button.background", theme);
        UIManager.put("ComboBox.background", theme);
        
        UIManager.put("Panel.arc", 50);
        
        
        new Launcher();
    }
    
}
