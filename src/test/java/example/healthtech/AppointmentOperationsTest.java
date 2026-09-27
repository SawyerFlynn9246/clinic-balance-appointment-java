package example.healthtech;

public final class AppointmentOperationsTest {
    public static void main(String[] args) throws Exception {
        ClinicPolicy policy = new ClinicPolicy(25.0, 100.0, "ops@example.org");
        AppointmentOperations operations = new AppointmentOperations(new FakeInfraiClient(), policy);
        require(operations.process("low", 10) == AppointmentOperations.Action.RECHARGE_AND_NOTIFY, "low balance decision");
        require(operations.process("healthy", 50) == AppointmentOperations.Action.NOTIFY_ONLY, "healthy balance decision");
        System.out.println("AppointmentOperationsTest passed");
    }
    private static void require(boolean value, String label) { if (!value) throw new AssertionError(label); }

    private static final class FakeInfraiClient extends InfraiClient {
        FakeInfraiClient() { super("test-key", "https://example.invalid"); }
        @Override public String accountBalance() { return "{\"ok\":true,\"data\":{\"balance\":50}}"; }
        @Override public String configureAutorecharge(double t, double a, String k) { return "{\"ok\":true}"; }
        @Override public String sendEmail(String to, String subject, String text, String k) { return "{\"ok\":true}"; }
    }
}
