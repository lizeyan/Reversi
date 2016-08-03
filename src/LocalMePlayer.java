import com.sun.xml.internal.bind.v2.model.core.ID;

import javax.swing.*;
import java.awt.*;
import java.awt.image.VolatileImage;

/**
 * Created by Li Zeyan on 2016/7/23.
 */
public class LocalMePlayer extends Player
{
    private ChessBoard chessBoard;
    private int undoCnt = 0;
    private boolean detached = false;
    volatile Point aiPolicy = null;
    public  LocalMePlayer (ChessBoard chessBoard, Reversi game)
    {
        super(game);
        this.chessBoard = chessBoard;
    }
    @Override
    public Point makingPolicy (long timeConstraint)
    {
        aiPolicy = null;
        Thread aiThread = new Thread (() -> {aiPolicy = game.getAiInstance ().makingPolicy (timeConstraint);});
        aiThread.start ();
        if (!chessBoard.getComposition ().queryAvailble ())
        {
            return new Point (-1, -1);
        }
        chessBoard.active ();
        long sleepTimeChip = timeConstraint / 100;
        long limit = System.currentTimeMillis () + timeConstraint;
        Point ret = null;
        while (System.currentTimeMillis () <= limit)
        {
            if (detached && aiPolicy != null)
            {
                ret = new Point (aiPolicy.x, aiPolicy.y);
                break;
            }
            if (!detached && chessBoard.getFinalPolicy () != null)
            {
                ret = new Point (chessBoard.getFinalPolicy ().x, chessBoard.getFinalPolicy ().y);
                break;
            }
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
        aiThread.interrupt ();
        chessBoard.setFinalPolicy (null);
        chessBoard.shutdown ();
        return ret;
    }
    public void detach (boolean on)
    {
        detached = on;
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
