package com.banco.anteojos.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@Tag("unit")
class RequestLoggingFilterTest {

	private final RequestLoggingFilter filter = new RequestLoggingFilter();

	@Test
	void Filter_WithoutTraceHeader_GeneratesTraceIdAndClearsMdc() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/frames");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> traceInChain = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) -> traceInChain.set(MDC.get("traceId")));

		assertThat(traceInChain.get()).isNotBlank();
		assertThat(response.getHeader("X-Trace-Id")).isEqualTo(traceInChain.get());
		assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
	}

	@Test
	void Filter_WithUnsafeTraceHeader_DiscardsIt() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/frames");
		request.addHeader("X-Trace-Id", "abc\nFAKE LOG LINE");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (req, res) -> { });

		assertThat(response.getHeader("X-Trace-Id")).doesNotContain("FAKE");
	}

	@Test
	void Filter_WithSafeTraceHeader_ReusesIt() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/frames");
		request.addHeader("X-Trace-Id", "front-123");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (req, res) -> { });

		assertThat(response.getHeader("X-Trace-Id")).isEqualTo("front-123");
	}

	@Test
	void Filter_WhenChainThrows_StillClearsMdc() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/frames");

		try {
			filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
				throw new IllegalStateException("boom");
			});
		} catch (Exception expected) {
			// se espera: lo que importa es que el MDC no quede sucio
		}

		assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
	}
}
