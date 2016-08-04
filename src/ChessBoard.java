import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by Li Zeyan on 2016/7/21.
 */
public class ChessBoard extends JPanel implements MouseMotionListener, MouseListener
{
    private Composition composition;
    private int margin = 20;//棋盘网格的边距
    private float blockBreadth = 3;//网格线宽度
    private int blockSize = 100;//棋盘网格的大小
    private int pieceRadius = 45;//棋子半径
    private Color availablePositionColorActivated = new Color (0.0f, 1.0f, 0.0f, 0.5f);
    private Color availablePositionColorClosed = new Color (1.0f, 1.0f, 0.0f, 0.5f);
    private int lx, ty, rx, by;//棋盘网格的边界坐标，在pack中自动计算
    private boolean pressed = false;
    private Image backgroundImage;
    private int pieceCenterShiftFromZero;
    private int pieceDiameter;
//    private boolean firstTime = true;
    private Point mousePosition = new Point ();
    private Point finalPolicy = null;
    private boolean on = false;
    private Reversi game;
    /*
    public
     */
    public ChessBoard (Composition composition, Reversi game)
    {
        this.game = game;
        setComposition (composition);
        init ();
    }
    public Composition getComposition ()
    {
        return composition;
    }
    public void setComposition (Composition composition)
    {
        this.composition = composition;
    }
    public Point getFinalPolicy ()
    {
        return finalPolicy;
    }
    public void setFinalPolicy (Point finalPolicy)
    {
        this.finalPolicy = finalPolicy;
    }
    public void active ()
    {
        on = true;
        addMouseListener (this);
        addMouseMotionListener (this);
        repaint ();
    }
    public void shutdown ()
    {
        on = false;
        removeMouseListener (this);
        removeMouseMotionListener (this);
        repaint ();
    }
    public void mouseDragged (MouseEvent event)
    {
        mousePosition = event.getPoint();
        this.repaint();
    }
    public void mouseMoved (MouseEvent event)
    {
        mousePosition = event.getPoint();
        this.repaint();
    }
    public void mouseEntered (MouseEvent event)
    {
        //do nothing
    }
    public void mouseExited (MouseEvent event)
    {
        //do nothing
    }
    public void mouseClicked (MouseEvent event)
    {
        //do nothing
    }
    public void mousePressed (MouseEvent event)
    {
        finalPolicy = null;
        if (event.getButton() == MouseEvent.BUTTON1) {
            pressed = true;
            mousePosition = event.getPoint();
            repaint();
        }
    }
    public void mouseReleased (MouseEvent event)
    {
        pressed = false;
        finalPolicy = toCompositionPosition (mousePosition);
        if (!composition.legal (finalPolicy.x, finalPolicy.y) || !composition.queryAvailble (finalPolicy.x, finalPolicy.y))
            finalPolicy = null;
    }
    public void paintComponent (Graphics graphics)
    {
//        super.paintComponent (graphics);
//        graphics.drawImage (backgroundImage, 0, 0, this.getPreferredSize ().width, this.getPreferredSize ().height, null);
        //paint crosses
        Graphics2D g2 = (Graphics2D)graphics;
        //anti aliasing
        RenderingHints renderingHints = new RenderingHints (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints (renderingHints);
        
        //draw line with breadth
        g2.setStroke(new BasicStroke(blockBreadth));
        for (int i = 0; i <= composition.getWidth(); ++i) {
            int tmp = margin + i * blockSize;
            g2.drawLine(lx, tmp, rx, margin + i * blockSize);
            g2.drawLine(tmp, ty, tmp, by);
        }
        g2.setFont (new Font ("AR DECODE", Font.BOLD, 48));
        for (int i = 0; i < composition.getWidth (); ++i)
        {
            g2.drawString (String.valueOf ((char)('A' + i)), margin + i * blockSize + (blockSize >> 1) - 20, margin);
        }
        for (int j = 0; j < composition.getHeight (); ++j)
        {
            g2.drawString (String.valueOf (j + 1), 20, margin + j * blockSize + 24 + (blockSize >> 1));
        }
        //paint chess pieces and available positions
        Composition.STATUS[][] board = composition.getBoard();
        for (int i = 0; i < composition.getWidth(); ++i)
        {
            for (int j = 0; j < composition.getHeight(); ++j)
            {
                if (board[i][j] == Composition.STATUS.EMPTY)
                {
                    if (composition.queryAvailble (i, j))
                    {
                        if (on)
                            g2.setColor (availablePositionColorActivated);
                        else
                            g2.setColor (availablePositionColorClosed);
                        g2.fillRect (margin + i * blockSize, margin + j * blockSize, blockSize, blockSize);
                    }
                    else
                        continue;
                }
                else
                {
                    g2.setColor (status2Color (board[i][j]));
                    g2.fillOval (pieceCenterShiftFromZero + i * blockSize, pieceCenterShiftFromZero + j * blockSize, pieceDiameter, pieceDiameter);
                }
            }
        }
        //paint current mouse
        Point abstractPosition = toCompositionPosition(mousePosition);
        if (composition.legal (abstractPosition.x, abstractPosition.y))
        {
            if (composition.queryAvailble (abstractPosition.x, abstractPosition.y))
            {
                if (pressed)
                {
                    Color color = status2Color (Composition.reverseStatus (composition.getLastStatus ()));
                    g2.setColor (new Color (color.getRed (), color.getGreen (), color.getBlue (), 180));
                    g2.fillOval (pieceCenterShiftFromZero + abstractPosition.x * blockSize, pieceCenterShiftFromZero + abstractPosition.y * blockSize, pieceDiameter, pieceDiameter);
                }
                else
                {
                    //do nothing
                }
            }
            else
            {
                //dp nothing
            }
        }
    }
    /*
    private
     */
    private Color status2Color (Composition.STATUS status)
    {
        switch (status)
        {
            case WHITE:
                return Color.WHITE;
            case BLACK:
                return Color.BLACK;
            default:
                return new Color(1.0f, 1.0f, 1.0f, 0.0f);
        }
    }
    public void pack ()//处理所有和尺寸有关的事宜，在修改尺寸参数后必须调用
    {
        int s = Math.min (game.getContentPane ().getWidth () * 4 / 5, game.getContentPane ().getHeight ());
        blockSize = s / 10 + 5;
        margin = s / 25 + 5;
        pieceRadius = (int)((double)blockSize / 2.3);
        pieceCenterShiftFromZero = (blockSize >> 1) + margin - pieceRadius;
        pieceDiameter = pieceRadius << 1;
        lx = margin;
        ty = margin;
        rx = lx + blockSize * composition.getWidth();
        by = ty + blockSize * composition.getHeight();
        setPreferredSize(new Dimension(margin + rx, margin + by));
        setSize (margin + rx, margin + by);
    }
    private Point toCompositionPosition (Point realPosition)
    {
        return new Point ((realPosition.x - margin) / blockSize, (realPosition.y - margin) / blockSize);
    }
    private void readBackgroundImage ()
    {
        try
        {
            backgroundImage = ImageIO.read (new File ("./resources/images/cross.jpg"));
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog (this, "Failed to open background image", "WARNING", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void init ()
    {
        readBackgroundImage ();
        setOpaque (false);
        pack();
    }
}
