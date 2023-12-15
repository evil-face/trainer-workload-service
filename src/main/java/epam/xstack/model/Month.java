package epam.xstack.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Month {
    private java.time.Month month;
    private int totalTrainingDuration;
}
