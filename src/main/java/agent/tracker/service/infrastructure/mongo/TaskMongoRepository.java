package agent.tracker.service.infrastructure.mongo;

import agent.tracker.service.domain.model.TaskStatus;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Slice;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@MongoRepository
public interface TaskMongoRepository extends CrudRepository<TaskDocument, String> {
    List<TaskDocument> findByStatusOrderByUpdatedAtDescTaskIdDesc(TaskStatus status);

    List<TaskDocument> findAllByOrderByUpdatedAtDescTaskIdDesc();

    Slice<TaskDocument> findByStatus(TaskStatus status, Pageable pageable);

    Slice<TaskDocument> findAll(Pageable pageable);
}
