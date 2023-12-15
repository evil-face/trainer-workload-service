package epam.xstack.service;

import epam.xstack.dto.workload.Action;
import epam.xstack.dto.workload.TrainerWorkloadRequestDTO;
import epam.xstack.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.Set;

@Service
public class WorkloadReceiver {
    private final TrainerWorkloadService trainerWorkloadService;
    private final Validator<TrainerWorkloadRequestDTO> validator;
    private static final String WORKLOAD_QUEUE = "trainer.workload.queue";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkloadReceiver.class);


    public WorkloadReceiver(TrainerWorkloadService trainerWorkloadService,
                            Validator<TrainerWorkloadRequestDTO> validator) {
        this.trainerWorkloadService = trainerWorkloadService;
        this.validator = validator;
    }


    @JmsListener(destination = WORKLOAD_QUEUE)
    public void receiveMessage(@Payload TrainerWorkloadRequestDTO requestDTO,
                               @Header(name = "txID") String txID) {
        validatePayload(txID, requestDTO);

        LOGGER.info("TX ID: {} — Received message from main service with workload for trainer '{}'",
                txID, requestDTO.getUsername());

        Action action = requestDTO.getAction();

        if (action.equals(Action.ADD)) {
            trainerWorkloadService.addNewTraining(txID, requestDTO);
        } else if (action.equals(Action.DELETE)) {
            trainerWorkloadService.deleteTraining(txID, requestDTO);
        }

    }

    private void validatePayload(String txID, TrainerWorkloadRequestDTO requestDTO) {
        Set<String> violations = validator.validate(requestDTO);

        if (!violations.isEmpty()) {
            LOGGER.warn("TX ID: {} — Found violations in payload: {}", txID, violations);
            throw new ValidationException();
        }
    }
}
