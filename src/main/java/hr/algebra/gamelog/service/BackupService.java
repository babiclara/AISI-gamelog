package hr.algebra.gamelog.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BackupService {

    private static final String BACKUP_DIR = "backups";

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public String createBackup() throws IOException {
        Path dir = Paths.get(BACKUP_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = "backup-" + timestamp + ".sql";
        Path filePath = dir.resolve(filename).toAbsolutePath();

        entityManager
                .createNativeQuery("SCRIPT TO '" + filePath.toString().replace("\\", "/") + "'")
                .getResultList();

        return filePath.toString();
    }

    @Transactional
    public void restoreBackup(String filename) throws IOException {
        Path filePath = Paths.get(BACKUP_DIR).resolve(filename).toAbsolutePath();

        if (!Files.exists(filePath)) {
            throw new IOException("Backup file not found: " + filePath);
        }

        entityManager.createNativeQuery("DROP ALL OBJECTS").executeUpdate();

        entityManager
                .createNativeQuery("RUNSCRIPT FROM '" + filePath.toString().replace("\\", "/") + "'")
                .executeUpdate();
    }
}