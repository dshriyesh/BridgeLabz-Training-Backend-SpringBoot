package com.fundoonotes.service;

import com.fundoonotes.dto.response.FileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    FileResponse upload(MultipartFile file, Long noteId);
    List<FileResponse> listByNote(Long noteId);
    Resource get(Long id);
    void delete(Long id);
}
