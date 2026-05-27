package hr.algebra.gamelog.controller.rest;

import hr.algebra.gamelog.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Database backup and restore (admin only)")
public class BackupRestController {

    private final BackupService backupService;

    public BackupRestController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/backup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a full SQL backup of the database")
    public ResponseEntity<Map<String, String>> backup() {
        try {
            String absolutePath = backupService.createBackup();
            String filename = Paths.get(absolutePath).getFileName().toString();
            return ResponseEntity.ok(Map.of(
                    "message", "Backup created successfully.",
                    "filename", filename,
                    "path", absolutePath
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Backup failed: " + e.getMessage()));
        }
    }

    @PostMapping("/restore/{filename}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restore the database from a previously created backup file")
    public ResponseEntity<Map<String, String>> restore(@PathVariable String filename) {
        try {
            backupService.restoreBackup(filename);
            return ResponseEntity.ok(Map.of(
                    "message", "Database restored successfully from " + filename
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Restore failed: " + e.getMessage()));
        }
    }
}