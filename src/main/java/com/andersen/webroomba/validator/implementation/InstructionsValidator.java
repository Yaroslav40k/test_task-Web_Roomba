package com.andersen.webroomba.validator.implementation;

import com.andersen.webroomba.exception.MissingParameterException;
import com.andersen.webroomba.validator.Validator;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validates hoover instructions.
 *
 * @author Yaroslav Dmitriev (
 * @since 15.05.2021
 */
@Component
public class InstructionsValidator implements Validator<String> {

    private static final Pattern INSTRUCTIONS_PATTERN = Pattern.compile("^[{WNES}]*$");

    @Override
    public void validate(String instructions) {

        if (instructions == null || instructions.isBlank()) {
            throw new MissingParameterException("Instructions  were not specified");
        }

        if (Boolean.FALSE.equals(INSTRUCTIONS_PATTERN.matcher(instructions).matches())) {
            throw new IllegalArgumentException("Unsupported chars found in the instruction");
        }
    }


}
