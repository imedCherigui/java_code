package eu.siassb.common.helper;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import net.ssb.dataset.DataSetFactory;
import net.ssb.dataset.DataSet_itf;
import net.ssb.dataset.Row_itf;
import net.ssb.errors.AppCrash;
import net.ssb.errors.Logger;
import net.ssb.misc.Util;

/**
 * Controlli comuni per MNW incassi
 * 
 * @author ConsCheriguiImed
 * 
 */
public class CommonChecks {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+[,.]{1}[0-9]{2}");
    private static final Pattern INPUT_PATTERN  = Pattern.compile("^[.,;:!-_+?$£%^&()/=\\w\\s]*$");

    /**
     * Controlla che una data con il formato specificato sia valida a calendario
     * 
     * @param indate: la data da controllare
     * @param pattern: il formato della data (es. ddMMyy)
     * @return la data in formato java.util.Date se il controllo va a buon fine, null altrimenti
     */
    public static Date checkDateValidity(String inDate, String pattern) {

        if (inDate == null || pattern == null) {
            Logger.GetInstance().log0(
                    CommonChecks.class.getCanonicalName() + "Data passata [" + inDate + "], formato atteso [" + pattern
                            + "]");
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        if (inDate.trim().length() != dateFormat.toPattern().length()) {
            Logger.GetInstance().log0(
                    CommonChecks.class.getCanonicalName() + "Data passata [" + inDate + "], formato atteso [" + pattern
                            + "]");
            Logger.GetInstance().log0(
                    CommonChecks.class.getCanonicalName() + ": lunghezza data (" + inDate.length()
                            + ") diversa da lunghezza pattern (" + dateFormat.toPattern().length() + ")");
            return null;
        }

        dateFormat.setLenient(false);

        try {
            return dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            Logger.GetInstance().log0(
                    CommonChecks.class.getCanonicalName() + "Data passata [" + inDate + "], formato atteso [" + pattern
                            + "]");
            Logger.GetInstance().log0(CommonChecks.class.getCanonicalName() + ": data non valida a calendario");
            return null;
        }
    }

    /**
     * Controlla che due date rappresentino un intervallo valido ("data A" non può essere inferiore a "data DA")<br>
     * Il formato delle date viene controllato in base al Locale in sessione per l'utente
     * 
     * @param fromDate data DA
     * @param fromPattern formato "data DA"
     * @param toDate data A
     * @param toPattern formato "data A"
     * @return true se "data DA" è inferiore o uguale a "data A"
     */
    public static boolean isDateIntervalValid(String fromDate, String fromPattern, String toDate, String toPattern) {

        Date dataDa = checkDateValidity(fromDate, fromPattern);
        if (dataDa == null) {
            return false;
        }

        Date dataA = checkDateValidity(toDate, toPattern);
        if (dataA == null) {
            return false;
        }

        if (dataA.before(dataDa)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Controlla che due date rappresentino un intervallo valido ("data A" non può essere inferiore a "data DA")
     * 
     * @param from
     * @param to
     * @return
     */
    public static boolean isDateIntervalValid(Timestamp from, Timestamp to) {

        if (from == null || to == null) {
            return false;
        }

        if (to.before(from)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Controlla che un valore ricevuto da una combo box da JSP sia legittimamente contenuto nella lista dei valori
     * ammessi per quella combo box.
     * 
     * @param codeValue
     * @param datasetComboName
     * @return true se il controllo va a buon fine, false altrimenti
     * @throws AppCrash
     */
    public static boolean isValueFromDropDownMenu(String codeValue, String datasetName, Map<String, Object> params,
            boolean isMandatory) throws AppCrash {

        if (!Util.IsNotEmpty(codeValue)) {
            if (isMandatory) {
                Logger.GetInstance().log0("Valore null non previsto per il menu' a tendina [" + datasetName + "]");
                return false;
            } else {
                return true;
            }
        }

        DataSetFactory dsFactory = DataSetFactory.getInstance();
        DataSet_itf dataset = dsFactory.makeDataSet("", datasetName);
        if (params == null) {
            params = new HashMap<String, Object>();
        }

        try {
            dataset.setParam(params);
            dataset.open();
            while (dataset.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataset.nextElement();
                if (codeValue.equals(dbRow.getField("codice").toString())) {
                    return true;
                }
            }
        } catch (AppCrash ac) {
            ac.logContext(CommonChecks.class.getCanonicalName(), "isValueFromDropDownMenu [" + datasetName + "]");
            throw ac;
        } finally {
            if (dataset != null) {
                dataset.close();
            }
        }

        Logger.GetInstance().log0(
                "Valore da client non previsto: [" + codeValue + "] per menu' a tendina [" + datasetName + "]");
        return false;
    }

    /**
     * Controlla che un campo sia alfanumerico
     */
    public static boolean isAlphanumeric(String field, boolean isMandatory) {

        if (!Util.IsNotEmpty(field)) {
            if (isMandatory) {
                Logger.GetInstance().log0("Valore null non previsto per il campo [" + field + "]");
                return false;
            } else {
                return true;
            }
        }

        return StringUtils.isAlphanumeric(field);
    }

    /**
     * Controlla che un campo sia un numero: sono ammessi i decimali completi di virgola o punto
     * 
     * @param field: il campo da controllare
     * @return true se il controllo va a buon fine
     */
    public static boolean isNumber(String field, boolean isMandatory) {

        if (!Util.IsNotEmpty(field)) {
            if (isMandatory) {
                Logger.GetInstance().log0(
                        CommonChecks.class.getCanonicalName() + ": campo numerico [" + field + "] null");
                return false;
            } else {
                return true;
            }
        }
        Matcher matcher = NUMBER_PATTERN.matcher(field);
        if (matcher.matches()) {
            return true;
        } else {
            Logger.GetInstance().log0(
                    CommonChecks.class.getCanonicalName() + ": campo numerico [" + field + "] non valido");
            return false;
        }
    }

    /**
     * Controlla che un campo sia un input valido: una stringa valida
     * 
     * @param field: il campo da controllare
     * @return true se il controllo va a buon fine
     */
    public static boolean isValidInput(String field, boolean isMandatory) {

        if (!Util.IsNotEmpty(field)) {
            if (isMandatory) {
                Logger.GetInstance().log0(CommonChecks.class.getCanonicalName() + ": campo [" + field + "] null");
                return false;
            } else {
                return true;
            }
        }
        Matcher matcher = INPUT_PATTERN.matcher(field);
        if (matcher.matches()) {
            return true;
        } else {
            Logger.GetInstance().log0(CommonChecks.class.getCanonicalName() + ": campo [" + field + "] non valido");
            return false;
        }
    }

    /**
     * Controlla che un campo sia una Divisa valida (e.s EUR, USD)
     */
    public static boolean isValidCurrency(String field, boolean isMandatory) {

        if (!Util.IsNotEmpty(field)) {
            if (isMandatory) {
                Logger.GetInstance().log0("Valore null non previsto per il campo [" + field + "]");
                return false;
            } else {
                return true;
            }
        }
        return StringUtils.isAlpha(field) & (field.length() == 3) ? true : false;
    }

}
