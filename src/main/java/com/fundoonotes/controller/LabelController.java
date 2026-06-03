package com.fundoonotes.controller;

import com.fundoonotes.dto.request.LabelRequest;
import com.fundoonotes.dto.response.ApiResponse;
import com.fundoonotes.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    public ApiResponse<Object> create(@Valid @RequestBody LabelRequest request) {
        return ok("Label created", labelService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Object> update(@PathVariable Long id, @Valid @RequestBody LabelRequest request) {
        return ok("Label updated", labelService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        labelService.delete(id);
        return ok("Label deleted", null);
    }

    @GetMapping
    public ApiResponse<Object> list() {
        return ok("Labels fetched", labelService.list());
    }

    private ApiResponse<Object> ok(String message, Object data) {
        return ApiResponse.builder().success(true).message(message).data(data).timestamp(LocalDateTime.now()).build();
    }
}
