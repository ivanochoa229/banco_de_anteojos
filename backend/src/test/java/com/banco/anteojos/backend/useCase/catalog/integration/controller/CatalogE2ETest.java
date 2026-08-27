package com.banco.anteojos.backend.useCase.catalog.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.catalog.entities.Product;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.catalog.ProductPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;
import com.jayway.jsonpath.JsonPath;

/** Catálogo de venta (RF-28 a RF-30): dominio autocontenido, no necesita templates de otro. */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private ProductPostgresSqlRepository productRepository;

	@MockitoBean
	private R2StorageClient r2StorageClient;

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	private String creationBody() {
		return """
				{"name": "Ray-Ban Aviator", "description": "clásico", "price": 25000.00, "stockQuantity": 10}
				""";
	}

	private Long createProduct() throws Exception {
		String response = mockMvc.perform(post("/v1/catalog/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	private Product productInDb(Long productId) {
		return productRepository.findById(productId).orElseThrow();
	}

	@Test
	void CreateProduct_Successful() throws Exception {
		mockMvc.perform(post("/v1/catalog/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Ray-Ban Aviator"))
				.andExpect(jsonPath("$.stockQuantity").value(10))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void CreateProduct_WhenPriceIsNegative() throws Exception {
		mockMvc.perform(post("/v1/catalog/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name": "Wayfarer", "price": -1, "stockQuantity": 5}
								""")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void ListProducts_FiltersByStatus() throws Exception {
		Long productId = createProduct();
		mockMvc.perform(put("/v1/catalog/products/" + productId + "/discontinuation")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())).andExpect(status().isOk());
		createProduct();

		mockMvc.perform(get("/v1/catalog/products").param("status", "ACTIVE")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(get("/v1/catalog/products")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void UpdateProduct_Successful() throws Exception {
		Long productId = createProduct();

		mockMvc.perform(put("/v1/catalog/products/" + productId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name": "Aviator Classic", "price": 27000.00, "stockQuantity": 20}
								""")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Aviator Classic"))
				.andExpect(jsonPath("$.stockQuantity").value(20));
	}

	@Test
	void DiscontinueProduct_Successful() throws Exception {
		Long productId = createProduct();

		mockMvc.perform(put("/v1/catalog/products/" + productId + "/discontinuation")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DISCONTINUED"));

		assertThat(productInDb(productId).getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
	}

	@Test
	void DiscontinueProduct_WhenAlreadyDiscontinued() throws Exception {
		Long productId = createProduct();
		mockMvc.perform(put("/v1/catalog/products/" + productId + "/discontinuation")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())).andExpect(status().isOk());

		mockMvc.perform(put("/v1/catalog/products/" + productId + "/discontinuation")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict());
	}

	@Test
	void SellProduct_DiscountsStock() throws Exception {
		Long productId = createProduct();

		mockMvc.perform(post("/v1/catalog/products/" + productId + "/sales")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"quantity": 3, "buyerName": "Juana Pérez"}
								""")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.quantity").value(3))
				.andExpect(jsonPath("$.totalAmount").value(75000.00));

		assertThat(productInDb(productId).getStockQuantity()).isEqualTo(7);

		mockMvc.perform(get("/v1/catalog/products/" + productId + "/sales")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void SellProduct_WhenInsufficientStock() throws Exception {
		Long productId = createProduct();

		mockMvc.perform(post("/v1/catalog/products/" + productId + "/sales")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"quantity": 11}
								""")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict());

		assertThat(productInDb(productId).getStockQuantity()).isEqualTo(10);
	}

	@Test
	void SellProduct_WhenDiscontinued() throws Exception {
		Long productId = createProduct();
		mockMvc.perform(put("/v1/catalog/products/" + productId + "/discontinuation")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())).andExpect(status().isOk());

		mockMvc.perform(post("/v1/catalog/products/" + productId + "/sales")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"quantity": 1}
								""")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict());
	}

	@Test
	void UploadProductImage_Successful() throws Exception {
		Long productId = createProduct();

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/catalog/products/" + productId + "/image")
						.file(new MockMultipartFile("file", "aviator.png", "image/png",
								new byte[] { (byte) 0x89, 'P', 'N', 'G' }))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.imageOriginalName").value("aviator.png"));

		verify(r2StorageClient).upload(any(), any(), eq("image/png"));
		assertThat(productInDb(productId).getImageKey()).startsWith("products/" + productId + "/").endsWith(".png");
	}

	@Test
	void GetProductImage_WhenProductHasNoImage() throws Exception {
		Long productId = createProduct();

		mockMvc.perform(get("/v1/catalog/products/" + productId + "/image")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound());
	}

	@Test
	void GetProduct_WhenNotFound() throws Exception {
		mockMvc.perform(get("/v1/catalog/products/999999")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound());
	}
}
