
package eu.siassb.mnwinc.flusso.function;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import net.ssb.errors.AppCrash;
import net.ssb.servlet.frame.ApplicationServices_itf;
import net.ssb.servlet.frame.SsbServletRequest;
import net.ssb.servlet.frame.SsbServletResponse;
import net.ssb.servlet.security.UserSecurityInfo;
import eu.siassb.mnwinc.common.FunctionMnwInc_base;
import eu.siassb.mnwinc.flusso.enums.MaskedFieldType;

/**
 * Dettaglio Flussi Logici MNW Incassi
 * 
 * @author ConsCheriguiImed
 */
public class RicercaLogicoDettaglio extends FunctionMnwInc_base {

    // nomi TEMPLATE
    private static final String TEMPLATE_NAME = "LogicoDettaglio";

    public RicercaLogicoDettaglio(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        super.elabora(req, res, userInfo);

    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        try {
            // Preparazione della where condition
            setParams(dsParam, req, userInfo, SERVICE_LEVEL_LOGICO);
            setWhereCondition(dsParam, req);

            templateData = setCommonTags(req, userInfo);
            setSpecificTags(req, userInfo);

            // settare i parametri di ricerca
            Map<String, Object> dsParamArray[] = new HashMap[1];
            dsParamArray[0] = dsParam;

            _applicationSrv.displayPage(TEMPLATE_NAME, templateData, dsParamArray, res);
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "Elabora : " + req);
            throw ac;
        }
    }

    private void setWhereCondition(Map<String, Object> dsParam, SsbServletRequest req) throws AppCrash, ParseException {

        dsParam.put(MaskedFieldType.C_MSG_LOGICO.getCode(),
                retrieveMaskedField(req, MASKED_FIELD, MaskedFieldType.C_MSG_LOGICO));
    }

    @Override
    protected void setSpecificTags(SsbServletRequest req, UserSecurityInfo userInfo) {

    }
}