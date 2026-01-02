package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    private JdbcTemplate jdbcTemplate;  // Auto-configured if starter-jdbc or data-jpa is present

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot!";
    }

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
        return "GET DATA FROM DATABASE FOR KEY: ";
    }

    @PostMapping("/POST_VALUE")
    public String postValue(@RequestBody KeyValueData data){
        return " POSTING DATA AT KEY + " + data.get_Key() + "WITH VALUE" + data.get_Values() ;
    }

    @DeleteMapping("/DELETE_VALUE")
    public String deleteValue(@RequestBody KeyValueData data){
        return "DELETING VALUE AT KEY: " + data.get_Key();
    }

}