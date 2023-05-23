package hao.common.http.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BceCredentials implements Credentials {

    private final String ak;
    private final String sk;

    @Override
    public String getAccessKey() {
        return ak;
    }

    @Override
    public String getSecretKey() {
        return sk;
    }
}
