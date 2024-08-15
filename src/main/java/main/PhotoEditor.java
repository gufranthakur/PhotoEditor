package main;

import com.formdev.flatlaf.FlatClientProperties;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

public class PhotoEditor extends JFrame{
    
    private JPanel rootPanel;
    private JSplitPane rootSplitPane;
    public JPanel rightPanel;
    private JPanel leftPanel;
    private JButton exportButton;
    private JPanel controlPanel;
    private JSlider brightnessSlider;
    private JRadioButton grayScaleRadioButton;
    private JSlider blurSlider;
    private JPanel effectsPanel;
    private JPanel addTextPanel;
    private JButton addTextButton;
    private JButton addImageButton;
    private JComboBox fontComboBox;
    private JButton textColorButton;
    private JComboBox fontSizeComboBox;
    private JButton undoButton;
    private JButton redoButton;
    private JButton refreshButton;
    private JPanel addImagePanel;
    public JSlider imageOpacitySlider;
    private JRadioButton backgroundRadioButton;
    private JButton BGColorButton;
    private JSlider widthSlider;
    private JPanel adjustmentPanel;
    private JSlider heightSlider;
    public JTextField customWidthTextField;
    public JTextField customHeightTextfield;
    private JButton resizeToViewportButton;
    private JSlider contrastSlider;
    private JSlider sharpnessSlider;

    public Timer refreshTimer;
    public BufferedImage currentImage;
    public static Stack<BufferedImage> undoStack;
    public static Stack<BufferedImage> redoStack;
    public ImagePanel imagePanel;
    public BufferedImage image;

    public PhotoEditor() {
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        
        this.setSize(1280, 720);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setTitle("Photo Editor");
        this.setContentPane(rootPanel);
        init();
        this.setVisible(true);
    }
    
    public void setDefaultValues() {
        imagePanel.setBrightness(255);
        imagePanel.setImageWidth(1000);
        imagePanel.setImageHeight(720);
        imagePanel.setSharpness(0);
        imagePanel.setContrast(10);
        
        brightnessSlider.setValue(255);
        blurSlider.setValue(0);
        widthSlider.setValue(1000);
        heightSlider.setValue(720);
        contrastSlider.setValue(10);
        sharpnessSlider.setValue(0);
    }
    
    public void init() {
        refreshTimer = new Timer(10, e -> imagePanel.repaint());

        for (String s : Arrays.asList("Aerial", "SansSerif", "Monospaced")) fontComboBox.addItem(s);
        for (int i : new int[]{24, 28, 32, 48, 72, 96, 120}) fontSizeComboBox.addItem(i);
        
        rightPanel.putClientProperty(FlatClientProperties.STYLE, "arc:30");
        controlPanel.putClientProperty(FlatClientProperties.STYLE, "arc:30");
        effectsPanel.putClientProperty(FlatClientProperties.STYLE, "arc:30");
        addTextPanel.putClientProperty(FlatClientProperties.STYLE, "arc:30");
        addImagePanel.putClientProperty(FlatClientProperties.STYLE, "arc:30");
        adjustmentPanel.putClientProperty(FlatClientProperties.STYLE, "arc:30");

        customWidthTextField.setHorizontalAlignment(SwingConstants.CENTER);
        customHeightTextfield.setHorizontalAlignment(SwingConstants.CENTER);
        
        controlPanel.setBackground(getBackground().darker());
        controlPanel.setOpaque(true);
        
        brightnessSlider.addChangeListener(e -> imagePanel.setBrightness(brightnessSlider.getValue()));
        blurSlider.addChangeListener(e -> imagePanel.setBlurriness(blurSlider.getValue()));
        widthSlider.addChangeListener(e -> imagePanel.setImageWidth(widthSlider.getValue()));
        heightSlider.addChangeListener(e -> imagePanel.setImageHeight(heightSlider.getValue()));
        imageOpacitySlider.addChangeListener(e -> imagePanel.setAddedImageOpacity(imageOpacitySlider.getValue()));
        contrastSlider.addChangeListener(e -> imagePanel.setContrast(contrastSlider.getValue()));
        sharpnessSlider.addChangeListener(e -> imagePanel.setSharpness(sharpnessSlider.getValue()));
        
        grayScaleRadioButton.addActionListener(e -> imagePanel.isGrayScale = grayScaleRadioButton.isSelected());
        backgroundRadioButton.addActionListener(e -> {imagePanel.textBG = backgroundRadioButton.isSelected();
            BGColorButton.setEnabled(imagePanel.textBG);
        });
        
        textColorButton.addActionListener(e -> imagePanel.textColor = JColorChooser.showDialog(null, "Select color", Color.BLACK));
        
        fontComboBox.addActionListener(e -> imagePanel.textFont = fontComboBox.getSelectedItem().toString());
        fontSizeComboBox.addActionListener(e -> imagePanel.textSize = (int) fontSizeComboBox.getSelectedItem());
        
        BGColorButton.addActionListener(e -> imagePanel.textBGColor = JColorChooser.showDialog(null, "Select Color", Color.BLACK));
        resizeToViewportButton.addActionListener(e -> imagePanel.resizeToViewport());
        refreshButton.addActionListener(e -> imagePanel.repaint());
        
        undoButton.addActionListener(e -> undo());
        redoButton.addActionListener(e -> redo());

        customWidthTextField.addActionListener(e -> {
            try {
                int width = Integer.parseInt(customWidthTextField.getText());
                imagePanel.setImageWidth(width);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        customHeightTextfield.addActionListener(e -> {
            try {
                int height = Integer.parseInt(customHeightTextfield.getText());
                imagePanel.setImageHeight(height);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        addTextButton.addActionListener(e -> {
            String text = JOptionPane.showInputDialog("Enter text");
            if (text == null) return;
            imagePanel.text = text;
            imagePanel.isPlacingText = true;
        });
        
        addImageButton.addActionListener(e -> {
            imageOpacitySlider.setEnabled(true);
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    imagePanel.addedImage = ImageIO.read(file);
                    imagePanel.isPlacingImage = true;
                    
                    imagePanel.addedImageWidth = imagePanel.addedImage.getWidth() / 2;
                    imagePanel.addedImageHeight = imagePanel.addedImage.getHeight() / 2;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error loading image: " + ex.getMessage());
                }
            }
        });

        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Edited Image");
            fileChooser.setSelectedFile(new File("edited_image.png"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
            fileChooser.setFileFilter(filter);

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".png")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
                }

                try {
                    BufferedImage imageToSave = deepCopy(currentImage);
                    if (imagePanel.isGrayScale) imageToSave = imagePanel.toGrayScale(imageToSave);
                    if (imagePanel.isBlur) imageToSave = imagePanel.blur(imageToSave, imagePanel.blur);
                    
                    BufferedImage resizedImage = new BufferedImage(imagePanel.imageWidth, imagePanel.imageHeight, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = resizedImage.createGraphics();
                    
                    float brightnessLevel = (float)(255 - imagePanel.brightness) / 255;
                    if (brightnessLevel != 1.0f) {
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, brightnessLevel));
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, imagePanel.imageWidth, imagePanel.imageHeight);
                    }
                    
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(imageToSave, 0, 0, imagePanel.imageWidth, imagePanel.imageHeight, null);
                    
                    g.dispose();

                    ImageIO.write(resizedImage, "png", fileToSave);
                    JOptionPane.showMessageDialog(this, "Image saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
       
    }
    
    public void saveState() { 
        undoStack.push(deepCopy(currentImage)); 
        redoStack.clear();
    }
    
    public void setImage(BufferedImage image) {
        this.image = image;
        if (image != null) {
            currentImage = image;
            undoStack.clear();
            redoStack.clear();
            saveState();
        }
    }
    
    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(deepCopy(currentImage));
            currentImage = undoStack.pop();
            if (imagePanel != null) {
                imagePanel.setImage(currentImage);
                imagePanel.repaint();
                System.gc();
            }
        }
    }
    
    public void redo() {
        if(!redoStack.isEmpty()) {
            undoStack.push(deepCopy(currentImage));
            currentImage = redoStack.pop();
            if (imagePanel != null) {
                imagePanel.setImage(currentImage);
                imagePanel.repaint();
                System.gc();
            }
        }
    }
    
    public static BufferedImage deepCopy(BufferedImage image) {
        ColorModel colorModel = image.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

}
