package com.relog.relog.event.controller;

import com.relog.relog.common.ApiResponse;
import com.relog.relog.event.dto.CalendarMonthResponse;
import com.relog.relog.event.dto.EventCreateRequest;
import com.relog.relog.event.dto.EventResponse;
import com.relog.relog.event.dto.EventUpdateRequest;
import com.relog.relog.event.service.EventService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody EventCreateRequest request) {
        EventResponse response = eventService.createEvent(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsByDate(
            @AuthenticationPrincipal Long memberId,
            @RequestParam LocalDate date) {
        List<EventResponse> responses = eventService.getEventsByDate(memberId, date);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long eventId) {
        EventResponse response = eventService.getEvent(memberId, eventId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<CalendarMonthResponse>> getCalendarMonth(
            @AuthenticationPrincipal Long memberId,
            @RequestParam int year,
            @RequestParam int month) {
        CalendarMonthResponse response = eventService.getCalendarMonth(memberId, year, month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long eventId,
            @RequestBody EventUpdateRequest request) {
        EventResponse response = eventService.updateEvent(memberId, eventId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long eventId) {
        eventService.deleteEvent(memberId, eventId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
