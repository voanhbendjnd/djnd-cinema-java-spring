package com.djnd.cinema_java_spring.domain.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.djnd.cinema_java_spring.domain.enumeration.MovieGenre;
import com.djnd.cinema_java_spring.domain.enumeration.MovieStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "movies")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie extends AbstractAuditingEntity<Integer> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @NotNull
    @Column(name = "title", length = 200, nullable = false)
    String title;
    @Column(columnDefinition = "MEDIUMTEXT")
    String description;
    @NotNull
    @Column(name = "duration_minutes", nullable = false)
    Integer durationMinutes;
    @Enumerated(EnumType.STRING)
    MovieGenre genre;
    @Column(name = "release_date")
    LocalDateTime releaseDate;
    @Column(name = "poster_url", length = 500)
    String posterUrl;
    @Column(name = "director", length = 100)
    String director;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    MovieStatus status = MovieStatus.UPCOMING;
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<Showtime> showtimes = new ArrayList<>();
    @Column(name = "activated", nullable = false)
    @Builder.Default
    boolean activated = true;
}
