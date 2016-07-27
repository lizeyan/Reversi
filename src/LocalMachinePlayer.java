import java.awt.*;

/**
 * Created by Li Zeyan on 2016/7/23.
 */
public class LocalMachinePlayer extends Player
{
    private Composition composition;
    public LocalMachinePlayer (Composition composition, Reversi game)
    {
        super (game);
        this.composition = composition;
    }
    
    @Override
    public Point makingPolicy (long timeConstraint)
    {
        if (!composition.queryAvailble ())
            return new Point (-1, -1);
        for (int i = 0; i < composition.getWidth (); ++i)
        {
            for (int j = 0; j < composition.getHeight (); ++j)
            {
                if (composition.queryAvailble (i, j))
                    return new Point (i, j);
            }
        }
        return new Point (-1, -1);
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
        
    }
}
