package com.banco.anteojos.backend.presentation.controllers.catalog;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.banco.anteojos.backend.business.catalog.dto.request.ProductImageUploadRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductImageResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductResponseDto;
import com.banco.anteojos.backend.business.catalog.exception.InvalidProductImageException;
import com.banco.anteojos.backend.orchestrator.catalog.CatalogUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

// Sub-recurso singleton: un producto tiene a lo sumo una foto. PUT y no POST: subir otra la reemplaza.
@RestController
@RequestMapping("/v1/catalog/products/{productId}/image")
@RequiredArgsConstructor
public class ProductImageController {

	private final CatalogUseCaseOrchestrator catalogOrchestrator;

	@PutMapping
	public ProductResponseDto upload(@PathVariable Long productId,
			@RequestParam("file") MultipartFile file) {
		return catalogOrchestrator.uploadProductImage(productId, toUploadRequest(file));
	}

	/** Devuelve la presigned URL, no el binario: el backend no proxea archivos de R2. */
	@GetMapping
	public ProductImageResponseDto get(@PathVariable Long productId) {
		return catalogOrchestrator.getProductImage(productId);
	}

	private ProductImageUploadRequestDto toUploadRequest(MultipartFile file) {
		try {
			return new ProductImageUploadRequestDto(file.getBytes(), file.getContentType(),
					file.getOriginalFilename());
		} catch (IOException e) {
			throw new InvalidProductImageException("No se pudo leer el archivo");
		}
	}
}
