import sun.awt.image.GifImageDecoder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Li Zeyan on 2016/7/29.
 */
public class NoticeBoard extends JPanel implements ActionListener
{
    private JTextPane messageBoard;
    private JScrollPane messageScrollPane;
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
    public void pack ()
    {
//        messageBoard.setRows (getHeight () / 60);
//        messageBoard.setColumns (getWidth () / 18);
//        messageBoard.invalidate ();
        messageScrollPane.setPreferredSize (new Dimension (getWidth (), getHeight () >> 1));
        messageEdit.setColumns ((getWidth () - sendButton.getWidth ()) / 18);
        messageEdit.invalidate ();
    }
    private void setup ()
    {
        messageBuffer = new StringBuffer (1 << 16);
        messageBoard = new JTextPane ();
//        messageBoard.setFont (new Font ("Microsoft yahei", Font.PLAIN, 18));
        messageBoard.setEditable (false);
//        messageBoard.setLineWrap (true);
        myIcon = new JLabel ();
        myPieces = new JLabel ();
        myPieces.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 48));
        myName = new JLabel ();
        myName.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 48));
        enemyIcon = new JLabel ();
        enemyPieces = new JLabel ();
        enemyPieces.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 48));
        enemyName = new JLabel ();
        enemyName.setFont (new Font ("Mircrosoft Yahei", Font.PLAIN, 48));
        timeLabel = new JLabel ();
        timeLabel.setFont (new Font ("Mircrosoft Yahei", Font.BOLD, 64));
        timeLabel.setSize (getWidth (), 64);
        messageEdit = new JTextArea (0, 0);
        messageEdit.setLineWrap (true);
        messageEdit.setFont (new Font ("Microsoft yahei", Font.PLAIN, 18));
        sendButton = new JButton ("SEND");
        sendButton.addActionListener (this);
        sendButton.setFont (new Font ("Microsoft yahei", Font.BOLD, 18));
        
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints = new GridBagConstraints ();
        setLayout (layout);
        constraints.ipady = 20;
        constraints.ipadx = 10;
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 2;
        add (myIcon, constraints);
        constraints.gridx = 2;
        constraints.gridwidth = 1;
        add (myPieces, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridx = 3;
        add (myName, constraints);
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        add (enemyIcon, constraints);
        constraints.gridx = 2;
        constraints.gridwidth = 1;
        add (enemyPieces, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridx = 3;
        add (enemyName, constraints);
        constraints.gridy = 5;
        constraints.gridx = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add (timeLabel, constraints);
        constraints.gridy = 7;
        constraints.gridx = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = 6;
        messageScrollPane = new JScrollPane (messageBoard, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add (messageScrollPane, constraints);
    
        constraints.gridy = 13;
        constraints.gridx = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = 2;
        JPanel panel = new JPanel (new FlowLayout ());
        panel.add (new JScrollPane (messageEdit));
        panel.add (sendButton);
        panel.setOpaque (false);
        add (panel, constraints);
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
