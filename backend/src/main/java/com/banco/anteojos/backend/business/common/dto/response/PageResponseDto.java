package com.banco.anteojos.backend.business.common.dto.response;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

/**
 * Una página de un listado. DTO propio y no el {@code Page} de Spring Data: el JSON de PageImpl
 * no es un contrato estable (Spring Data lo advierte) y arrastra campos que el front no usa.
 *
 * @param page número de página, empezando en 0.
 */
public record PageResponseDto<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <T> PageResponseDto<T> from(Page<T> page) {
		return new PageResponseDto<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages());
	}

	public <R> PageResponseDto<R> map(Function<T, R> mapper) {
		return new PageResponseDto<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
	}

	/**
	 * Las referencias a otro dominio que aparecen en la página (ids de beneficiario, de marco...),
	 * para que el orchestrator las resuelva todas juntas en una sola consulta.
	 */
	public <K> Set<K> distinctValues(Function<T, K> field) {
		return content.stream().map(field).filter(Objects::nonNull).collect(Collectors.toSet());
	}
}
