package dev.portfolio.confirm;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

@Service
public class ConfirmationService {

	private final ConfirmationRepository confirmations;

	private final SecureRandom random = new SecureRandom();

	public ConfirmationService(ConfirmationRepository confirmations) {
		this.confirmations = confirmations;
	}

	@Transactional
	public IssuedToken issue(String email) {
		String normalized = email.trim().toLowerCase();
		if (confirmations.existsByEmailIgnoreCase(normalized)) {
			ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Email is already registered");
			problem.setTitle("Conflict");
			throw new ErrorResponseException(HttpStatus.CONFLICT, problem, null);
		}
		byte[] raw = new byte[32];
		random.nextBytes(raw);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
		confirmations.save(new Confirmation(normalized, sha256(token)));
		return new IssuedToken(normalized, token);
	}

	@Transactional
	public void confirm(String token) {
		Confirmation confirmation = confirmations.findByTokenHash(sha256(token))
				.orElseThrow(ConfirmationService::missing);
		confirmation.confirm();
	}

	public static String sha256(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (java.security.NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static ErrorResponseException missing() {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Confirmation token was not found");
		problem.setTitle("Not Found");
		return new ErrorResponseException(HttpStatus.NOT_FOUND, problem, null);
	}

	public record IssuedToken(String email, String token) {
	}
}
