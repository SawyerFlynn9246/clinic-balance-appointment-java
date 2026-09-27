package example.healthtech;

public final class DemoApplication {
    public static void main(String[] args) throws Exception {
        ClinicPolicy policy = new ClinicPolicy(25.0, 100.0, "chenhua@changba.com");
        AppointmentOperations operations = new AppointmentOperations(new InfraiClient(), policy);
        System.out.println("Appointment appt-101 action: " + operations.process("appt-101", 12.50));
    }
}
