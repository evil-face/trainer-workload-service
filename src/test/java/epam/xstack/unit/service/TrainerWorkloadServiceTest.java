package epam.xstack.unit.service;

import epam.xstack.dto.workload.Action;
import epam.xstack.dto.workload.TrainerWorkloadRequestDTO;
import epam.xstack.exception.NoSuchTrainerExistException;
import epam.xstack.model.Month;
import epam.xstack.model.TrainerWorkload;
import epam.xstack.model.Year;
import epam.xstack.repository.TrainerWorkloadRepository;
import epam.xstack.service.TrainerWorkloadService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainerWorkloadServiceTest {
    @InjectMocks
    TrainerWorkloadService trainerWorkloadService;

    @Mock
    TrainerWorkloadRepository repository;

    @Test
    void testAddNewTrainingNoTrainerYet() {
        TrainerWorkloadRequestDTO requestDTO = getCorrectTrainerWorkloadRequestDTO();
        TrainerWorkload expected = getExpectedTrainerWorkload();

        when(repository.getByUsername(requestDTO.getUsername())).thenReturn(Optional.empty());

        trainerWorkloadService.addNewTraining("txID", requestDTO);

        verify(repository).save(expected);
    }

    @Test
    void testAddNewTrainingAddingSameMonthTraining() {
        TrainerWorkloadRequestDTO requestDTO = getCorrectTrainerWorkloadRequestDTO();
        TrainerWorkload expected = getExpectedTrainerWorkload();
        Year year = expected.getWorkloadByYears().get(0);
        Month month = year.getMonthList().get(0);
        month.setTotalTrainingDuration(month.getTotalTrainingDuration() + requestDTO.getTrainingDuration());

        when(repository.getByUsername(requestDTO.getUsername())).thenReturn(Optional.of(getExpectedTrainerWorkload()));

        trainerWorkloadService.addNewTraining("txID", requestDTO);

        verify(repository).save(expected);
    }

    @Test
    void testAddNewTrainingAddingAnotherMonthTraining() {
        TrainerWorkloadRequestDTO requestDTO = getCorrectTrainerWorkloadRequestDTO();
        requestDTO.setTrainingDate(LocalDate.of(2023, 12, 6));

        TrainerWorkload expected = getExpectedTrainerWorkload();
        Year year = expected.getWorkloadByYears().get(0);
        List<Month> monthList = year.getMonthList();
        monthList.add(new Month(requestDTO.getTrainingDate().getMonth(), requestDTO.getTrainingDuration()));

        when(repository.getByUsername(requestDTO.getUsername())).thenReturn(Optional.of(getExpectedTrainerWorkload()));

        trainerWorkloadService.addNewTraining("txID", requestDTO);

        verify(repository).save(expected);
    }

    @Test
    void testDeleteTraining() {
        TrainerWorkloadRequestDTO requestDTO = getCorrectTrainerWorkloadRequestDTO();

        TrainerWorkload expected = getPopulatedTrainerWorkloadAfterDeletion();

        when(repository.getByUsername(requestDTO.getUsername())).thenReturn(Optional.of(getPopulatedTrainerWorkload()));

        trainerWorkloadService.deleteTraining("txID", requestDTO);

        verify(repository).save(expected);
    }

    @Test
    void testDeleteTrainingNoTrainerExists() {
        TrainerWorkloadRequestDTO requestDTO = getCorrectTrainerWorkloadRequestDTO();

        when(repository.getByUsername(requestDTO.getUsername())).thenReturn(Optional.empty());

        assertThrows(NoSuchTrainerExistException.class, () -> {
           trainerWorkloadService.deleteTraining("txID", requestDTO);
        });

        verifyNoMoreInteractions(repository);
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

    private TrainerWorkload getExpectedTrainerWorkload() {
        TrainerWorkloadRequestDTO requestDTO = getCorrectTrainerWorkloadRequestDTO();
        Month month = new Month(java.time.Month.NOVEMBER, requestDTO.getTrainingDuration());
        Year year = new Year(requestDTO.getTrainingDate().getYear(), new ArrayList<>(List.of(month)));

        return new TrainerWorkload(
                null,
                requestDTO.getUsername(),
                requestDTO.getFirstName(),
                requestDTO.getLastName(),
                requestDTO.getActive(),
                new ArrayList<>(List.of(year)));
    }

    private TrainerWorkload getPopulatedTrainerWorkload() {
        Month may2022 = new Month(java.time.Month.MAY, 50);
        Month oct2022 = new Month(java.time.Month.OCTOBER, 100);
        Month nov2023 = new Month(java.time.Month.NOVEMBER, 110);
        Month dec2023 = new Month(java.time.Month.DECEMBER, 120);

        Year year2022 = new Year(2022, new ArrayList<>(List.of(may2022, oct2022)));
        Year year2023 = new Year(2023, new ArrayList<>(List.of(nov2023, dec2023)));

        return new TrainerWorkload(
                null,
                "alex.nevsky",
                "Alex",
                "Nevsky",
                true,
                new ArrayList<>(List.of(year2022, year2023)));
    }

    private TrainerWorkload getPopulatedTrainerWorkloadAfterDeletion() {
        TrainerWorkload trainerWorkload = getPopulatedTrainerWorkload();
        Year yearEntry = trainerWorkload.getWorkloadByYears().stream()
                .filter(year -> year.getYear() == 2023).findFirst().get();
        Month monthForDeletion = yearEntry.getMonthList().stream()
                .filter(month -> month.getMonth().equals(java.time.Month.NOVEMBER)).findFirst().get();

        monthForDeletion.setTotalTrainingDuration(60);

        return trainerWorkload;
    }
}
