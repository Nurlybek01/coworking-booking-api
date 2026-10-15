package kz.nurlybek.coworking.client.session;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String accessToken;
    private String refreshToken;
    private String userEmail;
    private String userRole;

    private SessionManager() {}

    public static SessionManager get() {
        return INSTANCE;
    }

    public void store(String accessToken, String refreshToken, String email, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userEmail = email;
        this.userRole = role;
    }

    public void clear() {
        accessToken = null;
        refreshToken = null;
        userEmail = null;
        userRole = null;
    }

    public boolean isAuthenticated() {
        return accessToken != null;
    }

    public String getAccessToken()  { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getUserEmail()    { return userEmail; }
    public String getUserRole()     { return userRole; }
}