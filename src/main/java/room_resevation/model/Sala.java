package room_resevation.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "salas")                        //avisa que essa classe representa um documento(um registro) que será salvo na coleção chamada "salas" dentro bd
public class Sala {

    @Id
    private  String id;
    private String nome;
    private int capacidade;
    private List<String> recursos;
    private boolean ativa;
}
