import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SettingDialog extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField timeEdit;
    private JTextField nameEdit;
    private JLabel timeLabel;
    private JLabel nameLabel;
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
    }
    
    private void onOK ()
    {
        long t = Integer.parseInt (timeEdit.getText ());
        if (t > 0)
            game.setTimeConstraintPerStep (t * 1000);
        game.setMyName (nameEdit.getText ());
        // add your code here
        dispose ();
    }
    
    private void onCancel ()
    {
        // add your code here if necessary
        dispose ();
    }
    
}
