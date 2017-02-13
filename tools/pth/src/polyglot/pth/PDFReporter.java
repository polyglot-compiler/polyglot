package polyglot.pth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFReporter {

    public static final FontFamily sf = FontFamily.HELVETICA;
    public static final Font sfFont = new Font(sf, 9);
    public static final Font sfFontBold = new Font(sf, 9, Font.BOLD);
    public static final Font sfFontItalic = new Font(sf, 9, Font.ITALIC);

    public static final FontFamily tt = FontFamily.COURIER;
    public static final Font ttFont = new Font(tt, 9);
    public static final Font ttFontBold = new Font(tt, 9, Font.BOLD);
    public static final Font ttFontItalic = new Font(tt, 9, Font.ITALIC);
    public static final Font fontLineNo =
            new Font(tt, 9, Font.ITALIC, BaseColor.LIGHT_GRAY);
    public static final Font invisible =
            new Font(tt, 1, Font.NORMAL, BaseColor.WHITE);

    protected String filename;

    protected int chapterNo;

    protected Chapter summary;
    protected StringBuffer summarysb;
    protected int indent = 0;
    protected boolean printIndent = true;

    protected String testCollectionName;
    protected List<Chapter> chapters;
    protected Chapter chapter;
    protected StringBuffer chaptersb = new StringBuffer();
    protected Phrase phrase;

    public PDFReporter(String filename) {
        this.filename = filename;

        chapterNo = 1;

        summary = new Chapter(new Paragraph("Summary", invisible), chapterNo++);
        summary.setNumberDepth(0);
        summarysb = new StringBuffer();

        chapters = new LinkedList<>();
        phrase = new Phrase(12);
    }

    public void startTest(Test t) {
        if (t instanceof ScriptTestSuite)
            startScriptTestSuite((ScriptTestSuite) t);
        else if (t instanceof SourceFileTestCollection)
            startSourceFileTestCollection((SourceFileTestCollection) t);
        else if (t instanceof SourceFileTest)
            startSourceFileTest((SourceFileTest) t);
        else if (t instanceof BuildTest) startBuildTest((BuildTest) t);
    }

    public void finishTest(Test t) {
        if (t instanceof ScriptTestSuite)
            finishScriptTestSuite((ScriptTestSuite) t);
        else if (t instanceof SourceFileTestCollection)
            finishSourceFileTestCollection((SourceFileTestCollection) t);
        else if (t instanceof SourceFileTest)
            finishSourceFileTest((SourceFileTest) t);
        else if (t instanceof BuildTest) finishBuildTest((BuildTest) t);
    }

    protected void beginBlock() {
        indent += 2;
    }

    protected void endBlock() {
        indent -= 2;
    }

    protected void print(String s) {
        if (printIndent) printIndent();
        summarysb.append(s);
        printIndent = false;
    }

    protected void println(String s) {
        if (printIndent) printIndent();
        summarysb.append(s);
        appendChapter(summary, summarysb.toString(), indent);
        summarysb.setLength(0);
        printIndent = true;
    }

    protected void println() {
        if (printIndent) printIndent();
        summarysb.setLength(0);
        appendChapter(summary, summarysb.toString(), indent);
        summarysb.setLength(0);
        printIndent = true;
    }

    protected void printIndent() {
        for (int i = 0; i < indent; i++)
            summarysb.append(' ');
    }

    protected void startScriptTestSuite(ScriptTestSuite sts) {
        println("Test script: " + sts.getName());
        beginBlock();
    }

    protected void finishScriptTestSuite(ScriptTestSuite sts) {
        endBlock();
        String notice = sts.getNotice();
        if (notice != null) println(notice);

        if (!sts.success() && sts.getFailureMessage() != null)
            println(sts.getFailureMessage());

        println(sts.getName() + ": " + sts.getSuccessfulTestCount() + " out of "
                + sts.getExecutedTestCount() + " tests succeeded.");
    }

    protected void startSourceFileTestCollection(
            SourceFileTestCollection sftc) {
        testCollectionName = sftc.getName();
        println("Test collection: " + testCollectionName);
        beginBlock();
    }

    protected void finishSourceFileTestCollection(
            SourceFileTestCollection sftc) {
        endBlock();
        String notice = sftc.getNotice();
        if (notice != null) println(notice);

        if (!sftc.success() && sftc.getFailureMessage() != null)
            println(sftc.getFailureMessage());

        println(sftc.getSummary());
    }

    protected void startSourceFileTest(SourceFileTest sft) {
        print(anonymizedName(sft.getName()) + ": ");
        beginBlock();
    }

    protected void finishSourceFileTest(SourceFileTest sft) {
        endBlock();
        String notice = sft.getNotice();
        if (notice != null) print("[" + anonymizedName(notice) + "] ");

        String result;
        if (sft.success())
            result = "OK";
        else {
            String msg = sft.getFailureMessage();
            if (msg == null) msg = "Failed (no message)";
            result = anonymizedName(msg);
        }
        println(result);

        if (!sft.success()) {
            chapter = new Chapter(new Paragraph(
                                                testCollectionName + ": "
                                                        + anonymizedName(sft.getUniqueId()),
                                                invisible),
                                  chapterNo++);
            chapter.setNumberDepth(0);

            sft.printTestResult(this);

            chapters.add(chapter);
        }
    }

    protected void startBuildTest(BuildTest b) {
        print(b.getName() + ": ");
        beginBlock();
    }

    protected void finishBuildTest(BuildTest b) {
        endBlock();
        String notice = b.getNotice();
        if (notice != null) println(notice);

        if (b.success())
            println("OK");
        else if (b.getFailureMessage() != null)
            println(b.getFailureMessage());
        else println("Failed (no message)");
    }

    public void printHeader(String header) {
        appendChapter(chapter, header, sfFontBold);
    }

    public void printText(String content) {
        for (String line : content.split("\\n"))
            appendChapter(chapter, line);
    }

    public void printCode(File srcFile) {
        appendChapter(chapter, anonymizedName(srcFile.getPath()), ttFontItalic);
        try (BufferedReader br = new BufferedReader(new FileReader(srcFile))) {
            List<String> lines = new ArrayList<>();
            for (String line = br.readLine(); line != null; line =
                    br.readLine())
                lines.add(line);

            int numLines = lines.size();
            int numLineDigits =
                    numLines == 0 ? 0 : (int) Math.log10(numLines) + 1;
            int lineNo = 1;
            String format = "%" + numLineDigits + "d ";

            for (Iterator<String> itr =
                    lines.iterator(); itr.hasNext(); lineNo++) {
                String line = itr.next();
                phrase.add(new Chunk(String.format(format, lineNo),
                                     fontLineNo));
                appendChapter(chapter, line);
            }
        }
        catch (FileNotFoundException e) {
            appendChapter(chapter, "<file not found>");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        appendChapter(chapter, "");
    }

    public String anonymizedName(String name) {
        return name;
    }

    protected void appendChapter(Chapter chapter, String s) {
        appendChapter(chapter, s, ttFont);
    }

    protected void appendChapter(Chapter chapter, String s, int indent) {
        appendChapter(chapter, s, ttFont, indent);
    }

    protected void appendChapter(Chapter chapter, String s, Font font) {
        appendChapter(chapter, s, font, 0);
    }

    protected void appendChapter(Chapter chapter, String s, Font font,
            int indent) {
        boolean toIndent = false;
        for (String line : s.split("\\n")) {
            chaptersb.setLength(0);
            if (toIndent) {
                for (int i = 0; i < indent + 2; i++)
                    chaptersb.append(' ');
                phrase = new Phrase(12);
            }
            else toIndent = true;
            line = line.replaceAll("\t", "    ");
            chaptersb.append(line);
            phrase.add(new Chunk(chaptersb.toString(), font));
            chapter.add(phrase);
            chapter.add(Chunk.NEWLINE);
        }
        phrase = new Phrase(12);
    }

    public void flush() {
        Document doc = new Document(PageSize.LETTER, 45, 45, 36, 36);
        File output = new File(filename);
        try (FileOutputStream outFile = new FileOutputStream(output)) {
            PdfWriter writer = PdfWriter.getInstance(doc, outFile);
            writer.setPdfVersion(PdfWriter.VERSION_1_6);
            writer.setBoxSize("art", new Rectangle(45, 45, 576, 756));
            writer.setPageEvent(new HeaderFooter());
            chapterNo = 1;
            doc.open();

            doc.add(summary);

            for (Chapter chapter : chapters)
                doc.add(chapter);

            if (writer.isPageEmpty()) {
                outFile.close();
                output.delete();
            }
            else doc.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (DocumentException de) {
            System.err.println(de.getMessage());
            de.printStackTrace();
        }
    }

    public static class HeaderFooter extends PdfPageEventHelper {

        public static final Font headerFont =
                new Font(FontFamily.HELVETICA, 9, Font.BOLD);
        public static final Font footerFont = new Font(FontFamily.HELVETICA, 8);

        /** Phrase for the header. */
        Phrase header;
        /** Current page number (will be reset for every chapter). */
        int pagenumber;
        /** The template with the total number of pages. */
        PdfTemplate numPages;

        /**
         * Initialize the header, based on the chapter title;
         * reset the page number.
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onChapter(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document, float,
         *      com.itextpdf.text.Paragraph)
         */
        @Override
        public void onChapter(PdfWriter writer, Document document,
                float paragraphPosition, Paragraph title) {
            header = new Phrase(title.getContent(), headerFont);
            pagenumber = 1;
            numPages = writer.getDirectContent().createTemplate(30, 9);
        }

        /**
         * Increase the page number.
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onStartPage(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            pagenumber++;
        }

        /**
         * Adds the header and the footer.
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            PdfContentByte dc = writer.getDirectContent();
            ColumnText.showTextAligned(dc,
                                       Element.ALIGN_LEFT,
                                       header,
                                       rect.getLeft(),
                                       rect.getTop(-9),
                                       0);
            ColumnText.showTextAligned(dc,
                                       Element.ALIGN_RIGHT,
                                       new Phrase(String.format("%d/",
                                                                pagenumber),
                                                  footerFont),
                                       (rect.getLeft() + rect.getRight()) / 2,
                                       rect.getBottom(-18),
                                       0);
            dc.addTemplate(numPages,
                           (rect.getLeft() + rect.getRight()) / 2,
                           rect.getBottom(-18));
        }

        @Override
        public void onChapterEnd(PdfWriter writer, Document document,
                float paragraphPosition) {
            ColumnText.showTextAligned(numPages,
                                       Element.ALIGN_LEFT,
                                       new Phrase(String.valueOf(pagenumber),
                                                  footerFont),
                                       0,
                                       0,
                                       0);
        }
    }
}
