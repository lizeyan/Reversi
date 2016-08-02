import com.sun.javafx.scene.paint.GradientUtils;

import java.awt.*;
import java.net.Socket;

/**
 * Created by Li Zeyan on 2016/7/27.
 */
public class OnlineMePlayer extends LocalMePlayer
{
    private Proxy proxy;
    public OnlineMePlayer (ChessBoard chessBoard, Reversi game)
    {
        super(chessBoard, game);
    }
    public void setProxy (Proxy proxy)
    {
        this.proxy = proxy;
    }
    
    @Override
    public Point makingPolicy (long timeConstraint)
    {
        Point ret = super.makingPolicy (timeConstraint);
        if (game.getTerminateSignal ())
            return null;
        try
        {
            proxy.send ("POLICY", ret.x + " " + ret.y);
        }
        catch (Exception e)
        {
            game.terminate (e.getMessage ());
            return null;
        }
        return ret;
    }
    
    @Override
    public boolean receiveGiveIn ()
    {
        return game.askForGivein ();
    }
    
    @Override
    public boolean receiveStart ()
    {
        return game.askForStart ();
    }
    
    @Override
    public boolean receiveSueForPeace ()
    {
        return game.askForSue ();
    }
    
    @Override
    public boolean receiveUndo ()
    {
        return game.askForUndo ();
    }
    
    @Override
    public void receiveQuit ()
    {
        super.receiveQuit ();
    }
}
