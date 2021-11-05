package com.kerneldc.common.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.BioStats;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.enums.UploadTableEnum;

public interface BioStatsRepository extends BaseTableRepository<BioStats, Long> {

	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.BIO_STATS;
	}
}
