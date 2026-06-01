package com.hivehub.app.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @GetMapping("/api/handshake")
    public ResponseEntity<Map<String, Object>> handshake() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "app", "HiveHub Backend",
                "databaseConnection", "OK"
        ));
    }
}