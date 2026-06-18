package com.djnd.cinema_java_spring.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
                    if (room != null) {
                        if (isOccupied) {
                            errorMessages.add(
                                    String.format("Room %s overlapping schedules at %s %s", room.getName(), date,
                                            time));
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
        }
        if (!errorMessages.isEmpty()) {
            throw new RequestInvalidException(String.join("\n", errorMessages));
        }
        showtimeRepository.saveAll(showTimesToSave);
    }

    public void updateComplexShowtimes(ComplexShowtimeRequestDTO dto) {
        Movie movie = movieRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found!"));
        List<Integer> targetRoomIds = dto.getRooms().stream().map(roomDTO -> roomDTO.getId()).toList();
        this.deleteShowtimeExists(targetRoomIds, movie.getId());
        int durationCleanUp = movie.getDurationMinutes() + 15;
        List<LocalDate> allDates = dto.getRooms().stream()
                .flatMap(room -> room.getDays().stream().map(x -> x.getDate())).distinct().toList();
        LocalDate minDate = allDates.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = allDates.stream().max(LocalDate::compareTo).orElse(LocalDate.now());

        List<Showtime> existingShowtimes = showtimeRepository
                .findConflictShowtimes(targetRoomIds, movie.getId(),
                        minDate.atStartOfDay(),
                        maxDate.plusDays(1).atStartOfDay());
        Map<Integer, List<Showtime>> showtimeByRoomMap = existingShowtimes.stream()
                .collect(Collectors.groupingBy(showtime -> showtime.getRoom().getId()));
        var realRoomsMap = roomRepository.findByIdIn(targetRoomIds).stream()
                .collect(Collectors.toMap(Room::getId, room -> room));
        List<Showtime> showtimesToSave = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        for (var roomDTO : dto.getRooms()) {
            List<Showtime> roomCoflicts = showtimeByRoomMap.getOrDefault(roomDTO.getId(), List.of());
            for (var dayDTO : roomDTO.getDays()) {
                for (var time : dayDTO.getStartTimes()) {
                    LocalDateTime newStartTime = LocalDateTime.of(dayDTO.getDate(), time);
                    LocalDateTime newEndTime = newStartTime.plusMinutes(durationCleanUp);
                    boolean isOccupied = roomCoflicts.stream()
                            .anyMatch(showtime -> newStartTime.isBefore(showtime.getEndDateTime())
                                    && newEndTime.isAfter(showtime.getStartDateTime()));
                    Room room = realRoomsMap.get(roomDTO.getId());
                    if (room != null) {
                        if (isOccupied) {
                            errorMessages.add(
                                    String.format("Room %s overlapping schedules at %s %s", room.getName(),
                                            dayDTO.getDate(),
                                            time));
                        } else {
                            Showtime showtime = new Showtime();
                            showtime.setMovie(movie);
                            showtime.setRoom(room);
                            showtime.setStartDateTime(newStartTime);
                            showtime.setEndDateTime(newEndTime);
                            showtimesToSave.add(showtime);
                        }
                    }

                }
            }

        }
        if (!errorMessages.isEmpty()) {
            throw new RequestInvalidException(String.join("\n", errorMessages));
        }
        showtimeRepository.saveAll(showtimesToSave);
    }

    public void deleteShowtimeExists(List<Integer> targetRoomIds, Integer movieId) {
        showtimeRepository.deleteByMovieIdAndRoomIdIn(movieId, targetRoomIds);
        showtimeRepository.flush();
    }

    public ComplexShowtimeRequestDTO toComplexShowtimeRequestDTO(Movie movie) {
        var res = new ComplexShowtimeRequestDTO();
        res.setDescription(movie.getDescription());
        res.setDirector(movie.getDirector());
        res.setDurationMinutes(movie.getDurationMinutes());
        res.setGenre(movie.getGenre().toString());
        res.setId(movie.getId());
        res.setPosterUrl(movie.getPosterUrl());
        res.setReleaseDate(movie.getReleaseDate());
        res.setStatus(movie.getStatus().toString());
        res.setTitle(movie.getTitle());
        var showtimes = showtimeRepository.findByMovieId(movie.getId());
        var treeRoomsAndShowtimes = showtimes.stream()
                .collect(Collectors.groupingBy(Showtime::getRoom,
                        Collectors.groupingBy(showtime -> showtime.getStartDateTime().toLocalDate(), Collectors
                                .mapping(showtime -> showtime.getStartDateTime().toLocalTime(), Collectors.toList()))));
        var roomSchedules = treeRoomsAndShowtimes.entrySet().stream().map(roomEntry -> {
            Room room = roomEntry.getKey();
            var daySchedules = roomEntry.getValue().entrySet().stream().map(dayEntry -> {
                var dayDTO = new ComplexShowtimeRequestDTO.DayScheduleDTO();
                dayDTO.setDate(dayEntry.getKey());
                dayDTO.setStartTimes(dayEntry.getValue());
                return dayDTO;
            })
                    .sorted(Comparator.comparing(ComplexShowtimeRequestDTO.DayScheduleDTO::getDate))
                    .toList();

            var roomDTO = new ComplexShowtimeRequestDTO.RoomScheduleDTO();
            roomDTO.setId(room.getId());
            roomDTO.setDays(daySchedules);
            roomDTO.setName(room.getName());
            return roomDTO;

        })
                .toList();
        res.setRooms(roomSchedules);
        return res;
    }
}
