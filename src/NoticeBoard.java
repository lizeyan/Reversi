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
        pack ();
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
    public void pack ()
    {
//        setPreferredSize (new Dimension (game.getWidth () >> 2, game.getHeight ()));
        messageBoard.setRows (getHeight () / 60);
        messageBoard.setColumns (getWidth () / 18);
        messageBoard.invalidate ();
        messageEdit.setColumns (getWidth () / 25);
        messageEdit.invalidate ();
//        messageBoard.setSize (game.getWidth () >> 2, game.getHeight () >> 1);
    }
    private void setup ()
    {
        messageBuffer = new StringBuffer (1 << 16);
        messageBoard = new JTextArea (0, 0);
        messageBoard.setFont (new Font ("MonacoMicrosoft yahei", Font.PLAIN, 18));
        messageBoard.setEditable (false);
        messageBoard.setLineWrap (true);
        myIcon = new JLabel ();
        myPieces = new JLabel ();
        myPieces.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 36));
        myName = new JLabel ();
        myName.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 36));
        enemyIcon = new JLabel ();
        enemyPieces = new JLabel ();
        enemyPieces.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 30));
        enemyName = new JLabel ();
        enemyName.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 36));
        timeLabel = new JLabel ();
        timeLabel.setAlignmentX (LEFT_ALIGNMENT);
        timeLabel.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 48));
        messageEdit = new JTextArea (0, 0);
        messageEdit.setLineWrap (true);
        messageEdit.setFont (new Font ("Microsoft yahei", Font.PLAIN, 18));
        sendButton = new JButton ("SEND");
        sendButton.addActionListener (this);
        sendButton.setFont (new Font ("Microsoft yahei", Font.BOLD, 18));
        
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints = new GridBagConstraints ();
        setLayout (layout);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.ipady = 20;
//        constraints.ipady = 10;
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 2;
        add (myIcon, constraints);
        constraints.gridx = 2;
        constraints.gridwidth = 1;
        add (myPieces, constraints);
        constraints.gridwidth = 6;
        constraints.gridx = 3;
        add (myName, constraints);
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        add (enemyIcon, constraints);
        constraints.gridx = 2;
        constraints.gridwidth = 1;
        add (enemyPieces, constraints);
        constraints.gridwidth = 6;
        constraints.gridx = 3;
        add (enemyName, constraints);
        constraints.gridy = 5;
        constraints.gridx = 0;
        constraints.gridwidth = 12;
        add (timeLabel, constraints);
        constraints.gridy = 7;
        constraints.gridx = 0;
        constraints.gridwidth = 12;
        constraints.gridheight = 6;
        add (new JScrollPane (messageBoard), constraints);
    
        constraints.gridy = 13;
        constraints.gridx = 1;
        constraints.gridwidth = 6;
        constraints.gridheight = 2;
        add (new JScrollPane (messageEdit), constraints);
        constraints.gridwidth = 2;
        constraints.gridx = 6;
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
