import java.awt.*;

/**
 * Created by Li Zeyan on 2016/7/23.
 */
public interface Player
{
    public Point makingPolicy(long timeConstraint);
    public boolean receiveGiveIn ();
    public boolean receiveStart ();
    public boolean receiveSueForPeace ();
    public void receiveQuit ();
}
