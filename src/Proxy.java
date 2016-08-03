import sun.plugin2.message.GetAppletMessage;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

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
    private Consumer<String> messageConsumer = null;
    private Consumer<String> infoConsumer = null;
    private Consumer<Long> timeConsumer = null;
    private boolean terminateWaitingSignal = false;
    private Point policyBuffer = null;
    private int undoRspBuffer = -1, sueRspBuffer = -1, giveInRspBuffer = -1, startRspBuffer = -1;
    public Proxy (Socket socket, Consumer<String> messageConsumer, Consumer<String> infoConsumer, Consumer<Long> timeConsumer) throws Exception
    {
        this.socket = socket;
        this.messageConsumer = messageConsumer;
        this.infoConsumer = infoConsumer;
        this.timeConsumer = timeConsumer;
        reader = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
        writer = new BufferedWriter (new OutputStreamWriter (socket.getOutputStream ()));
        Thread thread = new Thread (()->
        {
            try
            {
                receive ();
            }
            catch (Exception e)
            {
            }
        });
        thread.setDaemon (true);
        thread.start ();
        thread.yield ();
    }
    public boolean isServer ()
    {
        return serverSocket != null;
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
            terminateWaitingSignal = true;
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
        else if (key.equals ("MESSAGE"))
        {
            writer.write ("5 " + value);
        }
        else if (key.equals ("INFO"))
        {
            writer.write ("6 " + value);
        }
        else if (key.equals ("TIME"))
        {
            writer.write ("7 " + value);
        }
        else if (key.equals ("START"))
        {
            writer.write ("8 " + value);
        }
        else if (key.equals ("CLOSE"))
        {
            writer.write ("9 " + value);
        }
        writer.newLine ();
        writer.flush ();
    }
    public void receive () throws Exception
    {
        String line;
        int type;
        while (true)
        {
            try
            {
                line = reader.readLine ();
            }
            catch (Exception e)
            {
                continue;
            }
            Scanner scanner = new Scanner (line);
            type = scanner.nextInt ();
            if (type == 1)
            {
                policyBuffer = new Point (scanner.nextInt (), scanner.nextInt ());
            }
            else if (type == 2)
            {
                if (!scanner.hasNextInt ())
                {
                    send ("GIVEIN", localPlayer.receiveGiveIn ()? "1":"0");
                    terminateWaitingSignal = true;
                }
                else
                    giveInRspBuffer = scanner.nextInt ();
            }
            else if (type == 3)
            {
                if (!scanner.hasNextInt ())
                {
                    send ("SUE", localPlayer.receiveSueForPeace ()? "1":"0");
                    terminateWaitingSignal = true;
                }
                else
                    sueRspBuffer = scanner.nextInt ();
            }
            else if (type == 4)
            {
                if (!scanner.hasNextInt ())
                {
                    send ("UNDO", localPlayer.receiveUndo ()? "1":"0");
                }
                else
                    undoRspBuffer = scanner.nextInt ();
            }
            else if (type == 5)
            {
                messageConsumer.accept (scanner.nextLine ());
            }
            else if (type == 6)
            {
                infoConsumer.accept (scanner.nextLine ());
            }
            else if (type == 7)
            {
                timeConsumer.accept (scanner.nextLong ());
            }
            else if (type == 8)
            {
                if (!scanner.hasNextInt ())
                {
                    send ("START", localPlayer.receiveStart ()? "1":"0");
                }
                else
                    startRspBuffer = scanner.nextInt ();
            }
            else if (type == 9)
            {
                localPlayer.receiveQuit ();
                terminateWaitingSignal = true;
                return;
            }
            else
            {
                continue;
            }
        }
    }
    public Point waitForPolicy (long timeConstraint) throws Exception
    {
        long limit = System.currentTimeMillis () + timeConstraint;
        while (policyBuffer == null && System.currentTimeMillis () <= limit && terminateWaitingSignal == false)
        {
            Thread.sleep (timeConstraint / 1000);
        }
        if (terminateWaitingSignal = true)
            terminateWaitingSignal = false;
        Point policy = null;
        if (policyBuffer != null)
            policy = new Point (policyBuffer.x, policyBuffer.y);
        policyBuffer = null;
        return policy;
    }
    public boolean waitForSueRsp () throws Exception
    {
        while (sueRspBuffer == -1)
        {
            Thread.sleep (100);
        }
        boolean ret = (sueRspBuffer == 1);
        sueRspBuffer = -1;
        return ret;
    }
    public boolean waitForStartRsp () throws Exception
    {
        while (startRspBuffer == -1)
        {
            Thread.sleep (100);
        }
        boolean ret = (startRspBuffer == 1);
        startRspBuffer = -1;
        return ret;
    }
    public boolean waitForGiveInRsp () throws Exception
    {
        while (giveInRspBuffer == -1)
        {
            Thread.sleep (100);
        }
        boolean ret = (giveInRspBuffer == 1);
        giveInRspBuffer = -1;
        return ret;
    }
    public boolean waitForUndoRsp () throws Exception
    {
        while (undoRspBuffer == -1)
        {
            Thread.sleep (100);
        }
        boolean ret = (undoRspBuffer == 1);
        undoRspBuffer = -1;
        return ret;
    }
    
}
