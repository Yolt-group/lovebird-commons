package nl.ing.lovebird.errorhandling;

class DummyExceptions {

    static class DummyException extends RuntimeException {
        DummyException() {
            super("dummy");
        }
    }

    static class TraceDummyException extends DummyException {
    }


    static class DebugDummyException extends DummyException {
    }


    static class InfoDummyException extends DummyException {
    }


    static class WarnDummyException extends DummyException {
    }


    static class ErrorDummyException extends DummyException {
    }
}
