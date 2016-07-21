import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by Li Zeyan on 2016/7/21.
 */
public class ChessBoard extends JPanel implements MouseMotionListener, MouseListener
{
    private Composition composition;
    private int margin = 10;
    private int blockSize = 50;
    public Composition getComposition ()
    {
        return composition;
    }
    public void setComposition (Composition composition)
    {
        this.composition = composition;
    }
    public ChessBoard (Composition composition)
    {
        setComposition(composition);
    }
    public ChessBoard ()
    {
        setComposition(new Composition());
    }
    public void mouseDragged (MouseEvent event)
    {

    }
    public void mouseMoved (MouseEvent event)
    {

    }
    public void mouseEntered (MouseEvent event)
    {

    }
    public void mouseExited (MouseEvent event)
    {

    }
    public void mouseClicked (MouseEvent event)
    {

    }
    public void mousePressed (MouseEvent event)
    {

    }
    public void mouseReleased (MouseEvent event)
    {

    }
    public void paint (Graphics graphics)
    {
        super.paint(graphics);
        //paint crosses

        //paint chess pieces

    }
}
