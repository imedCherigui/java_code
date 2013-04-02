
package eu.siassb.mnwinc.flusso.function;

import static eu.siassb.common.i18n.MnwResourceBundleKeys.DOCUMENT_LOGICAL;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_AZIENDA;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_CONTROPARTE;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_DATA_RICEZIONE;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_DIREZIONE;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_DIVISA;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_ID_DISTINTA;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_IMPORTO;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_NOME_FLUSSO;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_TIPO_DISTINTA;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_TRATTA;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_ULTIMO_STATO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.ssb.dataset.DBRow;
import net.ssb.errors.AppCrash;
import net.ssb.misc.Util;
import net.ssb.servlet.frame.ApplicationServices_itf;
import net.ssb.servlet.frame.SsbServletRequest;
import net.ssb.servlet.frame.SsbServletResponse;
import net.ssb.servlet.security.UserSecurityInfo;
import eu.siassb.common.helper.Utils;
import eu.siassb.common.i18n.ResourceBundleDomainEnum;

/**
 * Genera il file Excel della lista di RicercaLogico
 * 
 * @author ConsCheriguiImed
 * 
 */

public class RicercaLogicoListaXLS extends RicercaLogicoLista {

    public RicercaLogicoListaXLS(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        try {
            setFileNameDownload(getResourceBundle(ResourceBundleDomainEnum.TEXT,
                    (Locale) req.getSession().getAttribute(LOCALE)).getString(DOCUMENT_LOGICAL));
            createExcelOverResponse(req, res, userInfo);
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(), "Errore nella generazione del file excel: " + req);
            Map<String, Object> templateData = new HashMap<String, Object>();
            templateData.put(ERROR_MSG, "Errore nella generazione del file excel");
            _applicationSrv.displayPage(TEMPLATE_ERROR, templateData, res);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean createReport(SsbServletRequest req, UserSecurityInfo userInfo, WritableWorkbook workbook,
            int sheetNumber) throws AppCrash {

        boolean completato = false;
        Locale loc = (Locale) req.getSession().getAttribute(LOCALE);
        ResourceBundle rb = getResourceBundle(ResourceBundleDomainEnum.TEXT, loc);

        WritableSheet ws = workbook.createSheet(rb.getString(DOCUMENT_LOGICAL), sheetNumber);
        WritableCellFormat cellTitleFormat = Utils.getCellTileFormat();
        WritableCellFormat wcfDate = Utils.getCellFormatDate(loc);
        WritableCellFormat wcfNumber = Utils.getCellFormatNumber(loc);
        int col = 0;
        int row = 0;

        try {
            // sheets' titles
            ws.addCell(new Label(col++, row, rb.getString(FL_AZIENDA), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_DIREZIONE), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_TRATTA), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_CONTROPARTE), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_NOME_FLUSSO), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_TIPO_DISTINTA), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_ID_DISTINTA), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_IMPORTO), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_DIVISA), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_ULTIMO_STATO), cellTitleFormat));
            ws.addCell(new Label(col++, row, rb.getString(FL_DATA_RICEZIONE), cellTitleFormat));
            ws.addRowPageBreak(row);
            row++;
            col = 0;
            templateData = (Map<String, Object>) req.getSession().getAttribute(TEMPLATE_DATA);
            setParams(dsParam, req, userInfo, SERVICE_LEVEL_LOGICO);
            setWhereCondition(req);
            prepareDataSet(DATASET_NAME);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                col = 0;
                DBRow dbRow = (DBRow) dataSet.nextElement();

                ws.addCell(new Label(col++, row, Utils.concatFields(dbRow.getField(C_PROPRIETARIO),
                        dbRow.getField(C_RAG_SOCIALE))));
                ws.addCell(new Label(col++, row, (String) dbRow.getField(S_DIREZIONE)));
                ws.addCell(new Label(col++, row, (String) dbRow.getField(S_VERSO)));
                ws.addCell(new Label(col++, row, Utils.concatFields(dbRow.getField(C_RICEVENTE),
                        dbRow.getField(S_RICEVENTE))));
                ws.addCell(new Label(col++, row, (String) dbRow.getField(C_NOME_SUPPORTO)));
                ws.addCell(new Label(col++, row, (String) dbRow.getField(S_TIPO)));
                ws.addCell(new Label(col++, row, (String) dbRow.getField(C_DISTINTA)));
                ws.addCell(new Number(col++, row, Util.IsNotEmpty(dbRow.getField(N_IMPORTO)) ? ((BigDecimal) dbRow
                        .getField(N_IMPORTO)).doubleValue() : 0, wcfNumber));
                ws.addCell(new Label(col++, row, (String) dbRow.getField(S_DIVISA)));
                ws.addCell(new Label(col++, row, (String) dbRow.getField(S_ULTIMO_STATO)));
                ws.addCell(new DateTime(col++, row, (Timestamp) dbRow.getField(E_ULTIMO_STATO), wcfDate));

                ws.addRowPageBreak(row);
                row++;
            }
            completato = true;

        } catch (Throwable t) {
            throw new AppCrash(t);
        } finally {
            if (dataSet != null) dataSet.close();
        }
        return completato;
    }
}
