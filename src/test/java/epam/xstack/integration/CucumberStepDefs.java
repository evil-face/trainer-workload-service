package epam.xstack.integration;

import epam.xstack.dto.workload.Action;
import epam.xstack.dto.workload.TrainerWorkloadRequestDTO;
import epam.xstack.model.Month;
import epam.xstack.model.TrainerWorkload;
import epam.xstack.model.Year;
import epam.xstack.repository.TrainerWorkloadRepository;
import epam.xstack.service.WorkloadReceiver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("stg")
@Testcontainers
public class CucumberStepDefs {
    @Container
    private static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:latest")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        mongoContainer.start();
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    @Autowired
    private WorkloadReceiver workloadReceiver;

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    private TrainerWorkloadRequestDTO requestDTO;

    @Given("the new message with trainer workload payload of {int} minutes")
    public void the_new_message_with_trainer_workload_payload(int duration) {
        requestDTO = new TrainerWorkloadRequestDTO();

        requestDTO.setUsername("alex.nevsky");
        requestDTO.setFirstName("Alex");
        requestDTO.setLastName("Nevsky");
        requestDTO.setActive(true);
        requestDTO.setTrainingDate(LocalDate.of(2023, 11, 5));
        requestDTO.setTrainingDuration(duration);
        requestDTO.setAction(Action.ADD);
    }

    @When("the message comes to this microservice")
    public void the_message_comes_to_this_microservice() {
        try {
            workloadReceiver.receiveMessage(requestDTO, "cucumberTX");
        } catch (ValidationException e) {
            System.out.println("Validation exception thrown");
        }
    }

    @Then("the training workload is added to the database")
    public void the_training_workload_is_added_to_the_database() {
        Optional<TrainerWorkload> trainerWorkload = trainerWorkloadRepository.getByUsername(requestDTO.getUsername());
        assertThat(trainerWorkload).isPresent();

        int actualDuration = trainerWorkload.get().getWorkloadByYears().get(0)
                .getMonthList().get(0).getTotalTrainingDuration();
        assertThat(actualDuration).isEqualTo(requestDTO.getTrainingDuration());
    }

    @Given("the new message with incorrect trainer workload payload")
    public void the_new_message_with_incorrect_trainer_workload_payload() {
        requestDTO = new TrainerWorkloadRequestDTO();
        requestDTO.setUsername("badtrainer");
    }

    @Then("the training workload will not be added to database")
    public void the_training_workload_will_not_be_added_to_database() {
        Optional<TrainerWorkload> trainerWorkload = trainerWorkloadRepository.getByUsername(requestDTO.getUsername());
        assertThat(trainerWorkload).isNotPresent();
    }

    @Given("the trainer with existing payload of {int} minutes")
    public void the_trainer_with_existing_payload_of_minutes(Integer existingDuration) {
        Month month = new Month(java.time.Month.MARCH, existingDuration);
        Year year = new Year(2023, List.of(month));
        TrainerWorkload trainerWorkload = new TrainerWorkload(null, "existing.trainer",
                "existing", "trainer", true, List.of(year));

        trainerWorkloadRepository.save(trainerWorkload);
    }
    @Given("the new message with delete payload of {int} minutes")
    public void the_new_message_with_delete_payload_of_minutes(Integer deletingDuration) {
        requestDTO = new TrainerWorkloadRequestDTO();

        requestDTO.setUsername("existing.trainer");
        requestDTO.setFirstName("existing");
        requestDTO.setLastName("trainer");
        requestDTO.setActive(true);
        requestDTO.setTrainingDate(LocalDate.of(2023, 3, 5));
        requestDTO.setTrainingDuration(deletingDuration);
        requestDTO.setAction(Action.DELETE);
    }

    @Then("the trainer with existing payload will have {int} minutes left")
    public void the_trainer_with_existing_payload_will_have_minutes_left(Integer remainingDuration) {
        Optional<TrainerWorkload> trainerWorkload = trainerWorkloadRepository.getByUsername(requestDTO.getUsername());
        assertThat(trainerWorkload).isPresent();

        int actualDuration = trainerWorkload.get().getWorkloadByYears().get(0)
                .getMonthList().get(0).getTotalTrainingDuration();
        assertThat(actualDuration).isEqualTo(remainingDuration);
    }
}
