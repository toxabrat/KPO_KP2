package org.example.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.dto.FileDTO;
import org.example.entity.FileEntity;
import org.example.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileRepository fileRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public FileDTO store(MultipartFile file) throws IOException {
        // Генерируем хеш файла
        String hash = DigestUtils.md5DigestAsHex(file.getInputStream());

        // Проверяем, существует ли файл с таким хешем
        Optional<FileEntity> existingFile = fileRepository.findByHash(hash);
        if (existingFile.isPresent()) {
            return convertToDTO(existingFile.get());
        }

        // Если файл с таким хешем не существует, сохраняем новый
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        
        // Сохраняем файл
        Files.copy(file.getInputStream(), filePath);

        // Создаем сущность файла
        FileEntity fileEntity = FileEntity.builder()
                .name(file.getOriginalFilename())
                .location(filePath.toString())
                .hash(hash)
                .contentType(file.getContentType())
                .size(file.getSize())
                .createdAt(LocalDateTime.now())
                .build();

        // Сохраняем информацию в базу
        FileEntity savedFile = fileRepository.save(fileEntity);
        return convertToDTO(savedFile);
    }

    public List<FileDTO> getAllFiles() {
        return fileRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<FileDTO> getFile(Long id) {
        return fileRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<byte[]> getFileContent(Long id) throws IOException {
        Optional<FileEntity> fileEntity = fileRepository.findById(id);
        if (fileEntity.isPresent()) {
            Path path = Paths.get(fileEntity.get().getLocation());
            if (Files.exists(path)) {
                return Optional.of(Files.readAllBytes(path));
            }
        }
        return Optional.empty();
    }

    public boolean deleteFile(Long id) throws IOException {
        Optional<FileEntity> fileOptional = fileRepository.findById(id);
        if (fileOptional.isPresent()) {
            FileEntity file = fileOptional.get();
            Path path = Paths.get(file.getLocation());
            if (Files.exists(path)) {
                Files.delete(path);
            }
            fileRepository.delete(file);
            return true;
        }
        return false;
    }

    public boolean checkFileExists(MultipartFile file) throws IOException {
        String fileHash = calculateHash(file.getBytes());
        return fileRepository.existsByHash(fileHash);
    }

    private String calculateHash(byte[] content) {
        return DigestUtils.md5DigestAsHex(content);
    }

    private FileDTO convertToDTO(FileEntity entity) {
        return FileDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .contentType(entity.getContentType())
                .size(entity.getSize())
                .hash(entity.getHash())
                .build();
    }
} 