package com.booking.system.repository;

import com.booking.system.entity.model.WaitingList;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitingListRepository extends CrudRepository<WaitingList,Long> {
}
