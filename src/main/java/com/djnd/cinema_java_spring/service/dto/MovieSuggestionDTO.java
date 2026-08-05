package com.djnd.cinema_java_spring.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieSuggestionDTO {
    private Integer id;
    private String title;
    private String genre;
}
