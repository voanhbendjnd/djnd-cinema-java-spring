package com.djnd.cinema_java_spring.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import com.djnd.cinema_java_spring.service.dto.MovieRoomTimeDTORequest;
import com.djnd.cinema_java_spring.service.projection.ShowtimeProjection;
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

    @Transactional(readOnly = true)
    public void checkScheduleConflictAtRoom(MovieRoomTimeDTORequest dto) {
        LocalDateTime newStartDateTime = LocalDateTime.of(dto.getDate(), dto.getTime());
        int durationClearUp = dto.getDuration() + 15;
        LocalDateTime newEndDateTime = newStartDateTime.plusMinutes(durationClearUp);
        boolean isOcuppied = showtimeRepository.isRoomOccupied(dto.getRoomId(), newStartDateTime, newEndDateTime,
                dto.getMovieId());
        if (isOcuppied) {
            throw new RequestInvalidException(
                    String.format("Start date time %s at %s is before release date time movie",
                            newStartDateTime, dto.getRoomName()));
        }

    }

    @Transactional(readOnly = true)
    public List<ShowtimeProjection> getAllScheduleRoomAndMovieTitle(Integer roomId, LocalDate date, Integer movieId) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return showtimeRepository.getAllScheduleRoomAndMovieTitle(roomId, startOfDay, endOfDay, movieId);
    }

    public void createComplexShowtimes(ComplexShowtimeRequestDTO dto) {
        Movie movie = movieRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found!"));
        if (dto.getRooms().isEmpty()) {
            movie.getShowtimes().clear();
        }
        int durationClearUp = movie.getDurationMinutes() + 15;
        var showTimesToSave = new ArrayList<Showtime>();
        var errorMessages = new ArrayList<String>();
        var roomMaps = roomRepository.findByIdIn(dto.getRooms().stream()
                .map(ComplexShowtimeRequestDTO.RoomScheduleDTO::getId).toList()).stream()
                .collect(Collectors.toMap(Room::getId, r -> r));
        for (var roomDTO : dto.getRooms()) {
            for (var dayDTO : roomDTO.getDays()) {
                LocalDate date = dayDTO.getDate();
                // check showtime conflict
                this.checkDurationAfterShowtime(dayDTO.getStartTimes(), durationClearUp, date, roomDTO.getId());
                for (var time : dayDTO.getStartTimes()) {
                    LocalDateTime startTime = LocalDateTime.of(date, time);
                    LocalDateTime endTime = startTime.plusMinutes(durationClearUp);
                    if (startTime.isBefore(dto.getReleaseDate())) {
                        errorMessages.add(String.format("Start date time %s at %s is before release date time movie",
                                startTime, roomDTO.getName()));
                        continue;
                    }
                    boolean isOccupied = showtimeRepository.isRoomOccupied(roomDTO.getId(), startTime, endTime,
                            movie.getId());
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

    @Transactional(readOnly = true)
    public void checkDurationAfterShowtime(List<LocalTime> times, int duration, LocalDate date, Integer roomId) {
        record TimeBlock(LocalDateTime start, LocalDateTime end) {
        }
        List<TimeBlock> allBlocks = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        int durationClearUp = duration + 15;
        for (LocalTime time : times) {
            LocalDateTime startNew = LocalDateTime.of(date, time);
            allBlocks.add(new TimeBlock(startNew, startNew.plusMinutes(durationClearUp)));
        }
        LocalDateTime startDate = date.minusDays(1).atStartOfDay();
        LocalDateTime endDate = date.plusDays(2).atStartOfDay();
        List<Showtime> currentShowtimes = showtimeRepository.getAllShowtimesMovie(roomId, startDate, endDate);
        for (Showtime showtime : currentShowtimes) {
            // get showtime end date at current date
            if (showtime.getEndDateTime().isAfter(date.atStartOfDay()) &&
                    showtime.getStartDateTime().isBefore(date.plusDays(1).atStartOfDay()))
                allBlocks.add(
                        new TimeBlock(showtime.getStartDateTime(), showtime.getEndDateTime()));
        }
        allBlocks.sort(Comparator.comparing(TimeBlock::start));

        for (int i = 0; i < allBlocks.size() - 1; i++) {
            TimeBlock current = allBlocks.get(i);
            TimeBlock next = allBlocks.get(i + 1);
            if (next.start().isBefore(current.end())) {
                errorMessages.add(String.format(
                        "Schedule conflict at date [%s]: Showtime [%s] (ending at %s) overlaps with the next showtime [%s]",
                        date, current.start(), current.end(), next.start()));
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new RequestInvalidException(String.join("\n", errorMessages));
        }
    }

    public void updateComplexShowtimes(ComplexShowtimeRequestDTO dto) {

        Movie movie = movieRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found!"));
        if (dto.getRooms().isEmpty()) {
            movie.getShowtimes().clear();
        }
        List<Integer> targetRoomIds = dto.getRooms().stream().map(roomDTO -> roomDTO.getId()).toList();

        this.deleteShowtimeExists(targetRoomIds, movie.getId());
        int durationClearUp = movie.getDurationMinutes() + 15;
        List<LocalDate> allDates = dto.getRooms().stream()
                .flatMap(room -> room.getDays().stream().map(x -> x.getDate())).distinct().toList();
        LocalDate minDate = allDates.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = allDates.stream().max(LocalDate::compareTo).orElse(LocalDate.now());

        List<Showtime> existingShowtimes = showtimeRepository
                .findConflictShowtimes(targetRoomIds, movie.getId(),
                        minDate.minusDays(1).atStartOfDay(),
                        maxDate.plusDays(2).atStartOfDay());
        // showtime : 5 AM
        Map<Integer, List<Showtime>> showtimeByRoomMap = existingShowtimes.stream()
                .collect(Collectors.groupingBy(showtime -> showtime.getRoom().getId()));
        var realRoomsMap = roomRepository.findByIdIn(targetRoomIds).stream()
                .collect(Collectors.toMap(Room::getId, room -> room));
        List<Showtime> showtimesToSave = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        for (var roomDTO : dto.getRooms()) {
            List<Showtime> roomCoflicts = showtimeByRoomMap.getOrDefault(roomDTO.getId(), List.of());
            for (var dayDTO : roomDTO.getDays()) {
                this.checkDurationAfterShowtime(dayDTO.getStartTimes(), durationClearUp, dayDTO.getDate(),
                        roomDTO.getId());

                for (var time : dayDTO.getStartTimes()) {
                    LocalDateTime newStartTime = LocalDateTime.of(dayDTO.getDate(), time);
                    LocalDateTime newEndTime = newStartTime.plusMinutes(durationClearUp);
                    if (newStartTime.isBefore(dto.getReleaseDate())) {
                        errorMessages.add(String.format("Start date time %s at %s is before release date time movie",
                                newStartTime, roomDTO.getName()));
                        continue;
                    }
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
