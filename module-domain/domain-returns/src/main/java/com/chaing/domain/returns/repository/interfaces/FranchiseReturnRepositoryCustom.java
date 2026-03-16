package com.chaing.domain.returns.repository.interfaces;

import com.chaing.domain.returns.dto.response.FranchiseReturnItemProjection;
import com.chaing.domain.returns.dto.response.HQReturnItemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FranchiseReturnRepositoryCustom {
    Page<FranchiseReturnItemProjection> findReturnItemPage(Long franchiseId, Pageable pageable);

    Page<HQReturnItemProjection> findHQReturnPage(boolean isAll, Pageable pageable);
}
