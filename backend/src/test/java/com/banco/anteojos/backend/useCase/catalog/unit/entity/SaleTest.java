package com.banco.anteojos.backend.useCase.catalog.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.catalog.entities.Sale;

@Tag("unit")
class SaleTest {

	@Test
	void Create_ComputesTotalAmount() {
		Sale sale = new Sale(1L, 3, new BigDecimal("25000.00"), "  Juana Pérez  ", "  retira mañana  ");

		assertThat(sale.getProductId()).isEqualTo(1L);
		assertThat(sale.getQuantity()).isEqualTo(3);
		assertThat(sale.getUnitPrice()).isEqualByComparingTo("25000.00");
		assertThat(sale.getTotalAmount()).isEqualByComparingTo("75000.00");
		assertThat(sale.getBuyerName()).isEqualTo("Juana Pérez");
		assertThat(sale.getNotes()).isEqualTo("retira mañana");
		assertThat(sale.getSoldAt()).isNotNull();
	}

	@Test
	void Create_WithoutBuyerName() {
		Sale sale = new Sale(1L, 1, new BigDecimal("18000.00"), "  ", null);

		assertThat(sale.getBuyerName()).isNull();
		assertThat(sale.getNotes()).isNull();
	}
}
