import java.awt.*;

/**
 * Created by Li Zeyan on 2016/7/23.
 */
abstract public class Player
{
    protected Reversi game;
    public Player (Reversi game)
    {
        this.game = game;
    }
    abstract public Point makingPolicy(long timeConstraint);
    abstract public boolean receiveGiveIn ();
    abstract public boolean receiveStart ();
    abstract public boolean receiveSueForPeace ();
    abstract public void receiveQuit ();
    abstract public boolean receiveUndo ();
}
