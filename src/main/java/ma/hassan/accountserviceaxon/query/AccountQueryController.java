package ma.hassan.accountserviceaxon.query;


import lombok.AllArgsConstructor;
import ma.hassan.accountserviceaxon.query.entitie.Account;
import ma.hassan.accountserviceaxon.query.queries.GetAllAccounts;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/query/account")
@AllArgsConstructor
public class AccountQueryController {

    public QueryGateway queryGateway ;
    public List<Account> accountList ;

    @GetMapping ("/list")
    public CompletableFuture<List<Account>> accountList(){
        return queryGateway.query(new GetAllAccounts(), ResponseTypes.multipleInstancesOf(Account.class));
    }

}
