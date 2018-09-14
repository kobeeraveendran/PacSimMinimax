import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacUtils;
import pacsim.PacmanCell;
import pacsim.PacSim;

public class PacSimMinimax implements PacAction
{
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

    }

    @Override
    public PacFace action(Object state)
    {
        PacCell[][] grid = (PacCell[][]) state;
        PacFace newFace = null;

        // minimax here
        
        return newFace;
    }
}