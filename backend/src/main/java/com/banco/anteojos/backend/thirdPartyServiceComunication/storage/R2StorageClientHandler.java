package com.banco.anteojos.backend.thirdPartyServiceComunication.storage;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@Slf4j
public class R2StorageClientHandler implements R2StorageClient {

	private final S3Client s3Client;
	private final S3Presigner presigner;
	private final String bucket;
	private final Duration presignedUrlTtl;

	public R2StorageClientHandler(
			@Value("${r2.account-id}") String accountId,
			@Value("${r2.access-key-id}") String accessKeyId,
			@Value("${r2.secret-access-key}") String secretAccessKey,
			@Value("${r2.bucket}") String bucket,
			@Value("${r2.presigned-url-ttl-minutes}") long presignedUrlTtlMinutes) {
		this.bucket = bucket;
		this.presignedUrlTtl = Duration.ofMinutes(presignedUrlTtlMinutes);

		URI endpoint = URI.create("https://" + accountId + ".r2.cloudflarestorage.com");
		StaticCredentialsProvider credentials = StaticCredentialsProvider
				.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));

		// R2 no tiene regiones, pero el SDK de S3 exige una para firmar: usa siempre "auto".
		this.s3Client = S3Client.builder()
				.endpointOverride(endpoint)
				.region(Region.of("auto"))
				.credentialsProvider(credentials)
				.build();
		this.presigner = S3Presigner.builder()
				.endpointOverride(endpoint)
				.region(Region.of("auto"))
				.credentialsProvider(credentials)
				.build();
	}

	@Override
	public void upload(String key, byte[] content, String contentType) {
		try {
			s3Client.putObject(PutObjectRequest.builder()
					.bucket(bucket)
					.key(key)
					.contentType(contentType)
					.build(), RequestBody.fromBytes(content));
		} catch (RuntimeException e) {
			throw new StorageException(e);
		}
	}

	@Override
	public PresignedUrl presignedGetUrl(String key) {
		String url = presigner.presignGetObject(GetObjectPresignRequest.builder()
				.signatureDuration(presignedUrlTtl)
				.getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
				.build())
				.url()
				.toString();
		return new PresignedUrl(url, LocalDateTime.now().plus(presignedUrlTtl));
	}

	@Override
	public void delete(String key) {
		s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
	}

	@PreDestroy
	void close() {
		s3Client.close();
		presigner.close();
	}
}
