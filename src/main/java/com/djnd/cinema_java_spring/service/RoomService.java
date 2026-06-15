package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.repository.SeatRepository;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Room;
import com.djnd.cinema_java_spring.domain.entity.Seat;
import com.djnd.cinema_java_spring.domain.enumeration.RoomStatus;
import com.djnd.cinema_java_spring.domain.enumeration.RoomType;
import com.djnd.cinema_java_spring.domain.enumeration.SeatType;
import com.djnd.cinema_java_spring.repository.RoomRepository;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.RoomDTO;
import com.djnd.cinema_java_spring.service.dto.RoomDetailDTO;
import com.djnd.cinema_java_spring.service.dto.SeatDTO;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class RoomService {
    final SeatRepository seatRepository;
    final RoomRepository roomRepository;

    public RoomDetailDTO createRoom(RoomDTO roomDTO) {
        Room room = new Room();
        room.setName(roomDTO.getName());
        room.setStatus(RoomStatus.valueOf(roomDTO.getStatus()));
        room.setType(RoomType.valueOf(roomDTO.getType()));
        room.setTotalSeats(roomDTO.getTotalCols() * roomDTO.getTotalRows());
        room = roomRepository.save(room);
        if (roomDTO.getTotalCols() != null && roomDTO.getTotalRows() != null) {
            var seats = this.getSeats(roomDTO.getTotalRows(), roomDTO.getTotalCols(), room);
            seatRepository.saveAllAndFlush(seats);
            room.setSeats(seats);
        }
        return this.toSeatingChart(room);
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
        if (room.getSeats() != null) {
            seats = room.getSeats().stream().map(x -> {
                return SeatDTO.builder()
                        .id(x.getId())
                        .seatNo(x.getSeatNo())
                        .seatRow(x.getSeatRow())
                        .type(x.getType().toString())
                        .build();
            }).toList();
        }

        return RoomDetailDTO.builder()

                .id(room.getId())
                .name(room.getName())
                .status(room.getStatus().toString())
                .type(room.getType().toString())
                .seats(seats)
                .build();
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

}
