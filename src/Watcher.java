
/**
 *  Watcher storage class
 *
 *  @author Myron Su (myronsu)
 *  @version Sep 17, 2013
 */
public class Watcher
{
    private double x;
    private double y;
    private String name;

    // ----------------------------------------------------------
    /**
     * Create a new Watcher object.
     * @param _x x location
     * @param _y y location
     * @param _name name
     */
    public Watcher (double _x, double _y, String _name)
    {
        x = _x;
        y = _y;
        name = _name;
    }
    // ----------------------------------------------------------
    /**
     * Get function for X location
     * @return returns X location
     */
    public double getX()
    {
        return x;
    }
    // ----------------------------------------------------------
    /**
     * Get function for Y location
     * @return returns Y location
     */
    public double getY()
    {
        return y;
    }
    // ----------------------------------------------------------
    /**
     * Get function for watcher name
     * @return returns name of watcher
     */
    public String getName()
    {
        return name;
    }
}
