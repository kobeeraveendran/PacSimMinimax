import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.WallCell;
import pacsim.HouseCell;
import pacsim.PacFace;
import pacsim.PacUtils;
import pacsim.PacmanCell;
import pacsim.GhostCell;
import pacsim.PacSim;
import pacsim.PacMode;


/* 
 * University of Central Florida
 * CAP4630 - Fall 2018
 * Author(s): Kobee Raveendran
 */

public class PacSimMinimax implements PacAction
{
    int initDepth;
    int numFood;
    int numMoves;
    int numPower;
    static final double loss = -1e7;
    static final double win = 1e7;
    static final double stateInit = 1e8;

    public PacSimMinimax(int depth, String fname, int te, int gran, int max)
    {
        PacSim sim = new PacSim(fname, te, gran, max);

        initDepth = depth;
        numFood = 0;
        numPower = 0;
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
    }

    /*      ********************* EVALUATION FUNCTION EXPLANATION *******************
     * The evaluation function below uses a variety of factors, mainly concerned with properties in the
     * game state related to food or the placement of ghosts. Initially, I placed a heavy emphasis on 
     * Pacman's survival (by making the "safe" distance from ghosts > 2) but I found that this often lead 
     * to sub-par winrates. So, I concluded that making Pacman adopt a more "aggressive" playstyle by 
     * prioritizing food pellets and only evading ghosts when absolutely necessary was the way to go about 
     * getting an accuracy that was consistently above 40%. Below I explain in detail some of the heuristics 
     * I used to achieve this "aggressive" playstyle.
     * 
     * First was the number of food pellets remaining on the board; if there are none left, I assigned an 
     * absolute win value to the state (overriding any other factors). This forces Pacman to make the move
     * that will eat the last food pellet no matter what, in order to end the game. I think that, most of the time,
     * this pays off.
     * 
     * Next is the distances to the ghosts. This metric was somewhat important in that it was the limiter for 
     * the other factors; I wanted to eliminate a state if it caused Pacman to be in a dangerous zone, where he'd 
     * be likely to be cornered by ghosts (in my function, that zone had a radius of 1). Before adding the 
     * later factors discussed below, this saved Pacman from trashing endlessly by forcing him to move when a 
     * ghost came nearby.
     * 
     * Next comes the distance to the nearest food, as suggested in the assignment. I wanted to make Pacman 
     * actively seek out food rather than just passively pick them up as he goes by, but also wanted to ensure 
     * that he didn't die in the process. So, if the nearest food was closer than the nearest ghost, I 
     * gave a huge boost to the score by multiplying it by 5. This heuristic alone seemed to increase my winrate from 
     * percentage from the high 30's/low 40's to mid 40's/low 50's. Another thing that helped cause this boost 
     * was the addition of the inner condition, which checks if there's a food pellet within a small range. 
     * I believe this largely stunted Pacman's tendency to thrash around even near uncontested food pellets.
     * 
     * Finally, the distance to all other food pellets was what made the winrate breach into the 60%+ range. 
     * This also caused Pacman to more actively seek out food, but unlike the previous heuristic, it did this 
     * even when there was no food nearby. This proved useful in the early stages of the game, where food 
     * tended to be more densely spread around the board. I gave it a somewhat harsh negative weight by 
     * multiplying the total distances by 0.75, subtracted from the score. In the beginning of the game, this 
     * value would be pretty large, which would motivate Pacman to prioritize getting food pellets early, 
     * and letting him relax (but still "lean" toward food) in the later stages of the game. Also, although 
     * it doesn't matter much in the context of this program (since winrate is the metric we're using to 
     * determine success), this heuristic would also increase the average score per game played, even if 
     * Pacman dies, since he'd so aggressively hunt food early on.
     * 
     * Further improvements: I also wanted to incorporate the use of Power pellets into the evaluation, 
     * but for some reason this tended to tank the winrate slightly, down to the 30%/40% range. I couldn't quite 
     * extract the PacMode from the GhostCell using just the points, but also using another method in which I keep 
     * track of the number of power pellets didn't seem to increase the winrate either. This might be because 
     * the way I'd planned on doing (the second method in the previous sentence) it only affected one state 
     * (only the state in which Pacman COLLECTS the power pellet, rather than the entire duration of 
     * the ghosts' FEAR mode).
     */
    public double evaluationFunction(PacCell[][] parentState, Point pacman, Point inky, Point blinky)
    {

        double score;

        int remainingFood = PacUtils.numFood(parentState);

        if (remainingFood == 0)
        {
            return win;
        }

        List<Point> foodList = PacUtils.findFood(parentState);
        Point nearestFood = PacUtils.nearestFood(pacman, parentState);
        int distNearestFood;

        if (nearestFood == null)
        {
            distNearestFood = 0;
        }
        else
        {
            distNearestFood = PacUtils.manhattanDistance(pacman, nearestFood);
        }

        int inkyDist = PacUtils.manhattanDistance(pacman, inky);
        int blinkyDist = PacUtils.manhattanDistance(pacman, blinky);
        Point nearestGhost;
        int distNearestGhost;

        if (inkyDist < blinkyDist)
        {
            nearestGhost = inky;
            distNearestGhost = inkyDist;
        }
        else
        {
            nearestGhost = blinky;
            distNearestGhost = blinkyDist;
        }

        if (distNearestGhost <= 1)
        {
            return loss;
        }

        int distToAllFood = 0;

        for (Point food : foodList)
        {
            distToAllFood += PacUtils.manhattanDistance(pacman, food);
        }

        score = 10.0 * (numFood - remainingFood);

        if (distNearestGhost >= 2)
        {
            score += 12.5;
        }

        //PacMode ghostMode = ((GhostCell) parentState[nearestGhost.x][nearestGhost.y]).getMode();

        /*
        if (PacUtils.numPower(parentState) < numPower)
        {
            score += 100;
        }
        */

        if (distNearestFood <= distNearestGhost)
        {
            score = score * 5;

            if (distNearestFood < 2)
            {
                score += 5;
            }
        }

        score -= 0.75 * distToAllFood;

        return score;
    }

    public double max(PacCell[][] parentState, int depth, Point pacman, Point inky, Point blinky)
    {
        if (depth == 0)
        {
            return evaluationFunction(parentState, pacman, inky, blinky);
        }

        double curr = -stateInit;

        for (PacFace c : PacFace.values())
        {
            PacCell newPacLoc = PacUtils.neighbor(c, parentState[pacman.x][pacman.y], parentState);

            if (!(newPacLoc instanceof WallCell) && !(newPacLoc instanceof HouseCell))
            {
                PacCell[][] tempState = PacUtils.movePacman(pacman, newPacLoc.getLoc(), parentState);
                curr = Math.max(mini(tempState, depth, newPacLoc.getLoc(), inky, blinky), curr);
            }
        }

        return curr;
    }

    public double mini(PacCell[][] parentState, int depth, Point pacman, Point inky, Point blinky)
    {
        double curr = stateInit;

        for (PacFace c : PacFace.values())
        {
            PacCell newInkyLoc = PacUtils.neighbor(c, parentState[inky.x][inky.y], parentState);
            
            if (!(newInkyLoc instanceof WallCell))
            {
                for (PacFace d : PacFace.values())
                {
                    PacCell newBlinkyLoc = PacUtils.neighbor(d, parentState[blinky.x][blinky.y], parentState);

                    if (!(newBlinkyLoc instanceof WallCell))
                    {
                        // move them in the same state (since both are minimizing agents)
                        PacCell[][] tempState = PacUtils.moveGhost(inky, newInkyLoc.getLoc(), parentState);
                        tempState = PacUtils.moveGhost(blinky, newBlinkyLoc.getLoc(), tempState);

                        curr = Math.min(max(tempState, depth - 1, pacman, newInkyLoc.getLoc(), newBlinkyLoc.getLoc()), curr);
                    }
                }
            }
            
        }

        return curr;
    }

    public PacFace minimaxRoot(PacCell[][] state, int depth)
    {
        PacmanCell pc = PacUtils.findPacman(state);
        List<Point> ghostList = PacUtils.findGhosts(state);
        Point inky = ghostList.get(0);
        Point blinky = ghostList.get(1);

        // first step of minimax (pretty much just max() but returning a face)
        double curr = -stateInit;
        PacFace bestFace = null;

        for (PacFace c : PacFace.values())
        {
            PacCell newPacLoc = PacUtils.neighbor(c, pc, state);

            if (!(newPacLoc instanceof WallCell) && !(newPacLoc instanceof HouseCell))
            {
                PacCell[][] tempState = PacUtils.movePacman(pc.getLoc(), newPacLoc.getLoc(), state);
                
                double newVal = mini(tempState, depth, newPacLoc.getLoc(), inky, blinky);

                if (newVal > curr)
                {
                    curr = newVal;
                    bestFace = c;
                }
            }
        }

        return bestFace;
    }

    @Override
    public PacFace action(Object state)
    {
        PacCell[][] grid = (PacCell[][]) state;
        PacFace newFace = null;
        numPower = PacUtils.numPower(grid);

        if (numFood == 0)
        {
            numFood = PacUtils.numFood(grid);
        }

        newFace = minimaxRoot(grid, initDepth);

        //double bestCost = minimax(grid, initDepth, pc.getLoc(), ghostList.get(0), ghostList.get(1));

        numMoves++;

        return newFace;
    }
}