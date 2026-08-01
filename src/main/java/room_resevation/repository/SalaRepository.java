package room_resevation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import room_resevation.model.Sala;

public interface SalaRepository extends MongoRepository<Sala,String> {

}
