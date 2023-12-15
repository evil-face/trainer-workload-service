package epam.xstack.unit.service;

import epam.xstack.dto.workload.Action;
import epam.xstack.dto.workload.TrainerWorkloadRequestDTO;
import epam.xstack.service.TrainerWorkloadService;
import epam.xstack.service.WorkloadReceiver;
import epam.xstack.validator.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ValidationException;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkloadReceiverTest {
    @InjectMocks
    WorkloadReceiver workloadReceiver;

    @Mock
    TrainerWorkloadService trainerWorkloadService;

    @Spy
    Validator<TrainerWorkloadRequestDTO> validator;

    @Test
    void testReceiveMessageCorrectPayloadAddTraining() {
        TrainerWorkloadRequestDTO requestDTO = getCorrectTrainerWorkloadRequestDTO();

        workloadReceiver.receiveMessage(requestDTO, "txID");

        verify(trainerWorkloadService).addNewTraining(anyString(), any(TrainerWorkloadRequestDTO.class));
    }

    @Test
    void testReceiveMessageCorrectPayloadDeleteTraining() {
        TrainerWorkloadRequestDTO requestDTO = getCorrectTrainerWorkloadRequestDTO();
        requestDTO.setAction(Action.DELETE);

        workloadReceiver.receiveMessage(requestDTO, "txID");

        verify(trainerWorkloadService).deleteTraining(anyString(), any(TrainerWorkloadRequestDTO.class));
    }

    @Test
    void testReceiveMessageBadPayload() {
        TrainerWorkloadRequestDTO requestDTO = getBadTrainerWorkloadRequestDTO();

        Assertions.assertThrows(ValidationException.class, () -> {
            workloadReceiver.receiveMessage(requestDTO, "txID");
        });

        verifyNoInteractions(trainerWorkloadService);
    }


    private TrainerWorkloadRequestDTO getCorrectTrainerWorkloadRequestDTO() {
        TrainerWorkloadRequestDTO requestDTO = new TrainerWorkloadRequestDTO();
        requestDTO.setUsername("alex.nevsky");
        requestDTO.setFirstName("Alex");
        requestDTO.setLastName("Nevsky");
        requestDTO.setActive(true);
        requestDTO.setTrainingDate(LocalDate.of(2023, 11, 5));
        requestDTO.setTrainingDuration(50);
        requestDTO.setAction(Action.ADD);

        return requestDTO;
    }

    private TrainerWorkloadRequestDTO getBadTrainerWorkloadRequestDTO() {
        TrainerWorkloadRequestDTO requestDTO = new TrainerWorkloadRequestDTO();
        requestDTO.setUsername("");
        requestDTO.setFirstName(null);
        requestDTO.setLastName("");
        requestDTO.setActive(null);
        requestDTO.setTrainingDate(null);
        requestDTO.setTrainingDuration(-10);
        requestDTO.setAction(null);

        return requestDTO;
    }
}
