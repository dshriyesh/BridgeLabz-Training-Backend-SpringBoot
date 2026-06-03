package com.fundoonotes.service.impl;

import com.fundoonotes.dto.response.FileResponse;
import com.fundoonotes.entity.Attachment;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.NoteNotFoundException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.exception.ValidationException;
import com.fundoonotes.repository.AttachmentRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "png", "pdf");

    private final AttachmentRepository attachmentRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Value("${file.storage.path:uploads}")
    private String storagePath;

    @Override
    @Transactional
    public FileResponse upload(MultipartFile file, Long noteId) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is required");
        }

        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ValidationException("Unsupported file type. Allowed: jpg, png, pdf");
        }

        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));
        String generatedName = buildStoredName(extension);
        Path uploadDir = Paths.get(storagePath).toAbsolutePath().normalize();
        Path targetPath = uploadDir.resolve(generatedName);
        if (!targetPath.normalize().startsWith(uploadDir)) {
            throw new ValidationException("Invalid target file path");
        }

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ValidationException("Failed to store file: " + ex.getMessage());
        }

        Attachment saved = attachmentRepository.save(Attachment.builder()
                .fileName(originalName)
                .fileType(extension.toLowerCase())
                .filePath(targetPath.toString())
                .note(note)
                .user(user)
                .build());

        log.info("File uploaded by {}: {}", user.getUsername(), saved.getId());
        return FileResponse.builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .fileType(saved.getFileType())
                .noteId(saved.getNote().getId())
                .build();
    }

    @Override
    public List<FileResponse> listByNote(Long noteId) {
        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));
        return attachmentRepository.findByNoteAndUser(note, user).stream()
                .map(attachment -> FileResponse.builder()
                        .id(attachment.getId())
                        .fileName(attachment.getFileName())
                        .fileType(attachment.getFileType())
                        .noteId(noteId)
                        .build())
                .toList();
    }

    @Override
    public Resource get(Long id) {
        User user = getCurrentUser();
        Attachment attachment = attachmentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ValidationException("File not found for current user"));
        Path path = Paths.get(attachment.getFilePath()).toAbsolutePath().normalize();
        if (!path.startsWith(Paths.get(storagePath).toAbsolutePath().normalize())) {
            throw new ValidationException("Invalid file path");
        }
        Resource resource = new PathResource(path);
        if (!resource.exists()) {
            throw new ValidationException("File does not exist on server");
        }
        return resource;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getCurrentUser();
        Attachment attachment = attachmentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ValidationException("File not found for current user"));
        try {
            Files.deleteIfExists(Paths.get(attachment.getFilePath()).toAbsolutePath().normalize());
        } catch (IOException ex) {
            throw new ValidationException("Failed to delete file from storage: " + ex.getMessage());
        }
        attachmentRepository.delete(attachment);
        log.info("File deleted by {}: {}", user.getUsername(), id);
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new ValidationException("Invalid file name");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private String buildStoredName(String extension) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "file_" + ts + "_" + java.util.UUID.randomUUID() + "." + extension.toLowerCase();
    }

    private User getCurrentUser() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }
}
