package com.banco.anteojos.backend.useCase.applicants.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banco.anteojos.backend.business.applicants.ApplicantServiceHandler;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionFileUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionFileResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;
import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.business.applicants.entities.Prescription;
import com.banco.anteojos.backend.business.applicants.exception.ApplicantNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.DniAlreadyExistsException;
import com.banco.anteojos.backend.business.applicants.exception.InvalidPrescriptionFileException;
import com.banco.anteojos.backend.business.applicants.exception.PrescriptionFileNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.PrescriptionNotFoundException;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ApplicantServiceHandlerTest {

	@Mock
	private ApplicantPostgresSqlRepository applicantRepository;

	@Mock
	private PrescriptionPostgresSqlRepository prescriptionRepository;

	@Mock
	private R2StorageClient r2StorageClient;

	@InjectMocks
	private ApplicantServiceHandler applicantServiceHandler;

	private Applicant applicant() {
		return new Applicant("Juana", "Pérez", "30123456", LocalDate.of(1985, 3, 12),
				"3815550000", "juana.perez@mail.com");
	}

	private Prescription prescription() {
		return new Prescription(1L, new BigDecimal("-1.25"), null, null, null, null, null);
	}

	private PrescriptionFileUploadRequestDto pdfUpload() {
		return new PrescriptionFileUploadRequestDto("%PDF-1.4".getBytes(), "application/pdf", "receta.pdf");
	}

	@Test
	void CreateApplicant_Successful() {
		when(applicantRepository.findByDni("30123456")).thenReturn(Optional.empty());
		when(applicantRepository.save(any(Applicant.class))).thenAnswer(inv -> inv.getArgument(0));

		ApplicantResponseDto response = applicantServiceHandler.createApplicant(new ApplicantCreationRequestDto(
				"Juana", "Pérez", "30123456", LocalDate.of(1985, 3, 12), "3815550000", "Juana.Perez@Mail.com"));

		assertThat(response.dni()).isEqualTo("30123456");
		assertThat(response.email()).isEqualTo("juana.perez@mail.com");
		assertThat(response.identityValidated()).isFalse();
	}

	@Test
	void CreateApplicant_WhenDniAlreadyExists() {
		when(applicantRepository.findByDni("30123456")).thenReturn(Optional.of(applicant()));

		assertThatThrownBy(() -> applicantServiceHandler.createApplicant(new ApplicantCreationRequestDto(
				"Juana", "Pérez", "30123456", null, null, null)))
				.isInstanceOf(DniAlreadyExistsException.class);
		verify(applicantRepository, never()).save(any());
	}

	@Test
	void GetApplicant_WhenNotFound() {
		when(applicantRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> applicantServiceHandler.getApplicant(999L))
				.isInstanceOf(ApplicantNotFoundException.class);
	}

	@Test
	void UpdateApplicant_Successful() {
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(applicantRepository.save(any(Applicant.class))).thenAnswer(inv -> inv.getArgument(0));

		ApplicantResponseDto response = applicantServiceHandler.updateApplicant(1L, new ApplicantUpdateRequestDto(
				"Juana María", "Pérez", LocalDate.of(1985, 3, 12), "3815551111", "nuevo@mail.com"));

		assertThat(response.firstName()).isEqualTo("Juana María");
		assertThat(response.phone()).isEqualTo("3815551111");
		assertThat(response.email()).isEqualTo("nuevo@mail.com");
		assertThat(response.dni()).isEqualTo("30123456");
	}

	@Test
	void UpdateApplicant_WhenNotFound() {
		when(applicantRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> applicantServiceHandler.updateApplicant(999L, new ApplicantUpdateRequestDto(
				"Juana", "Pérez", null, null, null)))
				.isInstanceOf(ApplicantNotFoundException.class);
	}

	@Test
	void AddPrescription_Successful() {
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(inv -> inv.getArgument(0));

		PrescriptionResponseDto response = applicantServiceHandler.addPrescription(1L,
				new PrescriptionCreationRequestDto(new BigDecimal("-1.25"), new BigDecimal("-0.50"), 90,
						new BigDecimal("-1.00"), null, null));

		assertThat(response.applicantId()).isEqualTo(1L);
		assertThat(response.rightSphere()).isEqualByComparingTo("-1.25");
		assertThat(response.rightAxis()).isEqualTo(90);
		assertThat(response.leftCylinder()).isNull();
	}

	@Test
	void AddPrescription_WhenApplicantDoesNotExist() {
		when(applicantRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> applicantServiceHandler.addPrescription(999L,
				new PrescriptionCreationRequestDto(null, null, null, null, null, null)))
				.isInstanceOf(ApplicantNotFoundException.class);
		verify(prescriptionRepository, never()).save(any());
	}

	@Test
	void ListPrescriptions_Successful() {
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByApplicantId(1L)).thenReturn(List.of(
				new Prescription(1L, new BigDecimal("-1.25"), null, null, null, null, null)));

		List<PrescriptionResponseDto> response = applicantServiceHandler.listPrescriptions(1L);

		assertThat(response).hasSize(1);
		assertThat(response.get(0).rightSphere()).isEqualByComparingTo("-1.25");
	}

	@Test
	void UploadPrescriptionFile_Successful() {
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByIdAndApplicantId(7L, 1L)).thenReturn(Optional.of(prescription()));
		when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(inv -> inv.getArgument(0));

		PrescriptionResponseDto response = applicantServiceHandler.uploadPrescriptionFile(1L, 7L, pdfUpload());

		ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
		verify(r2StorageClient).upload(key.capture(), any(), eq("application/pdf"));
		// El prefijo por solicitante es lo que permitiría mover el resto a un bucket público.
		assertThat(key.getValue()).startsWith("prescriptions/1/").endsWith(".pdf");
		assertThat(response.fileOriginalName()).isEqualTo("receta.pdf");
	}

	@Test
	void UploadPrescriptionFile_IgnoresContentTypeParameters() {
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByIdAndApplicantId(7L, 1L)).thenReturn(Optional.of(prescription()));
		when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(inv -> inv.getArgument(0));

		applicantServiceHandler.uploadPrescriptionFile(1L, 7L, new PrescriptionFileUploadRequestDto(
				new byte[] { 1, 2, 3 }, "IMAGE/JPEG; charset=binary", "foto.jpeg"));

		ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
		verify(r2StorageClient).upload(key.capture(), any(), eq("image/jpeg"));
		assertThat(key.getValue()).endsWith(".jpg");
	}

	@Test
	void UploadPrescriptionFile_ReplacesPreviousFile() {
		Prescription prescription = prescription();
		prescription.attachFile("prescriptions/1/viejo.pdf", "application/pdf", "viejo.pdf");
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByIdAndApplicantId(7L, 1L)).thenReturn(Optional.of(prescription));
		when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(inv -> inv.getArgument(0));

		applicantServiceHandler.uploadPrescriptionFile(1L, 7L, pdfUpload());

		verify(r2StorageClient).delete("prescriptions/1/viejo.pdf");
	}

	@Test
	void UploadPrescriptionFile_WhenDeletingPreviousFileFails() {
		Prescription prescription = prescription();
		prescription.attachFile("prescriptions/1/viejo.pdf", "application/pdf", "viejo.pdf");
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByIdAndApplicantId(7L, 1L)).thenReturn(Optional.of(prescription));
		when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(inv -> inv.getArgument(0));
		doThrow(new RuntimeException("R2 caído")).when(r2StorageClient).delete("prescriptions/1/viejo.pdf");

		// Limpiar el huérfano no es crítico: el archivo nuevo ya quedó asociado a la receta.
		PrescriptionResponseDto response = applicantServiceHandler.uploadPrescriptionFile(1L, 7L, pdfUpload());

		assertThat(response.fileOriginalName()).isEqualTo("receta.pdf");
	}

	@Test
	void UploadPrescriptionFile_WhenContentTypeNotAllowed() {
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByIdAndApplicantId(7L, 1L)).thenReturn(Optional.of(prescription()));

		assertThatThrownBy(() -> applicantServiceHandler.uploadPrescriptionFile(1L, 7L,
				new PrescriptionFileUploadRequestDto(new byte[] { 1 }, "application/zip", "receta.zip")))
				.isInstanceOf(InvalidPrescriptionFileException.class);
		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void UploadPrescriptionFile_WhenFileIsEmpty() {
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByIdAndApplicantId(7L, 1L)).thenReturn(Optional.of(prescription()));

		assertThatThrownBy(() -> applicantServiceHandler.uploadPrescriptionFile(1L, 7L,
				new PrescriptionFileUploadRequestDto(new byte[0], "application/pdf", "vacio.pdf")))
				.isInstanceOf(InvalidPrescriptionFileException.class);
		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void UploadPrescriptionFile_WhenPrescriptionBelongsToAnotherApplicant() {
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByIdAndApplicantId(7L, 1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> applicantServiceHandler.uploadPrescriptionFile(1L, 7L, pdfUpload()))
				.isInstanceOf(PrescriptionNotFoundException.class);
		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void GetPrescriptionFile_Successful() {
		Prescription prescription = prescription();
		prescription.attachFile("prescriptions/1/abc.pdf", "application/pdf", "receta.pdf");
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByIdAndApplicantId(7L, 1L)).thenReturn(Optional.of(prescription));
		when(r2StorageClient.presignedGetUrl("prescriptions/1/abc.pdf"))
				.thenReturn(new PresignedUrl("https://r2.example/firmada", expiresAt));

		PrescriptionFileResponseDto response = applicantServiceHandler.getPrescriptionFile(1L, 7L);

		assertThat(response.url()).isEqualTo("https://r2.example/firmada");
		assertThat(response.expiresAt()).isEqualTo(expiresAt);
		assertThat(response.fileName()).isEqualTo("receta.pdf");
		assertThat(response.contentType()).isEqualTo("application/pdf");
	}

	@Test
	void GetPrescriptionFile_WhenPrescriptionHasNoFile() {
		when(applicantRepository.findById(1L)).thenReturn(Optional.of(applicant()));
		when(prescriptionRepository.findByIdAndApplicantId(7L, 1L)).thenReturn(Optional.of(prescription()));

		assertThatThrownBy(() -> applicantServiceHandler.getPrescriptionFile(1L, 7L))
				.isInstanceOf(PrescriptionFileNotFoundException.class);
	}
}
