import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.WallCell;
import pacsim.HouseCell;
import pacsim.PacFace;
import pacsim.PacUtils;
import pacsim.PacmanCell;
import pacsim.PacSim;

class Node
{
    private ArrayList<Node> children = null;
    private PacCell[][] state;
    private double value;
    //private String type;

    public Node(double value, PacCell[][] state)
    {
        this.children = new ArrayList<>();
        this.state = state;
        this.value = value;
        //this.type = type;
    }

    public Node(Node copy)
    {
        this.children = copy.getChildren();
        this.state = copy.state;
        this.value = copy.value;
    }

    public void addChild(Node child)
    {
        children.add(child);
    }

    public ArrayList<Node> getChildren()
    {
        return this.children;
    }

    public PacCell[][] getState()
    {
        return this.state;
    }

    public void setState(PacCell[][] newState)
    {
        this.state = PacUtils.cloneGrid(newState);
    }

    public double getValue()
    {
        return this.value;
    }

    public void setValue(double value)
    {
        this.value = value;
    }
}

public class PacSimMinimax implements PacAction
{
    int numFood;
    int numMoves;
    int initDepth;
    boolean pacmanTurn;

    /*             ******************** EXPLANATION OF EVALUATION FUNCTION ********************
     *
     * This minimax function takes into account 3 factors at every 
     * Pacman move: the distance to the nearest ghost (may consider using distance to the
     * farther ghost to some extent), how much food is remaining on the board, and 
     * the distance to the nearest food around Pacman.
     * 
     * I chose to use the distance to the nearest ghost as a deterrent for Pacman, 
     * forcing him to avoid making moves that put him in danger, since survival is 
     * something he can't afford to slack on (otherwise the game ends). This is solely why 
     * the absolute value of the weight I gave to the ghost distance (-10) is higher than 
     * the absolute values of any of the other metrics.
     * 
     * I also chose to use the amount of remaining food to motivate Pacman to look to end the game, 
     * so that he wouldn't be stuck making useless moves if he's not in immediate danger and only a few
     * pellets remain. This is why the weight for it is still somewhat high for a positive weight. The more 
     * food he collects, the lower the value of total/current will be, which is how he'll be 
     * incentivized to end the game. 
     * 
     * Tying into this, my third factor is the distance to the nearest food cell. I chose this because
     * I want Pacman to be "pushed" toward nearby food dots if the situation allows for it. I gave it 
     * a somewhat small weight to avoid forcing him to prioritize food over his own safety. However, 
     * that weight scales with the distance to the food. If the distance is relatively small, 
     * he will be more pressured to go for the food (i.e. if it's in an adjacent cell to his own), 
     * but if the food is extremely far, the resulting weight will be small, which allows him the 
     * other factors to exert more influence. In the case that there's only a few food dots remaining, 
     * but all are far, the second criteria will end up taking over, pushing him toward the food dots 
     * anyway.
     * 
     */

    public double evaluation(PacCell[][] state)
    {
        double leafScore = (double) numMoves;
        PacmanCell pc = PacUtils.findPacman(state);

        int distNearestFood = PacUtils.manhattanDistance(pc.getLoc(), PacUtils.nearestFood(pc.getLoc(), state));
        int distNearestGhost = PacUtils.manhattanDistance(pc.getLoc(), PacUtils.nearestGhost(pc.getLoc(), state));

        // boolean isFood = PacUtils.goody(pc.getLoc().getX(), pc.getLoc().getY(), state)

        if (distNearestFood < distNearestGhost)
        {
            leafScore = leafScore * 2;
        }

        for (Point p : PacUtils.findFood(state))
        {
            int distToFood = PacUtils.manhattanDistance(pc.getLoc(), p);
            leafScore = leafScore - 0.5 * distToFood;
        }

        return leafScore;
    }

    public PacSimMinimax(int depth, String fname, int te, int gran, int max)
    {
        PacSim sim = new PacSim(fname, te, gran, max);

        initDepth = depth;
        sim.init(this);
    }

    public static void main(String[] args)
    {
        String fname = args[0];
        int depth = Integer.parseInt(args[1]);
    }
    
}