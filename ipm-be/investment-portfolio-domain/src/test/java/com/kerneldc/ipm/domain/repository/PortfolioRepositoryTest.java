package com.kerneldc.ipm.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.HolderEnum;
import com.kerneldc.ipm.domain.Portfolio;
import com.kerneldc.ipm.repository.PortfolioRepository;

@DataJpaTest
class PortfolioRepositoryTest {

	@Autowired
	private PortfolioRepository portfolioRepository;
	
	@Test
	void testPortfolioRepositoryIsNotNull() {
		assertThat(portfolioRepository).isNotNull();
	}

	@Test
	void testInsertPortfolioRepository_lkIsComposedCorrectly() {
		var p = new Portfolio();
		var td = FinancialInstitutionEnum.TD;
		p.setFinancialInstitution(td);
		var accountNumber = "597904S";
		p.setAccountNumber(accountNumber);
		p.setCurrency(CurrencyEnum.CAD);
		p.setName("Tarif SDRSP CAD");
		p.setLogicallyDeleted(false);
		p.setHolder(HolderEnum.TARIF);
		portfolioRepository.saveAndFlush(p);
		System.out.println("Portfolio: " + p);
		assertThat(p.getId()).isNotNull();
		assertThat(p.getLogicalKeyHolder().getLogicalKey()).isEqualTo(td.getInstitutionNumber()+"|"+accountNumber);
	}

	@Test
	void testInsertPortfolioRepository_logicallyDeletedNotSet_throwsException() {
		var p = new Portfolio();
		p.setFinancialInstitution(FinancialInstitutionEnum.TD);
		p.setAccountNumber("597904S");
		p.setCurrency(CurrencyEnum.CAD);
		p.setName("Tarif SDRSP CAD");
		
		DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> {
			portfolioRepository.saveAndFlush(p);
	    });
		System.out.println("exception message: "+exception.getMessage());
	}
}
