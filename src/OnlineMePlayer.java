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
        return super.receiveGiveIn ();
    }
    
    @Override
    public boolean receiveStart ()
    {
        return super.receiveStart ();
    }
    
    @Override
    public boolean receiveSueForPeace ()
    {
        return super.receiveSueForPeace ();
    }
    
    @Override
    public void receiveQuit ()
    {
        super.receiveQuit ();
    }
}
