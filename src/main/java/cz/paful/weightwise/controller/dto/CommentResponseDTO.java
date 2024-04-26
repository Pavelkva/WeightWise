package cz.paful.weightwise.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CommentResponseDTO {

    private LocalDate date;
    private LocalTime time;
    private String comment;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
