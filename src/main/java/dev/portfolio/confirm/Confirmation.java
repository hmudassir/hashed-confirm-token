package dev.portfolio.confirm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "confirmations")
public class Confirmation {

	public enum Status {
		PENDING,
		CONFIRMED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 254)
	private String email;

	@Column(name = "token_hash", length = 64)
	private String tokenHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	protected Confirmation() {
	}

	public Confirmation(String email, String tokenHash) {
		this.email = email;
		this.tokenHash = tokenHash;
		this.status = Status.PENDING;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Status getStatus() {
		return status;
	}

	public void confirm() {
		this.status = Status.CONFIRMED;
		this.tokenHash = null;
	}
}
