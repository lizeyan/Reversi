import javax.swing.*;
import java.awt.*;

/**
 * Created by Li Zeyan on 2016/7/23.
 */
public class LocalMePlayer extends Player
{
    private ChessBoard chessBoard;
    private int undoCnt = 0;
    public  LocalMePlayer (ChessBoard chessBoard, Reversi game)
    {
        super(game);
        this.chessBoard = chessBoard;
    }
    @Override
    public Point makingPolicy (long timeConstraint)
    {
        if (!chessBoard.getComposition ().queryAvailble ())
        {
            JOptionPane.showMessageDialog (null, "There is no available position now", "INFO", JOptionPane.INFORMATION_MESSAGE);
            return new Point (-1, -1);
        }
        chessBoard.active ();
        long sleepTimeChip = timeConstraint / 100;
        long startTime = System.currentTimeMillis ();
        while (System.currentTimeMillis () - startTime <= timeConstraint && chessBoard.getFinalPolicy () == null)
        {
            if (game.getTerminateSignal ())
                break;
            try
            {
                Thread.sleep (sleepTimeChip);
            }
            catch (Exception e)
            {
                continue;
            }
        }
        Point ret = null;
        if (chessBoard.getFinalPolicy () != null)
            ret = new Point (chessBoard.getFinalPolicy ().x, chessBoard.getFinalPolicy ().y);
        chessBoard.setFinalPolicy (null);
        chessBoard.shutdown ();
        return ret;
    }
    
    @Override
    public boolean receiveGiveIn ()
    {
        return true;
    }
    
    @Override
    public boolean receiveStart ()
    {
        return true;
    }
    
    @Override
    public boolean receiveSueForPeace ()
    {
        return true;
    }
    
    @Override
    public void receiveQuit ()
    {
        game.disconnect (false);
    }
    @Override
    public boolean receiveUndo ()
    {
        ++undoCnt;
        if (undoCnt > 2)
            return false;
        else
            return true;
    }
}
