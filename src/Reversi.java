import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Li Zeyan on 2016/7/22.
 */
public class Reversi extends JFrame implements ActionListener
{
    private static SecurityKey securityKey = new SecurityKey ();
    private ChessBoard chessBoard;
    private NoticeBoard noticeBoard;
    private JMenuBar menuBar;
    private JMenu localMenu, onlineMenu, operateMenu, generalMenu;
    private JMenuItem startLocalGameItem, saveGameItem, loadLocalGameItem, startOnlineGameItem, undoItem, giveInItem, peaceItem, detachItem, settingItem, helpItem, aboutItem, connectItem, disconnectItem;
    private Composition composition = null;
    private Player[] players;
    private long timeConstraintPerStep = 20000;
    private Object[] colorRoleOption;
    private Object[] tcpRoleOptions;
    private Object[] agreementOptions;
    private Object[] localEnemyOptions;
    private Composition.STATUS meStatus = Composition.STATUS.EMPTY;
    private Proxy proxy = null;
    private Composition.STATUS terminateWinner = Composition.STATUS.EMPTY;
    private boolean terminateSignal = false;
    private SettingDialog settingDialog = null;
    private BackgroundImage backgroundImage = null;
    private boolean gameRunning = false;
    private Class aiClass;
    private String backgroundMusicName;
    private volatile Point policy = null;
    private Clip backgroundMusicClip = null;
    private long remoteTimeConstraint;
    private String myName = "LI ZEYAN";
    private String enemyName = "BetaCat";
    
    public Reversi (String name)
    {
        super (name);
        initOptions ();
        chessBoard = new ChessBoard (composition = new Composition (), this);
        chessBoard.setOpaque (false);
        noticeBoard = new NoticeBoard (this);
        noticeBoard.setOpaque (false);
        noticeBoard.appendMessage ("<h1 style=\"color:FF6600\">Welcome to Reversi</h1><br/>");
        backgroundImage = new BackgroundImage ("./resources/images/shanshui2.jpg");
        settingDialog = new SettingDialog (this);
        try
        {
            aiClass = Class.forName ("LocalMachinePlayer");
        } catch (Exception e)
        {
            System.out.println (e.getMessage ());
        }
        try
        {
            setIconImage (ImageIO.read (new File ("./resources/images/panda.png")));
        } catch (Exception e)
        {
            
        }
        setContentPane (backgroundImage);
        add (chessBoard, BorderLayout.CENTER);
        add (noticeBoard, BorderLayout.EAST);
        setMinimumSize (new Dimension (1280, 960));
        initMenu ();
        initialize ();
        addComponentListener (new ComponentListener ()
        {
            @Override
            public void componentResized (ComponentEvent e)
            {
                arrange ();
            }
            
            @Override
            public void componentMoved (ComponentEvent e)
            {
            }
            
            @Override
            public void componentShown (ComponentEvent e)
            {
            }
            
            @Override
            public void componentHidden (ComponentEvent e)
            {
            }
        });
        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        pack ();
        arrange ();
    }
    
    public static void main (String[] argv)
    {
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName ();
        try
        {
            UIManager.setLookAndFeel (lookAndFeel);
        } catch (Exception e)
        {
        }
        Reversi reversi = new Reversi ("Reversi v3.0");
        reversi.setVisible (true);
    }
    
    public String getBackgroundMusicName ()
    {
        return this.backgroundMusicName;
    }
    
    public void setBackgroundMusicName (String backgroundMusicName)
    {
        this.backgroundMusicName = backgroundMusicName;
    }
    
    private void arrange ()
    {
        int w = getContentPane ().getWidth () * 19 / 20, h = getContentPane ().getHeight () * 19 / 20;
        chessBoard.pack ();
        chessBoard.setBounds (0, 0, chessBoard.getWidth (), chessBoard.getHeight ());
        noticeBoard.setBounds (chessBoard.getWidth (), 0, w - chessBoard.getWidth (), h);
        noticeBoard.setSize (w - chessBoard.getWidth (), h);
        noticeBoard.pack ();
    }
    
    public ChessBoard getChessBoard ()
    {
        return this.chessBoard;
    }
    
    public Clip getBackgroundMusicClip ()
    {
        return this.backgroundMusicClip;
    }
    
    public void setBackgroundMusicClip (Clip backgroundMusicClip)
    {
        this.backgroundMusicClip = backgroundMusicClip;
    }
    
    public long getTimeConstraintPerStep ()
    {
        return this.timeConstraintPerStep;
    }
    
    public void setTimeConstraintPerStep (long timeConstraintPerStep)
    {
        this.timeConstraintPerStep = timeConstraintPerStep;
        if (proxy == null || proxy.isServer ())
        {
            noticeBoard.setTime (timeConstraintPerStep / 1000);
            if (proxy != null)
            {
                try
                {
                    proxy.send ("TIME", String.valueOf (timeConstraintPerStep));
                } catch (Exception e)
                {
                    
                }
            }
        }
        repaint ();
    }
    
    public String getMyName ()
    {
        return this.myName;
    }
    
    public void setMyName (String myName)
    {
        this.myName = myName;
        if (proxy != null)
        {
            try
            {
                proxy.send ("INFO", myName);
            } catch (Exception e)
            {
                
            }
        }
        noticeBoard.setName (myName, true);
        repaint ();
    }
    
    public String getEnemyName ()
    {
        return this.enemyName;
    }
    
    public void setEnemyName (String enemyName)
    {
        this.enemyName = enemyName;
        noticeBoard.setName (enemyName, false);
        repaint ();
    }
    
    public BackgroundImage getBackgroundImage ()
    {
        return backgroundImage;
    }
    
    public void setBackgroundImage (String name)
    {
        backgroundImage.setBackgroundImage (name);
        repaint ();
    }
    
    public boolean getTerminateSignal ()
    {
        return terminateSignal;
    }
    
    public LocalMachinePlayer getAiInstance ()
    {
        try
        {
            return (LocalMachinePlayer) aiClass.getConstructor (Composition.class).newInstance (composition);
        } catch (Exception e)
        {
            return null;
        }
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
        else if (source == connectItem)
            connect ();
        else if (source == disconnectItem)
            disconnect (true);
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
        else if (source == detachItem)
            detach (((JCheckBoxMenuItem) detachItem).getState ());
    }
    
    private void initMenu ()
    {
        menuBar = new JMenuBar ();
        Font font = new Font ("Microsoft yahei", Font.BOLD, 18);
        
        localMenu = new JMenu ("Local");
        localMenu.setFont (font);
        localMenu.setMnemonic (KeyEvent.VK_L);
        startLocalGameItem = new JMenuItem ("Start");
        startLocalGameItem.addActionListener (this);
        startLocalGameItem.setFont (font);
        startLocalGameItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        loadLocalGameItem = new JMenuItem ("Load");
        loadLocalGameItem.addActionListener (this);
        loadLocalGameItem.setFont (font);
        loadLocalGameItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        localMenu.add (startLocalGameItem);
        localMenu.add (loadLocalGameItem);
        menuBar.add (localMenu);
        
        onlineMenu = new JMenu ("Online");
        onlineMenu.setFont (font);
        onlineMenu.setMnemonic (KeyEvent.VK_O);
        startOnlineGameItem = new JMenuItem ("Start");
        startOnlineGameItem.addActionListener (this);
        startOnlineGameItem.setEnabled (false);
        startOnlineGameItem.setFont (font);
        startOnlineGameItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_N, KeyEvent.SHIFT_MASK));
        connectItem = new JMenuItem ("Connect");
        connectItem.setFont (font);
        connectItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_O, KeyEvent.SHIFT_MASK));
        disconnectItem = new JMenuItem ("Disconnect");
        disconnectItem.setFont (font);
        connectItem.addActionListener (this);
        disconnectItem.addActionListener (this);
        disconnectItem.setEnabled (false);
        disconnectItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_W, KeyEvent.SHIFT_MASK));
        onlineMenu.add (connectItem);
        onlineMenu.add (disconnectItem);
        onlineMenu.add (startOnlineGameItem);
        menuBar.add (onlineMenu);
        
        operateMenu = new JMenu ("Operate");
        operateMenu.setFont (font);
        operateMenu.setMnemonic (KeyEvent.VK_P);
        undoItem = new JMenuItem ("Undo");
        undoItem.setFont (font);
        undoItem.addActionListener (this);
        undoItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
        giveInItem = new JMenuItem ("Give In");
        giveInItem.setFont (font);
        giveInItem.addActionListener (this);
        giveInItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_U, KeyEvent.CTRL_MASK));
        peaceItem = new JMenuItem ("Sue For Peace");
        peaceItem.setFont (font);
        peaceItem.addActionListener (this);
        giveInItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_E, KeyEvent.CTRL_MASK));
        detachItem = new JCheckBoxMenuItem ("Deatch");
        detachItem.addActionListener (this);
        detachItem.setFont (font);
        detachItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_D, KeyEvent.CTRL_MASK));
        operateMenu.add (undoItem);
        operateMenu.add (giveInItem);
        operateMenu.add (peaceItem);
        operateMenu.add (detachItem);
        menuBar.add (operateMenu);
        
        generalMenu = new JMenu ("General");
        generalMenu.setFont (font);
        generalMenu.setMnemonic (KeyEvent.VK_G);
        settingItem = new JMenuItem ("Setting");
        settingItem.addActionListener (this);
        settingItem.setFont (font);
        settingItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_H, KeyEvent.CTRL_MASK));
        helpItem = new JMenuItem ("Help");
        helpItem.addActionListener (this);
        helpItem.setFont (font);
        aboutItem = new JMenuItem ("About");
        aboutItem.addActionListener (this);
        aboutItem.setFont (font);
        aboutItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_M, KeyEvent.CTRL_MASK));
        saveGameItem = new JMenuItem ("Save");
        saveGameItem.addActionListener (this);
        saveGameItem.setFont (font);
        saveGameItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        generalMenu.add (settingItem);
        generalMenu.addSeparator ();
        generalMenu.add (saveGameItem);
        generalMenu.addSeparator ();
//        generalMenu.add (helpItem);
        generalMenu.add (aboutItem);
        menuBar.add (generalMenu);
        
        this.setJMenuBar (menuBar);
    }
    
    private void connect ()
    {
        int response = JOptionPane.showOptionDialog (this, "Choose your role", "CHOOSE", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, tcpRoleOptions, tcpRoleOptions[0]);
        if (response == 0)
        {
            String portStr = JOptionPane.showInputDialog (this, "Input Port that will be listened to", "PORT", JOptionPane.INFORMATION_MESSAGE);
            if (portStr == null)
                return;
            ServerSocket serverSocket = null;
            try
            {
                int port = Integer.parseInt (portStr);
                serverSocket = new ServerSocket (port);
                serverSocket.setSoTimeout (10000);
                Socket client = serverSocket.accept ();
                noticeBoard.appendMessage ("<p style=\"color:green\">Connected.</p><br/>");
                proxy = new Proxy (client,
                        msg ->
                        {
                            noticeBoard.appendMessage (msg + "<br/>");
                        },
                        ename ->
                        {
                            setEnemyName (ename);
                        },
                        time ->
                        {
                            remoteTimeConstraint = time;
                            noticeBoard.setTime (remoteTimeConstraint / 1000);
                        });
                proxy.setServerSocket (serverSocket);
                proxy.send ("INFO", myName);
                proxy.send ("TIME", String.valueOf (timeConstraintPerStep));
                meStatus = Composition.STATUS.BLACK;
            } catch (Exception e)
            {
                if (serverSocket != null)
                {
                    try
                    {
                        serverSocket.close ();
                    } catch (Exception se)
                    {
                        
                    }
                }
                terminate ("Connection Failed: " + e.getMessage ());
                return;
            }
        } else
        {
            String ipStr = JOptionPane.showInputDialog (this, "Input the address of Server", "IP", JOptionPane.INFORMATION_MESSAGE);
            if (ipStr == null)
                return;
            String portStr = JOptionPane.showInputDialog (this, "Input the port of Server", "PORT", JOptionPane.INFORMATION_MESSAGE);
            if (portStr == null)
                return;
            try
            {
                int port = Integer.parseInt (portStr);
                Socket server = new Socket (InetAddress.getByName (ipStr), port);
                noticeBoard.appendMessage ("<p style=\"color:green\">Connected.</p><br/>");
                proxy = new Proxy (server,
                        msg ->
                        {
                            noticeBoard.appendMessage (msg);
                        },
                        ename ->
                        {
                            setEnemyName (ename);
                        },
                        time ->
                        {
                            remoteTimeConstraint = time;
                            noticeBoard.setTime (remoteTimeConstraint / 1000);
                        });
                proxy.send ("INFO", myName);
                meStatus = Composition.STATUS.WHITE;
            } catch (Exception e)
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
        connectItem.setEnabled (false);
        disconnectItem.setEnabled (true);
        localMenu.setEnabled (false);
        startOnlineGameItem.setEnabled (true);
        settingDialog.lock ();
    }
    
    public void disconnect (boolean inform)
    {
        if (gameRunning)
            terminateSignal = true;
        if (inform)
        {
            try
            {
                terminateWinner = Composition.reverseStatus (meStatus);
                proxy.send ("CLOSE", null);
            } catch (Exception e)
            {
            }
        } else
            terminateWinner = meStatus;
        if (proxy != null)
            proxy.close ();
        noticeBoard.appendMessage ("<p style=\"color:FF00CC\">Disconnected.</p><br/>");
        noticeBoard.setTime (timeConstraintPerStep / 1000);
        noticeBoard.setName ("", false);
        proxy = null;
        connectItem.setEnabled (true);
        disconnectItem.setEnabled (false);
        localMenu.setEnabled (true);
        startOnlineGameItem.setEnabled (false);
        settingDialog.unlock ();
    }
    
    private void startLocalGame ()
    {
        int rsp = askForLocalEnemy ();
        players = new Player[2];
        players[0] = new LocalMePlayer (chessBoard, this);
        if (rsp == 0)
        {
            meStatus = Composition.STATUS.EMPTY;
            players[1] = new LocalMePlayer (chessBoard, this);
        } else
        {
            int response = JOptionPane.showOptionDialog (this, "Choose Your Role, Black is always the first", "Choosing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, colorRoleOption, null);
            if (response == 1)
                meStatus = Composition.STATUS.WHITE;
            else
                meStatus = Composition.STATUS.BLACK;
            try
            {
                players[1] = getAiInstance ();
            } catch (Exception e)
            {
                terminate (e.getMessage ());
                return;
            }
        }
        composition.initializeBoard (securityKey);
        composition.setLastStatus (securityKey, Composition.STATUS.WHITE);
        enemyName = "BetaCat";
        repaint ();
        Thread thread = new Thread (() ->
        {
            int index = 0;
            if (meStatus == Composition.STATUS.WHITE)
                index = 1;
            gameOn (index);
        });
        thread.start ();
        thread.yield ();
        onlineMenu.setEnabled (false);
        startLocalGameItem.setEnabled (false);
        loadLocalGameItem.setEnabled (false);
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
            int rsp = askForLocalEnemy ();
            if (rsp != 0 && meStatus == Composition.STATUS.EMPTY)
            {
                meStatus = JOptionPane.showOptionDialog (this, "Choose Role", "CHOOSING", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, colorRoleOption, colorRoleOption[0]) == 0 ? Composition.STATUS.BLACK : Composition.STATUS.WHITE;
            }
            if (lastStatus == Composition.STATUS.EMPTY)
            {
                throw new RuntimeException ("WRONG status in a composition");
            }
            reader.close ();
            composition.setLastStatus (securityKey, lastStatus);
            composition.setBoard (securityKey, board);
            players = new Player[2];
            players[0] = new LocalMePlayer (chessBoard, this);
            if (rsp == 0)
                players[1] = new LocalMePlayer (chessBoard, this);
            else
            {
                try
                {
                    players[1] = getAiInstance ();
                } catch (Exception e)
                {
                    terminate (e.getMessage ());
                    return;
                }
            }
            int index = 0;
            if (lastStatus == meStatus)
                index = 1;
            int idx = index;
            Thread thread = new Thread (() ->
            {
                gameOn (idx);
            });
            thread.start ();
            thread.yield ();
            onlineMenu.setEnabled (false);
            startLocalGameItem.setEnabled (false);
            loadLocalGameItem.setEnabled (false);
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog (this, "Load file Failed", "WARNING", JOptionPane.ERROR_MESSAGE);
            initialize ();
            return;
        }
        
    }
    
    public void setAiClass (Class tmp)
    {
        try
        {
            LocalMachinePlayer player = (LocalMachinePlayer) tmp.getConstructor (Composition.class).newInstance (composition);
            aiClass = tmp;
            noticeBoard.appendMessage ("New Ai Class:" + tmp.getName () + "<br/>");
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog (this, "Set new Ai class Failed:" + e.getMessage (), "ERROR", JOptionPane.ERROR_MESSAGE);
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
                    writer.write (Composition.status2str (board[i][j]));
                    writer.newLine ();
                }
            }
            writer.write (Composition.status2str (composition.getLastStatus ()));
            writer.newLine ();
            writer.write (Composition.status2str (meStatus));
            writer.newLine ();
            writer.close ();
        } catch (Exception e)
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
        } else if (mode == JFileChooser.OPEN_DIALOG)
        {
            fileChooser.showOpenDialog (this);
        } else
            throw new IllegalArgumentException ("Wrong rc file chooser mode:" + mode);
        return fileChooser.getSelectedFile ();
    }
    
    private void startOnlineGame ()
    {
        try
        {
            proxy.send ("START", null);
        } catch (Exception e)
        {
            return;
        }
        if (!players[1].receiveStart ())
        {
            JOptionPane.showMessageDialog (this, "Start request is rejected", "INFO", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        composition.initializeBoard (securityKey);
        composition.setLastStatus (securityKey, Composition.STATUS.WHITE);
        meStatus = Composition.STATUS.BLACK;
        Thread thread = new Thread (() ->
        {
            gameOn (0);
        });
        thread.start ();
        thread.yield ();
        startOnlineGameItem.setEnabled (false);
    }
    
    private void undo ()
    {
        if (proxy != null)
        {
            try
            {
                proxy.send ("UNDO", null);
            } catch (Exception e)
            {
                JOptionPane.showMessageDialog (this, "Send request failed", "WARNING", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (players[1].receiveUndo ())
        {
            backward ();
            repaint ();
        } else
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
            } catch (Exception e)
            {
                JOptionPane.showMessageDialog (this, "Send request failed", "WARNING", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (players[1].receiveGiveIn ())
        {
            terminateSignal = true;
            if (meStatus == Composition.STATUS.EMPTY)
                terminateWinner = composition.getLastStatus ();
            else
                terminateWinner = Composition.reverseStatus (meStatus);
        } else
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
            } catch (Exception e)
            {
                JOptionPane.showMessageDialog (this, "Send request failed", "WARNING", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (players[1].receiveSueForPeace ())
        {
            terminateSignal = true;
            terminateWinner = Composition.STATUS.EMPTY;
        } else
        {
            JOptionPane.showMessageDialog (this, "The request is rejected.", "INFO", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void detach (boolean on)
    {
        ((LocalMePlayer) players[0]).detach (on);
    }
    
    private void setting ()
    {
        if (settingDialog == null)
            settingDialog = new SettingDialog (this);
        settingDialog.setModal (true);
        settingDialog.setVisible (true);
    }
    
    private void help ()
    {
        
    }
    
    private void about ()
    {
        JOptionPane.showMessageDialog (this, "This is a online Revesi game.\nAuthor: zy-li14\nRepo:git@github.com:lizeyan/Reversi.git", "About", JOptionPane.INFORMATION_MESSAGE, new ImageIcon ("./resources/images/about.png"));
    }
    
    private void gameOn (int index)
    {
        settingDialog.lock ();
        gameRunning = true;
        noticeBoard.setStatus (meStatus);
        noticeBoard.setName (myName, true);
        noticeBoard.setName (enemyName, false);
        operateMenu.setEnabled (true);
        startLocalGameItem.setEnabled (false);
        loadLocalGameItem.setEnabled (false);
        startOnlineGameItem.setEnabled (false);
        loadLocalGameItem.setEnabled (false);
        saveGameItem.setEnabled (true);
        long tc = timeConstraintPerStep;
        if (proxy != null && proxy.isServer () == false)
            tc = remoteTimeConstraint;
        while (true)
        {
            if (!composition.queryAvailble ())
            {
                noticeBoard.appendMessage ("<p style=\"color:6600CC\">WARNING: no available position now</p><br/>");
            }
            if (meStatus == Composition.STATUS.EMPTY)
                noticeBoard.setPieces (composition.queryNumber (Composition.STATUS.BLACK), composition.queryNumber (Composition.STATUS.WHITE));
            else
            {
                noticeBoard.setPieces (composition.queryNumber (meStatus), composition.queryNumber (Composition.reverseStatus (meStatus)));
                if (index == 0)
                    operateMenu.setEnabled (true);
                else
                    operateMenu.setEnabled (false);
            }
            noticeBoard.timerOn ();
            noticeBoard.setTime (tc / 1000);
            policy = null;
            int indexTmp = index;
            long tcTmp = tc;
            Thread thread = new Thread (() ->
            {
                policy = players[indexTmp].makingPolicy (tcTmp);
            });
            thread.start ();
            try
            {
                thread.join (tc);
            } catch (Exception e)
            {
                terminate (e.getMessage ());
            }
            noticeBoard.timerOff ();
            if (terminateSignal)
            {
                showWinner (meStatus, terminateWinner);
                initialize ();
                return;
            }
            if (policy == null || !composition.set (securityKey, policy.x, policy.y))
            {
                if (composition.queryAvailble ())
                {
                    try
                    {
                        policy = composition.setRandom (securityKey);
                        noticeBoard.appendMessage (Composition.status2str (composition.getLastStatus ()) + ":" + (char) ('A' + policy.x) + policy.y + "<br/>");
                    } catch (Exception e)
                    {
                    }
                } else
                {
                    composition.dropOver (securityKey);
                    try
                    {
                        noticeBoard.appendMessage (Composition.status2str (composition.getLastStatus ()) + ": drop<br/>");
                    } catch (Exception e)
                    {
                        terminate (e.getMessage ());
                    }
                }
            } else
            {
                try
                {
                    noticeBoard.appendMessage (Composition.status2str (composition.getLastStatus ()) + ":" + (char) ('A' + policy.x) + policy.y + "<br/>");
                } catch (Exception e)
                {
                    terminate (e.getMessage ());
                }
            }
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
        if (proxy == null)
        {
            settingDialog.unlock ();
        }
        gameRunning = false;
        composition.cleanBoard (securityKey);
        chessBoard.shutdown ();
        meStatus = Composition.STATUS.EMPTY;
        terminateSignal = false;
        terminateWinner = Composition.STATUS.EMPTY;
        
        operateMenu.setEnabled (false);
        onlineMenu.setEnabled (true);
        loadLocalGameItem.setEnabled (true);
        startLocalGameItem.setEnabled (true);
        saveGameItem.setEnabled (false);
        if (proxy != null)
            startOnlineGameItem.setEnabled (true);
        ((JCheckBoxMenuItem) detachItem).setState (false);
        if (proxy != null)
            detach (false);
        
        noticeBoard.setStatus (Composition.STATUS.EMPTY);
        noticeBoard.setPieces (0, 0);
        noticeBoard.setName (myName, true);
        if (proxy == null)
            noticeBoard.setName ("      ", false);
        else
            noticeBoard.setName (enemyName, false);
        if (proxy == null)
            noticeBoard.setTime (timeConstraintPerStep / 1000);
        else
            noticeBoard.setTime (remoteTimeConstraint / 1000);
        noticeBoard.timerOff ();
        
        repaint ();
    }
    
    private void showWinner (Composition.STATUS me, Composition.STATUS winner)
    {
        String msg = null, title = null;
        if (winner == Composition.STATUS.EMPTY)
        {
            title = "PEACE";
            msg = "平局";
            playMusic ("./resources/end.wav");
        } else if (me == Composition.STATUS.EMPTY)
        {
            title = "END";
            try
            {
                msg = Composition.status2str (winner) + " WIN";
            } catch (Exception e)
            {
                
            }
            playMusic ("./resources/win.wav");
        } else if (me == winner)
        {
            title = "WIN";
            msg = "你赢了";
            playMusic ("./resources/win.wav");
        } else
        {
            title = "LOSE";
            msg = "你输了";
            playMusic ("./resources/lose.wav");
        }
        noticeBoard.appendMessage ("<h1 color=blue>====" + title + "====</h1><br/>");
        JOptionPane.showMessageDialog (this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void playMusic (String name)
    {
        try
        {
            Clip clip = AudioSystem.getClip ();
            clip.open (AudioSystem.getAudioInputStream (new File (name)));
            clip.start ();
        } catch (Exception e)
        {
            
        }
    }
    
    public void terminate (String msg)
    {
        noticeBoard.appendMessage ("<div style=\"color:red\">" + msg + "</div><br/>");
        disconnect (true);
        JOptionPane.showMessageDialog (this, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
        initialize ();
    }
    
    public boolean askForGivein ()
    {
        boolean ret = JOptionPane.showOptionDialog (this, "Would you accept your enemy's surrender?", "QUESITON", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, agreementOptions, agreementOptions[0]) == 0;
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
            backward ();
            repaint ();
        }
        return ret;
    }
    
    private int askForLocalEnemy ()
    {
        return JOptionPane.showOptionDialog (this, "Choose local ENEMY", "CHOOSING", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, localEnemyOptions, localEnemyOptions[0]);
    }
    
    public boolean askForStart ()
    {
        boolean ret = JOptionPane.showOptionDialog (this, "Would you accept your enemy's start request", "QUESITON", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, agreementOptions, agreementOptions[0]) == 0;
        if (ret)
        {
            composition.initializeBoard (securityKey);
            composition.setLastStatus (securityKey, Composition.STATUS.WHITE);
            meStatus = Composition.STATUS.WHITE;
            repaint ();
            Thread thread = new Thread (() ->
            {
                gameOn (1);
            });
            thread.start ();
            thread.yield ();
        }
        return ret;
    }
    
    private void initOptions ()
    {
        colorRoleOption = new Object[2];
        colorRoleOption[0] = ("BLACK");
        colorRoleOption[1] = ("WHITE");
        tcpRoleOptions = new Object[2];
        tcpRoleOptions[0] = "Server";
        tcpRoleOptions[1] = "Client";
        agreementOptions = new Object[2];
        agreementOptions[0] = "YES";
        agreementOptions[1] = "NO";
        localEnemyOptions = new Object[2];
        localEnemyOptions[0] = "HUMAN";
        localEnemyOptions[1] = "MACHINE";
    }
    
    public void sendMessage (String message)
    {
        noticeBoard.appendMessage (myName + ":" + message);
        if (proxy != null)
        {
            try
            {
                proxy.send ("MESSAGE", message);
            } catch (Exception e)
            {
                
            }
        }
    }
    
    private void backward ()
    {
        composition.backward (securityKey, 2);
        try
        {
            noticeBoard.appendMessage (Composition.status2str (Composition.reverseStatus (composition.getLastStatus ())) + " UNDO<br/>");
        } catch (Exception e)
        {
            
        }
    }
    
    public static final class SecurityKey
    {
        private SecurityKey ()
        {
        }
    }
}
