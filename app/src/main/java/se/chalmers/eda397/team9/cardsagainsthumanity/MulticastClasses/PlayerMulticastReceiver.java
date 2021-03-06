package se.chalmers.eda397.team9.cardsagainsthumanity.MulticastClasses;

import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;


import se.chalmers.eda397.team9.cardsagainsthumanity.Classes.Game;

import se.chalmers.eda397.team9.cardsagainsthumanity.Classes.BlackCard;

import se.chalmers.eda397.team9.cardsagainsthumanity.ViewClasses.Message;
import se.chalmers.eda397.team9.cardsagainsthumanity.ViewClasses.PlayerInfo;
import se.chalmers.eda397.team9.cardsagainsthumanity.ViewClasses.Serializer;
import se.chalmers.eda397.team9.cardsagainsthumanity.ViewClasses.TableInfo;

public class PlayerMulticastReceiver extends MulticastReceiver {

    private PlayerInfo myPlayerInfo;
    private boolean isJoined;
    private TableInfo table;

    public PlayerMulticastReceiver(WifiManager.MulticastLock mcLock, MulticastSocket s,
                                   InetAddress group, PlayerInfo myPlayerInfo, boolean isJoined, TableInfo table) {
        super(mcLock, s, group);
        this.myPlayerInfo = myPlayerInfo;
        this.table = table;
        this.isJoined = isJoined;
    }


    public PlayerMulticastReceiver(WifiManager.MulticastLock mcLock, MulticastSocket s,
                                   InetAddress group, PlayerInfo myPlayerInfo, TableInfo table) {
        this(mcLock, s, group, myPlayerInfo, false, table);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        getPropertyChangeSupport().firePropertyChange(Message.Type.START_REFRESHING, 0, 1);
        getPropertyChangeSupport().firePropertyChange(Message.Type.REQUEST_TABLE, 0, 1);
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        int counter = 0;
        int maxCount = 3;

        while (!isCancelled() && counter < maxCount) {
            byte[] buf = new byte[100000];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            Object msg = null;

            try {
                getSocket().setSoTimeout(500);
            } catch (SocketException e) {
            }

            try {
                getSocket().receive(recv);
                msg = Serializer.deserialize(recv.getData());
            } catch (IOException e) {
                if (!isJoined) {
                    getPropertyChangeSupport().firePropertyChange(Message.Type.REQUEST_TABLE, 0, 1);
                    counter++;
                }
            }

            if (msg instanceof MulticastPackage) {
                String target = ((MulticastPackage) msg).getTarget();
                String type = ((MulticastPackage) msg).getPackageType();
                Object packageObject = ((MulticastPackage) msg).getObject();

                Log.d("PlayerMultRec", "Received a " + type + " with destination " + target + " joined " + isJoined);
/*                if (packageObject instanceof ArrayList) {
                    if (target.equals(table.getHost().getDeviceAddress())) {
                        if (type.equals(Message.Type.PLAYER_LIST)) {
                            getPropertyChangeSupport().firePropertyChange(Message.Type.PLAYER_LIST, null, packageObject);
                        }
                        if (type.equals(Message.Type.EXPANSION_LIST)) {
                            getPropertyChangeSupport().firePropertyChange(Message.Type.EXPANSION_LIST, null, packageObject);
                        }
                    }
                }
                if (packageObject instanceof PlayerInfo) {
                    if (target.equals(table.getHost().getDeviceAddress())) {
                        if (type.equals(Message.Type.KING)) {
                            getPropertyChangeSupport().firePropertyChange(Message.Type.KING, null, packageObject);
                        }
                    }
                }
                if (packageObject instanceof BlackCard) {
                    if (target.equals(table.getHost().getDeviceAddress())) {
                        if (type.equals(Message.Type.BLACK_CARD)) {
                            getPropertyChangeSupport().firePropertyChange(Message.Type.BLACK_CARD, null, packageObject);
                        }
                    }
                }*/
                if (packageObject instanceof Game) {
                    if (target.equals(table.getHost().getDeviceAddress())) {
                        if (type.equals(Message.Type.GAME_STARTED)) {
                            getPropertyChangeSupport().firePropertyChange(Message.Type.GAME_STARTED, null, packageObject);
                        }
                    }
                }
                if (packageObject instanceof TableInfo) {
                    Log.d("PlayerMultRec", target + " | " + ((TableInfo) packageObject).getHost().getDeviceAddress());
                    if (target.equals(table.getHost().getDeviceAddress())) {
                        //Someone else joins
                        if (isJoined) {
                            if (type.equals(Message.Response.PLAYER_JOIN_ACCEPTED)) {
                                getPropertyChangeSupport().firePropertyChange(
                                        Message.Response.OTHER_PLAYER_JOIN_ACCEPTED, 0, packageObject);
                            }
                        } else {
                            //I Join
                            if (type.equals(Message.Response.PLAYER_JOIN_ACCEPTED)) {
                                getPropertyChangeSupport().firePropertyChange(Message.Response.SELF_PLAYER_JOIN_ACCEPTED,
                                        null, packageObject);
                                getPropertyChangeSupport().firePropertyChange(Message.Type.STOP_REFRESHING, 0, 1);
                            }

                            if (type.equals(Message.Response.PLAYER_JOIN_DENIED)) {
                                getPropertyChangeSupport().firePropertyChange(Message.Type.TABLE_FULL, 0, 1);
                                getPropertyChangeSupport().firePropertyChange(Message.Type.STOP_REFRESHING, 0, 1);
                                return null;
                            }
                        }
                    }
                }
                if (type.equals(Message.Type.PLAYER_TIMED_OUT))
                    getPropertyChangeSupport().firePropertyChange(Message.Response.PLAYER_DISCONNECTED, 0, 1);
            }
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        Log.d("PlayerMultReceiver", "Receiver cancelled");
        getPropertyChangeSupport().firePropertyChange(Message.Type.STOP_REFRESHING, 0, 1);
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Object result) {
        getPropertyChangeSupport().firePropertyChange(Message.Type.START_REFRESHING, 0, 1);
        if (isJoined == false) {
            getPropertyChangeSupport().firePropertyChange(Message.Type.NO_RESPONSE, 0, 1);
        }
    }
}
