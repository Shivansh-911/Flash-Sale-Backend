package com.shivansh.InventoryEngine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shivansh.InventoryEngine.domain.entity.OutboxEvent;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {


    // Top 50 queries , which have false flag by worker , sorted in ascending order wrt created time (oldest at top)
    List<OutboxEvent> findTop50ByProcessedFalseOrderByCreatedAtAsc();
    
}
