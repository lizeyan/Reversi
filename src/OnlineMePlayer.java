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
    private volatile boolean rspTmp = false;
    
    @Override
    public Point makingPolicy (long timeConstraint)
    {
        Point ret = super.makingPolicy (timeConstraint);
        if (ret == null || game.getTerminateSignal ())
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
        rspTmp = false;
        Thread thread = new Thread (() -> {rspTmp = game.askForGivein ();});
        thread.start ();
        try
        {
            thread.join (3000);
        }
        catch (Exception e)
        {
            
        }
        thread.interrupt ();
        return rspTmp;
    }
    
    @Override
    public boolean receiveStart ()
    {
        return game.askForStart ();
    }
    
    @Override
    public boolean receiveSueForPeace ()
    {
        rspTmp = false;
        Thread thread = new Thread (() -> {rspTmp = game.askForSue ();});
        thread.start ();
        try
        {
            thread.join (3000);
        }
        catch (Exception e)
        {
        
        }
        thread.interrupt ();
        return rspTmp;
    }
    
    @Override
    public boolean receiveUndo ()
    {
        rspTmp = false;
        Thread thread = new Thread (() -> {rspTmp = game.askForUndo ();});
        thread.start ();
        try
        {
            thread.join (3000);
        }
        catch (Exception e)
        {
        
        }
        thread.interrupt ();
        return rspTmp;
    }
    
    @Override
    public void receiveQuit ()
    {
        super.receiveQuit ();
    }
}
