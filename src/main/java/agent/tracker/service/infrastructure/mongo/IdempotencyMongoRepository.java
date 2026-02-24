package agent.tracker.service.infrastructure.mongo;

import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@MongoRepository
public interface IdempotencyMongoRepository extends CrudRepository<IdempotencyRecordDocument, String> {
    Optional<IdempotencyRecordDocument> findByOperationAndKey(String operation, String key);
}
