package eu.siassb.mnwinc.main;

import static eu.siassb.mnwinc.main.helper.CostantiGlobali.LISTA_LINGUE;
import static eu.siassb.mnwinc.main.helper.CostantiGlobali.LOCALE;
import static eu.siassb.mnwinc.main.helper.CostantiGlobali.LOCALE_COUNTRY;
import static eu.siassb.mnwinc.main.helper.CostantiGlobali.LOCALE_STRING;
import static eu.siassb.mnwinc.main.helper.CostantiGlobali.RAGIONE_SOCIALE;
import static eu.siassb.mnwinc.main.helper.CostantiGlobali.SERVICE_LANGUAGE;
import static eu.siassb.mnwinc.main.helper.CostantiGlobali.SERVICE_LEVEL;
import static eu.siassb.mnwinc.main.helper.CostantiGlobali.SERVICE_MNWINC;
import static eu.siassb.mnwinc.main.helper.CostantiGlobali.SERVICE_NAME;
import static eu.siassb.mnwinc.main.helper.CostantiGlobali.USER_ORGANIZATION;
import it.ssb.cubo.allarea.AreaUser;
import it.ssb.cubo.allarea.CuboMultiTemplateFreeMarkerPage;
import it.ssb.cubo.allarea.FunctionCubo_base;
import it.ssb.cubo.allarea.User;
import it.ssb.cubo.common.rest.xstream.pojo.Descriptor;
import it.ssb.cubo.common.rest.xstream.pojo.MetaProfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.write.WritableWorkbook;

import com.itextpdf.text.Document;

import net.ssb.dataset.DataSetFactory;
import net.ssb.dataset.DataSet_itf;
import net.ssb.dataset.Row_itf;
import net.ssb.dataset.WindowDataSetDecorator;
import net.ssb.dataset.WindowDataSet_itf;
import net.ssb.errors.AppCrash;
import net.ssb.errors.Logger;
import net.ssb.events.Event;
import net.ssb.misc.Config;
import net.ssb.misc.RedirectUtilities;
import net.ssb.misc.Util;
import net.ssb.servlet.frame.ApplicationServices_itf;
import net.ssb.servlet.frame.SsbServletRequest;
import net.ssb.servlet.frame.SsbServletResponse;
import net.ssb.servlet.security.UserSecurityInfo;

import eu.siassb.mnwinc.i18n.LanguageUtils;
import eu.siassb.mnwinc.i18n.ResourceBundleDomainEnum;
import eu.siassb.mnwinc.main.helper.Costanti_itf;
import eu.siassb.mnwinc.main.helper.Utils;
import eu.siassb.mnwinc.main.helper.enums.MaskedFieldType;

/**
 * Funzione base per l'applicazione MNW Incassi
 * 
 * @author ConsCheriguiImed
 * 
 */
public abstract class FunctionMnwinc_base extends FunctionCubo_base implements Costanti_itf {

    protected static final String DS                  = "DS.";
    protected static final String _WINDOW_SIZE        = ".WindowSize";
    private static final String   ROW_NUMBER          = "RN";
    private static final String   MASKED_FIELD_LIST   = "maskedFieldList";
    protected static final String MASKED_FIELD        = "rowNumber";

    protected WindowDataSet_itf   wds                 = null;
    protected DataSet_itf         dataSet             = null;
    protected Map<String, Object> templateData        = null;
    protected String              fileNameDownload;
    protected Map<String, Object> dsParam             = new HashMap<String, Object>();

    protected String              ORDER_BY_FIELD      = "1";                          // di default ordino sulla prima
    // colonna del
    // DS
    protected boolean             ORDER_BY_ASC        = true;                         // di default l'ordine e'
    // ascendente

    private static final String   LESS                = "<=";
    private static final String   EQUAL               = "=";
    private static final String   GREATER             = ">=";
    private static final String   LIKE                = " LIKE ";
    private static final String   BETWEEN             = " BETWEEN ";
    private static final String   AND                 = " AND ";
    protected static final String WC                  = "WC_";
    // suffissi TAG HTML
    protected static final String TAG_ELAB            = "elab";
    protected static final String TAG_IMPORTO         = "importo";
    // DATA DA/A
    protected static final String TAG_DA              = "Da";
    protected static final String TAG_A               = "A";
    protected static final String TAG_DATA            = "Data";
    protected static final String TAG_ORA             = "Ora";
    protected static final String TAG_MIN             = "Minuti";
    protected static final String TAG_DECIMALI        = "Decimali";
    protected static final String TAG_USER_NOME       = "nomeUtente";
    protected static final String TAG_USER_COGNOME    = "cognomeUtente";
    protected static final String TAG_META_PROFILE    = "metaProfilo";
    protected static final String TAG_LAST_LOGIN      = "lastLogin";
    protected static final String TAG_AMBIENTE        = "ambiente";
    protected static final String TAG_RAGIONE_SOCIALE = "ragioneSociale";

    protected static final String PAGE_HOME           = "Home";
    protected static final String STRING_DELIMITER    = "-";
    protected static final String AMBIENTE_CODICE     = "Ambiente.codice";
    private static final String   CACHE_CONTROL       = null;

    /**
     * @param applServices
     * @param functionID
     * @param functionName
     */
    public FunctionMnwinc_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    /**
     * Esegue l'azione da compiere in caso di fallimento dell'autenticazione. Poiche' questa funzione non richiede
     * autenticazione, tale metodo NON E' MAI INVOCATO.
     * 
     * @param req net.ssb.servlet.frame.SsbServletRequest Oggetto contenente la richiesta alla servlet.
     * @param res net.ssb.servlet.frame.SsbServletResponse Oggetto contenente la risposta della servlet.
     */
    @Override
    protected void authenticationFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        displayError(res, "IDMSG_UtenteNonAutenticato");
    }

    /**
     * Esegue l'azione da compiere in caso di non corretta formattazione dei campi necessari all'autenticazione. Poiche'
     * questa funzione non richiede autenticazione, tale metodo NON E' MAI INVOCATO.
     * 
     * @param req net.ssb.servlet.frame.SsbServletRequest Oggetto contenente la richiesta alla servlet.
     * @param res net.ssb.servlet.frame.SsbServletResponse Oggetto contenente la risposta della servlet.
     */
    @Override
    protected void checkFieldAutFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        displayError(res, "IDMSG_UtenteNonAutenticato");
    }

    /**
     * Esegue l'azione da compiere in caso di non corretta formattazione dei campi della richiesta.
     * 
     * @param req net.ssb.servlet.frame.SsbServletRequest Oggetto contenente la richiesta alla servlet.
     * @param res net.ssb.servlet.frame.SsbServletResponse Oggetto contenente la risposta della servlet.
     * 
     * @exception net.ssb.errors.AppCrash In caso di errori.
     */
    @Override
    protected void checkFieldFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        displayError(res, "IDMSG_ErroreParametri");
    }

    /**
     * Visualizza la pagina di errore.
     * 
     * @param res net.ssb.servlet.frame.SsbServletResponse La risposta.
     * @param error1 java.lang.String Prima riga del messaggio d'errore.
     * @param error2 java.lang.String Seconda riga del messaggio d'errore.
     * 
     * @exception net.ssb.errors.AppCrash.
     */
    protected void displayError(SsbServletResponse res, String error) throws AppCrash {

        String pageRispostaErrore = "main.app?TEMPLATE_ERROR=" + PageError + "&" + MSG1 + "=" + error;

        RedirectUtilities.fullScreenRedirect(pageRispostaErrore, res);
    }

    /**
     * Questo metodo
     * 
     * @return DOCUMENT ME!
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    protected boolean displayError(Throwable error, HttpServletRequest req, HttpServletResponse res) throws AppCrash {

        try {
            HttpSession session = req.getSession();
            String lingua = (String) session.getAttribute(LINGUA);

            if (lingua == null) {
                lingua = LINGUA_DEFAULT;
            }

            SsbServletResponse resp = (SsbServletResponse) res;
            Map<String, Object> parameter = new HashMap<String, Object>();
            parameter.put(LINGUA, lingua);
            parameter.put(ERROR_MSG, "IDMSG_ServizioSospeso");

            setPageLanguage(lingua, parameter);

            _applicationSrv.displayPage(PageError, parameter, resp);

            return true;
        } catch (AppCrash e) {
            try {
                Event crash = new Event("CrashEvent");
                crash.raise();
            } catch (Throwable err) {
            }
            return false;
        }
    }

    // le restrizioni sui dati verranno fatte in alto modo (direttamente su query estrazione)
    // protected Abilitazioni getAbilitazioni(AreaUser areaUser) throws AppCrash {
    //
    // try {
    // XmlObject dataRestrictions = areaUser.getDataRestrictions();
    // XStream xstream = new XStream();
    // xstream.processAnnotations(Abilitazioni.class);
    // Abilitazioni abilitazioni = (Abilitazioni) xstream.fromXML(dataRestrictions.getXml());
    //
    // return abilitazioni;
    //
    // } catch (AppCrash ap) {
    // ap.logContext(getClass().getSimpleName(),
    // "Errore recuperando le abilitazioni per l'utente " + areaUser.getUserId());
    // throw ap;
    // }
    // }

    @SuppressWarnings("unchecked")
    protected HashMap<String, Object>[] getDsArray(Map<String, Object> templateData) {

        HashMap<String, Object>[] dsArray = new HashMap[0];

        return dsArray;
    }

    /*
     * Solo FunctionHome richiede l'autentificazione le altre funzioni usano quella in sessione
     * 
     * @see it.ssb.cubo.allarea.FunctionCubo_base#isAuthenticationRequired()
     */
    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    /**
     * Questo metodo inserisce nella map tutti i campi presenti nella request e restituisce la map risultante
     * 
     * @param req net.ssb.servlet.frame.SsbServletRequest ServletRequest da cui vengono copiati i campi di input
     * @param campi_della_request java.util.Map Map cui vengono accodati i campi della ServletRequest
     * 
     * @return java.util.Map Map contenente tutti i campi della map in input e tutti i campi della ServletRequest
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, Object> letturaParametriRequest(SsbServletRequest req, Map<String, Object> campi_della_request) {

        Enumeration<String> nomi_campi_della_request = req.getParameterNames();
        String valore = VALORE_VUOTO;
        String chiave = VALORE_VUOTO;

        while (nomi_campi_della_request.hasMoreElements()) {
            chiave = nomi_campi_della_request.nextElement();
            valore = req.getParameter(chiave);
            campi_della_request.put(chiave, valore);
        }

        Enumeration<String> requestAttributeNames = req.getAttributeNames();

        while (requestAttributeNames.hasMoreElements()) {
            chiave = requestAttributeNames.nextElement();
            Object attribute = req.getAttribute(chiave);
            campi_della_request.put(chiave, attribute);
        }

        return campi_della_request;
    }

    /**
     * Questo metodo viene richiamato dal framework se il controllo sui permessi di accesso alla funzione fallisce. Di
     * default esegue la displayPage() di una pagina. La pagina si chiama 'PageError' e deve esistere nel file di
     * configurazione. In questa pagina vengono settati due parametri: 'Name' con il functionID della funzione e 'Msg'
     * con un messaggi del framework.
     * <P>
     * Le classi concrete che estendono function_base dovrebbero ridefinire questo metodo per dare alla applicazione un
     * comportamento consono alle sue necssita'.
     * 
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @param message java.lang.String Il messaggio da visualizzare.
     * @exception net.ssb.errors.AppCrash.
     * @return void.
     */
    @Override
    protected void permissionFailed(SsbServletRequest req, SsbServletResponse res, String message) throws AppCrash {

        displayError(res, "IDMSG_UtenteNonAbilitato");
    }

    @Override
    protected Map<String, Object> setCommonTags(SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = super.setCommonTags(req, userInfo);

        User user = ((AreaUser) userInfo).getUser();
        if (user != null) {
            templateData.put(CuboMultiTemplateFreeMarkerPage.TEMPLATE_VARIANT, user.getIdDominio());
        }

        MetaProfile metaProfile = MetaProfile.getMetaProfile(user.getUrlMetaProfile());
        Descriptor descriptor = metaProfile.getDescriptor();
        // Locale language = (Locale) req.getSession().getAttribute(LOCALE);

        templateData.put(LISTA_LINGUE, LanguageUtils.getMnwLanguages());
        templateData.put(TAG_RAGIONE_SOCIALE, req.getSession().getAttribute(RAGIONE_SOCIALE));
        templateData.put(TAG_USER_NOME, user.getName());
        templateData.put(TAG_USER_COGNOME, user.getSurname());

        // TODO: Non mi piace questa cosa. E' possibile che l'utente abbia un descriptor in cui la label ha come _idLang
        // solo
        // "en"? Anche in pagamenti viene passata la stringa secca "it"
        templateData.put(TAG_META_PROFILE, descriptor.getShortDesc("it"));

        templateData.put(TAG_LAST_LOGIN, user.getLastLogin());
        templateData.put(TAG_AMBIENTE, Config.GetInstance().getProperty(AMBIENTE_CODICE));

        // TODO un giro assurdo da rivedere
        templateData.put(LOCALE_COUNTRY, LanguageUtils.getLocaleFromCuboLanguage(((AreaUser) userInfo).getIdLang())
                .getCountry());

        return templateData;
    }

    /**
     * Questo metodo
     * 
     * @param lingua
     * @param templateData
     * 
     * @see it.ssb.cubo.allarea.FunctionCubo_base#setPageLanguage(java.lang.String, java.util.Map)
     */
    @Override
    protected void setPageLanguage(String lingua, Map<String, Object> templateData) {

        super.setPageLanguage(lingua, templateData);

        Locale.setDefault(new Locale(lingua));
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        super.mostra(req, res, userInfo);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        super.elabora(req, res, userInfo);
    }

    /**
     * @param SsbServletRequest req
     * @param dominio: TEXT, AZIONE, FORMAT, ecc.
     * @return Il resource bundle richiesto
     */
    protected static final ResourceBundle getResourceBundle(ResourceBundleDomainEnum dominio, SsbServletRequest req) {

        Locale loc = (Locale) req.getSession().getAttribute(LOCALE);

        if (!Util.IsNotEmpty(loc)) {
            Logger.GetInstance().log0("Locale in sessione non trovato!!");
        }

        return getResourceBundle(dominio, loc);
    }

    /**
     * @param loc: Locale
     * @param dominio: TEXT, AZIONE, FORMAT, ecc.
     * @return Il resource bundle richiesto
     */
    protected static final ResourceBundle getResourceBundle(ResourceBundleDomainEnum dominio, Locale loc) {

        // Recupero nome della classe ResourceBundle
        String baseName = Config.GetInstance().getProperty("MultiLanguage." + dominio.name() + ".ResourceBundle.File");

        if (!Util.IsNotEmpty(baseName)) {
            Logger.GetInstance().log0(
                    "Classe ResourceBundle non trovata: " + "MultiLanguage." + dominio.name() + ".ResourceBundle.File");
        }

        return ResourceBundle.getBundle(baseName, loc);
    }

    /**
     * Serve per impostare nelle implementazioni specifiche i campi che arrivano dalle Request per le funzioni
     * specifiche
     * 
     * @param req
     * @param userInfo
     */
    protected abstract void setSpecificTags(SsbServletRequest req, UserSecurityInfo userInfo);

    /**
     * Serve per creare la WC di un campo DB di tipo VARCHAR
     * 
     * @param dbField
     * @return String
     */
    protected String setWCStringField(String htmlField, String dbField) {

        String value = (String) templateData.get(htmlField);
        return (Util.IsNotEmpty(value)) ? AND + dbField + EQUAL + "'" + value + "'" : VALORE_VUOTO;

    }

    /**
     * Serve per creare la WC di un campo DB di tipo VARCHAR con condizione LIKE
     * 
     * @param dbField
     * @return String
     */
    protected String setWCStringFieldLike(String htmlField, String dbField) {

        String value = (String) templateData.get(htmlField);
        return (Util.IsNotEmpty(value)) ? AND + dbField + LIKE + "'%" + value + "%'" : VALORE_VUOTO;

    }

    /**
     * Questo metodo crea la where condition per il range di due valori numerici.
     * 
     * @param htmlPrefix java.lang.String Il prefisso dei campi numerici da utilizzare (viene aggiunto 'Da', 'A' e
     *            'Decimali'.
     * @param dbField java.lang.String Il nome del campo su db sul quale creare il filtro.
     * @return java.lang.String La where condition.
     */
    protected String setWCNumericField(String htmlPrefix, String dbField) {

        String result = VALORE_VUOTO;
        String[] importi = new String[2];
        if (Util.IsNotEmpty(templateData.get(htmlPrefix + TAG_DA)) && Util.IsNotEmpty(htmlPrefix + TAG_A)) {
            importi = Utils.getOracleRangeAmount((String) templateData.get(htmlPrefix + TAG_DA), (String) templateData
                    .get(htmlPrefix + TAG_A), (String) templateData.get(htmlPrefix + TAG_DA + TAG_DECIMALI),
                    (String) templateData.get(htmlPrefix + TAG_A + TAG_DECIMALI));

            result = AND + dbField + BETWEEN + importi[0] + AND + importi[1];
        }
        return result;
    }

    /**
     * Questo metodo crea la where condition per il range di due valori data.
     * 
     * @param templateData
     * @param loc
     * @param htmlPrefix java.lang.String Il prefisso dei campi data da utilizzare (viene aggiunto 'Da', 'A', 'Data',
     *            'Ora' e 'Minuti'.
     * @param dbField java.lang.String Il nome del campo su db sul quale creare il filtro.
     * @return java.lang.String La where condition.
     */
    protected String setWCDateField(Locale loc, String htmlPrefix, String dbField) throws ParseException {

        String result = VALORE_VUOTO;

        String TmsDa = Utils.getOracleTimestamp(Utils.ToTimestamp(loc, (String) templateData.get(htmlPrefix + TAG_DA
                + TAG_DATA), (String) templateData.get(htmlPrefix + TAG_DA + TAG_ORA), (String) templateData
                .get(htmlPrefix + TAG_DA + TAG_MIN), Utils.TS_000));
        String TmsA = Utils.getOracleTimestamp(Utils.ToTimestamp(loc, (String) templateData.get(htmlPrefix + TAG_A
                + TAG_DATA), (String) templateData.get(htmlPrefix + TAG_A + TAG_ORA), (String) templateData
                .get(htmlPrefix + TAG_A + TAG_MIN), Utils.TS_999));

        if (Util.IsNotEmpty(TmsDa) && Util.IsNotEmpty(TmsDa)) {
            result = AND + dbField + BETWEEN + TmsDa + AND + TmsA;
        }
        return result;
    }

    protected void setParams(Map<String, Object> dsParam, SsbServletRequest req, UserSecurityInfo userInfo, String level) {

        dsParam.put(USER_ORGANIZATION, ((AreaUser) userInfo).getOrganization());
        dsParam.put(SERVICE_LEVEL, level);
        dsParam.put(SERVICE_NAME, SERVICE_MNWINC);
        dsParam.put(SERVICE_LANGUAGE, (String) req.getSession().getAttribute(LOCALE_STRING));
    }

    /**
     * Maschera il valore di una colonna con un identificativo numerico e mette l'elenco in sessione
     * 
     * @param req
     * @param wds
     * @param columnName
     * @return void
     * @throws AppCrash
     */

    protected void setMaskedField(SsbServletRequest req, WindowDataSet_itf wds, String columnName) throws AppCrash {

        Map<String, String> maskedField = new HashMap<String, String>();

        try {
            if (wds instanceof WindowDataSetDecorator) {
                while (wds.hasMoreElements()) {
                    Row_itf dsRow = (Row_itf) wds.nextElement();
                    maskedField.put(dsRow.getField(ROW_NUMBER).toString(), (String) dsRow.getField(columnName));
                }
                req.getSession().setAttribute(MASKED_FIELD_LIST, maskedField);
            }
        } catch (Throwable e) {
            AppCrash ac = new AppCrash("Errore nel salvataggio dei MaskedField");
            throw ac;
        }

    }

    /**
     * Recupera il valore salvato in sessione per il campo (solitamente un ID) richiesto.
     * 
     * @param req
     * @param htmlFieldIndex
     * @param maskedFieldType
     * @return String
     * @throws AppCrash
     */
    @SuppressWarnings("unchecked")
    protected String retrieveMaskedField(SsbServletRequest req, String htmlFieldIndex, MaskedFieldType maskedFieldType)
            throws AppCrash {

        Map<String, String> maskedFieldList = null;
        String id = "";

        try {
            String index = req.getField(htmlFieldIndex);
            // String index = "1";
            maskedFieldList = (Map<String, String>) req.getSession().getAttribute(MASKED_FIELD_LIST);

            Logger.GetInstance().log3(
                    "RetrieveMaskField: Tipo campo richiesto [" + maskedFieldType.getCode() + "], campo ["
                            + htmlFieldIndex + "], valore recuperato [" + id + "]");
            id = maskedFieldList.get(index);

        } catch (IndexOutOfBoundsException ioobe) {
            // se mi e' stato passato un indice non esistente nella lista non lancio un eccezione ma restituisco una
            // stringa vuota: mi stanno chiedendo un ID non originariamente presente, restituisco niente

            AppCrash ac = new AppCrash(ioobe);
            ac.logContext(Utils.class.getCanonicalName(), "Indice richiesto [" + htmlFieldIndex
                    + "] non presente nella lista in sessione [" + maskedFieldType.getCode() + "]");
            throw ac;
        } catch (Exception e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(Utils.class.getCanonicalName(), "Errore nel recupero dell'ID originale [" + htmlFieldIndex
                    + "], tipo [" + maskedFieldType.getCode() + "]");
            throw ac;
        }
        return id;

    }

    /**
     * Legge la property di massimo numero di pagine visualizzabili e setta il relativo tag per i controlli di frontend
     * 
     * @param datasetName
     * @return none
     */
    protected void setMaxNumberOfPagesTag(String datasetName) {

        String num_massimo_di_pagine_str = Config.GetInstance().getProperty(
                "DS." + datasetName + ".NumeroMassimoDiPagine");
        int num_massimo_di_pagine = Integer.parseInt(num_massimo_di_pagine_str);
        templateData.put(NUMERO_PAGINE_MAX, new Integer(num_massimo_di_pagine));
    }

    /**
     * Questo metodo controlla la presenza di un template associato al dominio corrente, in caso contrario torna il
     * template di default.
     * 
     * @param userInfo
     * @return String
     * @throws AppCrash
     * 
     * @see net.ssb.servlet.frame.Function_base#mostra(net.ssb.servlet.frame.SsbServletRequest,
     *      net.ssb.servlet.frame.SsbServletResponse, net.ssb.servlet.security.UserSecurityInfo)
     */
    protected String getPage(UserSecurityInfo userInfo) throws AppCrash {

        User user = ((AreaUser) userInfo).getUser();
        if ((existsPage(PAGE_HOME + STRING_DELIMITER + user.getIdDominio()))) {
            return PAGE_HOME + STRING_DELIMITER + user.getIdDominio();
        } else {
            return PAGE_HOME;
        }
    }

    /**
     * Questo metodo controlla la presenza di un template associato al dominio corrente
     * 
     * @param pageName
     * @return true se esiste altrimenti false
     * @throws AppCrash
     */
    protected boolean existsPage(String pageName) throws AppCrash {

        if (Util.IsNotEmpty(Config.GetInstance().getProperty("Page." + pageName + ".Template.Name"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Questo metodo crea WindowDataSet
     * 
     * @param dataSetName
     * @throws AppCrash
     */
    protected void prepareWindowDataSet(String dataSetName) throws AppCrash {

        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            wds = dsFactory.makeWindowDataSet("", dataSetName);
            wds.setParam(dsParam);

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "Errore nella validazione dei campi di ricerca :" + dataSetName);
            throw ac;
        }
    }

    /**
     * Questo metodo crea DataSet
     * 
     * @param dataSetName
     * @throws AppCrash
     */
    protected void prepareDataSet(String dataSetName) throws AppCrash {

        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", dataSetName);
            dataSet.setParam(dsParam);

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "Errore nella validazione dei campi di ricerca :" + dataSetName);
            throw ac;
        }
    }

    // metodi per la generazione di file pdf

    protected void createPdfOverResponse(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
            throws AppCrash {

        try {
            byte[] outArray = createPDF(req, userInfo);
            if (Util.IsNotEmpty(outArray)) {
                // settari i parametri della response
                res.reset();
                res.setContentType(CONTENT_TYPE_PDF);
                res.addHeader(CONTENT_DISPOSITION, CONTENT_TYPE_INLINE + getFileNamePDF());
                res.setHeader(CACHE_CONTROL, CONTENT_MUST_REVALIDATE);
                OutputStream outStream = res.getOutputStream();
                outStream.write(outArray);
                outStream.flush();
            } else {
                throw new AppCrash();
            }

        } catch (AppCrash ac) {
            ac.logContext(this.getClass().toString(), "Errore nella generazione del file excel");
            Logger.GetInstance().log0(ac.getMessage());
            throw ac;
        } catch (Exception e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), e.getMessage());
            Logger.GetInstance().log0(e.getMessage());
            throw ac;
        }

    }

    protected byte[] createPDF(SsbServletRequest req, UserSecurityInfo userInfo) throws Exception {

        byte[] pdfStream = null;

        try {
            ByteArrayOutputStream pdfBytesStream = new ByteArrayOutputStream();

            if (createReportPDF(req, userInfo, pdfBytesStream)) {
                pdfBytesStream.flush();
                pdfBytesStream.close();
                pdfStream = pdfBytesStream.toByteArray();
            }
            return pdfStream;

        } catch (Throwable t) {
            return null;
        }

    }

    public String getFileNamePDF() {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        fileNameDownload += UNDER_SCROLL + sdf.format(new Date()) + CONTENT_FORMAT_PDF;
        return fileNameDownload;
    }

    // metodi per la generazione di file excel

    protected void createExcelOverResponse(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
            throws AppCrash {

        try {
            byte[] outArray = createExcel(req, userInfo);
            if (Util.IsNotEmpty(outArray)) {
                // settari i parametri della response
                res.reset();
                res.setContentType(CONTENT_TYPE_EXCEL);
                res.addHeader(CONTENT_DISPOSITION, CONTENT_TYPE_INLINE + getFileNameXLS());
                res.setHeader(CACHE_CONTROL, CONTENT_MUST_REVALIDATE);
                OutputStream outStream = res.getOutputStream();
                outStream.write(outArray);
                outStream.flush();
            } else {
                throw new AppCrash();
            }

        } catch (AppCrash ac) {
            ac.logContext(this.getClass().toString(), "Errore nella generazione del file excel");
            Logger.GetInstance().log0(ac.getMessage());
            throw ac;
        } catch (Exception e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), e.getMessage());
            Logger.GetInstance().log0(e.getMessage());
            throw ac;
        }

    }

    protected byte[] createExcel(SsbServletRequest req, UserSecurityInfo userInfo) throws Exception {

        byte[] outArray = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            WritableWorkbook workbook = Workbook.createWorkbook(baos);
            if (createReport(req, userInfo, workbook, 0)) {
                workbook.write();
                workbook.close();
                baos.flush();
                baos.close();

                outArray = baos.toByteArray();
            }
            return outArray;

        } catch (Throwable t) {
            return null;
        }

    }

    protected boolean createReport(SsbServletRequest req, UserSecurityInfo userInfo, WritableWorkbook workbook, int i)
            throws AppCrash {

        return true;
    }

    protected boolean createReportPDF(SsbServletRequest req, UserSecurityInfo userInfo,
            ByteArrayOutputStream pdfBytesStream) throws AppCrash {

        return true;
    }

    public String getFileNameXLS() {

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        fileNameDownload += UNDER_SCROLL + sdf.format(new Date()) + CONTENT_FORMAT_EXCEL;
        return fileNameDownload;
    }

    /**
     * @return
     */
    public void setFileNameDownload(String fileNameDownload) {

        this.fileNameDownload = fileNameDownload;
    }
}
