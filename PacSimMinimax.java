import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacUtils;
import pacsim.PacmanCell;
import pacsim.PacSim;

public class PacSimMinimax implements PacAction
{

    int numFood;
    int numMoves;
    int boardManhattanDistance;


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


    // game plan: create game tree with depth as a variable, call evaluation function, 
    // and determine which move will be optimal, then udpate face

    public double sigmoid(int x)
    {
        return (1.0 / (1.0 + Math.exp(-1.0 * (double) x)));
    }

    public double weightFactor(int current, int total, String criteria)
    {
        if (criteria.equals("ghostDist"))
        {
            return -10.0 / ((double) current / total);
        }
        
        else if (criteria.equals("remainingFood"))
        {
            return 7.5 * ((double) total / current);
        }

        else if (criteria.equals("foodDist"))
        {
            // (total in this case will be the amount of remaining food)
            // helps slightly push Pacman to seek food if there is not much food left, 
            // while forcing him to go for any nearby food if there is plenty
            return (total / 1.5) / current;
        }

        else
        {
            return 0.0;
        }
    }

    // consider adding: remaining food count, distance to scared ghost, 
    // current number of moves (maybe), score so far
    public double evaluation(PacCell[][] state)
    {
        double leafScore;
        int ghostDist = Integer.MAX_VALUE;
        boardManhattanDistance = Math.max(state.length, state[0].length);

        PacmanCell pc = PacUtils.findPacman(state);

        // Pacman should be maximize distance from ghosts...
        List<Point> allGhosts = PacUtils.findGhosts(state);
        Point nearestGhost = null;
        Point otherGhost = null;

        for(int i = 0; i < allGhosts.size(); i++)
        {
            int currDist = PacUtils.manhattanDistance(allGhosts.get(i), pc.getLoc());

            if (currDist < ghostDist)
            {
                ghostDist = currDist;
                otherGhost = nearestGhost;
                nearestGhost = allGhosts.get(i);
            }
        }

        // ... while minimizing the distance to food (and minimizing the number of food cells left)
        Point nearestGoody = PacUtils.nearestGoody(pc.getLoc(), state);
        int foodDist = PacUtils.manhattanDistance(pc.getLoc(), nearestGoody);
        int remainingFood = PacUtils.findFood(state).size();


        // remember to add other sigmoided costs
        leafScore = weightFactor(ghostDist, boardManhattanDistance, "ghostDist") + weightFactor(foodDist, remainingFood, "foodDist") + weightFactor(remainingFood, numFood, "remainingFood");

        return leafScore;
    }

    public PacSimMinimax(int depth, String fname, int te, int gran, int max)
    {
        PacSim sim = new PacSim(fname, te, gran, max);
        sim.init(this);
    }

    public static void main(String[] args)
    {
        String fname = args[0];
        int depth = Integer.parseInt(args[1]);

        int te = 0;
        int gr = 0;
        int ml = 0;

        if (args.length == 5)
        {
            te = Integer.parseInt(args[2]);
            gr = Integer.parseInt(args[3]);
            ml = Integer.parseInt(args[4]);
        }

        new PacSimMinimax(depth, fname, te, gr, ml);

        System.out.println("\nAdversarial Search using Minimax by Kobee Raveendran:");

        System.out.println("\n   Game board : " + fname);

        System.out.println("\n   Search depth : " + depth + "\n");

        if (te > 0)
        {
            System.out.println("   Preliminary runs : " + te);
            System.out.println("   Granularity      : " + gr);
            System.out.println("   Max move limit   : " + ml);
            System.out.println("\n\nPrelimiminary run results : ");
        }
    }

    @Override
    public void init()
    {
        numMoves = 0;
        numFood = 0;
    }

    public PacFace minimax()
    {
        PacFace bestFace = null;

        // minimax structure here
        

        return bestFace;
    }

    @Override
    public PacFace action(Object state)
    {
        PacCell[][] grid = (PacCell[][]) state;
        PacFace newFace = null;
        PacmanCell pc = PacUtils.findPacman(grid);
        numFood = Math.max(numFood, PacUtils.findFood((PacCell[][]) state).size());

        // minimax here



        // TODO: move ghosts (?), move Pacman

        numMoves++;
        
        return newFace;
    }
}