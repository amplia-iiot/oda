package es.amplia.oda.subsystem.sshserver.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SshConfiguration {
    @NonNull
    private String ip;
    private int port;
    @NonNull
    private String username;
    @NonNull
    private String password;
}
