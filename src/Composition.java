import java.awt.*;
import java.util.AbstractList;
import java.util.ArrayList;

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
    private Point lastSetPoint = new Point ();
    private AbstractList<Point> history = new ArrayList<Point> ();
    private STATUS winnner = STATUS.EMPTY;
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
        initializeBoard ();
        updateAvailble ();
        judge ();
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
    public boolean set (int x, int y)
    {
        if (!legal(x, y) || !available[x][y])
            return false;
        history.add (new Point (x, y));
        lastStatus = reverseStatus(lastStatus);
        board[x][y] = lastStatus;
        reverse();
        updateAvailble();
        return true;
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
    public STATUS getWinnner ()
    {
        return winnner;
    }
    /*
    private methods
     */
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
    private void initializeBoard ()
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
                winnner = STATUS.BLACK;
            }
            else if (whiteNumber > blackNumber)
            {
                winnner = STATUS.WHITE;
            }
            else
            {
                winnner = STATUS.EMPTY;
            }
        }
        else
        {
            if (blackNumber == 0)
            {
                finished = true;
                winnner = STATUS.WHITE;
            }
            else if (whiteNumber == 0)
            {
                finished = true;
                winnner = STATUS.BLACK;
            }
        }
    }
}