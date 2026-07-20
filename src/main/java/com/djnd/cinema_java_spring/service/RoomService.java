package com.djnd.cinema_java_spring.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.djnd.cinema_java_spring.domain.entity.SeatMaintenance;
import com.djnd.cinema_java_spring.domain.enumeration.SeatStatus;
import com.djnd.cinema_java_spring.repository.SeatMaintenanceRepository;
import com.djnd.cinema_java_spring.web.rest.errors.OperationCannotPerformedException;
import jdk.jshell.Snippet;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Room;
import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.domain.enumeration.RoomStatus;
import com.djnd.cinema_java_spring.domain.enumeration.RoomType;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;
import com.djnd.cinema_java_spring.repository.RoomRepository;
import com.djnd.cinema_java_spring.repository.ShowtimeRepository;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.RoomDTO;
import com.djnd.cinema_java_spring.service.dto.RoomDetailDTO;
import com.djnd.cinema_java_spring.service.dto.SeatDTO;
import com.djnd.cinema_java_spring.service.projection.RoomNameProjection;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class RoomService {
    final RoomRepository roomRepository;
    final ShowtimeRepository showtimeRepository;
    final SeatMaintenanceRepository seatMaintenanceRepository;
    public RoomDetailDTO createRoom(RoomDetailDTO roomDTO) {
        Room room = new Room();
        room.setName(roomDTO.getName());
        room.setStatus(RoomStatus.valueOf(roomDTO.getStatus()));
        room.setType(RoomType.valueOf(roomDTO.getType()));
        room.setTotalSeats(roomDTO.getTotalCols() * roomDTO.getTotalRows());
        if (roomDTO.getTotalCols() != null && roomDTO.getTotalRows() != null) {
            List<Seat> seats = this.generateSeats(roomDTO, room);
            room.getSeats().addAll(seats);
        }
        room = roomRepository.save(room);
        return this.toSeatingChart(room);
    }

    public RoomDetailDTO updateRoom(RoomDetailDTO roomDetailDTO) {
        Room room = roomRepository.findWithDetail(roomDetailDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found!"));
        if(showtimeRepository.existByRoomId(room.getId())){
            throw new OperationCannotPerformedException("Cannot change seat type with room already have been showtime!");
        }
        room.setName(roomDetailDTO.getName());
        room.setStatus(RoomStatus.valueOf(roomDetailDTO.getStatus()));
        room.setType(RoomType.valueOf(roomDetailDTO.getType()));
        room.setTotalSeats(roomDetailDTO.getTotalCols() * roomDetailDTO.getTotalRows());
        if (roomDetailDTO.getTotalCols() != null && roomDetailDTO.getTotalRows() != null) {
            // start check logic booking ticket
            // end check
            room.getSeats().clear();
            List<Seat> newSeats = this.generateSeats(roomDetailDTO, room);
            room.getSeats().addAll(newSeats);
        }
        return this.toSeatingChart(room);

    }

    private List<Seat> generateSeats(RoomDetailDTO roomDetailDTO, Room room) {
        List<Seat> seatsToSave = new ArrayList<>();
        if (roomDetailDTO.getSeats() == null || roomDetailDTO.getSeats().isEmpty()) {
            throw new RequestInvalidException("Not found any seat at this room!");
        }
        Map<String, SeatType> seatTypeMap = roomDetailDTO.getSeats().stream()
                .collect(Collectors.toMap(seat -> seat.getSeatRow() + '-' + seat.getSeatNo(),
                        seat -> SeatType.valueOf(seat.getType())));
        // position seat - status seat
        Map<String, SeatStatus> seatStatusMap = roomDetailDTO.getSeats().stream().collect(Collectors.toMap(seat -> seat.getSeatRow() + '-' + seat.getSeatNo(),seat -> SeatStatus.valueOf(seat.getStatus())));
        for (char row = 'A'; row < 'A' + roomDetailDTO.getTotalRows(); row++) {
            String rowStr = String.valueOf(row);
            for (int no = 1; no <= roomDetailDTO.getTotalCols(); no++) {
                Seat seat = new Seat();
                seat.setSeatRow(rowStr);
                seat.setSeatNo(no);

                seat.setRoom(room);
                String coordinateKey = rowStr + "-" + no;
                SeatType dynamicType = seatTypeMap.get(coordinateKey);
                SeatStatus dynamicStatus = seatStatusMap.get(coordinateKey);
                if(dynamicStatus!= null){
                    seat.setStatus(dynamicStatus);
                }
                if (dynamicType != null) {
                    seat.setType(dynamicType);
                } else {
                    if (row >= 'D' && row <= 'H') {
                        seat.setType(SeatType.VIP);
                    } else if (row == 'J') {
                        if (no == roomDetailDTO.getTotalRows() && no % 2 != 0) {
                            seat.setType(SeatType.STANDARD);
                        } else {
                            seat.setType(SeatType.SWEETBOX);
                        }
                    } else {
                        seat.setType(SeatType.STANDARD);
                    }
                }
                seatsToSave.add(seat);
            }
        }
        return seatsToSave;
    }

    public void deleteRoom(Integer roomId) {
        if (showtimeRepository.existsByRoomId(roomId)) {
            throw new RequestInvalidException("Cannot delete this room cause room have showtime!");
        }

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found!"));
        roomRepository.delete(room);
    }

    private List<Seat> getSeats(Integer totalRows, Integer totalCols, Room room) {
        var seatsToSave = new ArrayList<Seat>();
        for (char row = 'A'; row < 'A' + totalRows; row++) {
            for (int no = 1; no <= totalCols; no++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setSeatRow(String.valueOf(row));
                seat.setSeatNo(no);
                if (row >= 'D' && row <= 'H') {
                    seat.setType(SeatType.VIP);
                } else if (row == 'J') {
                    if (no == totalCols && no % 2 != 0) {
                        seat.setType(SeatType.STANDARD);
                    } else {
                        seat.setType(SeatType.SWEETBOX);
                    }
                } else {
                    seat.setType(SeatType.STANDARD);
                }
                seatsToSave.add(seat);
            }
        }
        return seatsToSave;
    }

    @Transactional(readOnly = true)
    public RoomDetailDTO fetchById(Integer id) {
        var room = roomRepository.findWithDetailSeatById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found!"));
        return this.toSeatingChart(room);
    }

    private RoomDetailDTO toSeatingChart(Room room) {
        List<SeatDTO> seats = null;
        // seat maintenance if exist
        List<SeatMaintenance> seatMaintenancesExisting = seatMaintenanceRepository.getSeatMaintenanceWithRoomId(room.getId());
        Map<Integer, List<SeatMaintenance>> seatMaintenancesMapBySeatId;
        if(!seatMaintenancesExisting.isEmpty()){
            seatMaintenancesMapBySeatId = seatMaintenancesExisting.stream().collect(Collectors.groupingBy(SeatMaintenance::getSeatId));
        } else {
            seatMaintenancesMapBySeatId = null;
        }
        if (room.getSeats() != null) {
            seats = room.getSeats().stream().map(x -> {
                List<SeatMaintenance> seatMaintenances = seatMaintenancesMapBySeatId.get(x.getId());
                List<SeatDTO.SeatMaintenanceDTO> seatMaintenancesDTO;
                if(seatMaintenances == null || seatMaintenances.isEmpty()){
                    seatMaintenancesDTO = new ArrayList<>();
                }
                else{
                    seatMaintenancesDTO = seatMaintenances.stream().map(seatMaintenance -> {
                        SeatDTO.SeatMaintenanceDTO dto = new SeatDTO.SeatMaintenanceDTO();
                        dto.setReason(seatMaintenance.getReason());
                        dto.setStartTime(seatMaintenance.getStartTime());
                        dto.setEndTime(seatMaintenance.getEndTime());
                        return dto;
                    }).toList();
                }

                return SeatDTO.builder()
                        .id(x.getId())
                        .seatNo(x.getSeatNo())
                        .seatRow(x.getSeatRow())
                        .status(x.getStatus().toString())
                        .type(x.getType().toString())
                        .seatMaintenances(seatMaintenancesDTO)
                        .build();
            }).toList();
        }
        var roomDetail = new RoomDetailDTO();
        roomDetail.setId(room.getId());
        roomDetail.setName(room.getName());
        roomDetail.setStatus(room.getStatus().toString());
        roomDetail.setType(room.getType().toString());
        roomDetail.setSeats(seats);
        return roomDetail;
    }

    private RoomDTO toScreenTable(Room room, Integer totalRows) {
        return RoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .status(room.getStatus().toString())
                .type(room.getType().toString())
                .totalSeats(room.getTotalSeats())
                .totalCols(room.getTotalSeats() / totalRows)
                .totalRows(totalRows)
                .build();
    }

    private RoomDTO toRoomDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .status(room.getStatus().toString())
                .type(room.getType().toString())
                .totalSeats(room.getTotalSeats())
                .build();
    }

    public ResultPaginationDTO fetchAllWithPagination(String q, Pageable pageable) {
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        var page = roomRepository.fetchAllWithPagination(q != null ? q : "", pageable);
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(this::toRoomDTO).toList());
        return res;
    }

    public List<RoomNameProjection> getAllRoomForInitMovie() {
        return roomRepository.findAllRoomAvailable(RoomStatus.ACTIVE);
    }

}
