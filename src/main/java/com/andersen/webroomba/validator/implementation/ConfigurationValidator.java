package com.andersen.webroomba.validator.implementation;

import com.andersen.webroomba.entity.inner.GridConfiguration;
import com.andersen.webroomba.validator.Validator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates Grid configuration using smaller validators.
 *
 * @author Yaroslav Dmitriev (
 * @since 15.05.2021
 */
@Component
public class ConfigurationValidator implements Validator<GridConfiguration> {

    private final Validator<int[]> gridSizeValidator;
    private final Validator<GridConfiguration> hooverStartPositionValidator;
    private final Validator<List<int[]>> dirtPositionsValidator;
    private final Validator<String> instructionsValidator;

    public ConfigurationValidator(final Validator<int[]> gridSizeValidator,
                                  final Validator<GridConfiguration> hooverStartPositionValidator,
                                  final Validator<List<int[]>> dirtPositionsValidator,
                                  final Validator<String> instructionsValidator) {
        this.gridSizeValidator = gridSizeValidator;
        this.hooverStartPositionValidator = hooverStartPositionValidator;
        this.dirtPositionsValidator = dirtPositionsValidator;
        this.instructionsValidator = instructionsValidator;
    }

    @Override
    public void validate(final GridConfiguration suspiciousConfiguration) {
        //gridSizeValidator.validate(suspiciousConfiguration.getRoomSize());
        hooverStartPositionValidator.validate(suspiciousConfiguration);
        dirtPositionsValidator.validate(suspiciousConfiguration.getPatches());
        instructionsValidator.validate(suspiciousConfiguration.getInstructions());
    }


}
