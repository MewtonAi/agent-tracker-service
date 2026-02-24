package agent.tracker.service.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
    info = @Info(
        title = "Agent Tracker Service API",
        version = "v1",
        description = "Task-first REST surface with idempotent command semantics"
    ),
    tags = {
        @Tag(name = "Tasks", description = "Task command and query API")
    }
)
@ApiResponses({
    @ApiResponse(
        responseCode = "409",
        description = "Domain conflict",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiExceptionHandler.ApiProblem.class),
            examples = {
                @ExampleObject(
                    name = "concurrentModification",
                    value = "{\"code\":\"CONCURRENT_MODIFICATION\",\"title\":\"Conflict\",\"status\":409}"
                ),
                @ExampleObject(
                    name = "idempotencyKeyReuseMismatch",
                    value = "{\"code\":\"IDEMPOTENCY_KEY_REUSE_MISMATCH\",\"title\":\"Conflict\",\"status\":409}"
                )
            }
        )
    )
})
final class OpenApiContractConfiguration {
    private OpenApiContractConfiguration() {
    }
}
