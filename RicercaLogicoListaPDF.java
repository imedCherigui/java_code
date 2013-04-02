
package eu.siassb.mnwinc.flusso.function;

import static eu.siassb.common.i18n.MnwResourceBundleKeys.DATA_ORA_PROCESSO;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.DOCUMENT_LOGICAL;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.DOCUMENT_LOGICAL_TITLE;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_AZIENDA;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_CONTROPARTE;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_DIREZIONE;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_DIVISA;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_ID_DISTINTA;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_IMPORTO;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_NOME_FLUSSO;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_STATO;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_TIPO_DISTINTA;
import static eu.siassb.common.i18n.MnwResourceBundleKeys.FL_TRATTA;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import net.ssb.dataset.DBRow;
import net.ssb.errors.AppCrash;
import net.ssb.servlet.frame.ApplicationServices_itf;
import net.ssb.servlet.frame.SsbServletRequest;
import net.ssb.servlet.frame.SsbServletResponse;
import net.ssb.servlet.security.UserSecurityInfo;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import eu.siassb.common.i18n.ResourceBundleDomainEnum;

/**
 * Genera il file PDF della lista di RicercaLogico
 * 
 * @author ConsCheriguiImed
 * 
 */

public class RicercaLogicoListaPDF extends RicercaLogicoLista {

    public RicercaLogicoListaPDF(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        setFileNameDownload(getResourceBundle(ResourceBundleDomainEnum.TEXT,
                (Locale) req.getSession().getAttribute(LOCALE)).getString(DOCUMENT_LOGICAL));
        createPdfOverResponse(req, res, userInfo);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean createReportPDF(SsbServletRequest req, UserSecurityInfo userInfo,
            ByteArrayOutputStream pdfBytesStream) throws AppCrash {

        boolean completato;
        completato = false;
        ResourceBundle rb = getResourceBundle(ResourceBundleDomainEnum.TEXT,
                (Locale) req.getSession().getAttribute(LOCALE));

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 40, 20);

        try {
            PdfWriter.getInstance(document, pdfBytesStream);
            document.open();
            PdfPTable table1 = new PdfPTable(11);
            table1.setWidthPercentage(100f);
            table1.setWidths(new float[] { 2.3f, 1.7f, 2.2f, 3, 2, 1.5f, 2.4f, 1.5f, 0.8f, 1.5f, 2 });
            table1.setSpacingBefore(10);

            document.add(getHeaderTitle(rb.getString(DOCUMENT_LOGICAL_TITLE)));

            addPdfCellTitle(table1, rb.getString(FL_AZIENDA));
            addPdfCellTitle(table1, rb.getString(FL_DIREZIONE));
            addPdfCellTitle(table1, rb.getString(FL_TRATTA));
            addPdfCellTitle(table1, rb.getString(FL_CONTROPARTE));
            addPdfCellTitle(table1, rb.getString(FL_NOME_FLUSSO));
            addPdfCellTitle(table1, rb.getString(FL_TIPO_DISTINTA));
            addPdfCellTitle(table1, rb.getString(FL_ID_DISTINTA));
            addPdfCellTitle(table1, rb.getString(FL_IMPORTO));
            addPdfCellTitle(table1, rb.getString(FL_DIVISA));
            addPdfCellTitle(table1, rb.getString(FL_STATO));
            addPdfCellTitle(table1, rb.getString(DATA_ORA_PROCESSO));

            templateData = (Map<String, Object>) req.getSession().getAttribute(TEMPLATE_DATA);
            setParams(dsParam, req, userInfo, SERVICE_LEVEL_LOGICO);
            setWhereCondition(req);
            prepareDataSet(DATASET_NAME);
            dataSet.open();

            List<String> azienda = new ArrayList<String>();
            azienda.add(C_PROPRIETARIO);
            azienda.add(C_RAG_SOCIALE);

            List<String> controparte = new ArrayList<String>();
            controparte.add(C_RICEVENTE);
            controparte.add(S_RICEVENTE);

            while (dataSet.hasMoreElements()) {
                DBRow dbRow = (DBRow) dataSet.nextElement();
                table1.addCell(new PdfPCell(getConcatPhrase(dbRow, azienda, req)));
                table1.addCell(new PdfPCell(getPhrase(dbRow, S_DIREZIONE, req)));
                table1.addCell(new PdfPCell(getPhrase(dbRow, S_VERSO, req)));
                table1.addCell(new PdfPCell(getConcatPhrase(dbRow, controparte, req)));
                table1.addCell(new PdfPCell(getPhrase(dbRow, C_NOME_SUPPORTO, req)));
                table1.addCell(new PdfPCell(getPhrase(dbRow, S_TIPO, req)));
                table1.addCell(new PdfPCell(getPhrase(dbRow, C_DISTINTA, req)));
                table1.addCell(new PdfPCell(getPhrase(dbRow, N_IMPORTO, req)));
                table1.addCell(new PdfPCell(getPhrase(dbRow, S_DIVISA, req)));
                table1.addCell(new PdfPCell(getPhrase(dbRow, S_ULTIMO_STATO, req)));
                table1.addCell(new PdfPCell(getPhrase(dbRow, E_ULTIMO_STATO, req)));
            }

            dataSet.close();
            document.add(table1);

        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "Errore nella generazione del documento PDF");
            throw ac;
        }

        document.close();

        completato = true;

        return completato;
    }

}
