package br.edu.hub.service;

import br.edu.hub.dto.RegistrationRequest;
import br.edu.hub.dto.RegistrationResponse;
import br.edu.hub.entity.Activity;
import br.edu.hub.entity.Registration;
import br.edu.hub.repository.ActivityRepository;
import br.edu.hub.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.hub.exception.DuplicateRegistrationException;

import java.util.List;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final ActivityRepository activityRepository;
    private final ActivityService activityService;

    public RegistrationService(RegistrationRepository registrationRepository,
                               ActivityRepository activityRepository,
                               ActivityService activityService) {
        this.registrationRepository = registrationRepository;
        this.activityRepository = activityRepository;
        this.activityService = activityService;
    }

@Transactional
public RegistrationResponse register(Long activityId, RegistrationRequest request) {
    Activity activity = activityService.requireActivity(activityId);

    if (registrationRepository.existsByActivityIdAndStudentEmail(
            activityId, request.studentEmail())) {
        throw new DuplicateRegistrationException(
                "Estudante já inscrito nesta atividade"
        );
    }

    Registration registration = registrationRepository.save(
            new Registration(activity, request.studentName(), request.studentEmail())
    );

    activity.incrementRegistrations();
    activityRepository.save(activity);

    return RegistrationResponse.from(registration);
}

    @Transactional(readOnly = true)
    public List<RegistrationResponse> list(Long activityId) {
        activityService.requireActivity(activityId);
        return registrationRepository.findByActivityIdOrderByCreatedAtAsc(activityId).stream()
                .map(RegistrationResponse::from)
                .toList();
    }
}
