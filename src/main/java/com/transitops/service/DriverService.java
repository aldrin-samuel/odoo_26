package com.transitops.service;

import com.transitops.entity.Driver;
import com.transitops.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository repository;

    public DriverService(DriverRepository repository) {
        this.repository = repository;
    }

    public List<Driver> getAllDrivers() {
        return repository.findAll();
    }

    public Driver addDriver(Driver driver) {

        if(repository.existsByLicenseNumber(driver.getLicenseNumber())){
            throw new RuntimeException("License already exists");
        }

        return repository.save(driver);
    }

    public void deleteDriver(Long id){
        repository.deleteById(id);
    }

    public long countDrivers(){
        return repository.count();
    }

}