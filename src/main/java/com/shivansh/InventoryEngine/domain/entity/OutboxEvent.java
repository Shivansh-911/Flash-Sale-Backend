// Instead of calling payment directly: 
//      Inside purchase transaction we:
//      Save an event in DB (outbox table)
//      Then async wrker reads events and calls payment.
//  This guarantees:
//      no lost events
//      retry possible  
//      production safe architecture


// Transactional Outbox pattern working 
// Instead of sending a message directly to a broker, a service inserts a record into an "outbox" table within the same local database transaction 
// as the business entity. 
// A separate, asynchronous message relay service (or CDC tool) reads from this table, publishes the message, and removes or marks it as sent

package com.shivansh.InventoryEngine.domain.entity;

import java.time.Instant;
import java.util.UUID;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name="outbox_event",
    indexes = {
        @Index(name = "idx_outbox_processed_created_at", columnList = "flag, created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;
    //tells consumer which domain this table belong to 
    @Column(name = "orders")
    private String aggregateType;

    @Column(name = "order_id")
    private UUID aggregateId;
    
    @Column(name = "order_created")
    private String type;
    
    //json string of the order data at exact moment
    @Column(name = "payload")
    private String payload;
    
    //flag used by backgroung worker
    @Column(name = "flag")
    private boolean processed;

    @Column(name = "created_at")
    private Instant createdAt;
}
