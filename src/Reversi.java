import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Li Zeyan on 2016/7/22.
 */
public class Reversi extends JFrame implements ActionListener
{
    private ChessBoard chessBoard;
    private JMenuBar menuBar;
    private JMenu localMenu, onlineMenu, operateMenu, generalMenu;
    private JMenuItem startLocalGameItem, saveGameItem, loadLocalGameItem, startOnlineGameItem, undoItem, giveInItem, peaceItem, settingItem, helpItem, aboutItem;
    private Composition composition = null;
    private Player[] players;
    private long timeConstraintPerStep = 20000;
    private Object[] colorRoleOption;
    private Object[] tcpRoleOptions;
    private Object[] agreementOptions;
    private Composition.STATUS meStatus = Composition.STATUS.EMPTY;
    private Proxy proxy = null;
    private Composition.STATUS terminateWinner = Composition.STATUS.EMPTY;
    private boolean terminateSignal = false;
    public static final class SecurityKey {private SecurityKey () {} }
    private static SecurityKey securityKey = new SecurityKey ();
    public Reversi (String name)
    {
        super(name);
        colorRoleOption = new Object[2];
        colorRoleOption[0] = ("BLACK");
        colorRoleOption[1] = ("WHITE");
        tcpRoleOptions = new Object[2];
        tcpRoleOptions[0] = "Server";
        tcpRoleOptions[1] = "Client";
        agreementOptions = new Object[2];
        agreementOptions[0] = "YES";
        agreementOptions[1] = "NO";
        chessBoard = new ChessBoard(composition = new Composition ());
        setContentPane(chessBoard);
        initMenu ();
        initialize ();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }
    public static void main (String[] argv)
    {
        Reversi reversi = new Reversi("Reversi v2.1");
        reversi.setVisible(true);
    }
    public boolean getTerminateSignal ()
    {
        return terminateSignal;
    }
    public void actionPerformed (ActionEvent event)
    {
        Object source = event.getSource ();
        if (source == startLocalGameItem)
            startLocalGame ();
        else if (source == saveGameItem)
            saveLocalGame ();
        else if (source == loadLocalGameItem)
            loadLocalGame ();
        else if (source == startOnlineGameItem)
            startOnlineGame ();
        else if (source == undoItem)
            undo ();
        else if (source == giveInItem)
            giveIn ();
        else if (source == peaceItem)
            peace ();
        else if (source == settingItem)
            setting ();
        else if (source == helpItem)
            help ();
        else if (source == aboutItem)
            about ();
    }
    private void initMenu ()
    {
        menuBar = new JMenuBar ();
        
        localMenu = new JMenu ("Local");
        startLocalGameItem = new JMenuItem ("Start");
        startLocalGameItem.addActionListener (this);
        loadLocalGameItem = new JMenuItem ("Load");
        loadLocalGameItem.addActionListener (this);
        localMenu.add (startLocalGameItem);
        localMenu.add (loadLocalGameItem);
        menuBar.add (localMenu);
        
        onlineMenu = new JMenu ("Online");
        startOnlineGameItem = new JMenuItem ("Start");
        startOnlineGameItem.addActionListener (this);
        onlineMenu.add (startOnlineGameItem);
        menuBar.add (onlineMenu);
        
        operateMenu = new JMenu ("Operate");
        undoItem = new JMenuItem ("Undo");
        undoItem.addActionListener (this);
        saveGameItem = new JMenuItem ("Save");
        saveGameItem.addActionListener (this);
        giveInItem = new JMenuItem ("Give In");
        giveInItem.addActionListener (this);
        peaceItem = new JMenuItem ("Sue For Peace");
        peaceItem.addActionListener (this);
        operateMenu.add (undoItem);
        operateMenu.add (giveInItem);
        operateMenu.add (peaceItem);
        operateMenu.add (saveGameItem);
        menuBar.add (operateMenu);
        
        generalMenu = new JMenu ("General");
        settingItem = new JMenuItem ("Setting");
        settingItem.addActionListener (this);
        helpItem = new JMenuItem ("Help");
        helpItem.addActionListener (this);
        aboutItem = new JMenuItem ("About");
        aboutItem.addActionListener (this);
        generalMenu.add (settingItem);
        generalMenu.add (helpItem);
        generalMenu.add (aboutItem);
        menuBar.add (generalMenu);
        
        this.setJMenuBar (menuBar);
    }
    private void startLocalGame ()
    {
        int response = JOptionPane.showOptionDialog (this, "Choose Your Role, Black is always the first", "Choosing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, colorRoleOption, null);
        if (response == 1)
            meStatus = Composition.STATUS.WHITE;
        else
            meStatus = Composition.STATUS.BLACK;
        players = new Player[2];
        players[0] = new LocalMePlayer (chessBoard, this);
        players[1] = new LocalMachinePlayer (composition, this);
        composition.initializeBoard (securityKey);
        composition.setLastStatus (securityKey, Composition.STATUS.WHITE);
        repaint ();
        Thread thread = new Thread (()->
        {
            int index = 0;
            if (meStatus == Composition.STATUS.WHITE)
                index = 1;
            gameOn (index);
        });
        thread.start ();
        thread.yield ();
    }
    private void loadLocalGame ()
    {
        BufferedReader reader;
        try
        {
            File file = showRCFileDialog (JFileChooser.OPEN_DIALOG);
            if (file == null)
                return;
            reader = new BufferedReader (new FileReader (file));
            Composition.STATUS[][] board = new Composition.STATUS[composition.getWidth ()][composition.getHeight ()];
            for (int i = 0; i < composition.getWidth (); ++i)
            {
                for (int j = 0; j < composition.getHeight (); ++j)
                {
                    board[i][j] = Composition.str2status (reader.readLine ());
                }
            }
            Composition.STATUS lastStatus = Composition.str2status (reader.readLine ());
            repaint ();
            meStatus = Composition.str2status (reader.readLine ());
            if (lastStatus == Composition.STATUS.EMPTY || meStatus == Composition.STATUS.EMPTY)
            {
                throw new RuntimeException ("WRONG status in a composition");
            }
            reader.close ();
            composition.setLastStatus (securityKey, lastStatus);
            composition.setBoard (securityKey, board);
            System.out.println (lastStatus);
            System.out.println (meStatus);
            players = new Player[2];
            players[0] = new LocalMePlayer (chessBoard, this);
            players[1] = new LocalMachinePlayer (composition, this);
            int index = 0;
            if (lastStatus == meStatus)
                index = 1;
            int idx = index;
            Thread thread = new Thread (()->
            {
                gameOn (idx);
            });
            thread.start ();
            thread.yield ();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog (this, "Load file Failed", "WARNING", JOptionPane.ERROR_MESSAGE);
            initialize ();
            return;
        }
        
    }
    private void saveLocalGame ()
    {
        BufferedWriter writer = null;
        try
        {
            File file = showRCFileDialog (JFileChooser.SAVE_DIALOG);
            if (file == null)
                return;
            writer = new BufferedWriter (new PrintWriter (file));
            Composition.STATUS[][] board = composition.getBoard ();
            for (int i = 0; i < composition.getWidth (); ++i)
            {
                for (int j = 0; j < composition.getHeight (); ++j)
                {
                    writer.write (Composition.status2str(board[i][j]));
                    writer.newLine ();
                }
            }
            writer.write (Composition.status2str (composition.getLastStatus ()));
            writer.newLine ();
            writer.write (Composition.status2str (meStatus));
            writer.newLine ();
            writer.close ();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog (this, "Save file Failed", "WARNING", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    private File showRCFileDialog (int mode) throws Exception
    {
        JFileChooser fileChooser = new JFileChooser ("./");
        fileChooser.setAcceptAllFileFilterUsed (false);
        fileChooser.setFileFilter (new FileNameExtensionFilter ("Reversi Composition", "rc"));
        if (mode == JFileChooser.SAVE_DIALOG)
        {
            fileChooser.showSaveDialog (this);
        }
        else if (mode == JFileChooser.OPEN_DIALOG)
        {
            fileChooser.showOpenDialog (this);
        }
        else
            throw new IllegalArgumentException ("Wrong rc file chooser mode:" + mode);
        return fileChooser.getSelectedFile ();
    }
    private void startOnlineGame ()
    {
        int response = JOptionPane.showOptionDialog (this, "Choose your role", "CHOOSE", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, tcpRoleOptions, tcpRoleOptions[0]);
        if (response == 0)
        {
            String portStr = JOptionPane.showInputDialog (this, "Input Port that will be listened to", "PORT", JOptionPane.INFORMATION_MESSAGE);
            int port = Integer.parseInt (portStr);
            try
            {
                ServerSocket serverSocket = new ServerSocket (port);
                Socket client = serverSocket.accept ();
                proxy = new Proxy (client);
                proxy.setServerSocket (serverSocket);
                meStatus = Composition.STATUS.BLACK;
            }
            catch (Exception e)
            {
                terminate ("Connection Failed: " + e.getMessage ());
                return;
            }
        }
        else
        {
            String ipStr = JOptionPane.showInputDialog (this, "Input the address of Server", "IP", JOptionPane.INFORMATION_MESSAGE);
            String portStr = JOptionPane.showInputDialog (this, "Input the port of Server", "PORT", JOptionPane.INFORMATION_MESSAGE);
            int port = Integer.parseInt (portStr);
            try
            {
                Socket server = new Socket (InetAddress.getByName (ipStr), port);
                proxy = new Proxy (server);
                meStatus = Composition.STATUS.WHITE;
            }
            catch (Exception e)
            {
                terminate ("Connection Failed: " + e.getMessage ());
                return;
            }
        }
        OnlineEnemyPlayer enemyPlayer = new OnlineEnemyPlayer (this);
        OnlineMePlayer mePlayer = new OnlineMePlayer (chessBoard, this);
        mePlayer.setProxy (proxy);
        enemyPlayer.setProxy (proxy);
        players = new Player[2];
        players[0] = mePlayer;
        players[1] = enemyPlayer;
        proxy.setLocalPlayer (players[0]);
        composition.initializeBoard (securityKey);
        composition.setLastStatus (securityKey, Composition.STATUS.BLACK);
        Thread thread = new Thread (()->{gameOn (response);});
        thread.start ();
        thread.yield ();
    }
    private void undo ()
    {
        if (proxy != null)
        {
            try
            {
                proxy.send ("UNDO", null);
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog (this, "Send request failed", "WARNING", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (players[1].receiveUndo ())
        {
            composition.backward (securityKey, 2);
            repaint ();
        }
        else
        {
            JOptionPane.showMessageDialog (this, "The request is rejected.", "INFO", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void giveIn ()
    {
        if (proxy != null)
        {
            try
            {
                proxy.send ("GIVEIN", null);
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog (this, "Send request failed", "WARNING", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (players[1].receiveGiveIn ())
        {
            terminateSignal = true;
            terminateWinner = Composition.reverseStatus (meStatus);
        }
        else
        {
            JOptionPane.showMessageDialog (this, "The request is rejected.", "INFO", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void peace ()
    {
        if (proxy != null)
        {
            try
            {
                proxy.send ("SUE", null);
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog (this, "Send request failed", "WARNING", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (players[1].receiveSueForPeace ())
        {
            terminateSignal = true;
            terminateWinner = Composition.STATUS.EMPTY;
        }
        else
        {
            JOptionPane.showMessageDialog (this, "The request is rejected.", "INFO", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void setting ()
    {
        
    }
    private void help ()
    {
        
    }
    private void about ()
    {
        
    }
    private void gameOn (int index)
    {
        operateMenu.setEnabled (true);
        startLocalGameItem.setEnabled (false);
        loadLocalGameItem.setEnabled (false);
        startOnlineGameItem.setEnabled (false);
        loadLocalGameItem.setEnabled (false);
        while (true)
        {
            Point policy = players[index].makingPolicy (timeConstraintPerStep);
            if (policy == null || terminateSignal)
            {
                showWinner (meStatus, terminateWinner);
                initialize ();
                return;
            }
            if (policy == null || !composition.set (securityKey, policy.x, policy.y))
                composition.dropOver (securityKey);
            chessBoard.repaint ();
            if (composition.getFinished ())
            {
                showWinner (meStatus, composition.getWinner ());
                initialize ();
                return;
            }
            ++index;
            index %= 2;
        }
    }
    private void initialize ()
    {
        players = null;
        if (proxy != null)
            proxy.close ();
        proxy = null;
        composition.cleanBoard (securityKey);
        chessBoard.shutdown ();
        meStatus = Composition.STATUS.EMPTY;
        terminateSignal= false;
        terminateWinner = Composition.STATUS.EMPTY;
        
        operateMenu.setEnabled (false);
        onlineMenu.setEnabled (true);
        localMenu.setEnabled (true);
        startLocalGameItem.setEnabled (true);
        loadLocalGameItem.setEnabled (true);
        startOnlineGameItem.setEnabled (true);
        
        repaint ();
    }
    private void showWinner (Composition.STATUS me, Composition.STATUS winner)
    {
        String msg, title;
        if (me == winner)
        {
            title = "WIN";
            msg = "你赢了";
        }
        else if (winner != Composition.STATUS.EMPTY)
        {
            title = "LOSE";
            msg = "你输了";
        }
        else
        {
            title = "PEACE";
            msg = "平局";
        }
        JOptionPane.showMessageDialog (this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }
    public void terminate (String msg)
    {
        JOptionPane.showMessageDialog (this, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
        initialize ();
    }
    public boolean askForGivein ()
    {
        boolean ret =  JOptionPane.showOptionDialog (this, "Would you accept your enemy's surrender?", "QUESITON", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, agreementOptions, agreementOptions[0]) == 0;
        if (ret)
        {
            terminateSignal = true;
            terminateWinner = meStatus;
        }
        return ret;
    }
    public boolean askForSue ()
    {
        boolean ret = JOptionPane.showOptionDialog (this, "Would you accept your enemy's sue for peace?", "QUESITON", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, agreementOptions, agreementOptions[0]) == 0;
        if (ret)
        {
            terminateSignal = true;
            terminateWinner = Composition.STATUS.EMPTY;
        }
        return ret;
    }
    public boolean askForUndo ()
    {
        boolean ret = JOptionPane.showOptionDialog (this, "Would you accept your enemy's undo request", "QUESITON", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, agreementOptions, agreementOptions[0]) == 0;
        if (ret)
        {
            composition.backward (securityKey, 2);
            repaint ();
        }
        return ret;
    }
}
