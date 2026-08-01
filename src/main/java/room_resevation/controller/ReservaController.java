package room_resevation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import room_resevation.model.Reserva;
import room_resevation.service.ReservaService;

import java.util.List;

@RestController
@RequestMapping("/api/reserva")
@RequiredArgsConstructor
public class ReservaController {
    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<Reserva> create(@Valid @RequestBody Reserva reserva){
        Reserva created= reservaService.create(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/sala/{salaId}")
    public ResponseEntity<List<Reserva>> listBySala(@PathVariable String salaId){
        return ResponseEntity.ok(reservaService.listBySala(salaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id){
        reservaService.cancel(id);
        return ResponseEntity.noContent().build();
    }

}
