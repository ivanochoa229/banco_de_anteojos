package com.banco.anteojos.backend.thirdPartyServiceComunication.storage;

import java.time.LocalDateTime;

/** URL firmada y su vencimiento. El TTL lo define el cliente, no quien la consume. */
public record PresignedUrl(String url, LocalDateTime expiresAt) {
}
