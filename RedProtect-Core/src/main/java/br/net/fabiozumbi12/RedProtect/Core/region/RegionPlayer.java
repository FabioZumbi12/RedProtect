package br.net.fabiozumbi12.RedProtect.Core.region;

public class RegionPlayer<K, V> {

    private K uuid;
    private V playerName;

    public RegionPlayer(K uuid, V playerName){
        this.uuid = uuid;
        this.playerName = playerName;
    }

    /**
     * Get the UUID or playerName if OnlineMode is false.
     * @return String of UUID or PlayerName
     */
    public K getUUID(){
        return this.uuid;
    }

    /**
     * Get the last know playerName.
     * @return String of PlayerName
     */
    public V getPlayerName(){
        return this.playerName;
    }

    public void setUUID(K uuid){
        this.uuid = uuid;
    }

    public void setPlayerName(V playerName){
        this.playerName = playerName;
    }

    @Override
    public String toString(){
        return "(" + uuid + "," + playerName + ")";
    }
}
