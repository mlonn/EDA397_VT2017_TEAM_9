package se.chalmers.eda397.team9.cardsagainsthumanity.MulticastClasses;

import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import se.chalmers.eda397.team9.cardsagainsthumanity.ViewClasses.PlayerInfo;
import se.chalmers.eda397.team9.cardsagainsthumanity.ViewClasses.Serializer;

public class HostMulticastReceiver extends MulticastReceiver<Object, Void, Void>{

    private PlayerInfo hostInfo;
    private Map<PlayerInfo, Integer> connectingPlayers;
    private final int maxRetries = 10;

    public HostMulticastReceiver(WifiManager.MulticastLock mcLock, MulticastSocket s,
                                 InetAddress group, PlayerInfo hostInfo) {
        super(mcLock, s, group);
        this.hostInfo = hostInfo;
        connectingPlayers = new HashMap<>();
    }

    @Override
    protected Void doInBackground(Object... objects) {

        /* Handles receive message and send message */
        byte[] buf = new byte[10000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);

        try {
            getSocket().setSoTimeout(700);
        } catch (SocketException e) {
        }

        /* Keep trying to retrieve messages until cancelled */
        while (!isCancelled()) {
            Object inMsg = null;
            try {
                getSocket().receive(recv);
                inMsg = Serializer.deserialize(recv.getData());
            } catch (IOException e) {
                 /* If join acceptance from player doesn't arrive, try sending out the table again.
                    After maxRetries, stop sending and inform listeners that the player timed out */
                if(!connectingPlayers.isEmpty()) {
                    ArrayList<PlayerInfo> playersToRemove = new ArrayList<>();
                    for (Map.Entry<PlayerInfo, Integer> current : connectingPlayers.entrySet()) {
                        if (current.getValue() < maxRetries) {
                            current.setValue(current.getValue() + 1);
                            getPropertyChangeSupport().firePropertyChange("PLAYER_JOIN_REQUESTED",
                                    null, current.getKey());
                        } else {
                            playersToRemove.add(current.getKey());
                            getPropertyChangeSupport().firePropertyChange("PLAYER_TIMED_OUT",
                                    null, current.getKey());
                        }
                    }
                    for(PlayerInfo current : playersToRemove) {
                        connectingPlayers.remove(current);
                    }
                }
            }

            handleMessage(inMsg);
        }
        return null;
    }

    private void handleMessage(Object inMsg){
        if (inMsg instanceof MulticastPackage) {
            String targetAddress = (String) ((MulticastPackage) inMsg).getTarget();
            String packageType = (String) ((MulticastPackage) inMsg).getPackageType();
            Object packageObject = ((MulticastPackage) inMsg).getObject();
            Log.d("HMR", "Received a " + packageType);

            if (targetAddress.equals(MulticastSender.Target.ALL_DEVICES)){
                if(packageType.equals(MulticastSender.Type.GREETING)) {
                    getPropertyChangeSupport().firePropertyChange("TABLE_REQUESTED", 0, 1);
                    Log.d("HMR", "Sent table");
                }
            }

            if (targetAddress.equals(hostInfo.getDeviceAddress())) {
                if (packageType.equals(MulticastSender.Type.PLAYER_JOIN_REQUEST)) {
                    getPropertyChangeSupport().firePropertyChange("PLAYER_JOIN_REQUESTED",
                            null, packageObject);
                    connectingPlayers.put((PlayerInfo) packageObject, 0);
                    Log.d("HMR", "Player " + ((PlayerInfo) packageObject).getName() + " sent join request");
                }

                if (packageType.equals(MulticastSender.Type.PLAYER_JOIN_SUCCESS)) {
                    removePlayerFromMap(connectingPlayers, (PlayerInfo) packageObject);
                    getPropertyChangeSupport().firePropertyChange("PLAYER_JOIN_SUCCESSFUL",
                            null, packageObject);
                }

                if (packageType.equals(MulticastSender.Type.PLAYER_READY))
                    getPropertyChangeSupport().firePropertyChange("PLAYER_READY",
                            null, packageObject);

                if (packageType.equals(MulticastSender.Type.PLAYER_NOT_READY))
                    getPropertyChangeSupport().firePropertyChange("PLAYER_NOT_READY",
                            null, packageObject);
            }
        }
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        Log.d("HostMultRec", "Receiver cancelled");
    }

    private void removePlayerFromMap(Map<PlayerInfo, Integer> connectingPlayers,PlayerInfo player){
        PlayerInfo playerToRemove = null;
        for(Map.Entry<PlayerInfo, Integer> current : connectingPlayers.entrySet()){
            if(current.getKey().getDeviceAddress().equals(player.getDeviceAddress())){
                playerToRemove = current.getKey();
            }
        }
        if(playerToRemove != null){
            connectingPlayers.remove(playerToRemove);
        }
    }
}
