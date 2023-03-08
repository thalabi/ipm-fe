package com.kerneldc.ipm.rest.csv.service.transformer;

import java.util.Collection;

import org.springframework.stereotype.Service;

import com.kerneldc.ipm.rest.csv.service.transformer.bean.IBeanTransformer;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeanTransformerService {

	private final Collection<IBeanTransformer> beanTransformerCollection;
	
//	public record BeanTransformerResult(List<? extends AbstractPersistableEntity> beanList, List<BeanTransformerException> beanTransformerExceptionList) {
//		public BeanTransformerResult() {
//			this(List.of(), List.of());
//		}
//	};
	
	public void applyTransformers(FileProcessingContext context/*IEntityEnum uploadTabelEnum, List<? extends AbstractPersistableEntity> beanList*/) throws AbortFileProcessingException {
		
		//var context = FileProcessingContext.get();
		
		//var beanTransformerResult = new BeanTransformerResult(context.getBeans(), new ArrayList<>());
		
		for (IBeanTransformer transformer: beanTransformerCollection) {
			for (TransformationStageEnum stage : TransformationStageEnum.values())
				if (transformer.canHandle(context.getUploadTableEnum(), stage)) {
//					try {
						LOGGER.info("Using {} to transform bean of {} table.", transformer.getClass().getSimpleName(), context.getUploadTableEnum());
						transformer.transform(context);
//					} catch (BeanTransformerException e) {
//						beanTransformerResult.transformerExceptionList().add(e);
//						beanTransformerResult = new BeanTransformerResult(beanTransformerResult.beanList(), beanTransformerResult.transformerExceptionList());
//					}
				}
		}
	}

}
