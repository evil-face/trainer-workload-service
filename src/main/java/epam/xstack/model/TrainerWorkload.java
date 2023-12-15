package epam.xstack.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@Document("trainer-workload")
public class TrainerWorkload {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    private Boolean active;

    @Field("years")
    private List<Year> workloadByYears;
}
