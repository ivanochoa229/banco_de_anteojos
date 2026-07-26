package com.banco.anteojos.backend;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Necesita un Postgres local levantado: ./mvnw test -Dtest.excluded.groups=
@Tag("integration")
@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
