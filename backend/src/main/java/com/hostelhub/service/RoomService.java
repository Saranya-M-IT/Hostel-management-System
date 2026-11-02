package com.hostelhub.service;

import com.hostelhub.dto.AllocationRequest;
import com.hostelhub.dto.RoomCreateRequest;
import com.hostelhub.dto.RoomDTO;
import com.hostelhub.entity.Room;
import com.hostelhub.entity.Student;
import com.hostelhub.exception.BadRequestException;
import com.hostelhub.exception.ResourceNotFoundException;
import com.hostelhub.repository.RoomRepository;
import com.hostelhub.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private StudentRepository studentRepository;

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
        return mapToDTO(room);
    }

    @Transactional
    public RoomDTO createRoom(RoomCreateRequest request) {
        // ID format: Block A -> A-101
        String blockCode = request.getBlock().contains(" ") ? request.getBlock().split(" ")[1] : request.getBlock();
        String roomId = String.format("%s-%s", blockCode, request.getNumber());

        if (roomRepository.existsById(roomId)) {
            throw new BadRequestException("Room already exists with ID: " + roomId);
        }

        Room room = new Room(
                roomId,
                request.getBlock(),
                request.getNumber(),
                request.getFloor(),
                request.getType(),
                request.getCapacity()
        );

        room = roomRepository.save(room);
        return mapToDTO(room);
    }

    @Transactional
    public RoomDTO updateRoom(String id, RoomCreateRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));

        if (request.getBlock() != null) room.setBlock(request.getBlock());
        if (request.getNumber() != null) room.setRoomNumber(request.getNumber());
        if (request.getFloor() != null) room.setFloor(request.getFloor());
        if (request.getType() != null) room.setRoomType(request.getType());
        if (request.getCapacity() != null) {
            if (request.getCapacity() < room.getCurrentOccupancy()) {
                throw new BadRequestException("New capacity cannot be less than current occupancy (" + room.getCurrentOccupancy() + ")");
            }
            room.setCapacity(request.getCapacity());
        }

        room = roomRepository.save(room);
        return mapToDTO(room);
    }

    @Transactional
    public void deleteRoom(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));

        List<Student> occupants = studentRepository.findByAssignedRoomId(id);
        if (!occupants.isEmpty()) {
            throw new BadRequestException("Cannot delete room with active occupants! Please vacate students first.");
        }

        roomRepository.delete(room);
    }

    public List<RoomDTO> getVacantRooms() {
        return roomRepository.findAll().stream()
                .filter(r -> r.getStatus() != Room.RoomStatus.FULL && r.getCurrentOccupancy() < r.getCapacity())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDTO allocateRoom(AllocationRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.getRoomId()));

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        if (room.getStatus() == Room.RoomStatus.FULL || room.getCurrentOccupancy() >= room.getCapacity()) {
            throw new BadRequestException("Room is already full! Cannot allocate student to full room.");
        }

        // Vacate previous room if any
        if (student.getAssignedRoom() != null && !student.getAssignedRoom().getId().equals(room.getId())) {
            Room oldRoom = student.getAssignedRoom();
            List<Student> oldOccupants = studentRepository.findByAssignedRoomId(oldRoom.getId());
            oldRoom.setCurrentOccupancy(Math.max(0, oldOccupants.size() - 1));
            oldRoom.updateStatusBasedOnOccupancy();
            roomRepository.save(oldRoom);
        }

        student.setAssignedRoom(room);
        student.setAllocationStatus("Allocated");
        studentRepository.save(student);

        List<Student> newOccupants = studentRepository.findByAssignedRoomId(room.getId());
        room.setCurrentOccupancy(newOccupants.size());
        room.updateStatusBasedOnOccupancy();
        room = roomRepository.save(room);

        return mapToDTO(room);
    }

    public RoomDTO mapToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setBlock(room.getBlock());
        dto.setNumber(room.getRoomNumber());
        dto.setFloor(room.getFloor());
        dto.setType(room.getRoomType());
        dto.setCapacity(room.getCapacity());
        dto.setCurrentOccupancy(room.getCurrentOccupancy());

        // Map status for frontend: "booked" if full or occupied, "remaining" if vacant/partially occupied
        boolean isFull = (room.getStatus() == Room.RoomStatus.FULL || room.getStatus() == Room.RoomStatus.BOOKED || room.getCurrentOccupancy() >= room.getCapacity());
        dto.setStatus(isFull ? "booked" : "remaining");

        List<Student> occupants = studentRepository.findByAssignedRoomId(room.getId());
        dto.setOccupants(occupants.stream().map(Student::getId).collect(Collectors.toList()));

        return dto;
    }
}
