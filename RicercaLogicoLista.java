package eu.siassb.mnwinc.flusso.function;

import it.ssb.cubo.common.util.Util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.ssb.errors.AppCrash;
import net.ssb.misc.Config;
import net.ssb.servlet.frame.ApplicationServices_itf;
import net.ssb.servlet.frame.SsbServletRequest;
import net.ssb.servlet.frame.SsbServletResponse;
import net.ssb.servlet.security.UserSecurityInfo;
import eu.siassb.common.helper.CommonChecks;
import eu.siassb.common.helper.Utils;
import eu.siassb.mnwinc.common.FunctionMnwInc_base;
import eu.siassb.mnwinc.flusso.enums.LogicoFieldNames;
import eu.siassb.mnwinc.flusso.enums.MaskedFieldType;

/**
 * Ricerca Flussi Logici (Distinte) MNW Incassi
 * 
 * @author ConsCheriguiImed
 */
public class RicercaLogicoLista extends FunctionMnwInc_base {

    // nomi DATASET
    protected static final String DATASET_NAME            = "LogicoElenco";
    // DataSet campi di ricerca
    protected static final String DS_TIPO_DISTINTA        = "TipoLogico";

    // nomi TEMPLATE
    private static final String   TEMPLATE_NAME           = "LogicoRicerca";
    private static final String   PAGE_DATASET            = "ElencoLogici";

    // nomi TAG HTML
    protected static final String TAG_AZIENDA             = "distinta_tipoAzienda";
    protected static final String TAG_CONTROPARTE         = "distinta_controparte";
    protected static final String TAG_TIPO_FLUSSO         = "distinta_tipoFlusso";
    protected static final String TAG_ID_DISTINTA         = "distinta_id";
    protected static final String TAG_DIVISA              = "distinta_divisa";
    protected static final String TAG_STATO               = "distinta_stato";
    protected static final String TAG_NOME_FLUSSO         = "distinta_nomeFlusso";

    // nomi TAG DataBase
    protected static final String C_NOME_SUPPORTO         = "C_NOME_SUPPORTO";
    protected static final String C_ORD_NOMINATIVO        = "C_ORD_NOMINATIVO";
    protected static final String C_RAG_SOCIALE           = "C_RAG_SOCIALE";
    protected static final String C_DISTINTA              = "C_DISTINTA";
    protected static final String E_RICEZIONE             = "E_RICEZIONE";
    protected static final String C_ULTIMO_STATO          = "C_ULTIMO_STATO";
    protected static final String C_ULTIMO_STATO_BUSINESS = "BUSINESS_CODICE";
    protected static final String S_ULTIMO_STATO          = "S_ULTIMO_STATO";
    protected static final String E_ULTIMO_STATO          = "E_ULTIMO_STATO";
    protected static final String N_IMPORTO               = "N_IMPORTO";
    protected static final String C_DIVISA                = "C_DIVISA";
    protected static final String C_PROPRIETARIO          = "C_PROPRIETARIO";
    protected static final String C_RICEVENTE             = "C_RICEVENTE";
    protected static final String S_RICEVENTE             = "S_RICEVENTE";
    protected static final String C_TIPO                  = "C_TIPO";
    protected static final String S_TIPO                  = "S_TIPO";
    protected static final String C_VERSO                 = "C_VERSO";
    protected static final String S_VERSO                 = "S_VERSO";
    protected static final String C_DIREZIONE             = "C_DIREZIONE";
    protected static final String S_DIREZIONE             = "S_DIREZIONE";
    protected static final String C_DEB_NOMINATIVO        = "C_DEB_NOMINATIVO";
    protected static final String N_PROGRESSIVO           = "N_PROGRESSIVO";
    protected static final String D_SCADENZA              = "D_SCADENZA";
    protected static final String S_DIVISA                = "S_DIVISA";

    public RicercaLogicoLista(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        super.elabora(req, res, userInfo);
        int numeroPaginaRichiesta = 0;
        String windowSize = null;

        try {
            // se la chiamata è generata dal pulsante Indietro
            if (Util.IsNotEmpty(req.getAttribute(TAG_BACK))) {
                numeroPaginaRichiesta = (Integer) req.getSession().getAttribute(NUM_PAGINA_INT);
                templateData = (Map<String, Object>) req.getSession().getAttribute(TEMPLATE_DATA);
            } else {
                templateData = setCommonTags(req, userInfo);
                // lettura di parametri della ricerca
                setSpecificTags(req, userInfo);
                numeroPaginaRichiesta = Integer.parseInt(req.getField(NUM_PAGINA_INT));
            }
            // preparo la lista di DS params per popolare i combo di ricerca
            Map<String, Object> dsParamArray[] = prepareDsParamsArray(req, userInfo);

            // Validazione dei campi di ricerca
            if (validate(req)) {
                // preparazione della where condition
                setWhereCondition(req);
                prepareWindowDataSet(DATASET_NAME);
                wds.openWindowDataSet();

                setMaxNumberOfPagesTag(DATASET_NAME);
                wdsVaiAPagina(wds, numeroPaginaRichiesta);

                windowSize = Config.GetInstance().getProperty(DS + DATASET_NAME + _WINDOW_SIZE);
                templateData.put(PAGE_DATASET, wds);
                templateData.put(LIST_RESULT_CHECK, TRUE);
                templateData.put(VALIDATION_ERROR, FALSE);
                templateData.put(WINDOW_SIZE, windowSize);

                setMaskedField(req, wds, MaskedFieldType.C_MSG_LOGICO.getCode());
                // mettere la template data nella sessione
                // serve a restituire i campi della ricerca se la chiamata è generata dal pulsante Indietro
                req.getSession().setAttribute(TEMPLATE_DATA, templateData);
                req.getSession().setAttribute(NUM_PAGINA_INT, numeroPaginaRichiesta);
                req.getSession().setAttribute(VALORE_TRATTA, req.getField(TAG_TRATTA));

                _applicationSrv.displayPage(TEMPLATE_NAME, templateData, dsParamArray, res);
            } else {
                templateData.put(LIST_RESULT_CHECK, FALSE);
                templateData.put(VALIDATION_ERROR, TRUE);
                _applicationSrv.displayPage(TEMPLATE_NAME, templateData, dsParamArray, res);
            }
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "Elabora : " + req);
            throw ac;
        } finally {
            if (wds != null) wds.close();
        }
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        templateData = setCommonTags(req, userInfo);
        // preparo la lista di DS params per popolare i combo di ricerca
        Map<String, Object> dsParam[] = prepareDsParamsArray(req, userInfo);
        templateData.put(LIST_RESULT_CHECK, FALSE);
        templateData.put(VALIDATION_ERROR, FALSE);
        setSpecificTags(req, userInfo);

        _applicationSrv.displayPage(TEMPLATE_NAME, templateData, dsParam, res);
    }

    protected void setWhereCondition(SsbServletRequest req) throws AppCrash, ParseException {

        Locale loc = (Locale) req.getSession().getAttribute(LOCALE);

        String dbField = VALORE_VUOTO;
        String orderField = req.getField(TAG_ORDER_FIELD);
        if (Util.IsNotEmpty(orderField)) {
            dbField = LogicoFieldNames.valueOf(orderField).getCode();
        }

        dsParam.put(WC + C_PROPRIETARIO, setWCStringField(TAG_AZIENDA, C_PROPRIETARIO));
        dsParam.put(WC + C_DIREZIONE, setWCStringField(TAG_DIREZIONE, C_DIREZIONE));
        dsParam.put(WC + C_VERSO, setWCStringField(TAG_TRATTA, C_VERSO));
        dsParam.put(WC + C_RICEVENTE, setWCStringFieldLike(TAG_CONTROPARTE, C_RICEVENTE));
        dsParam.put(WC + C_NOME_SUPPORTO, setWCStringFieldLike(TAG_NOME_FLUSSO, C_NOME_SUPPORTO));
        dsParam.put(WC + C_TIPO, setWCStringField(TAG_TIPO_FLUSSO, C_TIPO));
        dsParam.put(WC + C_DISTINTA, setWCStringFieldLike(TAG_ID_DISTINTA, C_DISTINTA));
        dsParam.put(WC + N_IMPORTO, setWCNumericField(TAG_IMPORTO, N_IMPORTO));
        dsParam.put(WC + C_DIVISA, setWCStringFieldLike(TAG_DIVISA, C_DIVISA));
        dsParam.put(WC + C_ULTIMO_STATO, setWCStringField(TAG_STATO, C_ULTIMO_STATO_BUSINESS));
        dsParam.put(WC + E_ULTIMO_STATO, setWCDateField(loc, TAG_ELAB, E_ULTIMO_STATO));
        dsParam.put(ORDER_BY, setORDStringField(req, dbField, E_ULTIMO_STATO));
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object>[] prepareDsParamsArray(SsbServletRequest req, UserSecurityInfo userInfo)
            throws AppCrash {

        Map<String, Object> dsComboParam[] = new HashMap[4];
        setParams(dsParam, req, userInfo, SERVICE_LEVEL_LOGICO);
        dsComboParam[0] = dsParam;
        dsComboParam[1] = dsParam;
        dsComboParam[2] = dsParam;
        dsComboParam[3] = dsParam;
        return dsComboParam;
    }

    @Override
    protected void setSpecificTags(SsbServletRequest req, UserSecurityInfo userInfo) {

        templateData.put(TAG_AZIENDA, req.getField(TAG_AZIENDA));
        templateData.put(TAG_DIREZIONE, req.getField(TAG_DIREZIONE));
        templateData.put(TAG_TRATTA, req.getField(TAG_TRATTA));
        templateData.put(TAG_CONTROPARTE, ((String) req.getField(TAG_CONTROPARTE)).trim());
        templateData.put(TAG_TIPO_FLUSSO, ((String) req.getField(TAG_TIPO_FLUSSO)).trim());
        templateData.put(TAG_NOME_FLUSSO, ((String) req.getField(TAG_NOME_FLUSSO)).trim());
        templateData.put(TAG_ID_DISTINTA, ((String) req.getField(TAG_ID_DISTINTA)).trim());
        templateData.put(TAG_IMPORTO + TAG_DA, req.getField(TAG_IMPORTO + TAG_DA));
        templateData.put(TAG_IMPORTO + TAG_DA + TAG_DECIMALI, req.getField(TAG_IMPORTO + TAG_DA + TAG_DECIMALI));
        templateData.put(TAG_IMPORTO + TAG_A, req.getField(TAG_IMPORTO + TAG_A));
        templateData.put(TAG_IMPORTO + TAG_A + TAG_DECIMALI, req.getField(TAG_IMPORTO + TAG_A + TAG_DECIMALI));
        templateData.put(TAG_DIVISA, req.getField(TAG_DIVISA));
        templateData.put(TAG_STATO, req.getField(TAG_STATO));
        templateData.put(TAG_ORDER_FIELD, req.getField(TAG_ORDER_FIELD));
        templateData.put(TAG_ELAB + TAG_DA + TAG_DATA, req.getField(TAG_ELAB + TAG_DA + TAG_DATA));
        templateData.put(TAG_ELAB + TAG_DA + TAG_ORA, req.getField(TAG_ELAB + TAG_DA + TAG_ORA));
        templateData.put(TAG_ELAB + TAG_DA + TAG_MIN, req.getField(TAG_ELAB + TAG_DA + TAG_MIN));
        templateData.put(TAG_ELAB + TAG_A + TAG_DATA, req.getField(TAG_ELAB + TAG_A + TAG_DATA));
        templateData.put(TAG_ELAB + TAG_A + TAG_ORA, req.getField(TAG_ELAB + TAG_A + TAG_ORA));
        templateData.put(TAG_ELAB + TAG_A + TAG_MIN, req.getField(TAG_ELAB + TAG_A + TAG_MIN));
    }

    private boolean validate(SsbServletRequest req) {

        boolean checksOk = true;

        try {
            // Azienda
            checksOk &= CommonChecks.isValueFromDropDownMenu((String) templateData.get(TAG_AZIENDA), DS_AZIENDA,
                    dsParam, false);
            // Direzione
            if (checksOk) {
                checksOk &= CommonChecks.isValueFromDropDownMenu((String) templateData.get(TAG_DIREZIONE),
                        DS_DIREZIONE, dsParam, false);
            }
            // Tipo Tratta
            if (checksOk) {
                dsParam.put(TAG_DIREZIONE, (String) templateData.get(TAG_DIREZIONE));
                checksOk &= CommonChecks.isValueFromDropDownMenu((String) templateData.get(TAG_TRATTA), DS_TRATTA,
                        dsParam, false);
            }
            // Controparte
            if (checksOk) {
                checksOk &= CommonChecks.isValidInput((String) templateData.get(TAG_CONTROPARTE), false);
            }
            // Nome Supporto
            if (checksOk) {
                checksOk &= CommonChecks.isValidInput((String) templateData.get(TAG_NOME_FLUSSO), false);
            }
            // Tipo Distinta
            if (checksOk) {
                checksOk &= CommonChecks.isValueFromDropDownMenu((String) templateData.get(TAG_TIPO_FLUSSO),
                        DS_TIPO_DISTINTA, dsParam, false);
            }
            // Id Distinta
            if (checksOk) {
                checksOk &= CommonChecks.isValidInput((String) templateData.get(TAG_ID_DISTINTA), false);
            }
            // importo Da
            if (checksOk && Util.IsNotEmpty((String) templateData.get(TAG_IMPORTO + TAG_DA))) {
                checksOk &= CommonChecks.isNumber((String) templateData.get(TAG_IMPORTO + TAG_DA) + DECIMAL_SYMBOL
                        + (String) templateData.get(TAG_IMPORTO + TAG_DA + TAG_DECIMALI), false);
            }
            // importo A
            if (checksOk && Util.IsNotEmpty((String) templateData.get(TAG_IMPORTO + TAG_A))) {
                checksOk &= CommonChecks.isNumber((String) templateData.get(TAG_IMPORTO + TAG_A) + DECIMAL_SYMBOL
                        + (String) templateData.get(TAG_IMPORTO + TAG_A + TAG_DECIMALI), false);
            }
            // Divisa
            if (checksOk) {
                checksOk &= CommonChecks.isValidCurrency((String) templateData.get(TAG_DIVISA), false);
            }
            // Stato
            if (checksOk) {
                checksOk &= CommonChecks.isValueFromDropDownMenu((String) templateData.get(TAG_STATO), DS_STATO,
                        dsParam, false);
            }
            // DataDA, DataA
            if (checksOk) {
                Locale loc = (Locale) req.getSession().getAttribute(LOCALE);
                Timestamp TmsDa = Utils.ToTimestamp(loc, (String) templateData.get(TAG_ELAB + TAG_DA + TAG_DATA),
                        (String) templateData.get(TAG_ELAB + TAG_DA + TAG_ORA),
                        (String) templateData.get(TAG_ELAB + TAG_DA + TAG_MIN), Utils.TS_000);
                Timestamp TmsA = Utils.ToTimestamp(loc, (String) templateData.get(TAG_ELAB + TAG_A + TAG_DATA),
                        (String) templateData.get(TAG_ELAB + TAG_A + TAG_ORA),
                        (String) templateData.get(TAG_ELAB + TAG_A + TAG_MIN), Utils.TS_999);

                checksOk &= CommonChecks.isDateIntervalValid(TmsDa, TmsA);
            }

        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(), "Errore nella validazione dei campi di ricerca: " + templateData);
            checksOk = false;
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "Errore nella validazione dei campi di ricerca: " + templateData);
            checksOk = false;
        }
        return checksOk;
    }

}