import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Li Zeyan on 2016/7/22.
 */
public class Reversi extends JFrame implements ActionListener
{
    private ChessBoard chessBoard;
    private JMenuBar menuBar;
    private JMenu localMenu, onlineMenu, operateMenu, generalMenu;
    private JMenuItem startLocalGameItem, saveLocalGameItem, loadLocalGameItem, startOnlineGameItem, undoItem, redoItem, giveInItem, peaceItem, settingItem, helpItem, aboutItem;
    private Composition composition = null;
    private Player[] players;
    private long timeConstraintPerStep = 20000;
    private Object[] roleOptions;
    public Reversi (String name)
    {
        super(name);
        roleOptions = new Object[2];
        roleOptions[0] = ("BLACK");
        roleOptions[1] = ("WHITE");
        chessBoard = new ChessBoard(composition = new Composition ());
        setContentPane(chessBoard);
        initMenu ();
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
        else if (source == saveLocalGameItem)
            saveLocalGame ();
        else if (source == loadLocalGameItem)
            loadLocalGame ();
        else if (source == startOnlineGameItem)
            startOnlineGame ();
        else if (source == undoItem)
            undo ();
        else if (source == redoItem)
            redo ();
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
        saveLocalGameItem = new JMenuItem ("Save");
        saveLocalGameItem.addActionListener (this);
        loadLocalGameItem = new JMenuItem ("Load");
        loadLocalGameItem.addActionListener (this);
        localMenu.add (startLocalGameItem);
        localMenu.add (saveLocalGameItem);
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
        redoItem = new JMenuItem ("Redo");
        redoItem.addActionListener (this);
        giveInItem = new JMenuItem ("Give In");
        giveInItem.addActionListener (this);
        peaceItem = new JMenuItem ("Sue For Peace");
        peaceItem.addActionListener (this);
        operateMenu.add (undoItem);
        operateMenu.add (redoItem);
        operateMenu.add (giveInItem);
        operateMenu.add (peaceItem);
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
        Composition.STATUS meStatus;
        int response = JOptionPane.showOptionDialog (this, "Choose Your Role, Black is always the first", "Choosing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, roleOptions, null);
        if (response == 1)
            meStatus = Composition.STATUS.WHITE;
        else
            meStatus = Composition.STATUS.BLACK;
        players = new Player[2];
        players[0] = new LocalMePlayer (chessBoard);
        players[1] = new LocalMachinePlayer (composition);
        composition.initializeBoard ();
        repaint ();
        Thread thread = new Thread (()-> {gameOn (meStatus);});
        thread.start ();
        thread.yield ();
    }
    private void loadLocalGame ()
    {
        
    }
    private void saveLocalGame ()
    {
        
    }
    private void startOnlineGame ()
    {
        
    }
    private void undo ()
    {
        
    }
    private void redo ()
    {
        
    }
    private void giveIn ()
    {
        
    }
    private void peace ()
    {
        
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
    private void gameOn (Composition.STATUS meStatus)
    {
        int index = 0;
        if (meStatus == Composition.STATUS.WHITE)
            index = 1;
        while (true)
        {
            Point policy = players[index].makingPolicy (timeConstraintPerStep);
            if (!composition.set (policy.x, policy.y))
                composition.dropOver ();
            chessBoard.repaint ();
            if (composition.getFinished ())
            {
                gameOff (meStatus, composition.getWinnner ());
                return;
            }
            ++index;
            index %= 2;
        }
    }
    public void gameOff (Composition.STATUS me, Composition.STATUS winner)
    {
        String msg, title;
        if (me == winner)
        {
            title = "WIN";
            msg = "你赢了";
        }
        else
        {
            title = "LOSE";
            msg = "你输了";
        }
        JOptionPane.showMessageDialog (this, msg, title, JOptionPane.INFORMATION_MESSAGE);
        composition.cleanBoard ();
        repaint ();
    }
}
