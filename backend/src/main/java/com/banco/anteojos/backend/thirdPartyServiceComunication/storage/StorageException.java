package com.banco.anteojos.backend.thirdPartyServiceComunication.storage;

public class StorageException extends RuntimeException {

	public StorageException(Throwable cause) {
		super("No se pudo guardar el archivo, intentá de nuevo", cause);
	}
}
