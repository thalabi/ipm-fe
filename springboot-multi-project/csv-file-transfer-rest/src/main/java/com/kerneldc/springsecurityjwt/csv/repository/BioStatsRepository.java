package com.kerneldc.springsecurityjwt.csv.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.springsecurityjwt.csv.domain.BioStats;
import com.kerneldc.springsecurityjwt.csv.domain.UploadTableEnum;

public interface BioStatsRepository extends BaseTableRepository<BioStats, Long> {

	@Override
	default IEntityEnum canHandle() {
		return UploadTableEnum.BIO_STATS;
	}
}
