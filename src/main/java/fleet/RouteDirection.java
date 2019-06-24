package fleet;

import java.util.ArrayList;

public class RouteDirection {

    public static int FORWARD = 0, BACKWARD = 1, RING = 2;

    public static int action( String hat, int aktif_sefer_index, ArrayList<String> guzergahlar ) {

        String onceki = "VY", sonraki = "VY";
        String aktif_sefer = guzergahlar.get(aktif_sefer_index-1);
        int hat_length = hat.length();

        try {
            onceki = guzergahlar.get(aktif_sefer_index - 2);
        } catch (IndexOutOfBoundsException e) {
            //System.out.println("Önceki sefer yok");
        }

        try {
            sonraki = guzergahlar.get(aktif_sefer_index );
        } catch (IndexOutOfBoundsException e) {
            //System.out.println("Sonraki sefer yok");
        }

        int aktif_sefer_yon = getDirectionLetter(hat_length, aktif_sefer);
        int sonraki_sefer_yon = getDirectionLetter(hat_length, sonraki);
        int onceki_sefer_yon = getDirectionLetter(hat_length, onceki);

        if (onceki.equals("VY") && !sonraki.equals("VY")) {
			/*
				[HAT]_G/D -- aktif_sefer_index
				[HAT]_G/D -- sonraki
			*/
            // onceki sefer yok, sonraki var
            // sonraki ve aktif seferin guzegahını karşılaştır
            if (aktif_sefer_yon == sonraki_sefer_yon) {
                // aynilarsa hat ring
				/*
					# Ring
					[HAT]_G -- aktif_sefer_index
					[HAT]_G -- sonraki
				*/
                //System.out.println("Ring hat! [ Koordinatlardan değerlendirme yapilacak. ] --> DURUM: " + aktif_sefer_yon);
                return RING;
            } else {
                // farklilarsa;
                // 1 - ilk sefer gidiş veya dönüş, geri kalanlar ring
                // 2 - hat normal
                // durum 1 kontrolü
                // sonraki sefer ile ondan sonraki seferi karşılaştırıp ring olup olmadigin kontrol ediyoruz
                if (getDirectionLetter(hat_length, guzergahlar.get(aktif_sefer_index + 2)) == sonraki_sefer_yon) {
					/*
						# Ring ( başlangıç farklı ) #### BU KONTROLE GEREK VAR MI ?
						[HAT]_G -- aktif_sefer_index
						[HAT]_D -- sonraki
						[HAT]_D -- ondan sonraki
					*/
                    // bu durumda aktif sefer ring degil, normal sefer gibi degerlendiriyoruz
                    //System.out.println("Ring hat öncesi normal sefer --> DURUM: " + aktif_sefer_yon);
                    return aktif_sefer_yon;
                } else {
					/*	# Normal
						[HAT]_G -- aktif_sefer_index
						[HAT]_D -- sonraki
						[HAT]_G -- ondan sonraki
					*/
                    //System.out.println("Hat normal![1] --> DURUM: " + aktif_sefer_yon);
                }
                return aktif_sefer_yon;
            }
        } else if (!onceki.equals("VY") && sonraki.equals("VY")) {
			/*  ..
				..
				..
				[HAT]_G/D -- onceki
				[HAT]_G/D -- aktif_sefer_index
			*/
            // onceki sefer var, sonraki sefer yok
            // onceki seferle aktif seferi karşılaştırıyoruz
            if (aktif_sefer_yon == onceki_sefer_yon) {
                // aynilarsa hat ring olabilir
				/*
					# Ring
					..
					[HAT]_G -- onceki
					[HAT]_G -- aktif_sefer_index
				*/
                //System.out.println("Ring hat![1][ Koordinatlardan değerlendirme yapilacak. ] --> DURUM: " + aktif_sefer_yon);
                return RING;
            } else {
                // aktif ve onceki farkli
				/*
					# Normal
					..
					[HAT]_D -- onceki
					[HAT]_G -- aktif_sefer_index
				*/
                //System.out.println("Hat normal![2] --> DURUM: " + aktif_sefer_yon);
                return aktif_sefer_yon;
            }
        } else {
			/*
				[HAT]_G/D -- onceki
				[HAT]_G/D -- aktif_sefer_index
				[HAT]_G/D -- sonraki
			*/
            if (onceki_sefer_yon == sonraki_sefer_yon && onceki_sefer_yon == aktif_sefer_yon) {
				/*
					# Ring
					[HAT]_D -- onceki
					[HAT]_D -- aktif_sefer_index
					[HAT]_D -- sonraki
				*/
                //System.out.println("Ring hat![1][ Koordinatlardan değerlendirme yapilacak. ] --> DURUM: " + aktif_sefer_yon);
                return RING;
            } else {
				/*
					# Normal
					[HAT]_G -- onceki
					[HAT]_D -- aktif_sefer_index
					[HAT]_G -- sonraki

					# Karışık
					[HAT]_G -- onceki
					[HAT]_G -- aktif_sefer_index
					[HAT]_D -- sonraki
				*/
                //System.out.println("Hat normal![3] --> DURUM: " + aktif_sefer_yon);
                return aktif_sefer_yon;
            }
        }
    }

    private static int getDirectionLetter(int hat_length, String guzergah_str) {
        try {
            String orta_harf = guzergah_str.substring(hat_length + 1, hat_length + 2);
            if (orta_harf.equals("G")) {
                return FORWARD;
            } else {
                return BACKWARD;
            }
        } catch (StringIndexOutOfBoundsException e) {
            return -1;
        }

    }

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