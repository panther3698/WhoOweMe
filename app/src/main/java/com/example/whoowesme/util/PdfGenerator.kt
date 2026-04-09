package com.example.whoowesme.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.whoowesme.R
import com.example.whoowesme.model.MoneyTransaction
import com.example.whoowesme.model.Person
import com.example.whoowesme.model.enums.TransactionType
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
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
            // val goldColor = DeviceRgb(255, 215, 0) // Gold for accents

            // Header Section
            val headerTable = Table(UnitValue.createPointArray(floatArrayOf(350f, 150f)))
            headerTable.width = UnitValue.createPercentValue(100f)
            
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
            
            // Subtle Divider
            document.add(Paragraph("").setBorderBottom(SolidBorder(borderColor, 0.5f)).setMarginTop(5f).setMarginBottom(25f))
            
            // Client & Summary Box
            val mainInfoTable = Table(UnitValue.createPointArray(floatArrayOf(250f, 250f)))
            mainInfoTable.width = UnitValue.createPercentValue(100f)
            
            // Client Info
            val clientCell = Cell().add(Paragraph(context.getString(R.string.pdf_client_details)).setFontSize(8f).setBold().setFontColor(textSecondary).setMarginBottom(4f))
                .add(Paragraph(person.name).setBold().setFontSize(16f).setMarginTop(0f))
            if (person.phoneNumber.isNotEmpty()) {
                clientCell.add(Paragraph(person.phoneNumber).setFontSize(10f).setFontColor(textSecondary))
            }
            mainInfoTable.addCell(clientCell.setBorder(Border.NO_BORDER).setPadding(10f).setBackgroundColor(lightGray))

            // Summary Info
            val balanceLabel = if (totalBalance >= 0) context.getString(R.string.pdf_total_receivable) else context.getString(R.string.pdf_total_payable)
            val summaryCell = Cell().add(Paragraph(balanceLabel).setFontSize(8f).setBold().setFontColor(textSecondary).setMarginBottom(4f).setTextAlignment(TextAlignment.RIGHT))
                .add(Paragraph(MoneyFormatter.format(totalBalance, absolute = true))
                    .setBold().setFontSize(22f).setFontColor(accentColor).setTextAlignment(TextAlignment.RIGHT).setMarginTop(0f))
            mainInfoTable.addCell(summaryCell.setBorder(Border.NO_BORDER).setPadding(10f).setBackgroundColor(lightGray))

            document.add(mainInfoTable)
            document.add(Paragraph("\n"))

            // Transactions Table
            val table = Table(UnitValue.createPointArray(floatArrayOf(100f, 250f, 150f)))
            table.width = UnitValue.createPercentValue(100f)
            
            // Header
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
                
                // Date
                table.addCell(Cell().add(Paragraph(dateFormat.format(Date(tx.date))).setFontSize(9f))
                    .setBorder(Border.NO_BORDER).setBorderBottom(bottomBorder).setPadding(10f))
                
                // Description (Type + Note)
                val descText = if (tx.type == TransactionType.GIVEN) context.getString(R.string.pdf_funds_sent) else context.getString(R.string.pdf_funds_received)
                val descPara = Paragraph().add(Paragraph(descText).setBold().setFontSize(10f))
                if (tx.note.isNotBlank()) {
                    descPara.add(Paragraph("\n" + tx.note).setFontSize(8f).setFontColor(textSecondary).setItalic())
                }
                table.addCell(Cell().add(descPara).setBorder(Border.NO_BORDER).setBorderBottom(bottomBorder).setPadding(10f))
                
                // Amount
                val amountPrefix = if (tx.type == TransactionType.GIVEN) "+" else "-"
                table.addCell(Cell().add(Paragraph(amountPrefix + MoneyFormatter.format(tx.amount)).setFontSize(11f).setBold().setFontColor(txColor).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER).setBorderBottom(bottomBorder).setPadding(10f))
            }

            document.add(table)
            
            // Final Summary Line
            val totalTable = Table(UnitValue.createPointArray(floatArrayOf(350f, 150f)))
            totalTable.width = UnitValue.createPercentValue(100f)
            totalTable.setMarginTop(10f)
            
            totalTable.addCell(Cell().add(Paragraph(context.getString(R.string.pdf_final_outstanding)).setBold().setFontSize(11f).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setPadding(10f))
            totalTable.addCell(Cell().add(Paragraph(MoneyFormatter.format(totalBalance)).setBold().setFontSize(12f).setTextAlignment(TextAlignment.RIGHT).setFontColor(accentColor))
                .setBorder(Border.NO_BORDER).setPadding(10f).setBackgroundColor(lightGray))
            
            document.add(totalTable)

            // Signature/Footer
            document.add(Paragraph("\n\n").setMarginBottom(40f))
            
            val footerTable = Table(UnitValue.createPointArray(floatArrayOf(250f, 250f)))
            footerTable.width = UnitValue.createPercentValue(100f)
            
            val generationDateText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            footerTable.addCell(Cell().add(Paragraph(context.getString(R.string.pdf_generated_on, generationDateText))
                .setFontSize(8f).setFontColor(textSecondary))
                .setBorder(Border.NO_BORDER))
                
            footerTable.addCell(Cell().add(Paragraph(context.getString(R.string.pdf_footer_tagline))
                .setFontSize(8f).setItalic().setFontColor(textSecondary).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER))
                
            document.add(footerTable)
            
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
