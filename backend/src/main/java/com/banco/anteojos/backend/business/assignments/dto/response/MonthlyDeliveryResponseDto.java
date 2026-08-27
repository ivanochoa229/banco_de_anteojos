package com.banco.anteojos.backend.business.assignments.dto.response;

import java.time.YearMonth;

public record MonthlyDeliveryResponseDto(YearMonth month, long deliveries, long applicants) {
}
