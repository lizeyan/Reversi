import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Li Zeyan on 2016/7/29.
 */
public class NoticeBoard extends JPanel implements ActionListener
{
    private JTextArea messageBoard;
    private JLabel myIcon;
    private JLabel myPieces;
    private JLabel myName;
    private JLabel enemyIcon;
    private JLabel enemyPieces;
    private JLabel enemyName;
    private JLabel timeLabel;
    private JTextArea messageEdit;
    private JButton sendButton;
    private Reversi game;
    private StringBuffer messageBuffer;
    private boolean running = false;
    private long seconds = 0;
    
    public NoticeBoard (Reversi game)
    {
        this.game = game;
        setup ();
        Thread thread = new Thread (() ->
        {
            while (true)
            {
                try
                {
                    if (isRunning ())
                        updateTime ();
                    Thread.sleep (1000);
                }
                catch (Exception e)
                {
                    
                }
            }
        });
        thread.start ();
        thread.yield ();;
    }
    public void setTime (long second)
    {
        setSeconds (second);
        timeLabel.setText ("Time: " + second);
    }
    public void appendMessage (String msg)
    {
        messageBuffer.append (msg);
        messageBoard.setText (messageBuffer.toString ());
        messageBoard.setCaretPosition (messageBoard.getText ().length ());
        repaint ();
    }
    public void setStatus (Composition.STATUS my)
    {
        if (my == Composition.STATUS.WHITE)
        {
            myIcon.setIcon (new ImageIcon ("./resources/images/white.png"));
            enemyIcon.setIcon (new ImageIcon ("./resources/images/black.png"));
        }
        else
        {
            myIcon.setIcon (new ImageIcon ("./resources/images/black.png"));
            enemyIcon.setIcon (new ImageIcon ("./resources/images/white.png"));
        }
        repaint ();
    }
    public void setPieces (int my, int enemy)
    {
        myPieces.setText (String.valueOf (my));
        enemyPieces.setText (String.valueOf (enemy));
        repaint ();
    }
    public void setName (String name, boolean my)
    {
        if (my)
        {
            myName.setText (name);
        }
        else
        {
            enemyName.setText (name);
        }
        repaint ();
    }
    public void timerOn ()
    {
        running = true;
    }
    public void timerOff ()
    {
        running = false;
    }
    public long getSeconds ()
    {
        return seconds;
    }
    
    //========================================================================================================
    
    private void setSeconds (long seconds)
    {
        this.seconds = seconds;
    }
    private void setup ()
    {
        setSize (game.getChessBoard ().getWidth () / 4, game.getChessBoard ().getHeight ());
        messageBuffer = new StringBuffer (1 << 16);
        messageBoard = new JTextArea (20, 32);
        messageBoard.setFont (new Font ("Monaco", Font.PLAIN, 18));
        messageBoard.setEditable (false);
        messageBoard.setLineWrap (true);
        myIcon = new JLabel ();
        myPieces = new JLabel ();
        myPieces.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 36));
        myName = new JLabel ();
        myName.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 36));
        enemyIcon = new JLabel ();
        enemyPieces = new JLabel ();
        enemyPieces.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 36));
        enemyName = new JLabel ();
        enemyName.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 36));
        timeLabel = new JLabel ();
        timeLabel.setAlignmentX (LEFT_ALIGNMENT);
        timeLabel.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 48));
        messageEdit = new JTextArea (1, 25);
        messageEdit.setLineWrap (true);
        sendButton = new JButton ("SEND");
        sendButton.addActionListener (this);
        
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints = new GridBagConstraints ();
        setLayout (layout);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.ipady = 10;
        constraints.gridy = 0;
        constraints.gridx = 0;
        add (myIcon, constraints);
        constraints.gridx = 1;
        constraints.gridwidth = 1;
        add (myPieces, constraints);
        constraints.gridwidth = 3;
        constraints.gridx = 2;
        add (myName, constraints);
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        add (enemyIcon, constraints);
        constraints.gridx = 1;
        add (enemyPieces, constraints);
        constraints.gridwidth = 2;
        constraints.gridx = 2;
        add (enemyName, constraints);
        constraints.gridy = 2;
        constraints.gridx = 0;
        constraints.gridwidth = 5;
        add (timeLabel, constraints);
        constraints.gridy = 3;
        constraints.gridx = 0;
        constraints.gridwidth = 5;
        constraints.gridheight = 3;
        add (new JScrollPane (messageBoard), constraints);
    
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.gridx = 0;
        constraints.gridwidth = 3;
        constraints.gridheight = 1;
        add (new JScrollPane (messageEdit), constraints);
        constraints.gridwidth = 1;
        constraints.gridx = 3;
        add (sendButton, constraints);
        
    }
    private boolean isRunning ()
    {
        return running;
    }
    private void updateTime ()
    {
        setTime (getSeconds () - 1);
    }
    
    @Override
    public void actionPerformed (ActionEvent e)
    {
        Object source = e.getSource ();
        if (source == sendButton)
        {
            game.sendMessage (messageEdit.getText () + '\n');
            messageEdit.setText ("");
        }
    }
}
