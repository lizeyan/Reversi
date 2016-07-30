import java.awt.*;

/**
 * Created by Li Zeyan on 2016/7/27.
 */
public class OnlineEnemyPlayer extends Player
{
    private Proxy proxy;
    public OnlineEnemyPlayer (Reversi game)
    {
        super(game);
        this.proxy = proxy;
    }
    public void setProxy (Proxy proxy)
    {
        this.proxy = proxy;
    }
    
    @Override
    public Point makingPolicy (long timeConstraint)
    {
        try
        {
            return proxy.waitForPolicy (timeConstraint);
        }
        catch (Exception e)
        {
            game.terminate (e.getMessage ());
            return null;
        }
    }
    
    @Override
    public boolean receiveGiveIn ()
    {
        try
        {
            return proxy.waitForGiveInRsp ();
        }
        catch (Exception e)
        {
            return true;
        }
    }
    
    @Override
    public boolean receiveStart ()
    {
        try
        {
            return proxy.waitForStartRsp ();
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    @Override
    public boolean receiveSueForPeace ()
    {
        try
        {
            return proxy.waitForSueRsp ();
        }
        catch (Exception e)
        {
            return true;
        }
    }
    
    @Override
    public void receiveQuit ()
    {
        //
    }
    @Override
    public boolean receiveUndo ()
    {
        try
        {
            return proxy.waitForUndoRsp ();
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
