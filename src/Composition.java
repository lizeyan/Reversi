import java.awt.*;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.UnknownFormatConversionException;

/**
 * Created by Li Zeyan on 2016/7/21.
 */
public class Composition {
    public enum STATUS {EMPTY, WHITE, BLACK};
    private int width = 8;
    private int height = 8;
    private STATUS[][] board;
    private boolean[][] available;
    private STATUS lastStatus = STATUS.WHITE;
    private AbstractList<Point> history = new ArrayList<Point> ();
    private STATUS winner = STATUS.EMPTY;
    private static int[] dx = {-1, 0, 1, 1, 1, 0, -1, -1};
    private static int[] dy = {-1, -1, -1, 0, 1, 1, 1, 0};
    private int blackNumber = 0;
    private int whiteNumber = 0;
    private boolean finished = false;
    /*
        public methods
     */
    public Composition ()
    {
        board = new STATUS[width][height];
        available = new boolean[width][height];
        cleanBoard ();
        updateAvailble ();
    }
    public int getWidth ()
    {
        return width;
    }
    public int getHeight ()
    {
        return height;
    }
    public STATUS[][] getBoard ()
    {
        return board;
    }
    public void setLastStatus (Reversi.SecurityKey securityKey, STATUS status)
    {
        this.lastStatus = status;
    }
    public static String status2str (STATUS status) throws Exception
    {
        switch (status)
        {
            case EMPTY:
                return "EMPTY";
            case BLACK:
                return "BLACK";
            case WHITE:
                return "WHITE";
            default:
                throw new RuntimeException ("invalid Composition.STATUS value");
        }
    }
    public static STATUS str2status (String str)
    {
        if (str != null)
        {
            if (str.equals ("BLACK"))
                return STATUS.BLACK;
            else if (str.equals ("WHITE"))
                return STATUS.WHITE;
            else if (str.equals ("EMPTY"))
                return STATUS.EMPTY;
        }
        throw new RuntimeException ("wrong Composition.STATUS string:" + str);
    }
    public STATUS queryBoard (int x, int y) throws RuntimeException
    {
        if (!legal (x, y))
            throw new IllegalArgumentException ("棋盘坐标越界，于Compositon.queryBoard(int,int)");
        return board[x][y];
    }
    public boolean queryAvailble (int x, int y) throws RuntimeException
    {
        if (!legal(x, y))
            throw new IllegalArgumentException("棋盘坐标越界，于Compositon.queryAvailbel(int,int)");
        return available[x][y];
    }
    public boolean queryAvailble ()
    {
        for (int i = 0; i < width; ++i)
        {
            for (int j = 0; j < height; ++j)
                if (available[i][j])
                    return true;
        }
        return false;
    }
    public int queryNumber (STATUS status)
    {
        int cnt = 0;
        for (int i = 0; i < width; ++i)
        {
            for (int j = 0; j < height; ++j)
            {
                if (board[i][j] == status)
                    ++cnt;
            }
        }
        return cnt;
    }
    public boolean set (Reversi.SecurityKey securityKey, int x, int y)
    {
        if (!legal(x, y) || !available[x][y])
            return false;
        history.add (new Point (x, y));
        set (history.get (history.size () - 1));
        updateAvailble();
        judge ();
        return true;
    }
    public void dropOver (Reversi.SecurityKey securityKey)
    {
        history.add (new Point (-1, -1));
        lastStatus = reverseStatus (lastStatus);
        updateAvailble ();
    }
    public boolean legal (int x, int y)
    {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    public STATUS getLastStatus ()
    {
        return lastStatus;
    }
    public static STATUS reverseStatus (STATUS status)
    {
        switch (status)
        {
            case WHITE:
                return STATUS.BLACK;
            case BLACK:
                return STATUS.WHITE;
            default:
                return STATUS.EMPTY;
        }
    }
    public boolean getFinished ()
    {
        return finished;
    }
    public STATUS getWinner ()
    {
        return winner;
    }
    public void initializeBoard (Reversi.SecurityKey securityKey)
    {
        for (int i = 0; i < width; ++i)
        {
            for (int j = 0; j < height; ++j)
            {
                board[i][j] = STATUS.EMPTY;
            }
        }
        board[(width >> 1) - 1][(height >> 1) - 1] = STATUS.WHITE;
        board[width >> 1][height >> 1] = STATUS.WHITE;
        board[(width >> 1) - 1][height >> 1] = STATUS.BLACK;
        board[(width >> 1)][(height >> 1) - 1] = STATUS.BLACK;
        updateAvailble ();
    }
    public void cleanBoard (Reversi.SecurityKey securityKey)
    {
        cleanBoard ();
    }
    public void setBoard (Reversi.SecurityKey securityKey, STATUS[][] board)
    {
        if (board.length != width || board[0].length != height)
            throw new IllegalArgumentException ("board size is wrong, in setBoard");
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j)
                this.board[i][j] = board[i][j];
        updateAvailble ();
        judge ();
    }
    public void backward (Reversi.SecurityKey securityKey, int steps)
    {
        while (--steps >= 0 && history.size () > 0)
        {
            history.remove (history.size () - 1);
        }
        initializeBoard (securityKey);
        setLastStatus (securityKey, STATUS.WHITE);
        for (Point point : history)
        {
            set (point);
        }
        updateAvailble ();
    }
    /*
    private methods
     */
    private void cleanBoard ()
    {
        finished = false;
        lastStatus = STATUS.WHITE;
        history.clear ();
        winner = STATUS.EMPTY;
        for (int i = 0; i < width; ++i)
        {
            for (int j = 0; j < height; ++j)
            {
                board[i][j] = STATUS.EMPTY;
                available[i][j] = false;
            }
        }
    }
    private void updateAvailble ()
    {
        
        STATUS current = reverseStatus (lastStatus);
        for (int i = 0; i < width; ++i)
        {
            for (int j = 0; j < height; ++j)
            {
                available[i][j] = false;
            }
        }
        for (int i = 0; i < width; ++i)
        {
            for (int j = 0; j < height; ++j)
            {
                if (board[i][j] != current)
                    continue;
                for (int d = 0; d < 8; ++d)//8 directions
                {
                    Point point = new Point (i, j);
                    point.translate (dx[d], dy[d]);
                    if (!legal (point.x, point.y) || board[point.x][point.y] != lastStatus)
                        continue;
                    do
                    {
                        point.translate (dx[d], dy[d]);
                    }
                    while (legal (point.x, point.y) && board[point.x][point.y] == lastStatus);
                    if (legal (point.x, point.y) && board[point.x][point.y] == STATUS.EMPTY)
                        available[point.x][point.y] = true;
                }
            }
        }
    }
    private void reverse ()
    {
        for (int d = 0; d < 8; ++d)
        {
            Point point = new Point (history.get (history.size () - 1));
            point.translate (dx[d], dy[d]);
            STATUS enemy = reverseStatus (lastStatus);
            if (!legal (point.x, point.y) || board[point.x][point.y] != enemy)
                continue;
            int cnt = 0;
            do
            {
                ++cnt;
                point.translate (dx[d], dy[d]);
            }
            while (legal (point.x, point.y) && board[point.x][point.y] == enemy);
            if (legal (point.x, point.y) && board[point.x][point.y] == lastStatus)
            {
                for (int i = 0; i < cnt; ++i)
                {
                    point.translate (-dx[d], -dy[d]);
                    board[point.x][point.y] = lastStatus;
                }
            }
        }
    }
    private void judge ()
    {
        blackNumber = 0;
        whiteNumber = 0;
        int emptyNumber= 0;
        for (int i = 0; i < width; ++i)
        {
            for (int j = 0; j < height; ++j)
            {
                if (board[i][j] == STATUS.BLACK)
                    ++blackNumber;
                else if (board[i][j] == STATUS.WHITE)
                    ++whiteNumber;
                else
                    ++emptyNumber;
            }
        }
        if (emptyNumber == 0)
        {
            finished = true;
            if (blackNumber > whiteNumber)
            {
                winner = STATUS.BLACK;
            }
            else if (whiteNumber > blackNumber)
            {
                winner = STATUS.WHITE;
            }
            else
            {
                winner = STATUS.EMPTY;
            }
        }
        else
        {
            if (blackNumber == 0)
            {
                finished = true;
                winner = STATUS.WHITE;
            }
            else if (whiteNumber == 0)
            {
                finished = true;
                winner = STATUS.BLACK;
            }
        }
    }
    private void set (Point point)
    {
        lastStatus = reverseStatus (lastStatus);
        if (legal (point.x, point.y))
        {
            board[point.x][point.y] = lastStatus;
            reverse ();
        }
    }
}
