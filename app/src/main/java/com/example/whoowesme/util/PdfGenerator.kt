package com.example.whoowesme.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.whoowesme.R
import com.example.whoowesme.model.MoneyTransaction
import com.example.whoowesme.model.Person
import com.example.whoowesme.model.enums.TransactionType
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {
    fun openPdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.pdf_open_with)))
        } catch (e: Exception) {
            // No app to handle PDF
        }
    }

    fun generateMasterStatement(
        context: Context,
        data: List<Triple<Person, List<MoneyTransaction>, Double>>,
        totalNetBalance: Double
    ): File? {
        val fileName = "Full_Statement_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            val writer = PdfWriter(FileOutputStream(file))
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            document.setMargins(40f, 40f, 40f, 40f)

            val primaryColor = DeviceRgb(0, 51, 42) // Deep Emerald
            val brandColor = DeviceRgb(0, 77, 64) // Brand Emerald
            val lightGray = DeviceRgb(248, 249, 250)
            val borderColor = DeviceRgb(233, 236, 239)
            val textSecondary = DeviceRgb(108, 117, 125)

            // Header Section
            val headerTable = Table(UnitValue.createPointArray(floatArrayOf(350f, 150f)))
            headerTable.setWidth(UnitValue.createPercentValue(100f))

            headerTable.addCell(Cell().add(Paragraph(context.getString(R.string.export_statements_title))
                .setBold()
                .setFontSize(28f)
                .setFontColor(primaryColor))
                .setBorder(Border.NO_BORDER))

            headerTable.addCell(Cell().add(Paragraph(context.getString(R.string.pdf_app_name))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10f)
                .setBold()
                .setFontColor(brandColor)
                .setPaddingTop(15f))
                .setBorder(Border.NO_BORDER))

            document.add(headerTable)
            document.add(Paragraph("").setBorderBottom(SolidBorder(borderColor, 0.5f)).setMarginTop(5f).setMarginBottom(25f))

            // Master Summary Box
            val summaryTable = Table(1)
            summaryTable.setWidth(UnitValue.createPercentValue(100f))
            val netColor = if (totalNetBalance >= 0) DeviceRgb(0, 150, 136) else DeviceRgb(211, 47, 47)
            summaryTable.addCell(Cell().add(Paragraph(context.getString(R.string.total_net_balance)).setFontSize(10f).setBold().setFontColor(textSecondary))
                .add(Paragraph(MoneyFormatter.format(totalNetBalance))
                    .setBold().setFontSize(26f).setFontColor(netColor))
                .setBorder(Border.NO_BORDER).setPadding(20f).setBackgroundColor(lightGray).setTextAlignment(TextAlignment.CENTER))
            document.add(summaryTable)
            document.add(Paragraph("\n"))

            // NEW: Consolidated Summary Table
            document.add(Paragraph(context.getString(R.string.pdf_consolidated_summary)).setBold().setFontSize(14f).setFontColor(primaryColor).setMarginBottom(10f))
            val summaryListTable = Table(UnitValue.createPointArray(floatArrayOf(350f, 150f)))
            summaryListTable.setWidth(UnitValue.createPercentValue(100f))
            
            data.forEach { (person, _, balance) ->
                val balanceColor = when {
                    balance > 0 -> DeviceRgb(46, 125, 50)
                    balance < 0 -> DeviceRgb(198, 40, 40)
                    else -> textSecondary
                }
                summaryListTable.addCell(Cell().add(Paragraph(person.name).setFontSize(11f)).setBorder(Border.NO_BORDER).setBorderBottom(SolidBorder(borderColor, 0.5f)).setPadding(8f))
                summaryListTable.addCell(Cell().add(Paragraph(MoneyFormatter.format(balance)).setBold().setFontSize(11f).setFontColor(balanceColor).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER).setBorderBottom(SolidBorder(borderColor, 0.5f)).setPadding(8f))
            }
            document.add(summaryListTable)

            // Individual Breakdowns
            data.forEach { (person, transactions, balance) ->
                document.add(AreaBreak())
                addPersonToDocument(context, document, person, transactions, balance)
            }

            addFooter(context, document)
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun addPersonToDocument(
        context: Context,
        document: Document,
        person: Person,
        transactions: List<MoneyTransaction>,
        totalBalance: Double
    ) {
        val primaryColor = DeviceRgb(0, 51, 42)
        val accentColor = if (totalBalance >= 0) DeviceRgb(0, 150, 136) else DeviceRgb(211, 47, 47)
        val borderColor = DeviceRgb(233, 236, 239)
        val textSecondary = DeviceRgb(108, 117, 125)

        val personHeader = Table(UnitValue.createPointArray(floatArrayOf(300f, 200f)))
        personHeader.setWidth(UnitValue.createPercentValue(100f))
        
        personHeader.addCell(Cell().add(Paragraph(person.name).setBold().setFontSize(20f).setFontColor(primaryColor))
            .add(Paragraph(person.phoneNumber).setFontSize(10f).setFontColor(textSecondary))
            .setBorder(Border.NO_BORDER))
        
        val balanceLabel = if (totalBalance >= 0) context.getString(R.string.pdf_total_receivable) else context.getString(R.string.pdf_total_payable)
        personHeader.addCell(Cell().add(Paragraph(balanceLabel).setFontSize(8f).setBold().setFontColor(textSecondary).setTextAlignment(TextAlignment.RIGHT))
            .add(Paragraph(MoneyFormatter.format(totalBalance, absolute = true)).setBold().setFontSize(18f).setFontColor(accentColor).setTextAlignment(TextAlignment.RIGHT))
            .setBorder(Border.NO_BORDER))
        
        document.add(personHeader)
        document.add(Paragraph("\n"))

        if (transactions.isEmpty()) {
            document.add(Paragraph(context.getString(R.string.person_detail_empty_subtitle))
                .setFontSize(10f)
                .setFontColor(textSecondary)
                .setItalic())
            return
        }

        val table = Table(UnitValue.createPointArray(floatArrayOf(80f, 270f, 150f)))
        table.setWidth(UnitValue.createPercentValue(100f))
        val headers = arrayOf(context.getString(R.string.pdf_header_date), context.getString(R.string.pdf_header_description), context.getString(R.string.pdf_header_amount))
        headers.forEach { h ->
            table.addHeaderCell(Cell().add(Paragraph(h).setBold().setFontSize(9f).setFontColor(textSecondary)).setBorder(Border.NO_BORDER).setBorderBottom(SolidBorder(primaryColor, 1f)).setPadding(8f))
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        transactions.sortedByDescending { it.date }.forEachIndexed { i, tx ->
            val bottomBorder = if (i == transactions.size - 1) SolidBorder(primaryColor, 1f) else SolidBorder(borderColor, 0.5f)
            val txColor = if (tx.type == TransactionType.GIVEN) DeviceRgb(46, 125, 50) else DeviceRgb(198, 40, 40)
            
            table.addCell(Cell().add(Paragraph(dateFormat.format(Date(tx.date))).setFontSize(9f)).setBorder(Border.NO_BORDER).setBorderBottom(bottomBorder).setPadding(8f))
            
            val descText = if (tx.type == TransactionType.GIVEN) context.getString(R.string.pdf_funds_sent) else context.getString(R.string.pdf_funds_received)
            val descPara = Paragraph().add(Paragraph(descText).setBold().setFontSize(10f))
            if (tx.note.isNotBlank()) descPara.add(Paragraph("\n" + tx.note).setFontSize(8f).setFontColor(textSecondary).setItalic())
            table.addCell(Cell().add(descPara).setBorder(Border.NO_BORDER).setBorderBottom(bottomBorder).setPadding(8f))
            
            val amountPrefix = if (tx.type == TransactionType.GIVEN) "+" else "-"
            table.addCell(Cell().add(Paragraph(amountPrefix + MoneyFormatter.format(tx.amount)).setFontSize(10f).setBold().setFontColor(txColor).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER).setBorderBottom(bottomBorder).setPadding(8f))
        }
        document.add(table)
    }

    private fun addFooter(context: Context, document: Document) {
        val textSecondary = DeviceRgb(108, 117, 125)
        document.add(Paragraph("\n\n"))
        val footerTable = Table(UnitValue.createPointArray(floatArrayOf(250f, 250f)))
        footerTable.setWidth(UnitValue.createPercentValue(100f))
        val generationDateText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        footerTable.addCell(Cell().add(Paragraph(context.getString(R.string.pdf_generated_on, generationDateText)).setFontSize(8f).setFontColor(textSecondary)).setBorder(Border.NO_BORDER))
        footerTable.addCell(Cell().add(Paragraph(context.getString(R.string.pdf_footer_tagline)).setFontSize(8f).setItalic().setFontColor(textSecondary).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER))
        document.add(footerTable)
    }

    fun generateStatement(
        context: Context,
        person: Person,
        transactions: List<MoneyTransaction>,
        totalBalance: Double
    ): File? {
        val fileName = "Statement_${person.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        
        try {
            val writer = PdfWriter(FileOutputStream(file))
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            document.setMargins(40f, 40f, 40f, 40f)

            val primaryColor = DeviceRgb(0, 51, 42) // Deep Emerald
            val brandColor = DeviceRgb(0, 77, 64) // Brand Emerald
            val accentColor = if (totalBalance >= 0) DeviceRgb(0, 150, 136) else DeviceRgb(211, 47, 47)
            val lightGray = DeviceRgb(248, 249, 250)
            val borderColor = DeviceRgb(233, 236, 239)
            val textSecondary = DeviceRgb(108, 117, 125)

            // Header Section
            val headerTable = Table(UnitValue.createPointArray(floatArrayOf(350f, 150f)))
            headerTable.setWidth(UnitValue.createPercentValue(100f))
            
            headerTable.addCell(Cell().add(Paragraph(context.getString(R.string.pdf_statement_title))
                .setBold()
                .setFontSize(28f)
                .setFontColor(primaryColor))
                .setBorder(Border.NO_BORDER))

            headerTable.addCell(Cell().add(Paragraph(context.getString(R.string.pdf_app_name))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10f)
                .setBold()
                .setFontColor(brandColor)
                .setPaddingTop(15f))
                .setBorder(Border.NO_BORDER))
            
            document.add(headerTable)
            document.add(Paragraph("").setBorderBottom(SolidBorder(borderColor, 0.5f)).setMarginTop(5f).setMarginBottom(25f))
            
            val mainInfoTable = Table(UnitValue.createPointArray(floatArrayOf(250f, 250f)))
            mainInfoTable.setWidth(UnitValue.createPercentValue(100f))
            
            val clientCell = Cell().add(Paragraph(context.getString(R.string.pdf_client_details)).setFontSize(8f).setBold().setFontColor(textSecondary).setMarginBottom(4f))
                .add(Paragraph(person.name).setBold().setFontSize(16f).setMarginTop(0f))
            if (person.phoneNumber.isNotEmpty()) {
                clientCell.add(Paragraph(person.phoneNumber).setFontSize(10f).setFontColor(textSecondary))
            }
            mainInfoTable.addCell(clientCell.setBorder(Border.NO_BORDER).setPadding(10f).setBackgroundColor(lightGray))

            val balanceLabel = if (totalBalance >= 0) context.getString(R.string.pdf_total_receivable) else context.getString(R.string.pdf_total_payable)
            val summaryCell = Cell().add(Paragraph(balanceLabel).setFontSize(8f).setBold().setFontColor(textSecondary).setMarginBottom(4f).setTextAlignment(TextAlignment.RIGHT))
                .add(Paragraph(MoneyFormatter.format(totalBalance, absolute = true))
                    .setBold().setFontSize(22f).setFontColor(accentColor).setTextAlignment(TextAlignment.RIGHT).setMarginTop(0f))
            mainInfoTable.addCell(summaryCell.setBorder(Border.NO_BORDER).setPadding(10f).setBackgroundColor(lightGray))

            document.add(mainInfoTable)
            document.add(Paragraph("\n"))

            val table = Table(UnitValue.createPointArray(floatArrayOf(100f, 250f, 150f)))
            table.setWidth(UnitValue.createPercentValue(100f))
            
            val headers = arrayOf(
                context.getString(R.string.pdf_header_date),
                context.getString(R.string.pdf_header_description),
                context.getString(R.string.pdf_header_amount)
            )
            headers.forEach { h ->
                table.addHeaderCell(Cell().add(Paragraph(h).setBold().setFontSize(9f).setFontColor(textSecondary))
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(SolidBorder(primaryColor, 1f))
                    .setPadding(10f))
            }

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            transactions.sortedByDescending { it.date }.forEachIndexed { index, tx ->
                val bottomBorder = if (index == transactions.size - 1) SolidBorder(primaryColor, 1f) else SolidBorder(borderColor, 0.5f)
                val txColor = if (tx.type == TransactionType.GIVEN) DeviceRgb(46, 125, 50) else DeviceRgb(198, 40, 40)
                
                table.addCell(Cell().add(Paragraph(dateFormat.format(Date(tx.date))).setFontSize(9f))
                    .setBorder(Border.NO_BORDER).setBorderBottom(bottomBorder).setPadding(10f))
                
                val descText = if (tx.type == TransactionType.GIVEN) context.getString(R.string.pdf_funds_sent) else context.getString(R.string.pdf_funds_received)
                val descPara = Paragraph().add(Paragraph(descText).setBold().setFontSize(10f))
                if (tx.note.isNotBlank()) {
                    descPara.add(Paragraph("\n" + tx.note).setFontSize(8f).setFontColor(textSecondary).setItalic())
                }
                table.addCell(Cell().add(descPara).setBorder(Border.NO_BORDER).setBorderBottom(bottomBorder).setPadding(10f))
                
                val amountPrefix = if (tx.type == TransactionType.GIVEN) "+" else "-"
                table.addCell(Cell().add(Paragraph(amountPrefix + MoneyFormatter.format(tx.amount)).setFontSize(11f).setBold().setFontColor(txColor).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER).setBorderBottom(bottomBorder).setPadding(10f))
            }

            document.add(table)
            
            val totalTable = Table(UnitValue.createPointArray(floatArrayOf(350f, 150f)))
            totalTable.setWidth(UnitValue.createPercentValue(100f))
            totalTable.setMarginTop(10f)
            
            totalTable.addCell(Cell().add(Paragraph(context.getString(R.string.pdf_final_outstanding)).setBold().setFontSize(11f).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setPadding(10f))
            totalTable.addCell(Cell().add(Paragraph(MoneyFormatter.format(totalBalance)).setBold().setFontSize(12f).setTextAlignment(TextAlignment.RIGHT).setFontColor(accentColor))
                .setBorder(Border.NO_BORDER).setPadding(10f).setBackgroundColor(lightGray))
            
            document.add(totalTable)
            addFooter(context, document)
            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun generateAndShareStatement(
        context: Context,
        person: Person,
        transactions: List<MoneyTransaction>,
        totalBalance: Double
    ) {
        val file = generateStatement(context, person, transactions, totalBalance)
        if (file != null) {
            shareFile(context, file)
        }
    }

    fun generateAndShareMasterStatement(
        context: Context,
        data: List<Triple<Person, List<MoneyTransaction>, Double>>,
        totalNetBalance: Double
    ) {
        val file = generateMasterStatement(context, data, totalNetBalance)
        if (file != null) {
            shareFile(context, file)
        }
    }

    private fun shareFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.pdf_share_with)))
    }
}
