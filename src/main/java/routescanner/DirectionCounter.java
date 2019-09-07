/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package routescanner;

public class DirectionCounter {
    /**
     * Forward stop count
     */
    private int forward = 0;
    /**
     * Backward stop count
     */
    private int backward = 0;
    /**
     * Total stop count
     */
    private int total = 0;

    /**
     * Empty constructor
     */
    public DirectionCounter(){
    }

    /**
     * Increment the counter for given direction
     * @param dir direction
     */
    public void increment( int dir ){
        if( dir == RouteDirection.FORWARD ){
            forward++;
        } else {
            backward++;
        }
        total++;
    }

    /**
     * Return the direction when accumulate is finished
     *
     * @return final direction
     */
    public int getDirection(){
        if( total == 3 ){
            if( forward > backward ){
                return RouteDirection.FORWARD;
            } else if( forward < backward ){
                return RouteDirection.BACKWARD;
            }
        }
        return -1;
    }
}