package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.util.Arrays;

public class ImagePanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener{

    private final PhotoEditor photoEditor;
    public BufferedImage image;
    public int imageWidth, imageHeight;
    
    public int brightness = 255, blur = 0;
    public float contrast = 1f, sharpness = 0.0f;
    public boolean isGrayScale, isBlur = false;
    public int mouseX, mouseY, currentMouseX, currentMouseY;

    public String text = "text", textFont = "Aerial";
    public Color textColor = Color.BLACK, textBGColor = Color.WHITE;
    public int textSize = 32;
    public boolean textBG = false;

    public BufferedImage addedImage;
    public int addedImageWidth, addedImageHeight;
    public int addedImageOpacity = 100;

    public boolean isPlacingText = false, isPlacingImage = false; 

    public ImagePanel(PhotoEditor photoEditor, BufferedImage image, int imageWidth, int imageHeight) {
        this.photoEditor = photoEditor;
        this.image = image;
        
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
    }

    public void setImage(BufferedImage newImage) { image = newImage; }
    
    public void setImageWidth(int newWidth) {
        this.imageWidth = newWidth;
        photoEditor.customWidthTextField.setText("Width : " + newWidth);
    }
    
    public void setImageHeight(int newHeight) {
        this.imageHeight = newHeight;
        photoEditor.customHeightTextfield.setText("Height : " + newHeight);
    }
    
    public void setBrightness(int brightness) {
        this.brightness = 255 - brightness;
        repaint();
    }
    
    public void setBlurriness(int blur) {
        this.blur = blur;
        this.isBlur = true;
        repaint();
    }

    public void setContrast(float contrast) {
        this.contrast = contrast / 10;
        repaint();
    }

    public void setSharpness(float sharpness) {
        this.sharpness = sharpness / 10;
        repaint();
    }

    public void setAddedImageOpacity(int opacity) {
        this.addedImageOpacity = Math.max(0, Math.min(100, opacity));
        repaint();
    }
    
    public void resizeToViewport() {
        setImageWidth(this.getWidth());
        setImageHeight(this.getHeight());
    }

    private BufferedImage applyOpacity(BufferedImage image, float opacity) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return result;
    }

    public void renderFont(Graphics2D g2D, int textSize, String text) {
        g2D.setFont(new Font(textFont, Font.PLAIN, textSize));
        FontMetrics fm = g2D.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        if (textBG) {
            g2D.setColor(textBGColor);
            g2D.fillRect(currentMouseX, currentMouseY - textHeight + fm.getDescent(), textWidth, textHeight);
        }

        g2D.setColor(textColor);
        g2D.drawString(text, currentMouseX, currentMouseY);
    }

    public BufferedImage toGrayScale(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
        
        g.dispose();
        return grayImage;
    }

    public BufferedImage blur(BufferedImage input, int radius) {
        int size = radius * 2 + 1;
        float weight = 1.0f / (size * size);
        float[] data = new float[size * size];

        Arrays.fill(data, weight);

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);

        return op.filter(input, null);
    }
    public BufferedImage sharpen(BufferedImage image) {
        float[] sharpenMatrix = {
                0.0f, -1.0f,  0.0f,
                -1.0f,  5.0f, -1.0f,
                0.0f, -1.0f,  0.0f
        };

        Kernel kernel = new Kernel(3, 3, sharpenMatrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }
    private BufferedImage blendImages(BufferedImage img1, BufferedImage img2, float alpha) {
        BufferedImage result = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(img1, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(img2, 0, 0, null);
        g2d.dispose();
        return result;
    }

    public void addText(String text, int size) {
        Graphics2D g2D = image.createGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int scaledX = (int) ((double) currentMouseX / imageWidth * image.getWidth());
        int scaledY = (int) ((double) currentMouseY / imageHeight * image.getHeight());

        double scaleFactor = Math.min((double) image.getWidth() / imageWidth, (double) image.getHeight() / imageHeight);
        int scaledSize = (int) (size * scaleFactor);

        g2D.setFont(new Font(textFont, Font.PLAIN, scaledSize));
        FontMetrics fm = g2D.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        if (textBG) {
            g2D.setColor(textBGColor);
            g2D.fillRect(scaledX, scaledY - textHeight + fm.getDescent(), textWidth, textHeight);
        }

        g2D.setColor(textColor);
        g2D.drawString(text, scaledX, scaledY);

        g2D.dispose();
    }

    public void addImage() {
        Graphics2D g2D = image.createGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int scaledX = (int) ((double) currentMouseX / imageWidth * image.getWidth());
        int scaledY = (int) ((double) currentMouseY / imageHeight * image.getHeight());
        int scaledWidth = (int) ((double) addedImageWidth / imageWidth * image.getWidth());
        int scaledHeight = (int) ((double) addedImageHeight / imageHeight * image.getHeight());

        BufferedImage opaqueImage = applyOpacity(addedImage, addedImageOpacity / 100f);
        g2D.drawImage(opaqueImage, scaledX, scaledY, scaledWidth, scaledHeight, null);

        g2D.dispose();
        
        photoEditor.imageOpacitySlider.setEnabled(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        BufferedImage processedImage = image;

        if (isGrayScale) processedImage = toGrayScale(processedImage);
        if (isBlur) processedImage = blur(processedImage, blur);
        if (contrast != 1.0f) {
            RescaleOp rescaleOp = new RescaleOp(contrast, 0, null);
            processedImage = rescaleOp.filter(processedImage, null);
        }
        
            BufferedImage sharpenedImage = sharpen(processedImage);
            // Blend the sharpened image with the original based on sharpness value
            processedImage = blendImages(processedImage, sharpenedImage, sharpness);
        
        
        g2D.drawImage(processedImage, 0, 0, imageWidth, imageHeight, null);

        if (isPlacingText || isPlacingImage) {
            g2D.setColor(new Color(0, 0, 0, 150));
            g2D.fillRect(0, 0, this.getWidth(), this.getHeight());

            if (isPlacingText) renderFont(g2D, textSize, text);
            if (isPlacingImage) {
                BufferedImage opaqueImage = applyOpacity(addedImage, addedImageOpacity / 100f);
                g2D.drawImage(opaqueImage, currentMouseX, currentMouseY, addedImageWidth, addedImageHeight, null);
            }
            
            g2D.setColor(Color.RED);
            g2D.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));

            g2D.drawLine(currentMouseX, 0, currentMouseX, imageHeight);
            g2D.drawLine(0, currentMouseY, imageWidth, currentMouseY);

        } else {
            g2D.setColor(new Color(0, 0, 0, brightness));
            g2D.fillRect(0, 0, imageWidth, imageHeight);
        }
       
    }


    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isPlacingText || isPlacingImage) {
            Point point = e.getPoint();
            currentMouseX = point.x;
            currentMouseY = point.y;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point point = e.getPoint();
        mouseX = point.x;
        mouseY = point.y;

        currentMouseX = mouseX;
        currentMouseY = mouseY;

        if (isPlacingText) {
            photoEditor.saveState();
            addText(text, textSize);
            isPlacingText = false;
        } if (isPlacingImage) {
            photoEditor.saveState();
            addImage();
            isPlacingImage = false;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private static final int MIN_TEXT_SIZE = 8;
    private static final int MAX_TEXT_SIZE = 120;
    private static final int TEXT_SIZE_STEP = 4;

    private static final int MIN_IMG_SIZE = 200;
    private static final int MAX_IMG_SIZE = 2000;
    private static final int IMG_SIZE_STEP = 50;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (isPlacingText) {
            if (notches < 0) textSize = Math.min(textSize + TEXT_SIZE_STEP, MAX_TEXT_SIZE);
            if (notches > 0) textSize = Math.max(textSize - TEXT_SIZE_STEP, MIN_TEXT_SIZE);
        }

        if (isPlacingImage) {
            double aspectRatio = (double) addedImageWidth / addedImageHeight;
            if (notches < 0) {
                addedImageWidth = Math.min(addedImageWidth + IMG_SIZE_STEP, MAX_IMG_SIZE);
                addedImageHeight = (int) (addedImageWidth / aspectRatio);
            }
            if (notches > 0) {
                addedImageWidth = Math.max(addedImageWidth - IMG_SIZE_STEP, MIN_IMG_SIZE);
                addedImageHeight = (int) (addedImageWidth / aspectRatio);
            }
            repaint();
        }
    }

}