package br.edu.hub.service;

import br.edu.hub.dto.ActivityResponse;
import br.edu.hub.dto.ActivityUpdateRequest;
import br.edu.hub.entity.Activity;
import br.edu.hub.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> list(String search) {
        List<Activity> activities;

        if (search != null && !search.isBlank()) {
            String term = normalize(search);
            activities = activityRepository.findAllByOrderByDateDesc().stream()
                    .filter(activity -> containsNormalized(activity.getTitle(), term)
                            || containsNormalized(activity.getDescription(), term))
                    .toList();
        } else {
            activities = activityRepository.findAllByOrderByDateAsc();
        }

        return activities.stream()
                .map(ActivityResponse::from)
                .toList();
    }

    private boolean containsNormalized(String value, String term) {
        return normalize(value).contains(term);
    }

    private String normalize(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public ActivityResponse findById(Long id) {
        return ActivityResponse.from(requireActivity(id));
    }

    @Transactional
    public ActivityResponse update(Long id, ActivityUpdateRequest request) {
        Activity activity = requireActivity(id);
        if (request.title() != null) activity.setTitle(request.title());
        if (request.description() != null) activity.setDescription(request.description());
        if (request.category() != null) activity.setCategory(request.category());
        if (request.status() != null) activity.setStatus(request.status());
        if (request.capacity() != null) activity.setCapacity(request.capacity());
        if (request.organizer() != null) activity.setOrganizer(request.organizer());
        if (request.location() != null) activity.setLocation(request.location());
        if (request.date() != null) activity.setDate(request.date());
        return ActivityResponse.from(activityRepository.save(activity));
    }

    public Activity requireActivity(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));
    }
}
