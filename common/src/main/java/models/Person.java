package models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;

@JsonPropertyOrder({"name", "birthday", "height"})
public record Person(String name, LocalDate birthday, Long height) {
}
