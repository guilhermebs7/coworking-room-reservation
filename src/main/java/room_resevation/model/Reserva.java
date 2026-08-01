package room_resevation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservas")
public class Reserva {

    @Id
    private String id;

    @Indexed       // Cria um índice no banco para acelerar as buscas filtradas por esta sala
    private String salaId;
    private String userNome;
    private LocalDateTime dataInicio;
    private  LocalDateTime dataFinal;
    private String status;


}
