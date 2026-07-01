package com.ufb.auth.user_management.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Component
public class FileClaimTokenNotifier implements ClaimTokenNotifier {

    private static final Logger log = LoggerFactory.getLogger(FileClaimTokenNotifier.class);

    private final Path dir;

    public FileClaimTokenNotifier(@Value("${ufb.admin.claim-token-dir}") String dir) {
        this.dir = Paths.get(dir).toAbsolutePath();
    }

    @Override
    public void deliver(String recipientEmail, String rawClaimToken, Instant expiresAt) {
        try {
            Files.createDirectories(dir);

            String safeName = recipientEmail.replaceAll("[^a-zA-Z0-9]", "_");
            Path file = dir.resolve("claim-token-" + safeName + ".txt");

            String contents = """
                    UFB Admin Account Claim Token
                    ------------------------------
                    Account : %s
                    Token   : %s
                    Expires : %s

                    To claim this account, POST to /api/auth/claim with:
                    { "email": "%s", "claimToken": "<token above>", "newPassword": "<your password>" }

                    This token is one-time use. Delete this file once claimed.
                    """.formatted(recipientEmail, rawClaimToken, expiresAt, recipientEmail);

            Files.writeString(file, contents, StandardCharsets.UTF_8);

            try {
                Set<PosixFilePermission> perms = EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE
                );
                Files.setPosixFilePermissions(file, perms);
            } catch (UnsupportedOperationException e) {
                log.warn("Could not set owner-only permissions on {} (non-POSIX filesystem). "
                        + "Protect this file manually.", file);
            }

            log.info("Claim token for {} written to {} (owner-only). "
                    + "Retrieve it there, then claim the account.", recipientEmail, file);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to write claim token file for " + recipientEmail, e);
        }
    }
}
