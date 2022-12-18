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
