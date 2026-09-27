package example.healthtech;

import java.io.IOException;

public final class AppointmentOperations {
    public enum Action { RECHARGE_AND_NOTIFY, NOTIFY_ONLY }
    private final InfraiClient infrai;
    private final ClinicPolicy policy;

    public AppointmentOperations(InfraiClient infrai, ClinicPolicy policy) { this.infrai = infrai; this.policy = policy; }

    public Action process(String appointmentId, double balanceUsd) throws IOException, InterruptedException {
        infrai.accountBalance();
        Action action = balanceUsd < policy.balanceFloorUsd() ? Action.RECHARGE_AND_NOTIFY : Action.NOTIFY_ONLY;
        if (action == Action.RECHARGE_AND_NOTIFY) {
            infrai.configureAutorecharge(policy.balanceFloorUsd(), policy.rechargeAmountUsd(), "appointment-" + appointmentId);
        }
        String message = "Appointment " + appointmentId + " is scheduled; account balance is $" + balanceUsd + ". Action: " + action;
        infrai.sendEmail(policy.operationsEmail(), "Clinic appointment operations", message, "notice-" + appointmentId);
        return action;
    }
}
