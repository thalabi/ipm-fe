package com.kerneldc.ipm.rest.csv.service.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
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
	public BeanTransformerResult transform(BeanTransformerResult beanTransformerResult) throws TransformerException {
		var inputHoldingList = beanTransformerResult.beanList();
		var transformerExceptionList = beanTransformerResult.transformerExceptionList();
		
		List<Holding> transformedHoldingList = new ArrayList<>();
		
		for (AbstractPersistableEntity bean : inputHoldingList) {
			var holding = SerializationUtils.clone((Holding) bean);

			var exceptionMessageJoiner = new StringJoiner(". ", StringUtils.EMPTY, ".");
			var exceptionsFound = false;
			
			Consumer<AbstractPersistableEntity> setInstrument = instrument -> holding.setInstrument((Instrument)instrument);
			exceptionsFound = IBeanTransformer.lookupAndSetForeignEntity(instrumentRepository, "Instrument not found with ticker: [%s] and exchange: [%s]", exceptionMessageJoiner, exceptionsFound,
					setInstrument,
					holding.getTicker(), holding.getExchange());

			Consumer<AbstractPersistableEntity> setPortfolio = portfolio -> holding.setPortfolio((Portfolio)portfolio);
			exceptionsFound = IBeanTransformer.lookupAndSetForeignEntity(portfolioRepository, "Portfolio not found with instititution: [%s] and account number: [%s]", exceptionMessageJoiner, exceptionsFound,
					setPortfolio,
					holding.getInstitution(), holding.getAccountNumber());

			if (exceptionsFound) {
				transformerExceptionList.add(new TransformerException(getTransformerName(), holding, exceptionMessageJoiner.toString()));
			} else {
				transformedHoldingList.add(holding);
			}
		}
		
		return new BeanTransformerResult(transformedHoldingList, transformerExceptionList);
	}

	@Override
	public boolean canHandle(IEntityEnum uploadTableEnum, TransformationStageEnum transformationStageEnum) {
		return uploadTableEnum.equals(InvestmentPortfolioTableEnum.HOLDING)
				&& transformationStageEnum.equals(TransformationStageEnum.STAGE_ONE);
	}

	@Override
	public String getTransformerName() {
		return"HoldingBeanTransformerStage1";
	}
}
