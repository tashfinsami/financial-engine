package com.engine.financialengine.repository;

import com.engine.financialengine.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository
        extends JpaRepository<Account, Long> {
}