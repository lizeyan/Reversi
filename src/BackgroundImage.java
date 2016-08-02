import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Created by Li Zeyan on 2016/8/2.
 */
public class BackgroundImage extends JPanel
{
    Image image = null;
    public BackgroundImage (String name)
    {
        setBackgroundImage (name);
    }
    public BackgroundImage ()
    {
        
    }
    public void setBackgroundImage (String name)
    {
        try
        {
            image = ImageIO.read (new File (name));
            repaint ();
        }
        catch (Exception e)
        {
            
        }
    }
    public void paintComponent (Graphics graphics)
    {
        graphics.drawImage (image, 0, 0, getWidth (), getHeight (), null);
    }
}
