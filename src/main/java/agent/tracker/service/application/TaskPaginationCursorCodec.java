package agent.tracker.service.application;

final class TaskPaginationCursorCodec {

    private static final String INVALID_CURSOR = "cursor must be a non-negative integer or offset token (o:<n>)";

    private TaskPaginationCursorCodec() {
    }

    static int decodeOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }

        String normalized = cursor.trim();
        int separatorIndex = normalized.indexOf(':');
        if (separatorIndex > 0) {
            String prefix = normalized.substring(0, separatorIndex).trim().toLowerCase();
            String payload = normalized.substring(separatorIndex + 1).trim();

            return switch (prefix) {
                case "o" -> parseOffset(payload);
                case "s" -> throw new IllegalArgumentException("seek cursor tokens are not enabled yet (supported: <n> or o:<n>)");
                default -> throw new IllegalArgumentException("cursor prefix is unsupported (allowed today: o:<n>)");
            };
        }

        return parseOffset(normalized);
    }

    static String encodeNextCursor(int currentOffset, int pageSize) {
        try {
            return String.valueOf(Math.addExact(currentOffset, pageSize));
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("cursor is too large to paginate safely");
        }
    }

    private static int parseOffset(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(INVALID_CURSOR);
        }

        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                throw new IllegalArgumentException(INVALID_CURSOR);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(INVALID_CURSOR);
        }
    }
}
