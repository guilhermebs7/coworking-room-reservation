package room_resevation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import room_resevation.exception.ConflictException;
import room_resevation.exception.ResourceNotFoundException;
import room_resevation.model.Reserva;
import room_resevation.repository.ReservaRepository;
import room_resevation.repository.SalaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final SalaRepository salaRepository;

    public Reserva create(Reserva reserva){
        salaRepository.findById(reserva.getSalaId())
                .orElseThrow(()-> new ResourceNotFoundException("Sala não encontrada"));


        if(!reserva.getDataFinal().isAfter(reserva.getDataInicio())){
            throw new IllegalArgumentException("Data de término deve ser após a data de início");
        }

        List<Reserva> conflitos= reservaRepository.findOverlapping(
                reserva.getSalaId(),
                reserva.getDataInicio(),
                reserva.getDataFinal()
        );

        if(!conflitos.isEmpty()){
            throw new ConflictException("Já existe uma reserva para essa sala nesse horário");
        }

        reserva.setStatus("CONFIRMED");
        return reservaRepository.save(reserva);
    }

    public List<Reserva> listBySala(String salaId) {
        return reservaRepository.findBySalaIdAndStatus(salaId,"CONFIRMED");
    }

    public void cancel(String id) {
        Reserva reservation = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada"));
        reservation.setStatus("CANCELLED");
        reservaRepository.save(reservation);
    }
}
