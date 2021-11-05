package com.kerneldc.common.repository;

import java.util.List;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.AreaCode;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;

public interface AreaCodeRepository extends BaseTableRepository<AreaCode, Long> {

	
	List<AreaCode> findByCode(String code);
	
	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.AREA_CODE;
	}
	
}
