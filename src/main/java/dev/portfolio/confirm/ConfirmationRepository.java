package dev.portfolio.confirm;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmationRepository extends JpaRepository<Confirmation, Long> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<Confirmation> findByTokenHash(String tokenHash);
}
