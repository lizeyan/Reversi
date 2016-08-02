import jdk.jfr.events.ExceptionThrownEvent;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SettingDialog extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField timeEdit;
    private JTextField nameEdit;
    private JLabel timeLabel;
    private JLabel nameLabel;
    private JTextField musicEdit;
    private JButton chooseBtn;
    private JLabel musicLabel;
    private Reversi game;
    
    public SettingDialog (Reversi game)
    {
        this.game = game;
        setContentPane (contentPane);
        setModal (true);
        getRootPane ().setDefaultButton (buttonOK);
        
        buttonOK.addActionListener (new ActionListener ()
        {
            public void actionPerformed (ActionEvent e)
            {
                onOK ();
            }
        });
        
        buttonCancel.addActionListener (new ActionListener ()
        {
            public void actionPerformed (ActionEvent e)
            {
                onCancel ();
            }
        });
        
        // call onCancel() when cross is clicked
        setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
        addWindowListener (new WindowAdapter ()
        {
            public void windowClosing (WindowEvent e)
            {
                onCancel ();
            }
        });
        
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction (new ActionListener ()
        {
            public void actionPerformed (ActionEvent e)
            {
                onCancel ();
            }
        }, KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        nameEdit.setText (game.getMyName ());
        timeEdit.setText (String.valueOf (game.getTimeConstraintPerStep () / 1000));
        
        pack ();
        chooseBtn.addActionListener (new ActionListener ()
        {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                JFileChooser fileChooser = new JFileChooser ("./");
                fileChooser.setMultiSelectionEnabled (false);
                fileChooser.showOpenDialog (null);
                musicEdit.setText (fileChooser.getSelectedFile ().getAbsolutePath ());
            }
        });
    }
    
    private void onOK ()
    {
        long t = Integer.parseInt (timeEdit.getText ());
        if (t > 0)
            game.setTimeConstraintPerStep (t * 1000);
        game.setMyName (nameEdit.getText ());
        try
        {
            InputStream inputStream = new FileInputStream (musicEdit.getText ());
            AudioStream audioStream = new AudioStream (inputStream);
            game.setAudioDataStream (new ContinuousAudioDataStream (audioStream.getData ()));
            AudioPlayer.player.start (game.getAudioDataStream ());
        }
        catch (Exception e)
        {
            
        }
        // add your code here
        dispose ();
    }
    
    private void onCancel ()
    {
        // add your code here if necessary
        dispose ();
    }
    
}
