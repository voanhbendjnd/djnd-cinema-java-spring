package com.djnd.cinema_java_spring.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.djnd.cinema_java_spring.domain.entity.Movie;
import com.djnd.cinema_java_spring.domain.entity.Room;
import com.djnd.cinema_java_spring.domain.entity.Showtime;
import com.djnd.cinema_java_spring.repository.MovieRepository;
import com.djnd.cinema_java_spring.repository.RoomRepository;
import com.djnd.cinema_java_spring.repository.ShowtimeRepository;
import com.djnd.cinema_java_spring.service.dto.ComplexShowtimeRequestDTO;
import com.djnd.cinema_java_spring.web.rest.errors.RequestInvalidException;
import com.djnd.cinema_java_spring.web.rest.errors.ResourceNotFoundException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class ShowtimeService {
    final ShowtimeRepository showtimeRepository;
    final MovieRepository movieRepository;
    final RoomRepository roomRepository;

    public void createComplexShowtimes(ComplexShowtimeRequestDTO dto) {
        Movie movie = movieRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found!"));
        int durationClearUp = movie.getDurationMinutes() + 15;
        var showTimesToSave = new ArrayList<Showtime>();
        var errorMessages = new ArrayList<String>();
        var roomMaps = roomRepository.findByIdIn(dto.getRooms().stream()
                .map(ComplexShowtimeRequestDTO.RoomScheduleDTO::getId).toList()).stream()
                .collect(Collectors.toMap(Room::getId, r -> r));

        for (var roomDTO : dto.getRooms()) {
            for (var dayDTO : roomDTO.getDays()) {
                LocalDate date = dayDTO.getDate();
                for (var time : dayDTO.getStartTimes()) {
                    LocalDateTime startTime = LocalDateTime.of(date, time);
                    LocalDateTime endTime = startTime.plusMinutes(durationClearUp);
                    boolean isOccupied = showtimeRepository.isRoomOccupied(roomDTO.getId(), startTime, endTime);
                    Room room = roomMaps.get(roomDTO.getId());
                    if (isOccupied) {
                        errorMessages.add(
                                String.format("Room %s overlapping schedules at %s %s", room.getName(), date, time));
                    } else {
                        Showtime showtime = new Showtime();
                        showtime.setRoom(room);
                        showtime.setMovie(movie);
                        showtime.setStartDateTime(startTime);
                        showtime.setEndDateTime(endTime);
                        showTimesToSave.add(showtime);
                    }
                }
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new RequestInvalidException(errorMessages.stream().collect(Collectors.joining("[", "\n", "]")));
        }
        showtimeRepository.saveAll(showTimesToSave);
    }
}
