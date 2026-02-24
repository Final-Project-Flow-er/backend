package com.chaing.domain.returns.repository.impl;

import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;
import com.chaing.domain.returns.dto.response.QFranchiseReturnAndReturnItemResponse;
import com.chaing.domain.returns.repository.interfaces.FranchiseReturnRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.chaing.domain.returns.entity.QReturnItem.returnItem;
import static com.chaing.domain.returns.entity.QReturns.returns;

@Repository
@RequiredArgsConstructor
public class FranchiseReturnRepositoryImpl implements FranchiseReturnRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FranchiseReturnAndReturnItemResponse> searchAllReturns(Long franchiseId) {
        return queryFactory
                .select(new QFranchiseReturnAndReturnItemResponse(
                        returns.returnCode,
                        returns.returnStatus,
                        returns.franchiseOrderId,
                        returnItem.quantity,
                        returns.returnType,
                        returns.createdAt,
                        returnItem.franchiseOrderItemId
                ))
                .from(returnItem)
                .join(returnItem.returns, returns)
                .where(
                        returns.franchiseId.eq(franchiseId)
                )
                .fetch();
    }
}
