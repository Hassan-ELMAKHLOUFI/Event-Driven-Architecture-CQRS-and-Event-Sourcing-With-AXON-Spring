package ma.hassan.accountserviceaxon.query.repository;

import ma.hassan.accountserviceaxon.query.entitie.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.cdi.JpaRepositoryExtension;

public interface AccountRepository extends JpaRepository<Account,String> {
}
