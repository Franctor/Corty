package com.corty.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "corty.mail.brevo-api-key=test-key",
    "corty.mail.from=Corty <noreply@corty.app>",
    "corty.firebase.credentials-path=classpath:firebase-test.json",
    "stripe.secret-key=sk_test_dummy",
    "stripe.webhook-secret=whsec_dummy",
    "jwt.secret=test-secret-key-for-unit-tests-only-32chars",
    "jwt.expiration=3600000"
})
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
