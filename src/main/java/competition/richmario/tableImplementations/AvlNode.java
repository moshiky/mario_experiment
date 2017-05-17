package competition.richmario.tableImplementations;

/**
 * Created by Dev on 04/05/2017.
 */
// Basic node stored in AVL trees
// Note that this class is not accessible outside
// of package DataStructures

public class AvlNode
{
    // Constructors
    AvlNode( Comparable theElement )
    {
        this( theElement, null, null );
    }

    AvlNode( Comparable theElement, AvlNode lt, AvlNode rt )
    {
        element  = theElement;
        left     = lt;
        right    = rt;
        height   = 0;

        nextTree = null;
        actionValues = null;
    }

    // Friendly data; accessible by other package routines
    Comparable element;      // The data in the node
    AvlNode    left;         // Left child
    AvlNode    right;        // Right child
    int        height;       // Height

    // Added fields for Q table handling
    AvlTree nextTree;
    double[] actionValues;

    public void setNextTree(AvlTree tree) { nextTree = tree; }
    public void setActionValues(double[] actions) { actionValues = actions; }
}