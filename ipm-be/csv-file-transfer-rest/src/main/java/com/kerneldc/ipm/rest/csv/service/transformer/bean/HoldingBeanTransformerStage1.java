package com.kerneldc.ipm.rest.csv.service.transformer.bean;

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
import com.kerneldc.ipm.rest.csv.service.transformer.FileProcessingContext;
import com.kerneldc.ipm.rest.csv.service.transformer.TransformationStageEnum;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.AbortFileProcessingException;
import com.kerneldc.ipm.rest.csv.service.transformer.exception.BeanTransformerException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HoldingBeanTransformerStage1 implements IBeanTransformer {
	private final InstrumentRepository instrumentRepository;
	private final PortfolioRepository portfolioRepository;
	@Override
	public void transform(FileProcessingContext context) throws AbortFileProcessingException {
		
		var inputHoldingList = context.getBeans();
		
		List<AbstractPersistableEntity> transformedHoldingList = new ArrayList<>();
		
		for (AbstractPersistableEntity bean : inputHoldingList) {
			var holding = SerializationUtils.clone((Holding) bean);

			var exceptionMessageJoiner = new StringJoiner(". ", StringUtils.EMPTY, ".");
			
			Consumer<AbstractPersistableEntity> setInstrument = instrument -> holding.setInstrument((Instrument)instrument);
			var instrumentNotFound = IBeanTransformer.lookupAndSetForeignEntity(instrumentRepository,
					"Instrument not found with ticker: [%s] and exchange: [%s]", exceptionMessageJoiner,
					setInstrument, holding.getTicker(), holding.getExchange());

			Consumer<AbstractPersistableEntity> setPortfolio = portfolio -> holding.setPortfolio((Portfolio)portfolio);
			var portfolioNotFound = IBeanTransformer.lookupAndSetForeignEntity(portfolioRepository,
					"Portfolio not found with instititution: [%s] and account number: [%s]", exceptionMessageJoiner,
					setPortfolio, holding.getFinancialInstitution().getInstitutionNumber(), holding.getAccountNumber());

			if (instrumentNotFound || portfolioNotFound) {
				context.getBeanTransformerExceptionList().add(new BeanTransformerException(getTransformerName(), holding, exceptionMessageJoiner.toString()));
			} else {
				transformedHoldingList.add(holding);
			}
		}
		context.setBeans(transformedHoldingList);
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
