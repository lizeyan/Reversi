import javax.swing.*;
import java.awt.*;

/**
 * Created by Li Zeyan on 2016/7/22.
 */
public class Reversi extends JFrame {
    private ChessBoard chessBoard;
    public Reversi (String name)
    {
        super(name);
        chessBoard = new ChessBoard();
        setContentPane(chessBoard);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }
    public static void main (String[] argv)
    {
        Reversi reversi = new Reversi("Reversi v0.1");
        reversi.setVisible(true);
    }
}
