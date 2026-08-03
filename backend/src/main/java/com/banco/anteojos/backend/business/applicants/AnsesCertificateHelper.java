package com.banco.anteojos.backend.business.applicants;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.applicants.exception.InvalidAnsesCertificateException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;

/**
 * Lectura local del PDF de la Certificación Negativa de ANSES (RF-04/05). No es un cliente de
 * terceros: no hay API de ANSES, todo se extrae del archivo que sube el operador.
 *
 * El código de barras codifica el CUIL (con guiones) seguido del número de transacción, p. ej.
 * "20-12345678-9221145098". La fecha de emisión no viene en el barcode: se extrae del texto
 * ("Fecha de emisión: dd/mm/aaaa"). La adulteración del PDF no es detectable acá; la validación
 * final es del operador (RF-05).
 */
@Component
public class AnsesCertificateHelper {

	// 300 DPI: suficiente para que ZXing lea el barcode del PDF original de ANSES sin
	// hacer gigantes las imágenes intermedias.
	private static final int RENDER_DPI = 300;

	private static final Pattern BARCODE_CONTENT = Pattern.compile("^(\\d{11})(\\d+)$");

	private static final Pattern ISSUE_DATE = Pattern
			.compile("Fecha\\s+de\\s+emisi[oó]n:?\\s*(\\d{1,2}/\\d{1,2}/\\d{4})", Pattern.CASE_INSENSITIVE);

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);

	public ParsedAnsesCertificate parse(byte[] pdfContent) {
		try (PDDocument document = Loader.loadPDF(pdfContent)) {
			Matcher barcodeMatcher = BARCODE_CONTENT.matcher(decodeBarcode(document));
			// decodeBarcode solo devuelve contenido que ya matcheó el patrón.
			barcodeMatcher.matches();
			return new ParsedAnsesCertificate(barcodeMatcher.group(1), barcodeMatcher.group(2),
					extractIssueDate(document));
		} catch (IOException e) {
			throw new InvalidAnsesCertificateException("El archivo no es un PDF legible");
		}
	}

	/** Devuelve solo los dígitos del barcode (el CUIL viene con guiones), ya validados contra el patrón. */
	private String decodeBarcode(PDDocument document) throws IOException {
		PDFRenderer renderer = new PDFRenderer(document);
		Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		// Solo los formatos que usa ANSES (1D en los PDFs actuales, PDF417 en otros): con todos
		// los formatos habilitados, TRY_HARDER "encuentra" barcodes espurios en líneas de texto.
		hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.CODE_128, BarcodeFormat.PDF_417));
		GenericMultipleBarcodeReader reader = new GenericMultipleBarcodeReader(new MultiFormatReader());
		boolean foundAnyBarcode = false;
		for (int page = 0; page < document.getNumberOfPages(); page++) {
			BufferedImage image = renderer.renderImageWithDPI(page, RENDER_DPI, ImageType.GRAY);
			try {
				// Puede haber más de un barcode (o algún falso positivo): vale el que tenga
				// el contenido esperado, no el primero que aparezca.
				for (Result result : reader.decodeMultiple(new BinaryBitmap(new HybridBinarizer(
						new BufferedImageLuminanceSource(image))), hints)) {
					foundAnyBarcode = true;
					String digits = result.getText().replaceAll("\\D", "");
					if (BARCODE_CONTENT.matcher(digits).matches()) {
						return digits;
					}
				}
			} catch (NotFoundException e) {
				// Puede estar en otra página; si no está en ninguna se rechaza al salir del loop.
			}
		}
		throw new InvalidAnsesCertificateException(foundAnyBarcode
				? "El código de barras no tiene el formato de una certificación negativa de ANSES"
				: "No se pudo leer el código de barras del PDF. Verificá que sea la certificación original de ANSES");
	}

	private LocalDate extractIssueDate(PDDocument document) throws IOException {
		String text = new PDFTextStripper().getText(document);
		Matcher matcher = ISSUE_DATE.matcher(text);
		if (!matcher.find()) {
			throw new InvalidAnsesCertificateException("No se encontró la fecha de emisión en el PDF");
		}
		try {
			return LocalDate.parse(matcher.group(1), DATE_FORMAT);
		} catch (DateTimeParseException e) {
			throw new InvalidAnsesCertificateException("La fecha de emisión del PDF no es una fecha válida");
		}
	}
}
