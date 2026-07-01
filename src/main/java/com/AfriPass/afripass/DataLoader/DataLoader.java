package com.AfriPass.afripass.DataLoader;

import com.AfriPass.afripass.Model.EventInventory;
import com.AfriPass.afripass.Repositories.EventInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private EventInventoryRepository eventInventoryRepository;

    @Override
    public void run(String... args) {
        if (eventInventoryRepository.count() == 0) {
            // only seed if table is empty
            eventInventoryRepository.saveAll(List.of(
                    EventInventory.builder().eventId(1L).availableSeats(50).build(),
                    EventInventory.builder().eventId(2L).availableSeats(100).build(),
                    EventInventory.builder().eventId(3L).availableSeats(75).build(),
                    EventInventory.builder().eventId(4L).availableSeats(200).build(),
                    EventInventory.builder().eventId(5L).availableSeats(30).build(),
                    EventInventory.builder().eventId(6L).availableSeats(500).build(),
                    EventInventory.builder().eventId(7L).availableSeats(80).build(),
                    EventInventory.builder().eventId(8L).availableSeats(150).build()
            ));
        }
    }
}
