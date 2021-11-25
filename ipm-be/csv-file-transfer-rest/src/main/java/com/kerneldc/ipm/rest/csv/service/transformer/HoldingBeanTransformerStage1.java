package com.kerneldc.ipm.rest.csv.service.transformer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.Holding;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Portfolio;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.repository.PortfolioRepository;
import com.kerneldc.ipm.rest.csv.service.transformer.BeanTransformerService.BeanTransformerResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HoldingBeanTransformerStage1 implements IBeanTransformer {

	private final InstrumentRepository instrumentRepository;
	private final PortfolioRepository portfolioRepository;
	@Override
	public BeanTransformerResult transform(BeanTransformerResult beanTransformerResult/*List<? extends AbstractPersistableEntity> beanList*/)
			throws TransformerException {
		var inputHoldingList = beanTransformerResult.beanList();
		var transformerExceptionList = beanTransformerResult.transformerExceptionList();
		
		List<Holding> transformedHoldingList = new ArrayList<>();
		
		for (AbstractPersistableEntity bean : inputHoldingList) {
			var holding = SerializationUtils.clone((Holding) bean);

			var exceptionMessage = new StringBuilder(String.format("Exception while transforming bean: [%s]", holding.toString()));
			var exceptionsFound = false;
			
			List<Instrument> instrumentList = instrumentRepository.findByTickerAndExchange(holding.getTicker(), holding.getExchange());
			if (CollectionUtils.isEmpty(instrumentList)) {
				exceptionMessage.append(String.format(" Instrument not found with ticker: [%s] and exchange: [%s]", holding.getTicker(), holding.getExchange()));
				exceptionsFound = true;
			} else {
				holding.setInstrument(instrumentList.get(0));
			}
			
			List<Portfolio> portfolioList = portfolioRepository.findByinstitutionAndAccountNumber(holding.getInstitution(), holding.getAccountNumber());
			if (CollectionUtils.isEmpty(portfolioList)) {
				exceptionMessage.append(String.format(" Posrtfolio not found with instititution: [%s] and account number: [%s]", holding.getInstitution(), holding.getAccountNumber()));
				exceptionsFound = true;
			} else {
				holding.setPortfolio(portfolioList.get(0));
			}

			if (exceptionsFound) {
				transformerExceptionList.add(new TransformerException(exceptionMessage.toString()));
			} else {
				transformedHoldingList.add(holding);
			}
		}
		
		return new BeanTransformerResult(transformedHoldingList, transformerExceptionList);
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStagesEnum transformationStagesEnum) {
		return uploadTableEnum.equals(InvestmentPortfolioTableEnum.HOLDING)
				&& transformationStagesEnum.equals(TransformationStagesEnum.STAGE_ONE);
	}
}
