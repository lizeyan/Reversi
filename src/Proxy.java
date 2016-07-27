import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Li Zeyan on 2016/7/27.
 */
public class Proxy
{
    private Socket socket;
    private Player localPlayer;
    private int policyRmd = 1, undoRmd = 1, giveInRmd = 1, sueRmd = 1;
    private boolean me = false;
    private BufferedReader reader;
    private BufferedWriter writer;
    public Proxy (Socket socket)throws Exception
    {
        this.socket = socket;
        reader = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
        writer = new BufferedWriter (new OutputStreamWriter (socket.getOutputStream ()));
    }
    public void setLocalPlayer (Player localPlayer)
    {
        this.localPlayer = localPlayer;
    }
    public void startMeRound ()
    {
        resetRmd ();
        me = true;
    }
    public void startEnemyRound ()
    {
        resetRmd ();
        me = false;
    }
    private void resetRmd ()
    {
        policyRmd = 1;
        undoRmd = 1;
        giveInRmd = 1;
        sueRmd = 1;
    }
    public void send (String key, String value) throws Exception
    {
        if (key.equals ("POLICY"))
        {
            writer.write ("1 " + value);
            writer.newLine ();
        }
        writer.flush ();
    }
    public void receive ()
    {
        
    }
    public Point waitForPolicy (long timeConstraint) throws Exception
    {
        long startTime = System.currentTimeMillis ();
        while (true)
        {
            String line = reader.readLine ();
            Scanner scanner = new Scanner (line);
            int type = scanner.nextInt ();
            if (type != 1)
                continue;
            return new Point (scanner.nextInt (), scanner.nextInt ());
        }
    }
    public boolean waitForSueRsp ()
    {
        return true;
    }
    public boolean waitForGiveInRsp ()
    {
    
        return true;
    }
    public boolean waitForStartRsp ()
    {
    
        return true;
    }
    
}
