package com.kerneldc.springsecurityjwt.csv.service.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.IEntityEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeanTransformerService {

	private final Collection<IBeanTransformer> beanTransformerCollection;
	
	@SuppressWarnings("preview")
	public record BeanTransformerResult(List<? extends AbstractPersistableEntity> beanList, List<TransformerException> transformerExceptionList) {/*public BeanTransformerResult (){ this(null, null); }*/};
	
	public BeanTransformerResult applyTransformers(IEntityEnum uploadTabelEnum, List<? extends AbstractPersistableEntity> beanList) {
		
		var beanTransformerResult = new BeanTransformerResult(beanList, new ArrayList<>());
		
		for (IBeanTransformer transformer: beanTransformerCollection) {
			for (TransformationStagesEnum stage : TransformationStagesEnum.values())
				if (transformer.canHandle(uploadTabelEnum, stage)) {
					try {
						LOGGER.info("Using {} to transform bean of {} table.", transformer.getClass().getSimpleName(), uploadTabelEnum);
						beanTransformerResult = transformer.transform(beanTransformerResult);
					} catch (TransformerException e) {
						beanTransformerResult.transformerExceptionList().add(e);
						beanTransformerResult = new BeanTransformerResult(beanTransformerResult.beanList(), beanTransformerResult.transformerExceptionList());
					}
				}
		}
		return beanTransformerResult;
	}

}
