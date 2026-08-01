package room_resevation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import room_resevation.model.Reserva;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository  extends MongoRepository<Reserva,String> {



    @Query("{ 'salaId': ?0, 'status': 'CONFIRMED', "                             // Busca reservas da mesma sala que se sobrepõem ao intervalo informado
                                                                                 // Fórmula: existente.start < novoFim  E  existente.end > novoInicio
            + "'dataInicio': { $lt: ?2 }, 'dataFinal': { $gt: ?1 } }")
    List<Reserva> findOverlapping(String salaId, LocalDateTime inicio, LocalDateTime fim);

    List<Reserva> findBySalaIdAndStatus(String salaId, String status);
}


