package com.kerneldc.ipm.rest.csv.service.transformer.bean;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.TransformationStageEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;

public interface IBeanTransformer {

	void transform(FileProcessingContext context) throws AbortFileProcessingException;
	
	boolean canHandle(IEntityEnum entityEnum, TransformationStageEnum transformationStageEnum);
	
	String getTransformerName();
	
	static boolean lookupAndSetForeignEntity(BaseTableRepository<? extends AbstractPersistableEntity, ? extends Serializable> repository,
			String exceptionMessageTemplate,
			StringJoiner exceptionMessageJoiner,
			Consumer<AbstractPersistableEntity> setForeignEntity,
			Object...entityKeys) {
		boolean exceptionsFound = false;
		List<? extends AbstractPersistableEntity> entityList = repository.findByLogicalKeyHolder(LogicalKeyHolder.build(entityKeys));
		if (CollectionUtils.isEmpty(entityList)) {
			exceptionMessageJoiner.add(String.format(exceptionMessageTemplate, entityKeys));
			exceptionsFound = true;
		} else {
			setForeignEntity.accept(entityList.get(0));
		}
		return exceptionsFound;
	}

}
