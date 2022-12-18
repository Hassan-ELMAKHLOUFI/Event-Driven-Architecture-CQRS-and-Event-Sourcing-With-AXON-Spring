package ma.hassan.accountserviceaxon.query.repository;

import ma.hassan.accountserviceaxon.query.entitie.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<AccountTransaction,Long> {
}
