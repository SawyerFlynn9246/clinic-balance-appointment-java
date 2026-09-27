package example.healthtech;

public record ClinicPolicy(double balanceFloorUsd, double rechargeAmountUsd, String operationsEmail) {
    public ClinicPolicy {
        if (balanceFloorUsd < 0 || rechargeAmountUsd <= 0 || operationsEmail.isBlank()) {
            throw new IllegalArgumentException("invalid clinic policy");
        }
    }
}
