package com.djnd.cinema_java_spring.web.rest;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.djnd.cinema_java_spring.domain.enumeration.RoomStatus;
import com.djnd.cinema_java_spring.domain.enumeration.RoomType;
import com.djnd.cinema_java_spring.repository.RoomRepository;
import com.djnd.cinema_java_spring.security.AuthoritiesConstants;
import com.djnd.cinema_java_spring.service.RoomService;
import com.djnd.cinema_java_spring.service.dto.ResultPaginationDTO;
import com.djnd.cinema_java_spring.service.dto.RoomDTO;
import com.djnd.cinema_java_spring.service.dto.RoomDetailDTO;
import com.djnd.cinema_java_spring.util.annotation.ApiMessage;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/admin/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomResource {
    final RoomService roomService;
    final RoomRepository roomRepository;

    private void isValidData(RoomDTO roomDTO) {
        try {
            RoomStatus.valueOf(roomDTO.getStatus());
        } catch (Exception ex) {
            throw new RequestInvalidException("Room status invalid!");
        }
        try {
            RoomType.valueOf(roomDTO.getType());
        } catch (Exception ex) {
            throw new RequestInvalidException("Room type invalid!");
        }
    }

    @PostMapping
    @ApiMessage("Create new room")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<RoomDetailDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        if (roomDTO.getId() != null) {
            throw new RequestInvalidException("A new room cannot already have an ID!");
        }
        if (roomRepository.roomNameIsExist(roomDTO.getName())) {
            throw new RequestInvalidException("Room name already exist!");
        }
        isValidData(roomDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(roomDTO));

    }

    @GetMapping
    @ApiMessage("Get room with pagination")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<ResultPaginationDTO> getAllRoom(Pageable pageable,
            @RequestParam(name = "q", required = true) String q) {
        return ResponseEntity.ok(roomService.fetchAllWithPagination(q, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get room seating chart")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.MANAGER + "')")
    public ResponseEntity<RoomDetailDTO> fetchRoomById(@Positive @PathVariable("id") Integer roomId) {
        if (roomId == null)
            throw new RequestInvalidException("Missing room ID!");
        return ResponseEntity.ok(roomService.fetchById(roomId));
    }

}
