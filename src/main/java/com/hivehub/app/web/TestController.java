package com.hivehub.app.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @GetMapping("/api/test")
    public ResponseEntity<Map<String, String>> testConnection() {
        return ResponseEntity.ok(Map.of("message", "The backend is alive"));
    }
}