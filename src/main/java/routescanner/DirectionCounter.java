package routescanner;

public class DirectionCounter {
    private int forward = 0, backward = 0, total = 0;

    public DirectionCounter(){

    }

    public void increment( int dir ){
        if( dir == RouteDirection.FORWARD ){
            forward++;
        } else {
            backward++;
        }
        total++;
    }
    public int getDirection(){
        if( total == 4 ){
            if( forward > backward ){
                return RouteDirection.FORWARD;
            } else if( forward < backward ){
                return RouteDirection.BACKWARD;
            }
        }
        return -1;
    }


}