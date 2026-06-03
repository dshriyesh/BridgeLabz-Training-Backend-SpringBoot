package com.fundoonotes.controller;

import com.fundoonotes.dto.response.ApiResponse;
import com.fundoonotes.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Object> upload(@RequestPart("file") MultipartFile file, @RequestParam("noteId") Long noteId) {
        return ok("File uploaded", fileService.upload(file, noteId));
    }

    @GetMapping("/note/{noteId}")
    public ApiResponse<Object> listByNote(@PathVariable Long noteId) {
        return ok("Files fetched", fileService.listByNote(noteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> get(@PathVariable Long id) {
        Resource resource = fileService.get(id);
        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(Path.of(resource.getFile().getAbsolutePath()));
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        fileService.delete(id);
        return ok("File deleted", null);
    }

    private ApiResponse<Object> ok(String message, Object data) {
        return ApiResponse.builder().success(true).message(message).data(data).timestamp(LocalDateTime.now()).build();
    }
}
