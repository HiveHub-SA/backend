package com.hivehub.app.offline;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }

}