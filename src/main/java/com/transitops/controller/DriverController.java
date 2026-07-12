package com.transitops.controller;

import com.transitops.entity.Driver;
import com.transitops.service.DriverService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin("*")
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) {
        this.service = service;
    }

    @GetMapping
    public List<Driver> getDrivers(){
        return service.getAllDrivers();
    }

    @PostMapping
    public Driver addDriver(@RequestBody Driver driver){
        return service.addDriver(driver);
    }

    @DeleteMapping("/{id}")
    public void deleteDriver(@PathVariable Long id){
        service.deleteDriver(id);
    }

}