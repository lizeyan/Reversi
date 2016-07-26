import javax.swing.*;
import java.awt.*;

/**
 * Created by Li Zeyan on 2016/7/23.
 */
public class LocalMePlayer implements Player
{
    private ChessBoard chessBoard;
    public  LocalMePlayer (ChessBoard chessBoard)
    {
        this.chessBoard = chessBoard;
    }
    @Override
    public Point makingPolicy (long timeConstraint)
    {
        if (!chessBoard.getComposition ().queryAvailble ())
        {
            JOptionPane.showMessageDialog (null, "There is no available position now", "INFO", JOptionPane.INFORMATION_MESSAGE);
        }
        chessBoard.active ();
        long sleepTimeChip = timeConstraint / 100;
        long startTime = System.currentTimeMillis ();
        while (System.currentTimeMillis () - startTime <= timeConstraint && chessBoard.getFinalPolicy () == null)
        {
            try
            {
                Thread.sleep (sleepTimeChip);
            }
            catch (Exception e)
            {
                continue;
            }
        }
        if (chessBoard.getFinalPolicy () == null)
            return new Point (-1, -1);
        Point ret = new Point (chessBoard.getFinalPolicy ().x, chessBoard.getFinalPolicy ().y);
        chessBoard.setFinalPolicy (null);
        chessBoard.shutdown ();
        return ret;
    }
    
    @Override
    public boolean receiveGiveIn ()
    {
        return false;
    }
    
    @Override
    public boolean receiveStart ()
    {
        return false;
    }
    
    @Override
    public boolean receiveSueForPeace ()
    {
        return false;
    }
    
    @Override
    public void receiveQuit ()
    {
        
    }
}
