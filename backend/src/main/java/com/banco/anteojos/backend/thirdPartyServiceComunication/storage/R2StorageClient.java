package com.banco.anteojos.backend.thirdPartyServiceComunication.storage;

/**
 * Acceso a Cloudflare R2 (object storage S3-compatible). El bucket es privado: la única forma
 * de leer un objeto desde afuera es una presigned URL con vencimiento.
 */
public interface R2StorageClient {

	void upload(String key, byte[] content, String contentType);

	/** URL firmada de lectura. Firmar es un HMAC local: no le pega a la API de R2. */
	PresignedUrl presignedGetUrl(String key);

	void delete(String key);
}
