package com.banco.anteojos.backend.useCase.applicants.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.applicants.AnsesCertificateHelper;
import com.banco.anteojos.backend.business.applicants.ParsedAnsesCertificate;
import com.banco.anteojos.backend.business.applicants.exception.InvalidAnsesCertificateException;
import com.banco.anteojos.backend.useCase.applicants.AnsesCertificatePdfFactory;

@Tag("unit")
class AnsesCertificateHelperTest {

	private final AnsesCertificateHelper helper = new AnsesCertificateHelper();

	@Test
	void Parse_Successful() {
		LocalDate issueDate = LocalDate.of(2026, 8, 3);
		byte[] pdf = AnsesCertificatePdfFactory.certificate("20301234563", "221145098", issueDate);

		ParsedAnsesCertificate parsed = helper.parse(pdf);

		assertThat(parsed.cuil()).isEqualTo("20301234563");
		assertThat(parsed.transactionNumber()).isEqualTo("221145098");
		assertThat(parsed.issueDate()).isEqualTo(issueDate);
	}

	@Test
	void Parse_WhenPdfHasNoBarcode() {
		byte[] pdf = AnsesCertificatePdfFactory.withoutBarcode(LocalDate.of(2026, 8, 3));

		assertThatThrownBy(() -> helper.parse(pdf))
				.isInstanceOf(InvalidAnsesCertificateException.class)
				.hasMessageContaining("código de barras");
	}

	@Test
	void Parse_WhenPdfHasNoIssueDate() {
		byte[] pdf = AnsesCertificatePdfFactory.withoutIssueDate("20301234563", "221145098");

		assertThatThrownBy(() -> helper.parse(pdf))
				.isInstanceOf(InvalidAnsesCertificateException.class)
				.hasMessageContaining("fecha de emisión");
	}

	@Test
	void Parse_WhenFileIsNotAPdf() {
		assertThatThrownBy(() -> helper.parse("esto no es un pdf".getBytes()))
				.isInstanceOf(InvalidAnsesCertificateException.class)
				.hasMessageContaining("PDF");
	}
}
