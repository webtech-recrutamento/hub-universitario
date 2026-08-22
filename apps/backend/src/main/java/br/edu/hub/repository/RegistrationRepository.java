package br.edu.hub.repository;

import br.edu.hub.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByActivityIdOrderByCreatedAtAsc(Long activityId);
    boolean existsByActivityIdAndStudentEmail(Long activityId, String studentEmail);
}
