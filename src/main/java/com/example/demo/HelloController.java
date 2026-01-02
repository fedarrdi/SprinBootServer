package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/test-db")
    public String testDatabase() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return "Database connection successful! Test query returned: " + count;
        } catch (Exception e) {
            return "Database connection failed: " + e.getMessage();
        }
    }

    @PostMapping("/GET_VALUE")
    public String getData(@RequestBody KeyValueData data) {
        String key = data.getKey();
        try {
            String value = jdbcTemplate.queryForObject(
                    "SELECT value_text FROM key_value_store WHERE key_name = ?",
                    String.class, key);
            return value;
        } catch (EmptyResultDataAccessException e) {
            return "Key not found: " + key;
        }
    }

    @PostMapping("/POST_VALUE")
    public String postValue(@RequestBody KeyValueData data) {
        String key = data.getKey();
        String value = data.getValues();

        jdbcTemplate.update(
                "INSERT INTO key_value_store (key_name, value_text) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE value_text = VALUES(value_text)",
                key, value);

        return "Success: Stored/Updated key '" + key + "'";
    }

    @DeleteMapping("/DELETE_VALUE")
    public String deleteValue(@RequestBody KeyValueData data) {
        String key = data.getKey();
        int rows = jdbcTemplate.update(
                "DELETE FROM key_value_store WHERE key_name = ?", key);

        return rows > 0 ? "Success: Deleted key '" + key + "'" : "Key not found: " + key;
    }
}