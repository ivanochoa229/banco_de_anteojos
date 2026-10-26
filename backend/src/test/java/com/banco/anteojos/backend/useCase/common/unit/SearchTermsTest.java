package com.banco.anteojos.backend.useCase.common.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.common.SearchTerms;

@Tag("unit")
class SearchTermsTest {

	@Test
	void Normalize_RemovesAccentsCaseAndSurroundingSpaces() {
		assertThat(SearchTerms.normalize("  José MUÑOZ ")).isEqualTo("jose munoz");
	}

	@Test
	void Normalize_WhenNull() {
		assertThat(SearchTerms.normalize(null)).isEmpty();
	}

	// La query traduce letra por letra: si las dos tablas se desalinean, la búsqueda falla en silencio.
	@Test
	void TranslationTables_AreAlignedAndMatchNormalize() {
		assertThat(SearchTerms.ACCENTED).hasSameSizeAs(SearchTerms.PLAIN);
		assertThat(SearchTerms.normalize(SearchTerms.ACCENTED)).isEqualTo(SearchTerms.PLAIN);
	}
}
