# Architecture

# Partie Ecriture

# Commandq
```java

package ma.hassan.accountserviceaxon.commonapi.commands;

import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class BaseCommand<T> {

    @Getter @TargetAggregateIdentifier
    private T id;

    public BaseCommand(T id) {
        this.id = id;
    }
}

```
# Create Account Command
```java
package ma.hassan.accountserviceaxon.commonapi.commands;

import lombok.Getter;

public class CreateAccountCommand extends  BaseCommand<String>{
    @Getter
    private  String currency ;
    @Getter private  double initialSolde;
    public CreateAccountCommand(String id, String currency, double initialSolde) {
        super(id);
        this.currency = currency;
        this.initialSolde = initialSolde;
    }
}

```

# Credit Account Commmand

```java
package ma.hassan.accountserviceaxon.commonapi.commands;

import lombok.Getter;

public class CreditAccountCommand extends  BaseCommand<String>{
    @Getter
    private  String currency ;
    @Getter private  double amount;
    public CreditAccountCommand(String id, String currency, double amount) {
        super(id);
        this.currency = currency;
        this.amount = amount;
    }
}

```

# Debit Account Command
```java
package ma.hassan.accountserviceaxon.commonapi.commands;

import lombok.Getter;

public class DebitAccountCommand extends  BaseCommand<String>{
    @Getter
    private  String currency ;
    @Getter private  double amount;
    public DebitAccountCommand(String id, String currency, double amount) {
        super(id);
        this.currency = currency;
        this.amount = amount;
    }
}

```

# Account Command Controller 
```java
package ma.hassan.accountserviceaxon.Commands.controllers;

import ma.hassan.accountserviceaxon.commonapi.commands.DebitAccountCommand;
import ma.hassan.accountserviceaxon.commonapi.dtos.CreateAccountRequestDTO;
import ma.hassan.accountserviceaxon.commonapi.commands.CreateAccountCommand;
import ma.hassan.accountserviceaxon.commonapi.dtos.CreditAccountRequestDTO;
import ma.hassan.accountserviceaxon.commonapi.dtos.DebitAccountRequestDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@RestController
@RequestMapping("/commands/account")
public class AccountCommandController {


    private CommandGateway commandGateway ;
private EventStore eventStore ;
    public AccountCommandController(CommandGateway commandGateway, EventStore eventStore) {
        this.commandGateway = commandGateway;
        this.eventStore = eventStore;
    }

    @PostMapping("/create")
    public CompletableFuture<String> createNewAccount(@RequestBody CreateAccountRequestDTO request){
          return   commandGateway.send(new CreateAccountCommand(
                    UUID.randomUUID().toString(),
                    request.getCurrency(),
                    request.getInitialBalance()
            ));
    }

    @PostMapping("/debit")
    public CompletableFuture<String> DebitAccount(@RequestBody DebitAccountRequestDTO request){
        return   commandGateway.send(new DebitAccountCommand(
                request.getAccountId(),
                request.getCurrency(),
                request.getAmount()
        ));
    }


    @PostMapping("/credit")
    public CompletableFuture<String> DebitAccount(@RequestBody CreditAccountRequestDTO request){
        return   commandGateway.send(new DebitAccountCommand(
                request.getAccountId(),
                request.getCurrency(),
                request.getAmount()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exeptionHandler(Exception exception){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/eventStore/{id}")
    public Stream eventStore (@PathVariable String id){
        return eventStore.readEvents(id).asStream() ;
    }

}

```

# Account Aggregate
```java
package ma.hassan.accountserviceaxon.Commands.controllers.aggregates;


import ma.hassan.accountserviceaxon.commonapi.commands.CreditAccountCommand;
import ma.hassan.accountserviceaxon.commonapi.commands.DebitAccountCommand;
import ma.hassan.accountserviceaxon.commonapi.enums.AccountStatus;
import ma.hassan.accountserviceaxon.commonapi.commands.CreateAccountCommand;
import ma.hassan.accountserviceaxon.commonapi.exceptions.NegativeInitialBalanceException;
import ma.hassan.accountserviceaxon.event.AccountCreatedEvent;
import ma.hassan.accountserviceaxon.event.AccountCreditedEvent;
import ma.hassan.accountserviceaxon.event.AccountDebitedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class AccountAggregate {

    @AggregateIdentifier
    private String accoutId;
    private String currency;
    private double balance ;
    private AccountStatus status;

    //private List<Over>
    public  AccountAggregate(){}
    @CommandHandler
    public  AccountAggregate(CreateAccountCommand command) {
        if(command.getInitialSolde()<0) throw new NegativeInitialBalanceException("Negative Balance");
        AggregateLifecycle.apply(new AccountCreatedEvent(
                command.getId(),
                command.getCurrency(),
                command.getInitialSolde(),
                AccountStatus.CREATED
        ));
    }
    @EventSourcingHandler
    public void on(AccountCreatedEvent event){
        this.accoutId = event.getId();
        this.balance =event.getBalance() ;
        this.status =  event.getStatus();
        this.currency = event.getCurrency() ;
    }

    @CommandHandler
    public  void handle (CreditAccountCommand command){
        if(command.getAmount()<0) throw new NegativeInitialBalanceException("Negative Amount");
        AggregateLifecycle.apply(new AccountCreditedEvent(
                command.getId(),
                command.getCurrency(),
                command.getAmount()
        ));
    }
    @EventSourcingHandler
    public void on(AccountCreditedEvent event){
        this.balance = this.balance + event.getAmount() ;
    }

    @CommandHandler
    public  void handle (DebitAccountCommand command){
        if(command.getAmount()<0) throw new NegativeInitialBalanceException("Negative Amount");
        if(command.getAmount()>this.balance) throw new NegativeInitialBalanceException("Balance insufficient Exception");
        AggregateLifecycle.apply(new AccountDebitedEvent(
                command.getId(),
                command.getCurrency(),
                command.getAmount()
        ));
    }
    @EventSourcingHandler
    public void on(AccountDebitedEvent event){
        this.balance = this.balance - event.getAmount() ;
    }

}

```

##
# Partie Query 
```java
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
```

# Account Entity
```java
package ma.hassan.accountserviceaxon.query.entitie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.hassan.accountserviceaxon.commonapi.enums.AccountStatus;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@Entity

@AllArgsConstructor
public class Account {

    @Id
    private String id;
    private Instant createdAt;
    private  double balance ;
    private AccountStatus status ;
    private String currency ;

    @OneToMany(mappedBy = "account")
    private List<AccountTransaction> transactions;
    public Account() {
    }
}

```

# Account Transaction Entity
```java
package ma.hassan.accountserviceaxon.query.entitie;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ma.hassan.accountserviceaxon.commonapi.enums.TransactionType;

import javax.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date timestamp;
    private double amount;
    @Enumerated(EnumType.STRING)
    private TransactionType type ;
    @ManyToOne
    private Account account;
}

```
# Account Query Handler
```java
package ma.hassan.accountserviceaxon.query.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hassan.accountserviceaxon.event.AccountCreatedEvent;
import ma.hassan.accountserviceaxon.event.AccountCreditedEvent;
import ma.hassan.accountserviceaxon.query.entitie.Account;
import ma.hassan.accountserviceaxon.query.queries.GetAllAccounts;
import ma.hassan.accountserviceaxon.query.repository.AccountRepository;
import ma.hassan.accountserviceaxon.query.repository.TransactionRepository;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class AccountEventHandlerService {
    private AccountRepository accountRepository ;
    private TransactionRepository transactionRepository ;


    @EventHandler
    public void on(AccountCreatedEvent event, EventMessage<AccountCreatedEvent> eventEventMessage){
        log.info("Account Repository Received");
        Account account = new Account();
        account.setId(event.getId());
        account.setBalance(event.getBalance());
        account.setStatus(event.getStatus());
        account.setCreatedAt(eventEventMessage.getTimestamp());
        accountRepository.save(account);
    }
    @QueryHandler
    public List<Account> on(GetAllAccounts getAllAccounts){
        return accountRepository.findAll() ;
    }
}

```