/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package routescanner;

import java.util.ArrayList;

public class RouteDirection {
    /**
     * Direction enum for forward
     */
    public static int FORWARD = 0;
    /**
     * Direction enum for backward
     */
    public static int BACKWARD = 1;
    /**
     * Direction enum for ring
     */
    public static int RING = 2;

    /**
     * Direction finder
     *
     * @param routeCode code of the route
     * @param activeRouteIndex active run index
     * @param runDetails rundetails list
     *
     * @return direction
     */
    public static int action( String routeCode, int activeRouteIndex, ArrayList<String> runDetails ) {
        activeRouteIndex++; // new system returns zero based active run index
        String prevRunDetail = "VY", nextRunDetail = "VY";
        String activeRunDetail = runDetails.get(activeRouteIndex-1);
        int routeCodeLength = routeCode.length();

        try {
            prevRunDetail = runDetails.get(activeRouteIndex - 2);
        } catch (IndexOutOfBoundsException e) {
            //System.out.println("Önceki sefer yok");
        }

        try {
            nextRunDetail = runDetails.get(activeRouteIndex );
        } catch (IndexOutOfBoundsException e) {
            //System.out.println("Sonraki sefer yok");
        }

        int activeRunDirection = getDirectionLetter(routeCodeLength, activeRunDetail);
        int nextRunDirection = getDirectionLetter(routeCodeLength, nextRunDetail);
        int prevRunDirection = getDirectionLetter(routeCodeLength, prevRunDetail);

        if (prevRunDetail.equals("VY") && !nextRunDetail.equals("VY")) {
			/*
				[HAT]_G/D -- activeRouteIndex
				[HAT]_G/D -- sonraki
			*/
            // onceki sefer yok, sonraki var
            // sonraki ve aktif seferin guzegahını karşılaştır
            if (activeRunDirection == nextRunDirection) {
                // aynilarsa hat ring
				/*
					# Ring
					[HAT]_G -- activeRouteIndex
					[HAT]_G -- sonraki
				*/
                //System.out.println("Ring hat! [ Koordinatlardan değerlendirme yapilacak. ] --> DURUM: " + activeRunDirection);
                return RING;
            } else {
                // farklilarsa;
                // 1 - ilk sefer gidiş veya dönüş, geri kalanlar ring
                // 2 - hat normal
                // durum 1 kontrolü
                // sonraki sefer ile ondan sonraki seferi karşılaştırıp ring olup olmadigin kontrol ediyoruz
                try {
                    String twoNextRunDetail = runDetails.get(activeRouteIndex + 2);
                    if (getDirectionLetter(routeCodeLength, twoNextRunDetail) == nextRunDirection) {
					/*
						# Ring ( başlangıç farklı ) #### BU KONTROLE GEREK VAR MI ?
						[HAT]_G -- activeRouteIndex
						[HAT]_D -- sonraki
						[HAT]_D -- ondan sonraki
					*/
                        // bu durumda aktif sefer ring degil, normal sefer gibi degerlendiriyoruz
                        //System.out.println("Ring hat öncesi normal sefer --> DURUM: " + activeRunDirection);
                        return activeRunDirection;
                    } else {
					/*	# Normal
						[HAT]_G -- activeRouteIndex
						[HAT]_D -- sonraki
						[HAT]_G -- ondan sonraki
					*/
                        //System.out.println("Hat normal![1] --> DURUM: " + activeRunDirection);
                    }
                    return activeRunDirection;
                } catch( IndexOutOfBoundsException e ){
                    return activeRunDirection;
                }
            }
        } else if (!prevRunDetail.equals("VY") && nextRunDetail.equals("VY")) {
			/*  ..
				..
				..
				[HAT]_G/D -- onceki
				[HAT]_G/D -- activeRouteIndex
			*/
            // onceki sefer var, sonraki sefer yok
            // onceki seferle aktif seferi karşılaştırıyoruz
            if (activeRunDirection == prevRunDirection) {
                // aynilarsa hat ring olabilir
				/*
					# Ring
					..
					[HAT]_G -- onceki
					[HAT]_G -- activeRouteIndex
				*/
                //System.out.println("Ring hat![1][ Koordinatlardan değerlendirme yapilacak. ] --> DURUM: " + activeRunDirection);
                return RING;
            } else {
                // aktif ve onceki farkli
				/*
					# Normal
					..
					[HAT]_D -- onceki
					[HAT]_G -- activeRouteIndex
				*/
                //System.out.println("Hat normal![2] --> DURUM: " + activeRunDirection);
                return activeRunDirection;
            }
        } else {
			/*
				[HAT]_G/D -- onceki
				[HAT]_G/D -- activeRouteIndex
				[HAT]_G/D -- sonraki
			*/
            if (( prevRunDirection == nextRunDirection && prevRunDirection == activeRunDirection ) || ( activeRunDirection == nextRunDirection ) ) {
				/*
					# Ring
					[HAT]_D -- onceki
					[HAT]_D -- activeRouteIndex
					[HAT]_D -- sonraki
				*/
                //System.out.println("Ring hat![1][ Koordinatlardan değerlendirme yapilacak. ] --> DURUM: " + activeRunDirection);
                return RING;
            } else {
				/*
					# Normal
					[HAT]_G -- onceki
					[HAT]_D -- activeRouteIndex
					[HAT]_G -- sonraki

					# Karışık
					[HAT]_G -- onceki
					[HAT]_G -- activeRouteIndex
					[HAT]_D -- sonraki
				*/
                //System.out.println("Hat normal![3] --> DURUM: " + activeRunDirection);
                return activeRunDirection;
            }
        }
    }

    /**
     * Get direction letter from run details ( ex: 15BK_G_XXX -> G )
     *
     * @param routeCodeLength length of the route's code
     * @param runDetailString detail string to be investiagated
     *
     * @return direction
     */
    public static int getDirectionLetter(int routeCodeLength, String runDetailString) {
        try {
            String midDirectionLetter = runDetailString.substring(routeCodeLength + 1, routeCodeLength + 2);
            if (midDirectionLetter.equals("G")) {
                return FORWARD;
            } else {
                return BACKWARD;
            }
        } catch ( IndexOutOfBoundsException e) {
            return -1;
        }

    }

    /**
     * Returns direction as string
     *
     * @param dir direction enum
     *
     * @return
     */
    public static String returnText( int dir ){
        if( dir == FORWARD ){
            return "Gidiş";
        } else if( dir == BACKWARD ){
            return "Dönüş";
        } else {
            return "Ring";
        }
    }
}