package com.AfriPass.afripass.Client;

import com.AfriPass.afripass.DTOs.EventDetails;

import java.util.List;
import java.util.Optional;

public interface EventCatalogClient {

    List<EventDetails> getAll();
    Optional<EventDetails> getById(Long id);

}
