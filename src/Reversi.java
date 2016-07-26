import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

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
    private Object[] roleOptions;
    private Composition.STATUS meStatus = Composition.STATUS.EMPTY;
    public static final class SecurityKey {private SecurityKey () {} }
    private static SecurityKey securityKey = new SecurityKey ();
    public Reversi (String name)
    {
        super(name);
        roleOptions = new Object[2];
        roleOptions[0] = ("BLACK");
        roleOptions[1] = ("WHITE");
        chessBoard = new ChessBoard(composition = new Composition ());
        setContentPane(chessBoard);
        initMenu ();
        initialize ();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }
    public static void main (String[] argv)
    {
        Reversi reversi = new Reversi("Reversi v1.0");
        reversi.setVisible(true);
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
        int response = JOptionPane.showOptionDialog (this, "Choose Your Role, Black is always the first", "Choosing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, roleOptions, null);
        if (response == 1)
            meStatus = Composition.STATUS.WHITE;
        else
            meStatus = Composition.STATUS.BLACK;
        players = new Player[2];
        players[0] = new LocalMePlayer (chessBoard);
        players[1] = new LocalMachinePlayer (composition);
        composition.initializeBoard (securityKey);
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
            players[0] = new LocalMePlayer (chessBoard);
            players[1] = new LocalMachinePlayer (composition);
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
        
    }
    private void undo ()
    {
        
    }
    private void giveIn ()
    {
        if (players[1].receiveGiveIn ())
        {
            showWinner (meStatus, Composition.reverseStatus (meStatus));
            initialize ();
        }
        else
        {
            JOptionPane.showMessageDialog (this, "The request is rejected.", "INFO", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void peace ()
    {
        if (players[1].receiveSueForPeace ())
        {
            showWinner (meStatus, Composition.STATUS.EMPTY);
            initialize ();
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
            if (!composition.set (securityKey, policy.x, policy.y))
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
        composition.cleanBoard (securityKey);
        meStatus = Composition.STATUS.EMPTY;
        
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
}
