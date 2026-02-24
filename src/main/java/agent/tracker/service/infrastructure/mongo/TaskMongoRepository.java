package agent.tracker.service.infrastructure.mongo;

import agent.tracker.service.domain.model.TaskStatus;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@MongoRepository
public interface TaskMongoRepository extends CrudRepository<TaskDocument, String> {
    List<TaskDocument> findByStatusOrderByUpdatedAtDescTaskIdAsc(TaskStatus status);

    List<TaskDocument> findAllByOrderByUpdatedAtDescTaskIdAsc();
}
