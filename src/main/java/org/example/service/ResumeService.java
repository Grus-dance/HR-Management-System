package org.example.service;

/**
 * PDF 简历服务。
 */
public interface ResumeService {

    byte[] buildPdf(Long employeeId);

    String fileName(Long employeeId);
}
