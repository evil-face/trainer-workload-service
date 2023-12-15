package epam.xstack.service;

import epam.xstack.dto.workload.TrainerWorkloadRequestDTO;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.model.Month;
import epam.xstack.model.TrainerWorkload;
import epam.xstack.model.Year;
import epam.xstack.repository.TrainerWorkloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class TrainerWorkloadService {
    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerWorkloadService.class);;

    @Autowired
    public TrainerWorkloadService(TrainerWorkloadRepository trainerWorkloadRepository) {
        this.trainerWorkloadRepository = trainerWorkloadRepository;
    }

    public void addNewTraining(String txID, TrainerWorkloadRequestDTO requestDTO) {
        TrainerWorkload trainerWorkload = getOrCreateTrainerWorkload(requestDTO);
        Year year = getOrCreateYear(requestDTO, trainerWorkload);
        Month month = getOrCreateMonth(requestDTO, year);

        month.setTotalTrainingDuration(month.getTotalTrainingDuration() + requestDTO.getTrainingDuration());
        trainerWorkloadRepository.save(trainerWorkload);

        LOGGER.info("TX ID: {} — Added new workload for '{}' trainer: +{} minutes in {} {}",
                txID, requestDTO.getUsername(), requestDTO.getTrainingDuration(),
                requestDTO.getTrainingDate().getMonth(), requestDTO.getTrainingDate().getYear());
    }

    public void deleteTraining(String txID, TrainerWorkloadRequestDTO requestDTO) {
        TrainerWorkload trainerWorkload = trainerWorkloadRepository.getByUsername(requestDTO.getUsername())
                .orElseThrow(() -> new NoSuchTrainerExistException(txID));

        Optional<Year> yearOpt = trainerWorkload.getWorkloadByYears().stream()
                .filter(year -> year.getYear() == requestDTO.getTrainingDate().getYear())
                .findFirst();

        yearOpt.flatMap(year -> year.getMonthList().stream()
                .filter(month -> month.getMonth().equals(requestDTO.getTrainingDate().getMonth()))
                .findFirst())
                .ifPresent(month -> {
                    month.setTotalTrainingDuration(
                            month.getTotalTrainingDuration() - requestDTO.getTrainingDuration());

                    trainerWorkloadRepository.save(trainerWorkload);

                    LOGGER.info("TX ID: {} — Removed existing workload for '{}' trainer: -{} minutes in {} {}",
                            txID, requestDTO.getUsername(), requestDTO.getTrainingDuration(),
                            requestDTO.getTrainingDate().getMonth(), requestDTO.getTrainingDate().getYear());
                });
    }

    private TrainerWorkload getOrCreateTrainerWorkload(TrainerWorkloadRequestDTO requestDTO) {
        Optional<TrainerWorkload> trainerWorkloadOpt =
                trainerWorkloadRepository.getByUsername(requestDTO.getUsername());

        trainerWorkloadOpt.ifPresent(value -> updateTrainerFields(value, requestDTO));

        return trainerWorkloadOpt.orElseGet(() -> new TrainerWorkload(
                null,
                requestDTO.getUsername(),
                requestDTO.getFirstName(),
                requestDTO.getLastName(),
                requestDTO.getActive(),
                new ArrayList<>()
        ));
    }

    private Year getOrCreateYear(TrainerWorkloadRequestDTO requestDTO, TrainerWorkload trainerWorkload) {
        int trainingYear = requestDTO.getTrainingDate().getYear();

        return trainerWorkload.getWorkloadByYears().stream()
                .filter(year -> year.getYear() == trainingYear)
                .findFirst()
                .orElseGet(() -> {
                    Year newYear = new Year(trainingYear, new ArrayList<>());
                    trainerWorkload.getWorkloadByYears().add(newYear);
                    return newYear;
                });
    }

    private Month getOrCreateMonth(TrainerWorkloadRequestDTO requestDTO, Year year) {
        java.time.Month trainingMonth = requestDTO.getTrainingDate().getMonth();

        return year.getMonthList().stream()
                .filter(month -> month.getMonth().equals(trainingMonth))
                .findFirst()
                .orElseGet(() -> {
                    Month newMonth = new Month(trainingMonth, 0);
                    year.getMonthList().add(newMonth);
                    return newMonth;
                });
    }


    private void updateTrainerFields(TrainerWorkload trainerWorkload, TrainerWorkloadRequestDTO requestDTO) {
        trainerWorkload.setFirstName(requestDTO.getFirstName());
        trainerWorkload.setLastName(requestDTO.getLastName());
        trainerWorkload.setActive(requestDTO.getActive());
    }

    @ExceptionHandler(NoSuchTrainerExistException.class)
    private void handleNoSuchTrainerExistsException(RuntimeException e) {
        String errorMessage = "This trainer had no workload in database";
        LOGGER.warn("TX ID: {} — {}", e.getMessage(), "No workload found for this trainer - nothing to delete");
    }
}
