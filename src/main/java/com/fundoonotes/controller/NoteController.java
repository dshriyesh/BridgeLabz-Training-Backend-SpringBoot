package com.fundoonotes.controller;

import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.ApiResponse;
import com.fundoonotes.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @PostMapping
    public ApiResponse<Object> create(@Valid @RequestBody NoteRequest request) {
        return ok("Note created", noteService.create(request));
    }
    @PutMapping("/{id}")
    public ApiResponse<Object> update(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        return ok("Note updated", noteService.update(id, request));
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ok("Note deleted", null);
    }
    @GetMapping("/{id}")
    public ApiResponse<Object> get(@PathVariable Long id) {
        return ok("Note fetched", noteService.get(id));
    }
    @GetMapping
    public ApiResponse<Object> list(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(defaultValue = "updatedDate") String sortBy,
                                    @RequestParam(defaultValue = "desc") String direction,
                                    @RequestParam(required = false) String title,
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false) String color,
                                    @RequestParam(required = false) Boolean pinned,
                                    @RequestParam(required = false) Boolean archived,
                                    @RequestParam(required = false) Boolean trashed,
                                    @RequestParam(required = false) Long labelId,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reminderFrom,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reminderTo) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        var pageable = PageRequest.of(page, size, sort);
        boolean hasFilter = title != null || description != null || color != null || pinned != null || archived != null || trashed != null || labelId != null || fromDate != null || toDate != null || reminderFrom != null || reminderTo != null;
        return hasFilter
                ? ok("Notes fetched", noteService.search(title, description, color, pinned, archived, trashed, labelId, fromDate, toDate, reminderFrom, reminderTo, pageable))
                : ok("Notes fetched", noteService.list(pageable));
    }
    @PutMapping("/pin/{id}")
    public ApiResponse<Object> pin(@PathVariable Long id) {
        return ok("Note pin status changed", noteService.pin(id));
    }
    @PutMapping("/archive/{id}")
    public ApiResponse<Object> archive(@PathVariable Long id) {
        return ok("Note archive status changed", noteService.archive(id));
    }
    @PutMapping("/trash/{id}")
    public ApiResponse<Object> trash(@PathVariable Long id) {
        return ok("Note trash status changed", noteService.trash(id));
    }

    private ApiResponse<Object> ok(String message, Object data){
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
