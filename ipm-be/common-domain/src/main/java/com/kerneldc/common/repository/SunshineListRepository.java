package com.kerneldc.common.repository;

import java.math.BigDecimal;
import java.util.List;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.SunshineList;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;

public interface SunshineListRepository extends BaseTableRepository<SunshineList, Long> {

//	List<SunshineList> findByCalendarYear(Short calendarYear);
	List<SunshineList> findBySalary(BigDecimal salary);
	List<SunshineList> findByBenefits(BigDecimal benefits);
	
	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.SUNSHINE_LIST;
	}

}
