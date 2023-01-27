package intellij.agent;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import amadeus.maho.lang.SneakyThrows;

public class ImageOverride {
    
    public static class Inner {
        
        private static final ImageIcon icon;
        
        static {
            final String iconUrl = System.getProperty("intellij.iconOverride");
            if (iconUrl != null)
                icon = { image(iconUrl) };
            else
                icon = null;
        }
        
    }
    
    private static volatile Image logoImage;
    
    public static Icon icon(final Icon source) = Inner.icon;
    
    @SneakyThrows
    public static void paint(final Window window, final Graphics graphics) {
        if (logoImage == null) {
            logoImage = image(System.getProperty("intellij.logoOverride"));
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final int width = logoImage.getWidth(null), height = logoImage.getHeight(null);
            window.setBounds((screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height);
        }
        window.getGraphics().drawImage(logoImage, 0, 0, window.getWidth(), window.getHeight(), null);
    }
    
    public static void showProgress(final Window window, final double progress) { }
    
    @SneakyThrows
    public static Image image(final String path) = ImageIO.read(Path.of(path).toAbsolutePath().toFile());
    
}
