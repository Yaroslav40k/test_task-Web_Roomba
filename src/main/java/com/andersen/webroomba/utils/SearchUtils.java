package com.andersen.webroomba.utils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class for <code>JpaSpecificationExecutor</code> implementations.
 * It provides handy methods to create 'search request' specifications (in the sense of Domain Driven Design).
 */
public final class SearchUtils {

    private static final int SORT_BY_FIRST_INDEX_WITHOUT_SORT_DIRECTION = 1;
    private static final String DESC = "-";
    private static final int SINGLE_SPECIFICATION_ARRAY_SIZE = 1;
    private static final int FIRST_SPECIFICATION = 0;
    private static final int SUBSEQUENT_SPECIFICATION = 1;
    public static final int ROOT = 0;

    private SearchUtils() { }

    /**
     * Creates <code>Specification</code> from both 'AND'(solid)  and 'OR'(optional) statements.
     *
     * @param andConditions - a list of 'AND' statements
     * @param orConditions  - a list of 'OR' statements
     * @param <T>           - type of the specification
     *
     * @return a united specification
     */
    public static <T> Specification<T> createSpecification(
            List<Specification<T>> andConditions,
            List<Specification<T>> orConditions) {

        Specification<T> specification = createSpecification(andConditions);

        if (!orConditions.isEmpty()) {
            specification = addOrSpecification(orConditions, specification);
        }
        return specification;
    }

    /**
     * Creates <code>Specification</code> from both 'AND'(solid)statements.
     *
     * @param andConditions - a list of 'AND' statements
     * @param <T>           - type of the specification
     *
     * @return a united specification
     */
    public static <T> Specification<T> createSpecification(List<Specification<T>> andConditions) {

        List<Specification<T>> nonNullConditions = andConditions.stream()
                .filter(Objects::nonNull)
                .toList();
        Specification<T> specification = null;

        if (!nonNullConditions.isEmpty()) {
            specification = addAndSpecification(nonNullConditions);
        }
        return specification;
    }

    /**
     * Creates <code>Sort</code> option for <code>Specification</code> based queries.
     *
     * @param sortBy       shortcut of a parameter to sort by
     * @param defaultValue a parameter that will be used if a passed shortcut doesn't have a value
     *
     * @return a sort option
     */
    public static Sort getSortSettings(String sortBy, String defaultValue) {
        if (sortBy == null) {
            sortBy = defaultValue;
        }
        boolean isDesc = sortBy.startsWith(DESC);

        String sortByFieldToCheck = isDesc ? sortBy.substring(SORT_BY_FIRST_INDEX_WITHOUT_SORT_DIRECTION) : sortBy;

        Sort.Direction sortDirection = isDesc ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(sortDirection, sortByFieldToCheck);
    }

    public static <T> Specification<T> createOrUnitedSpecification(List<Specification<T>> orConditions) {

        Specification<T> overallSpecification = null;
        List<Specification<T>> nonNullConditions = orConditions.stream().filter(Objects::nonNull).toList();
        if (!nonNullConditions.isEmpty()) {
            overallSpecification = nonNullConditions.get(FIRST_SPECIFICATION);
            if (orConditions.size() > SINGLE_SPECIFICATION_ARRAY_SIZE) {
                for (int i = SUBSEQUENT_SPECIFICATION; i < nonNullConditions.size(); i++) {
                    overallSpecification = overallSpecification.or(nonNullConditions.get(i));
                }
            }
        }
        return overallSpecification;
    }

    private static <T> Specification<T> addOrSpecification(List<Specification<T>> orConditions,
                                                           Specification<T> andSpecification) {

        Specification<T> overallSpecification = andSpecification;
        if (overallSpecification == null) {
            overallSpecification = orConditions.get(FIRST_SPECIFICATION);
        }
        if (orConditions.size() > SINGLE_SPECIFICATION_ARRAY_SIZE) {
            for (int i = SUBSEQUENT_SPECIFICATION; i < orConditions.size(); i++) {
                overallSpecification = overallSpecification.or(orConditions.get(i));
            }
        }
        return overallSpecification;
    }

    private static <T> Specification<T> addAndSpecification(List<Specification<T>> addConditions) {
        Specification<T> specification;
        specification = addConditions.get(FIRST_SPECIFICATION);

        if (addConditions.size() > SINGLE_SPECIFICATION_ARRAY_SIZE) {
            for (int i = SUBSEQUENT_SPECIFICATION; i < addConditions.size(); i++) {
                specification = specification.and(addConditions.get(i));
            }
        }
        return specification;
    }

    /**
     * Creates <code>Specification</code> statement.
     *
     * @param parameterGetter a supplier that holds the parameter for SQL search condition
     * @param entityParams    an array of an entity parameters
     * @param <T>             the type of the {@link Root} the resulting {@literal Specification} operates on
     * @param <E>             the type of results supplied by 'parameterGetter'
     *
     * @return a specification or null if a condition parameter wasn't passed
     */
    public static <T, E> Specification<T> createEqualsStatement(
            Supplier<E> parameterGetter,
            String... entityParams) {

        Specification<T> specification = null;

        if (parameterGetter.get() != null) {
            specification = (root, query, builder)
                    -> builder.equal(getExpression(root, entityParams), parameterGetter.get());
        }

        return specification;
    }

    /**
     * Creates <code>Specification</code> statement. Opposite to <code>createEqualsStatement</code>
     *
     * @param parameterGetter a supplier that holds the parameter for SQL search condition
     * @param entityParams    an array of an entity parameters
     * @param <T>             the type of the {@link Root} the resulting {@literal Specification} operates on
     * @param <E>             the type of results supplied by 'parameterGetter'
     *
     * @return a specification or null if a condition parameter wasn't passed
     */
    public static <T, E> Specification<T> createNotEqualsStatement(
            Supplier<E> parameterGetter,
            String... entityParams) {

        Specification<T> specification = null;

        if (parameterGetter.get() != null) {
            specification = (root, query, builder)
                    -> builder.notEqual(getExpression(root, entityParams), parameterGetter.get());
        }

        return specification;
    }

    /**
     * Creates <code>Specification</code> statement. Represents SQL IN clause.
     *
     * @param parameterGetter a supplier that holds a List of values for IN clause
     * @param entityParams    an array of an entity parameters
     * @param <T>             the type of the {@link Root} the resulting {@literal Specification} operates on
     * @param <E>             the type of results supplied by 'parameterGetter' List
     *
     * @return a specification or null if a condition parameter wasn't passed
     */
    public static <T, E> Specification<T> createInStatement(
            Supplier<List<E>> parameterGetter,
            String... entityParams) {

        Specification<T> specification = null;

        if (parameterGetter.get() != null) {
            specification = (root, query, builder)
                    -> {
                CriteriaBuilder.In<E> in = builder.in(getExpression(root, entityParams));
                for (E param : parameterGetter.get()) {
                    in.value(param);
                }
                return in;
            };
        }
        return specification;
    }

    private static <T, E> Expression<E> getExpression(Root<T> queryRoot, String[] params) {
        Path<E> expression = queryRoot.get(params[ROOT]);
        if (params.length > 1) {
            for (int i = 1; i < params.length; i++) {
                expression = expression.get(params[i]);
            }
        }
        return expression;
    }

    public static <T> Specification<T> createGreaterOrEqualsStatement(
            Supplier<OffsetDateTime> parameterGetter,
            String entityParam) {

        Specification<T> specification = null;

        if (parameterGetter.get() != null) {
            specification = (root, query, builder)
                    -> builder.greaterThanOrEqualTo(root.get(entityParam), parameterGetter.get());
        }
        return specification;
    }

    public static <T, E> Specification<T> createLikeIgnoreCaseStatement(
            Supplier<E> parameterGetter,
            String entityParam) {

        Specification<T> specification = null;

        if (parameterGetter.get() != null) {
            specification = (root, query, builder)
                    -> builder.like(
                            builder.upper(root.get(entityParam)),
                    "%" + parameterGetter.get().toString().toUpperCase(Locale.ROOT) + "%"
                    );
        }

        return specification;
    }

    public static <T, E> Specification<T> createStartsWithStatement(
            Supplier<E> parameterGetter,
            String entityParam) {

        Specification<T> specification = null;

        if (parameterGetter.get() != null) {
            specification = (root, query, builder)
                    -> builder.like(root.get(entityParam), "" + parameterGetter.get().toString() + "%");
        }

        return specification;
    }

    public static <T, E> Specification<T> createStartsWithIgnoreCaseStatement(
            Supplier<E> parameterGetter,
            String entityParam) {

        Specification<T> specification = null;

        if (parameterGetter.get() != null) {
            specification = (root, query, builder)
                    -> builder.like(
                            builder.upper(root.get(entityParam)),
                    "" + parameterGetter.get().toString().toUpperCase(Locale.ROOT) + "%"
                    );
        }

        return specification;
    }

}
