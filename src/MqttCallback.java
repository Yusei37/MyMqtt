
public interface MqttCallback {

    void connectionLost(Throwable cause);

    void messageArrived(String message);

    void deliveryComplete(String message);
}
