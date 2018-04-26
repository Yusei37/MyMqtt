
public class MqttConnectOptions {

    private String username = "";
    private String password = "";
    private String clientID = "";
    private boolean cleanSession = false;
    private int timeout = 10;
    private short keepalive = 20;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public short getKeepalive() {
        return keepalive;
    }

    public void setKeepalive(short keepalive) {
        this.keepalive = keepalive;
    }
}
