package com.crud.rental.controller;

import com.crud.rental.domain.Option;
import com.crud.rental.domain.OptionDto;
import com.crud.rental.exception.OptionNotFoundException;
import com.crud.rental.service.OptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/options")
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @GetMapping
    public ResponseEntity<List<OptionDto>> getAllOptions() {
        List<OptionDto> options = optionService.getAllOptions().stream()
                .map(o -> new OptionDto(o.getId(), o.getName(), o.getPrice()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(options);
    }

    @PostMapping
    public ResponseEntity<Void> addOption(@RequestBody OptionDto optionDto) {
        Option option = new Option(0L, optionDto.getName(), optionDto.getPrice(), null);
        optionService.addOption(option);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long id) throws OptionNotFoundException {
        optionService.deleteOption(id);
        return ResponseEntity.ok().build();
    }
}
