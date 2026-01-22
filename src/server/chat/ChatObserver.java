package server.chat;

public interface ChatObserver {
    void onUserBecameAvailable(String username);
}
