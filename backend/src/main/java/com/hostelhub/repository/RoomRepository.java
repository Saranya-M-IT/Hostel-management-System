package com.hostelhub.repository;

import com.hostelhub.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByStatus(Room.RoomStatus status);
    List<Room> findByBlock(String block);
}
