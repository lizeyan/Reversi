import java.awt.*;
import java.util.AbstractList;
import java.util.Stack;
import java.util.logging.XMLFormatter;

/**
 * Created by Li Zeyan on 2016/8/4.
 */
public class AlphaBetaAi extends LocalMachinePlayer
{
    private static int maxDepth = (4 << 1);
    private static int[][] evaluateTabel = {
            {90, -60, 10, 10, 10, 10, -60, 90},
            {-60, -80, 0, 0, 0, 0, -80, -60},
            {20, 0, 1, 1, 1, 1, 0, 20},
            {10, 0, 1, 1, 1, 1, 0, 10},
            {10, 0, 1, 1, 1, 1, 0, 10},
            {20, 0, 1, 1, 1, 1, 0, 20},
            {-60, -80, 0, 0, 0, 0, -80, -60},
            {90, -60, 10, 10, 10, 10, -60, 90},
    };
    public AlphaBetaAi (Composition composition)
    {
        super (composition);
    }
    @Override
    public Point makingPolicy (long timeConstraint)
    {
        Composition composition = getComposition ();
        if (!composition.queryAvailble ())
            return null;
        Stack<Node> stack = new Stack<Node> ();
        Node root = new Node (composition, 0, null);
        stack.push (root);
        int cnt = 0;
        while (!stack.isEmpty ())
        {
            Node current = stack.peek ();
            if (current.depth < maxDepth && current.bestChild == null)
            {
                //expand
                Composition curComposition = current.composition;
                if (composition.getFinished ())
                {
                    back (current, stack);
                    stack.pop ();
                }
                else if (!curComposition.queryAvailble ())
                {
                    Node child = new Node (new Composition (curComposition), current.depth + 1, current);
                    child.composition.dropOver (null);
                    stack.push (child);
                } else
                {
                    for (int i = 0; i < curComposition.getWidth (); ++i)
                        for (int j = 0; j < curComposition.getHeight (); ++j)
                            if (curComposition.queryAvailble (i, j))
                            {
                                Node child = new Node (new Composition (curComposition), current.depth + 1, current);
                                child.composition.set (null, i, j);
                                stack.push (child);
                            }
                }
            }
            else
            {
                if (current.depth == maxDepth)
                {
                    current.value = evaluate (current.composition);
                    ++cnt;
                }
                back (current, stack);
//                System.out.println ("" + System.identityHashCode (current) + " " + current.value + " " + System.identityHashCode (current.parent));
                stack.pop ();
            }
        }
        System.out.println ("Total evaluated:" + cnt);
        AbstractList<Point> h = root.bestChild.composition.getHistory ();
        return h.get (h.size () - 1);
    }
    private static void back (Node current, Stack stack)
    {
        Node ptr = current.parent;
        Node child = current;
        while (ptr != null)
        {
            if (ptr.bestChild == null)
            {
                ptr.bestChild = child;
                ptr.value = child.value;
                break;
            }
            if ((ptr.me && child.value > ptr.value) || (!ptr.me && child.value < ptr.value))
            {
                ptr.bestChild = child;
                ptr.value = child.value;
            }
            else if ((ptr.me && child.value < ptr.value) || (!ptr.me && child.value > ptr.value))
            {
                cutBranch (stack, child);
                break;
            }
            child = ptr;
            ptr = ptr.parent;
        }
    }
    private static void cutBranch (Stack stack, Node node)
    {
        while (stack.peek () != node)
            stack.pop ();
    }
    public static class Node
    {
        private Composition composition;
        private Node parent = null;
        private int depth;
        private int value = 0;
        private Node bestChild = null;
        private boolean me = false;
        public Node (Composition composition, int depth, Node parent)
        {
            this.depth = depth;
            this.composition = composition;
            this.parent = parent;
            if ((depth & 1) == 1)
            {
                me = false;
                value = Integer.MAX_VALUE;
            }
            else
            {
                me = true;
                value = Integer.MIN_VALUE;
            }
        }
    }
    private static int evaluate (Composition composition)
    {
        Composition.STATUS enemy = composition.getLastStatus ();
        Composition.STATUS me = Composition.reverseStatus (enemy);
        if (composition.getFinished ())
        {
            if (composition.getWinner () == me)
            {
                return Integer.MAX_VALUE;
            }
            else
            {
                return Integer.MIN_VALUE;
            }
        }
        int positonValue = 0;
        int motivation = 0;
        for (int i = 0; i < composition.getWidth (); ++i)
        {
            for (int j = 0; j < composition.getHeight (); ++j)
            {
                if (composition.queryBoard (i, j) == me)
                {
                    positonValue += evaluateTabel[i][j];
                }
                else if (composition.queryBoard (i, j) == enemy)
                {
                    positonValue -= evaluateTabel[i][j];
                }
                if (composition.queryAvailble (i, j))
                    ++motivation;
            }
        }
        int potentialMotivation = 0;
        for (int i = 0; i < composition.getWidth (); ++i)
            for (int j = 0; j < composition.getHeight (); ++j)
                if (composition.queryBoard (i, j) == Composition.STATUS.EMPTY)
                    for (int d = 0; d < 8; ++d)
                        if (composition.legal (i + Composition.dx[d], j + Composition.dy[d]) && composition.queryBoard (i + Composition.dx[d], j + Composition.dy[d]) == enemy)
                        {
                            ++potentialMotivation;
                            break;
                        }
        return 5 * motivation + 10 * positonValue + 3 * potentialMotivation;
    }
}
