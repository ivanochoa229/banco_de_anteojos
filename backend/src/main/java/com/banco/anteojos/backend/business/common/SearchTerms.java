package com.banco.anteojos.backend.business.common;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Búsqueda por texto libre en los listados. El personal tipea sin tildes ("perez", "munoz"), así
 * que el término se compara sin tildes ni mayúsculas. La otra mitad la hace la query con
 * {@link #ACCENTED} y {@link #PLAIN}: las dos tienen que mantenerse alineadas.
 */
public final class SearchTerms {

	/** Para {@code translate(lower(columna), ACCENTED, PLAIN)} en JPQL: letra por letra. */
	public static final String ACCENTED = "áàäâéèëêíìïîóòöôúùüûñ";
	public static final String PLAIN = "aaaaeeeeiiiioooouuuun";

	private SearchTerms() {
	}

	/** Sin término (null o en blanco) devuelve "", que en un {@code LIKE '%' || :search || '%'} matchea todo. */
	public static String normalize(String term) {
		if (term == null) {
			return "";
		}
		String decomposed = Normalizer.normalize(term.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
		return decomposed.replaceAll("\\p{M}", "");
	}
}
