package com.kerneldc.springsecurityjwt.csv.repository;

import java.util.List;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.springsecurityjwt.csv.domain.AreaCode;
import com.kerneldc.springsecurityjwt.csv.domain.UploadTableEnum;

public interface AreaCodeRepository extends BaseTableRepository<AreaCode, Long> {

	
	List<AreaCode> findByCode(String code);
	
	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.AREA_CODE;
	}
	
}
