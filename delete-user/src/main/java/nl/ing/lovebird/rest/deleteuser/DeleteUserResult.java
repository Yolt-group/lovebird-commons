package nl.ing.lovebird.rest.deleteuser;

import lombok.Data;

@Data
public class DeleteUserResult {
    private final long count;
    private final long success;
    private final long failed;

    public DeleteUserResult(final long count, final long success) {
        this.count = count;
        this.success = success;
        this.failed = count - success;
    }

    public boolean isSuccess() {
        return count == success;
    }
}
