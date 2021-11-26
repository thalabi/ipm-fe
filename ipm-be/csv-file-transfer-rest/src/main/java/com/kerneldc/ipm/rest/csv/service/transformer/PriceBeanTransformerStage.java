package com.kerneldc.ipm.rest.csv.service.transformer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.rest.csv.service.transformer.BeanTransformerService.BeanTransformerResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PriceBeanTransformerStage implements IBeanTransformer {

	private final InstrumentRepository instrumentRepository;
	@Override
	public BeanTransformerResult transform(BeanTransformerResult beanTransformerResult) throws TransformerException {
		var inputPriceList = beanTransformerResult.beanList();
		var transformerExceptionList = beanTransformerResult.transformerExceptionList();
		
		List<Price> transformedPriceList = new ArrayList<>();
		
		for (AbstractPersistableEntity bean : inputPriceList) {
			var price = SerializationUtils.clone((Price) bean);

			var exceptionMessage = new StringBuilder(String.format("Exception while transforming bean: [%s]", price.toString()));
			var exceptionsFound = false;
			
			List<Instrument> instrumentList = instrumentRepository.findByTickerAndExchange(price.getTicker(), price.getExchange());
			if (CollectionUtils.isEmpty(instrumentList)) {
				exceptionMessage.append(String.format(" Instrument not found with ticker: [%s] and exchange: [%s]", price.getTicker(), price.getExchange()));
				exceptionsFound = true;
			} else {
				price.setInstrument(instrumentList.get(0));
			}
			
			if (exceptionsFound) {
				transformerExceptionList.add(new TransformerException(exceptionMessage.toString()));
			} else {
				transformedPriceList.add(price);
			}
		}
		
		return new BeanTransformerResult(transformedPriceList, transformerExceptionList);
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStageEnum transformationStageEnum) {
		return uploadTableEnum.equals(InvestmentPortfolioTableEnum.PRICE)
				&& transformationStageEnum.equals(TransformationStageEnum.STAGE_ONE);
	}
}
