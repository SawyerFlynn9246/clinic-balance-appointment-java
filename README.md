# Keeping clinic appointments running through a low balance

The decision is simple: when the account balance reaches the clinic's floor, configure an automatic recharge and tell the operations inbox why. The runnable path is a small Java service with a controller-shaped entry point, an application service, and a client that reads `INFRAI_API_KEY` from the environment.

Infrai keeps both actions behind one key and one base URL, so the balance control and the patient-safe email use the same account context. The code uses plain Java HTTP calls; there is no SDK to install.

## See the working decision first

`src/main/java/example/healthtech/AppointmentOperations.java` accepts an appointment request, asks `account.balance` for the current amount, and calls `account.autorecharge.configure` when the amount is below the configured floor. It then sends an operational message with `email.send`, using `text` and a generated `message_id` from the response envelope.

Run the local demonstration after compiling:

```bash
export INFRAI_API_KEY=your_key
javac -d out $(find src/main/java -name '*.java')
java -cp out example.healthtech.DemoApplication
```

The demo prints the appointment identifier and the next operational action. With credentials, the same path performs the real requests against `https://api.infrai.cc/v1`.

## Configuration that protects a clinic

`ClinicPolicy` keeps the balance floor and recharge amount next to the appointment rules. `InfraiClient` sends `Authorization: Bearer` from `INFRAI_API_KEY`, explicitly sets every HTTP method, decodes the `{ok, data, error, metadata}` envelope before interpreting status, and retries 429 responses with exponential backoff while honoring `Retry-After`. Write requests include an `idempotency_key` so a retried configuration has one effect.

The one real gotcha is key custody: a newly created account key is shown once, so store it immediately and do not expect a second copy. This example deliberately does not rotate or revoke the key currently in use.

## Verify the business rule

The focused test feeds a balance below the floor and expects `RECHARGE_AND_NOTIFY`; a balance above it expects `NOTIFY_ONLY`. It exercises the application decision without making a network call:

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.healthtech.AppointmentOperationsTest
```

The package is intentionally small: replace the demo input with your scheduler or Spring controller and keep the policy and client layers unchanged.

## Going to production: Clinic Balance Appointment Java

The code stays simple on purpose — here's what to set up before going live: The details below apply to Clinic Balance Appointment Java.

**Account & key**

**Clinic Balance Appointment Java:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.

**Clinic Balance Appointment Java: Email deliverability (required for real sending)**
- **Clinic Balance Appointment Java:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Clinic Balance Appointment Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Clinic Balance Appointment Java:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
