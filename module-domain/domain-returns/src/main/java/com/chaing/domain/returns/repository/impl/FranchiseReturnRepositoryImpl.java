package com.chaing.domain.returns.repository.impl;

import com.chaing.domain.returns.dto.command.ReturnCommand;
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
    public List<ReturnCommand> searchAllReturns(Long franchiseId) {
        return queryFactory
                .select(new QReturnCommand(
                        returns.returnId,
                        returns.franchiseOrderId,
                        returns.returnCode,
                        returns.userid,
                        returns.returnType,
                        returns.description,
                        returns.totalReturnQuantity,
                        returns.totalReturnAmount,
                        returns.returnStatus,
                        returns.createdAt
                ))
                .from(returnItem)
                .join(returnItem.returns, returns)
                .where(
                        returns.franchiseId.eq(franchiseId),
                        returns.deletedAt.isNull()
                )
                .fetch();
    }
}
