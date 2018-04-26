import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MqttClient {

    private Socket socket;
    private DataOutputStream output;
    private InputStream input;
    private MqttConnectOptions mqttConnectOptions;
    private MqttCallback mqttCallback;
    private ExecutorService executorService;
    private HashMap<Short, byte[]> packetList = new HashMap<>();

    MqttClient(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            output = new DataOutputStream(socket.getOutputStream());
            input = socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMqttConnectOptions(MqttConnectOptions mqttConnectOptions) {
        this.mqttConnectOptions = mqttConnectOptions;
    }

    public void setMqttCallback(MqttCallback mqttCallback) {
        this.mqttCallback = mqttCallback;
    }

    public void connect() {
        byte remaininglength = 10;
        String clientID = mqttConnectOptions.getClientID();
        String username = mqttConnectOptions.getUsername();
        String password = mqttConnectOptions.getPassword();
        int index = 12;

        if (!clientID.equals("")) {
            remaininglength += 2 + clientID.length();
        }
        if (!username.equals("")) {
            remaininglength += 2 + username.length();
        }
        if (!password.equals("")) {
            remaininglength += 2 + password.length();
        }

        byte[] bytes = new byte[2 + remaininglength];
        //Fixed header
        bytes[0] = (byte) 0x10;
        bytes[1] = remaininglength;

        // Variable header
        // Protocol name "MQTT"
        bytes[2] = (byte) 0x0;
        bytes[3] = (byte) 0x4;
        bytes[4] = (byte) 0x4D;
        bytes[5] = (byte) 0x51;
        bytes[6] = (byte) 0x54;
        bytes[7] = (byte) 0x54;
        //Protocol level
        bytes[8] = (byte) 0x4;
        //Connect flags
        bytes[9] = (byte) 0x00;
        if (!username.equals("")) {
            bytes[9] = (byte) (bytes[9] | 0x80);
        }
        if (!password.equals("")) {
            bytes[9] = (byte) (bytes[9] | 0x40);
        }
        if (mqttConnectOptions.isCleanSession() == true) {
            bytes[9] = (byte) (bytes[9] | 0x02);
        }
        //Keep alive
        short keepalive = mqttConnectOptions.getKeepalive();
        bytes[10] = (byte) ((keepalive & 0xff00) >> 8);
        bytes[11] = (byte) (keepalive & 0x00ff);

        //Payload
        //clientID
        if (!clientID.equals("")) {
            short clientIDLength = (short) clientID.length();
            bytes[12] = (byte) ((clientIDLength >> 8) & 0x00ff);
            bytes[13] = (byte) (clientIDLength & 0x00ff);
            index = 14;
            for (byte b : clientID.getBytes()) {
                bytes[index] = b;
                index ++;
            }
        }
        //username
        if (!username.equals("")) {
            short usernameLength = (short) username.length();
            bytes[index] = (byte) ((usernameLength >> 8) & 0x00ff); index ++;
            bytes[index] = (byte) (usernameLength & 0x00ff); index ++;
            for (byte b : username.getBytes()) {
                bytes[index] = b;
                index ++;
            }
        }
        //password
        if (!password.equals("")) {
            short passwordLength = (short) password.length();
            bytes[index] = (byte) ((passwordLength >> 8) & 0x00ff); index ++;
            bytes[index] = (byte) (passwordLength & 0x00ff); index ++;
            for (byte b : password.getBytes()) {
                bytes[index] = b;
                index ++;
            }
        }
        commonSender(bytes);
        executorService = Executors.newFixedThreadPool(2);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                pingreq();
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                commonReciever();
            }
        });
    }

    private void connack(byte[] connack) {
        switch (connack[3]) {
            case 0:
                mqttCallback.messageArrived("Connection Accepted!");
                break;
            case 1:
                mqttCallback.connectionLost(new Throwable("Connection Refused: unacceptable protocol version"));
                break;
            case 2:
                mqttCallback.connectionLost(new Throwable("Connection Refused: identifier rejected"));
                break;
            case 3:
                mqttCallback.connectionLost(new Throwable("Connection Refused: server unavailable"));
                break;
            case 4:
                mqttCallback.connectionLost(new Throwable("Connection Refused: bad username or password"));
                break;
            case 5:
                mqttCallback.connectionLost(new Throwable("Connection Refused: not authorized"));
                break;
            default:
                mqttCallback.connectionLost(new Throwable("Unknown message"));
                break;
        }
    }

    //From client to server
    public void publish(String pubtopic, int qoS, String message, boolean retian) {
        int index = 4;
        byte remaininglength = (byte) 0;
        //while QoS = 1 or 2, publish packet has packet identifier
        if (qoS > 0) {
            remaininglength += 2 + pubtopic.length() + 2 + message.length();
        }
        else {
            remaininglength += 2 + pubtopic.length() + message.length();
        }
        byte[] publish = new byte[2 + remaininglength];

        //Fixed header
        publish[0] = (byte) (0x30 | ((qoS << 1) & 0xFE));
        if (retian == true) {
            publish[0] = (byte) (publish[0] | 1);
        }
        publish[1] = remaininglength;

        //Variable header
        short pubtopicLength = (short) pubtopic.length();
        publish[2] = (byte) ((pubtopicLength >> 8) & 0x00ff);
        publish[3] = (byte) (pubtopicLength & 0x00ff);
        for (byte b : pubtopic.getBytes()) {
            publish[index] = b;
            index++;
        }
        short messageID = 0;
        if (qoS > 0) {
            messageID = getMessageID();
            publish[index] = (byte) ((messageID >> 8) & 0x00ff); index++;
            publish[index] = (byte) (messageID & 0x00ff); index++;
        }

        //Payload
        for (byte b : message.getBytes()) {
            publish[index] = b;
            index++;
        }
        commonSender(publish);
        packetList.put(messageID, publish);
    }

    //From server to client
    private void publish(byte[] publish) {
        String topic = "";
        int qoS = (publish[0] >> 1) & 0x3;
        String message = "";

        int remaininglength = publish[1];
        int pubtopicLength = (publish[2] << 8) + publish[3];
        int index = 4;
        byte[] topicArray = new byte[pubtopicLength];
        for (int i = 0; i < pubtopicLength; i++) {
            topicArray[i] = publish[index + i];
        }
        topic = new String(topicArray);
        index += pubtopicLength;

        if (qoS == 1) {
            byte[] puback = new byte[4];
            //Fixed header
            puback[0] = (byte) 0x40;
            puback[1] = (byte) 0x02;
            //Variable header
            puback[2] = publish[index]; index++;
            puback[3] = publish[index]; index++;
            commonSender(puback);
        }
        else if (qoS == 2) {
            byte[] pubrec = new byte[4];
            //Fixed header
            pubrec[0] = (byte) 0x50;
            pubrec[1] = (byte) 0x02;
            //Variable header
            pubrec[2] = publish[index]; index++;
            pubrec[3] = publish[index]; index++;
            commonSender(pubrec);
        }

        byte[] messageArray = new byte[remaininglength + 2 - index];
        for (int i = index; i < remaininglength + 2; i++) {
            messageArray[i - index] = publish[i];
        }
        message = new String(messageArray);
        mqttCallback.messageArrived("Topic:" + topic + " QoS: " + qoS + " Message: " + message);
    }

    private void puback(byte[] puback) {
        mqttCallback.messageArrived("pub successes");
    }

    private void pubrec(byte[] pubrec) {
        byte[] pubrel = new byte[4];
        //Fixed header
        pubrel[0] = (byte) 0x62;
        pubrel[1] = (byte) 0x02;
        //Variable header
        pubrel[2] = pubrec[2];
        pubrel[3] = pubrec[3];
        commonSender(pubrel);
    }

    private void pubrel(byte[] pubrel) {
        byte[] pubcomp = new byte[4];
        //Fixed header
        pubcomp[0] = (byte) 0x70;
        pubcomp[1] = (byte) 0x02;
        //Variable header
        pubcomp[2] = pubrel[2];
        pubcomp[3] = pubrel[3];
        commonSender(pubcomp);
    }

    private void pubcomp(byte[] pubcomp) {
        mqttCallback.messageArrived("pub successes");
    }

    public void subscribe(String subtopic, int qoS) {
        byte remaininglength = (byte) (2 + 2 + subtopic.length() + 1);
        byte[] subsrcibe = new byte[2 + remaininglength];
        int index = 6;

        //Fixed header
        subsrcibe[0] = (byte) 0x82;
        subsrcibe[1] = remaininglength;

        //Variable header
        short messageID = getMessageID();
        subsrcibe[2] = (byte) ((messageID >> 8) & 0x00ff);
        subsrcibe[3] = (byte) (messageID & 0x00ff);

        //Payload
        short subtopicLength = (short) subtopic.length();
        subsrcibe[4] = (byte) ((subtopicLength >> 8) & 0x00ff);
        subsrcibe[5] = (byte) (subtopicLength & 0x00ff);
        for (byte b : subtopic.getBytes()) {
            subsrcibe[index] = b;
            index++;
        }
        subsrcibe[index] = (byte) qoS;
        commonSender(subsrcibe);
        packetList.put(messageID, subsrcibe);
    }

    private void suback(byte[] suback) {
        switch (suback[4]) {
            case 0: case 1: case 2:
                mqttCallback.messageArrived("Sub successes");
                break;
            case (byte) 0x80:
                mqttCallback.messageArrived("Sub failes");
                break;
            default:
                mqttCallback.messageArrived("Unknown suback message");
                break;
        }
    }

    public void unsubscribe(String unsubtopic) {
        byte remaininglength = (byte) (2 + 2 + unsubtopic.length());
        byte[] unsubsrcibe = new byte[2 + remaininglength];
        int index = 6;

        //Fixed header
        unsubsrcibe[0] = (byte) 0xA2;
        unsubsrcibe[1] = remaininglength;

        //Variable header
        short messageID = getMessageID();
        unsubsrcibe[2] = (byte) ((messageID >> 8) & 0x00ff);
        unsubsrcibe[3] = (byte) (messageID & 0x00ff);

        //Payload
        short unsubtopicLength = (short) unsubtopic.length();
        unsubsrcibe[4] = (byte) ((unsubtopicLength >> 8) & 0x00ff);
        unsubsrcibe[5] = (byte) (unsubtopicLength & 0x00ff);
        for (byte b : unsubtopic.getBytes()) {
            unsubsrcibe[index] = b;
            index++;
        }
        commonSender(unsubsrcibe);
        packetList.put(messageID, unsubsrcibe);
    }

    private void unsuback(byte[] unsuback) {
        mqttCallback.messageArrived("Unsub successes");
    }

    private void pingreq() {
        try {
            while (true) {
                Thread.sleep(mqttConnectOptions.getKeepalive()*1000);
                byte[] pingreq = new byte[2];
                //Fixed header
                pingreq[0] = (byte) 0xC0;
                pingreq[1] = (byte) 0x00;
                commonSender(pingreq);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void pingresp(byte[] pingresp) {

    }

    public void disconnect() {
        byte[] disconnect = new byte[2];
        //Fixed header
        disconnect[0] = (byte) 0xE0;
        disconnect[1] = (byte) 0x00;
        commonSender(disconnect);
        socketClose();
    }

    private short getMessageID() {
        short messageID = (short) (Math.random() * 65536);
        while (packetList.containsKey(messageID)) {
            messageID = (short) (Math.random() * 65536);
        }
        return messageID;
    }

    private void socketClose() {
        try {
            executorService.shutdown();
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void commonSender(byte[] bytes) {
        try {
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void commonReciever() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = input.read(buffer)) != -1) {
                    byte[] copy = new byte[2 + buffer[1]];
                    System.arraycopy(buffer, 0, copy, 0, 2 + buffer[1]);
                    switch ((copy[0] >> 4) & 0x0f) {
                        case 13:
                            pingresp(copy);
                            break;
                        case 11:
                            unsuback(copy);
                            break;
                        case 9:
                            suback(copy);
                            break;
                        case 7:
                            pubcomp(copy);
                            break;
                        case 6:
                            pubrel(copy);
                            break;
                        case 5:
                            pubrec(copy);
                            break;
                        case 4:
                            puback(copy);
                            break;
                        case 3:
                            publish(copy);
                            break;
                        case 2:
                            connack(copy);
                            break;
                        default:
                            mqttCallback.connectionLost(new Throwable("Error Packet: "+new String(copy)));
//                            System.out.print("Error Message: ");
//                            for (int i = 0; i < len; i++) {
//                                System.out.print(buffer[i] + " ");
//                            }
//                            System.out.println();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
