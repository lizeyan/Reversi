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
            return new Point (-1, -1);
        chessBoard.active ();
        long sleepTimeChip = timeConstraint / 1000;
        Thread thread = new Thread ( () ->
        {
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
        });
        thread.start ();
        try
        {
            thread.join ();
        }
        catch (Exception e)
        {
            return new Point (-1, -1);
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