package epam.xstack.dto.workload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TrainerWorkloadRequestDTO {
    @NotBlank(message = "Username cannot be empty")
    String username;

    @NotBlank(message = "First name cannot be empty")
    String firstName;

    @NotBlank(message = "Last name cannot be empty")
    String lastName;

    @NotNull(message = "Activation status cannot be empty")
    Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @NotNull(message = "Training date cannot be empty")
    LocalDate trainingDate;

    @Min(value = 1, message = "Training duration must be positive")
    int trainingDuration;

    @NotNull(message = "Action type must be either ADD or DELETE")
    Action action;
}
