package com.kerneldc.ipm.rest.csv.service.transformer;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.BeanTransformerService.BeanTransformerResult;

public interface IBeanTransformer {

	BeanTransformerResult transform(BeanTransformerResult beanTransformerResultList) throws TransformerException;
	
	boolean canHandle(IEntityEnum entityEnum, TransformationStagesEnum transformationStagesEnum);
}
