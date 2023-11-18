# pass-batch

This is a placement repository within the PT ticket management service.
It provides features such as ticket expiration, lump sum payment, notification before class, and ticket deduction after class.


## Environments
* OpenJDK 17.0.1 
* Spring Boot 2.7.17 
* Gradle 
* MySQL (Docker)
* JPA 
* lombok 
* ModelMapper

## Process
### Pass expiration
```mermaid
sequenceDiagram
    participant Batch
    participant DB
    Batch->>DB: View vouchers by user
    activate DB
    DB->>Batch: Pass response by user
    deactivate DB
    Batch->>DB: Change ticket expiration status
```

### Bulk payment of vouchers
```mermaid
sequenceDiagram
    actor user
    participant DB
    Participant Batch
    User->>DB: Bulk payment voucher registration request

    Batch->>DB: Batch payment voucher inquiry
    activate DB
    DB->>Batch: Batch payment voucher response
    deactivate DB
    Batch->>DB: Add relevant user access rights
```

### Reminder before class
```mermaid
sequenceDiagram
    Participant Batch
    participant DB
    participant Messenger
    Batch->>DB: Scheduled classes, user inquiry
    activate DB
    DB->>Batch: Scheduled class, user response
    deactivate DB
    Batch->>DB: Add notification target
    
    Batch->>DB: Look up notification target
    activate DB
    DB->>Batch: Notification target response
    deactivate DB
    Batch->>Messenger: Notification Request
    activate Messenger
    Messenger->>Batch: Notification response
    deactivate Messenger
```

### Voucher deducted after class
```mermaid
sequenceDiagram
    Participant Batch
    participant DB
    Batch->>DB: Scheduled classes, user inquiry
    activate DB
    DB->>Batch: Scheduled class, user response
    deactivate DB
    Batch->>DB: Subtraction for each user
```
