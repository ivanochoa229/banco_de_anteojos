package com.banco.anteojos.backend.useCase.applicants.unit.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.banco.anteojos.backend.business.applicants.AnsesCertificateHelper;
import com.banco.anteojos.backend.business.applicants.ParsedAnsesCertificate;

/**
 * Prueba manual contra una negativa REAL de ANSES, que no se commitea (contiene un CUIL
 * verdadero). Se corre a mano apuntando al archivo local:
 *
 * <pre>./mvnw test -Dtest=RealAnsesCertificateManualTest -Danses.real.pdf=/ruta/al/pdf</pre>
 *
 * Sin la property el test se saltea, así que puede vivir en el repo sin romper CI.
 */
@Tag("unit")
class RealAnsesCertificateManualTest {

	@Test
	@EnabledIfSystemProperty(named = "anses.real.pdf", matches = ".+")
	void Parse_RealCertificate() throws IOException {
		byte[] pdf = Files.readAllBytes(Path.of(System.getProperty("anses.real.pdf")));

		ParsedAnsesCertificate parsed = new AnsesCertificateHelper().parse(pdf);

		System.out.printf("CUIL=%s transacción=%s emisión=%s%n", parsed.cuil(),
				parsed.transactionNumber(), parsed.issueDate());
		assertThat(parsed.cuil()).hasSize(11).containsOnlyDigits();
		assertThat(parsed.transactionNumber()).isNotEmpty().containsOnlyDigits();
		assertThat(parsed.issueDate()).isNotNull();
	}
}
