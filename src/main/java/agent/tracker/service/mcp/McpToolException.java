package agent.tracker.service.mcp;

public class McpToolException extends RuntimeException {

    private final String code;
    private final String correlationId;

    public McpToolException(String code, String message, String correlationId) {
        super(message);
        this.code = code;
        this.correlationId = correlationId;
    }

    public McpToolException(String code, String message, String correlationId, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.correlationId = correlationId;
    }

    public String getCode() {
        return code;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
