package com.banco.anteojos.backend.useCase.applicants;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.oned.Code128Writer;

/**
 * Genera negativas de ANSES sintéticas imitando el formato real: código de barras 1D (Code 128)
 * con "XX-XXXXXXXX-X" + nro. de transacción pegado, y la fecha en el texto como
 * "Fecha de emisión: dd/mm/aaaa". Permite probar el pipeline completo (render + decode + texto)
 * sin commitear un PDF real, que contiene un CUIL verdadero.
 */
public final class AnsesCertificatePdfFactory {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private AnsesCertificatePdfFactory() {
	}

	public static byte[] certificate(String cuil, String transactionNumber, LocalDate issueDate) {
		return build(barcodeContent(cuil, transactionNumber), issueDate);
	}

	public static byte[] withoutBarcode(LocalDate issueDate) {
		return build(null, issueDate);
	}

	public static byte[] withoutIssueDate(String cuil, String transactionNumber) {
		return build(barcodeContent(cuil, transactionNumber), null);
	}

	/** CUIL con prefijo 20 y dígito verificador calculado (módulo 11 de AFIP). */
	public static String cuilFor(String dni) {
		String base = "20" + "0".repeat(8 - dni.length()) + dni;
		int[] weights = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };
		int sum = 0;
		for (int i = 0; i < weights.length; i++) {
			sum += Character.getNumericValue(base.charAt(i)) * weights[i];
		}
		int check = (11 - sum % 11) % 11;
		if (check == 10) {
			throw new IllegalArgumentException("Ese DNI no admite prefijo 20: elegí otro para el test");
		}
		return base + check;
	}

	// En el PDF real el CUIL va con guiones y la transacción pegada atrás: "20-41383873-9221145098".
	private static String barcodeContent(String cuil, String transactionNumber) {
		return "%s-%s-%s%s".formatted(cuil.substring(0, 2), cuil.substring(2, 10), cuil.substring(10),
				transactionNumber);
	}

	private static byte[] build(String barcodeContent, LocalDate issueDate) {
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);
			try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
				stream.beginText();
				stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
				stream.newLineAtOffset(50, 700);
				stream.showText("CERTIFICACIÓN NEGATIVA");
				stream.endText();
				if (issueDate != null) {
					stream.beginText();
					stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
					stream.newLineAtOffset(50, 650);
					stream.showText("Fecha de emisión: " + issueDate.format(DATE_FORMAT)
							+ "    Nº Transacción: 221145098");
					stream.endText();
				}
				if (barcodeContent != null) {
					BufferedImage barcode = MatrixToImageWriter.toBufferedImage(
							new Code128Writer().encode(barcodeContent, BarcodeFormat.CODE_128, 500, 80));
					PDImageXObject image = LosslessFactory.createFromImage(document, barcode);
					stream.drawImage(image, 47, 300, 500, 80);
				}
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			document.save(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
