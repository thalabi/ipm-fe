package com.kerneldc.common.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.SunshineList;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;

public interface SunshineListRepository extends BaseTableRepository<SunshineList, Long> {

	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.SUNSHINE_LIST;
	}

}
