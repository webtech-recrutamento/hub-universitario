package br.edu.hub;

import br.edu.hub.entity.Activity;
import br.edu.hub.entity.ActivityCategory;
import br.edu.hub.entity.ActivityStatus;
import br.edu.hub.repository.ActivityRepository;
import br.edu.hub.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ActivityControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ActivityRepository activityRepository;
    @Autowired RegistrationRepository registrationRepository;

    private Activity openActivity;
    private Activity fullActivity;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        activityRepository.deleteAll();
        openActivity = activityRepository.save(new Activity(
                "Workshop de APIs", "Uma atividade aberta para os testes.", ActivityCategory.WORKSHOP,
                ActivityStatus.OPEN, 2, 0, "Equipe Hub", "Lab 1", LocalDateTime.now().plusDays(2)));
        fullActivity = activityRepository.save(new Activity(
                "Curso lotado", "Uma atividade lotada para os testes.", ActivityCategory.COURSE,
                ActivityStatus.FULL, 1, 1, "Equipe Hub", "Lab 2", LocalDateTime.now().plusDays(3)));
        activityRepository.save(new Activity(
                "Encontro cultural", "Sessão comentada de cinema brasileiro.", ActivityCategory.EVENT,
                ActivityStatus.OPEN, 20, 0, "Equipe Hub", "Auditório", LocalDateTime.now().plusDays(4)));
    }

    @Test
    void shouldListActivities() throws Exception {
        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldFindAccentuatedTextWhenSearchTermHasNoAccent() throws Exception {
        mockMvc.perform(get("/api/activities").param("search", "sessao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Encontro cultural"));
    }

    @Test
    void shouldFindExistingActivity() throws Exception {
        mockMvc.perform(get("/api/activities/{id}", openActivity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Workshop de APIs"))
                .andExpect(jsonPath("$.remainingSpots").value(2));
    }

    @Test
    void shouldReturn404ForUnknownActivity() throws Exception {
        mockMvc.perform(get("/api/activities/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Activity not found"));
    }

    @Test
    void shouldPreventRegistrationWhenActivityIsFull() throws Exception {
        mockMvc.perform(post("/api/activities/{id}/registrations", fullActivity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentName":"Maria Souza","studentEmail":"maria@email.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Activity is full"));
    }

    @Test
    void shouldCreateRegistrationForOpenActivity() throws Exception {
        mockMvc.perform(post("/api/activities/{id}/registrations", openActivity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentName":"Maria Souza","studentEmail":"maria@email.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentEmail").value("maria@email.com"));
    }

    @Test
void shouldPreventDuplicateRegistrationForSameActivity() throws Exception {
    mockMvc.perform(post("/api/activities/{id}/registrations", openActivity.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"studentName":"Maria Souza","studentEmail":"maria@email.com"}
                            """))
            .andExpect(status().isCreated());

    mockMvc.perform(post("/api/activities/{id}/registrations", openActivity.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"studentName":"Maria Souza","studentEmail":"maria@email.com"}
                            """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message")
                    .value("Estudante já inscrito nesta atividade"));
}
}
