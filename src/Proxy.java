import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Li Zeyan on 2016/7/27.
 */
public class Proxy
{
    private Socket socket;
    private ServerSocket serverSocket = null;
    private Player localPlayer;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
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
    public void setServerSocket (ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
    }
    public void close ()
    {
        try
        {
            reader.close ();
            writer.close ();
            socket.close ();
            serverSocket.close ();
        }
        catch (Exception e)
        {
            
        }
    }
    public void send (String key, String value) throws Exception
    {
        if (value == null)
            value = "";
        if (key.equals ("POLICY"))
        {
            writer.write ("1 " + value);
        }
        else if (key.equals ("GIVEIN"))
        {
            writer.write ("2 " + value);
        }
        else if (key.equals ("SUE"))
        {
            writer.write ("3 " + value);
        }
        else if (key.equals ("UNDO"))
        {
            writer.write ("4 " + value);
        }
        writer.newLine ();
        writer.flush ();
    }
    public Point waitForPolicy (long timeConstraint) throws Exception
    {
        long startTime = System.currentTimeMillis ();
        while (true)
        {
            String line = reader.readLine ();
            Scanner scanner = new Scanner (line);
            int type = scanner.nextInt ();
            System.out.println (line);
            if (type == 1)
                return new Point (scanner.nextInt (), scanner.nextInt ());
            else if (type == 2)
            {
                boolean ret = localPlayer.receiveGiveIn ();
                send ("GIVEIN", ret? "1" : "0");
                if (ret)
                    break;
            }
            else if (type == 3)
            {
                boolean ret = localPlayer.receiveSueForPeace ();
                send ("SUE", ret? "1": "0");
                if (ret)
                    break;
            }
            else if (type == 4)
            {
                boolean ret = localPlayer.receiveUndo ();
                send ("UNDO", ret? "1": "0");
            }
        }
        return null;
    }
    public boolean waitForSueRsp () throws Exception
    {
        while (true)
        {
            String line = reader.readLine ();
            Scanner scanner = new Scanner (line);
            int type = scanner.nextInt ();
            if (type == 3)
            {
                return scanner.nextInt () == 1;
            }
        }
    }
    public boolean waitForGiveInRsp () throws Exception
    {
        while (true)
        {
            String line = reader.readLine ();
            Scanner scanner = new Scanner (line);
            int type = scanner.nextInt ();
            if (type == 2)
            {
                return scanner.nextInt () == 1;
            }
        }
    }
    public boolean waitForStartRsp ()
    {
    
        return true;
    }
    public boolean waitForUndoRsp () throws Exception
    {
        while (true)
        {
            String line = reader.readLine ();
            Scanner scanner = new Scanner (line);
            int type = scanner.nextInt ();
            if (type == 4)
            {
                return scanner.nextInt () == 1;
            }
        }
    }
    
}
