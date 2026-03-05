package com.chaing.domain.returns.repository.interfaces;

import com.chaing.domain.returns.dto.command.ReturnCommand;

import java.util.List;

public interface FranchiseReturnRepositoryCustom {
    List<ReturnCommand> searchAllReturns(Long franchiseId);
}
