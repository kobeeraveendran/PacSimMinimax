import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

import com.sun.corba.se.spi.orbutil.fsm.State;

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
            return -5.0 / ((double) current / total);
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
    public int evaluation(PacCell[][] state)
    {
        int leafScore;
        int ghostDist = Integer.MAX_VALUE;
        boardManhattanDistance = Math.max(state.length, state[0].length);

        PacmanCell pc = PacUtils.findPacman(state);

        // Pacman should be maximize distance from ghosts...
        List<Point> allGhosts = PacUtils.findGhosts(state);
        Point nearestGhost;
        Point otherGhost;

        for(int i = 0; i < allGhosts.size(); i++)
        {
            int currDist = PacUtils.manhattanDistance(allGhosts.get(i), pc.getLoc());

            if (currDist < distToGhost)
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
        numFood = PacUtils.findFood((PacCell[][]) state).size();
    }

    @Override
    public PacFace action(Object state)
    {
        PacCell[][] grid = (PacCell[][]) state;
        PacFace newFace = null;
        PacmanCell pc = PacUtils.findPacman(grid);

        // minimax here

        // TODO: move ghosts (?), move Pacman

        numMoves++;
        
        return newFace;
    }
}