package com.example.doctorchecker.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String originalName;
    private String storedFileName;
    private String uploadDate;
}