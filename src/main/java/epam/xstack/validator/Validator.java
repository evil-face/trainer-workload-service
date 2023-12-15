package epam.xstack.validator;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Validator<T> {
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final javax.validation.Validator validator = factory.getValidator();
    public Set<String> validate(T objectToValidate) {
        Set<ConstraintViolation<T>> violations = validator.validate(objectToValidate);
        if (!violations.isEmpty()) {
            return violations.stream()
                    .map(violation -> violation.getMessage() + " - " + violation.getInvalidValue())
                    .collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }
}
