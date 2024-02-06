package com.kerneldc.common.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import com.kerneldc.common.domain.AbstractEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntitySpecification<T> implements Specification<T>{

	private static final long serialVersionUID = 1L;

	private enum QueryOperatorEnum {
		EQUALS("equals"), NOT_EQUALS("notEquals"), GREATER_THAN("greaterThan"), GT("gt"),
		GREATER_THAN_OR_EQUAL_TO("greaterThanOrEqualTo"), LESS_THAN("lessThan"), LT("lt"), LESS_THAN_OR_EQUAL_TO("lessThanOrEqualTo"),
		STARTS_WITH("startsWith"), CONTAINS("contains"), NOT_CONTAINS("notContains"), ENDS_WITH("endsWith");

		private String operator;
		public String getOperator() {
			return operator;
		}
		QueryOperatorEnum(String operator) {
			this.operator = operator;
		}
		public static QueryOperatorEnum fromName(String name) {
			for (QueryOperatorEnum e : QueryOperatorEnum.values()) {
				if (e.getOperator().equals(name)) {
					return e;
				}
			}
			return null;
		}
	}
	record Filter(String field, QueryOperatorEnum operator, String value) {};
	private List<Filter> filterList = new ArrayList<>();
	private EntityType<? extends AbstractEntity> entityMetamodel;

	public EntitySpecification(EntityType<? extends AbstractEntity> entityMetamodel, String searchCriteria) {
		if (StringUtils.isEmpty(searchCriteria)) {
			return;
		}
		this.entityMetamodel = entityMetamodel;
		filterList = Arrays.asList(searchCriteria.split(",")).stream().map(criterion -> {
			var criterionParts = criterion.split("\\|");
			LOGGER.info("criterionParts: {}", Arrays.asList(criterionParts));
			return new Filter(criterionParts[0], QueryOperatorEnum.fromName(criterionParts[1]), criterionParts[2]);
		}).toList();
	}

	private Specification<T> getSpecificationFromFilters() {
		if (filterList.isEmpty()) {
			return Specification.where(null);
		}
		Specification<T> specification = Specification.where(createSpecification(filterList.get(0)));
		for (int i = 1; i < filterList.size(); i++) {
			specification = specification.and(createSpecification(filterList.get(i)));
		}
		return specification;
	}

	private Specification<T> createSpecification(Filter input) {
		var field = input.field();
		var value = input.value();
		var fieldTypeIsString = StringUtils.equals(entityMetamodel.getDeclaredAttribute(field).getJavaType().getSimpleName(), "String");
		
		if (fieldTypeIsString) {
			
			switch (input.operator()) {
			case EQUALS -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.lower(root.get(field)), value.toLowerCase());
			}
			case NOT_EQUALS -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(criteriaBuilder.lower(root.get(field)), value.toLowerCase());
			}
			case STARTS_WITH -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), value.toLowerCase() + "%");
			}
			case CONTAINS -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), "%" + value.toLowerCase() + "%");
			}
			case NOT_CONTAINS -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.not(criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), "%" + value.toLowerCase() + "%"));
			}
			case ENDS_WITH -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), "%" + value.toLowerCase());
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + input.operator());
			}
			
		} else {

			switch (input.operator()) {
			case EQUALS -> {
					return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
			}
			case NOT_EQUALS -> {
					return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(field), value);
			}
			case GREATER_THAN, GT -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(field), value);
			}
			case GREATER_THAN_OR_EQUAL_TO -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(field), value);
			}
			case LESS_THAN, LT -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(field), value);
			}
			case LESS_THAN_OR_EQUAL_TO -> {
				return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(field), value);
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + input.operator());
			}
		}
	}

//	private Object castToRequiredType(Class<?> fieldType, String value) {
//		if (fieldType.isAssignableFrom(Float.class)) {
//			return Float.valueOf(value);
//		} else if (fieldType.isAssignableFrom(Double.class)) {
//			return Double.parseDouble(value);
//		} else if (fieldType.isAssignableFrom(Integer.class)) {
//			return Integer.valueOf(value);
//		} else if (fieldType.isAssignableFrom(BigDecimal.class)) {
//			return new BigDecimal(value);
//		}
//		return value;
//	}


	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
		return getSpecificationFromFilters().toPredicate(root, query, criteriaBuilder);
	}

}
