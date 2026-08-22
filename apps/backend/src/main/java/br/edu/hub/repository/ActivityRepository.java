package br.edu.hub.repository;

import br.edu.hub.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findAllByOrderByDateAsc();
    List<Activity> findAllByOrderByDateDesc();
    List<Activity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByDateDesc(
        String title, 
        String description
    );
}
