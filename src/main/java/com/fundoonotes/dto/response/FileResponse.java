package com.fundoonotes.dto.response;
import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor public class FileResponse { private Long id; private String fileName; private String fileType; private Long noteId; }
