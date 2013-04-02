
package it.ssb.cubo.common.util;

import it.ssb.cubo.common.tripledes.CookieManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.ssb.errors.AppCrash;
import net.ssb.errors.ErrDetector;
import net.ssb.errors.ParamCrash;
import net.ssb.servlet.frame.SsbServletRequest;

/**
 * Classe contenente metodi statici di utilita' per le diverse classi dell'applicazione.
 */
public class Util {

    /**
	 *
	 */
    public static final String FORMATOTIMESTAMP = "yyyy-MM-dd-HH.mm.ss.SSS";
    public static final String YMD_HMS          = "yyyy-MM-dd-HH:mm:ss";
    public static final String DMY_HMS          = "dd/MM/yyyy HH:mm:ss";
    public static final String DMY_HMSS         = "dd/MM/yyyy HH:mm:ss.SSS";

    /**
     * @param formatIn
     * @param dateIn
     * @param formatOut
     * @return
     * @throws ParamCrash
     */
    public static String changeDateFormat(String formatIn, String dateIn, String formatOut) throws ParamCrash {

        // Devo girare la data
        try {
            SimpleDateFormat s0 = new SimpleDateFormat(formatIn);
            Date parsedDate = s0.parse(dateIn);

            SimpleDateFormat s1 = new SimpleDateFormat(formatOut);
            String dataGirata = s1.format(parsedDate);

            return dataGirata;

        } catch (ParseException pe) {
            String mess = "La Data: " + dateIn + " non ? nel formato corretto:" + formatIn;
            throw new ParamCrash(mess);
        }
    }

    /**
     * @param format
     * @param date
     * @return
     * @throws AppCrash
     * @throws ParseException
     */
    public static Date checkDateFormat(String format, String date, String fieldName, String function) throws ParamCrash {

        try {
            SimpleDateFormat s = new SimpleDateFormat(format);
            Date parsedDate = s.parse(date);
            return parsedDate;
        } catch (ParseException pe) {
            String mess = "Il campo " + fieldName + " non ? nel formato corretto:" + date;
            ParamCrash a = new ParamCrash(mess);
            a.logContext(function, mess);
            throw a;
        }
    }

    /**
     * Ritorna il timestamp currente nel formato db2 fino ai millisecondi.
     * 
     * @return dateString java.lang.String Il timestamp corrente.
     */
    public static String getCurrentTimestamp(String dateFormat) {

        Calendar currentTime = Calendar.getInstance();

        return getCurrentTimestamp(dateFormat, currentTime);
    }

    /**
     * Ritorna la data corrente nel formato passato in ingresso
     * 
     * @param formato java.lang.String Stringa contenente il formato che si vuole utilizzare
     * 
     * @return java.lang.String Il timestamp corrente.
     */
    public static String getCurrentTimestamp(String formato, Calendar currentTime) {

        SimpleDateFormat s = new SimpleDateFormat(formato);
        String dateString = s.format(currentTime.getTime());

        return dateString;
    }

    /**
     * @param formato
     * @param aDate
     * @return
     */
    public static String getCurrentTimestamp(String formato, Date aDate) {

        Calendar c = Calendar.getInstance();
        c.setTime(aDate);
        String dateString = Util.getCurrentTimestamp(formato, c);

        return dateString;
    }

    /**
     * Questo metodo
     * 
     */
    public static String getTimestamp() {

        return getTimestamp(FORMATOTIMESTAMP);
    }

    /**
     * Questo metodo
     * 
     */
    public static String getTimestamp(String formatoTimestamp) {

        Calendar calendar = Calendar.getInstance();
        String currentTimestamp = getCurrentTimestamp(formatoTimestamp, calendar);

        return currentTimestamp;
    }

    /**
     * Questo metodo verifica se l'oggetto stringa contiene un valore diverso stringa vuota o null.
     * 
     * @param valore java.lang.Object Oggetto stringa da verificare
     * 
     * @return true se la stringa contiene un valore diverso da stringa vuota o null, altrimenti false
     */
    public static boolean IsNotEmpty(Object valore) {

        if (valore != null) {
            return IsNotEmpty(valore.toString());
        } else {
            return false;
        }
    }

    /**
     * Dice se una stringa non e' vuota. Per 'vuota' s'intende null, stringa vuota, o stringa di soli blank.
     * 
     * @param what java.lang.String La stringa da controllare.
     * 
     * @return boolean true se la stringa non e' null, non e' vuota e non e' di soli blank; false altrimenti.
     */
    public static boolean IsNotEmpty(String what) {

        return ((what != null) && (what.trim().length() > 0));
    }

    /**
     * @param toBeFill
     * @param filler
     * @param length
     * @return
     */
    public static String fillLeftWith(String toBeFill, char filler, int size) {

        String filled = "";
        int length = size - toBeFill.length();
        for (int i = 0; i < length; i++) {
            filled += filler;
        }

        return filled + toBeFill;
    }

    /**
     * Questo metodo restituisce il valore della info contenuta nel cookie ssbFGUinfo identificata dal codice passato
     * 
     * @param infoID
     * @param req
     * @return
     * @throws AppCrash
     */
    public static String getFromInfoCookie(int infoID, SsbServletRequest req) throws AppCrash {

        CookieManager cm = new CookieManager(req);
        String[] values = cm.getInfoCookieValues();
        if (values == null) return "";

        ErrDetector.GetInstance().invariant(infoID >= 0 && values.length > infoID);

        return values[infoID];
    }

    /**
     * Questo metodo restituisce il timestamp formattato come richiesto.
     * 
     * @param date java.util.Date L'oggetto Date.
     * @param format java.lang.String La formattazione richiesta.
     * @return java.lang.String Il timestamp formattato.
     */
    public static String TimestampFormat(Date date, String format) {

        if (date == null) return "";
        return TimestampFormat(date.getTime(), format);
    }

    /**
     * Questo metodo restituisce il timestamp formattato come richiesto.
     * 
     * @param timeInMillis long Il timestamp.
     * @param format java.lang.String La formattazione richiesta.
     * @return java.lang.String Il timestamp formattato.
     */
    public static String TimestampFormat(long timeInMillis, String format) {

        DateFormat sdf = new SimpleDateFormat(format);
        String tsFmt = sdf.format(timeInMillis);

        return tsFmt;
    }

}
