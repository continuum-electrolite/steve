package de.rwth.idsg.steve.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
public class RestHealthController {

    public RestHealthController() {
    }

    @GetMapping(
            produces = {"application/json"}
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"UP\"}");
    }
}
