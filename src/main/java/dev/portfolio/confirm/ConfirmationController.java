package dev.portfolio.confirm;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/confirmations")
public class ConfirmationController {

	private final ConfirmationService confirmations;

	public ConfirmationController(ConfirmationService confirmations) {
		this.confirmations = confirmations;
	}

	@PostMapping
	public ResponseEntity<ConfirmationService.IssuedToken> issue(@Valid @RequestBody EmailRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(confirmations.issue(request.email()));
	}

	@PostMapping("/confirm")
	public ResponseEntity<Void> confirm(@Valid @RequestBody TokenRequest request) {
		confirmations.confirm(request.token());
		return ResponseEntity.noContent().build();
	}

	public record EmailRequest(@NotBlank @Email @Size(max = 254) String email) {
	}

	public record TokenRequest(@NotBlank @Size(max = 200) String token) {
	}
}
