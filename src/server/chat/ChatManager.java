package server.chat;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatManager {

    public static class ChatSession {
        public final String chatId;
        public final String userA;
        public final String userB;
        public final Set<String> participants = new HashSet<>();

        public final Queue<String> messages = new ConcurrentLinkedQueue<>();

        private final Map<String, Queue<String>> inboxByUser = new ConcurrentHashMap<>();

        public ChatSession(String chatId, String userA, String userB) {
            this.chatId = chatId;
            this.userA = userA;
            this.userB = userB;

            this.participants.add(userA);
            this.participants.add(userB);

            inboxByUser.put(userA, new ConcurrentLinkedQueue<>());
            inboxByUser.put(userB, new ConcurrentLinkedQueue<>());
        }

        public void ensureInbox(String username) {
            inboxByUser.putIfAbsent(username, new ConcurrentLinkedQueue<>());
        }

        public List<String> drainInbox(String username) {
            Queue<String> q = inboxByUser.get(username);
            if (q == null) return List.of();

            List<String> out = new ArrayList<>();
            String m;
            while ((m = q.poll()) != null) out.add(m);
            return out;
        }

        public void pushToOthers(String fromUser, String line) {
            for (String p : participants) {
                if (p == null) continue;
                if (p.equals(fromUser)) continue;
                ensureInbox(p);
                inboxByUser.get(p).add(line);
            }
        }
    }

    private final Map<String, Integer> userBranch = new ConcurrentHashMap<>();
    private final Set<String> busyUsers = ConcurrentHashMap.newKeySet();

    private final Map<Integer, Queue<String>> waitingByBranch = new ConcurrentHashMap<>();

    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> userToChat = new ConcurrentHashMap<>();

    private final Map<String, List<ChatObserver>> observers = new ConcurrentHashMap<>();

    public void setUserBranch(String username, int branchId) {
        userBranch.put(username, branchId);
    }

    public boolean isBusy(String username) {
        return busyUsers.contains(username);
    }

    public synchronized String requestChat(String fromUser, int targetBranchId, List<String> availableUsersInTargetBranch) {
        if (isBusy(fromUser) || userToChat.containsKey(fromUser)) {
            return "FAIL|reason=UserBusy";
        }

        String targetUser = pickAvailableUser(availableUsersInTargetBranch);
        if (targetUser == null) {
            waitingByBranch.putIfAbsent(targetBranchId, new ArrayDeque<>());
            waitingByBranch.get(targetBranchId).add(fromUser);
            return "WAIT|reason=NoFreeUser";
        }

        return createSession(fromUser, targetUser);
    }

    public synchronized String pollIncomingRequest(String username) {
        Integer branchId = userBranch.get(username);
        if (branchId == null) return "NONE";

        if (isBusy(username) || userToChat.containsKey(username)) return "NONE";

        Queue<String> q = waitingByBranch.get(branchId);
        if (q == null) return "NONE";

        String requester = q.poll();
        if (requester == null) return "NONE";

        return createSession(requester, username);
    }

    private String createSession(String userA, String userB) {
        if (isBusy(userA) || isBusy(userB)) return "FAIL|reason=UserBusy";

        String chatId = UUID.randomUUID().toString();
        ChatSession s = new ChatSession(chatId, userA, userB);
        sessions.put(chatId, s);

        busyUsers.add(userA);
        busyUsers.add(userB);

        userToChat.put(userA, chatId);
        userToChat.put(userB, chatId);

        return "OK|chatId=" + chatId + ";userA=" + userA + ";userB=" + userB;
    }

    public synchronized String joinAsManager(String managerUser, String chatId) {
        ChatSession s = sessions.get(chatId);
        if (s == null) return "FAIL|reason=ChatNotFound";
        s.participants.add(managerUser);
        s.ensureInbox(managerUser);
        userToChat.put(managerUser, chatId);
        busyUsers.add(managerUser);
        return "OK|chatId=" + chatId;
    }

    public String send(String username, String text) {
        String chatId = userToChat.get(username);
        if (chatId == null) return "FAIL|reason=NotInChat";

        ChatSession s = sessions.get(chatId);
        if (s == null) return "FAIL|reason=ChatNotFound";

        if (!s.participants.contains(username)) return "FAIL|reason=NotInChat";

        String line = "[" + Instant.now() + "] " + username + ": " + text;

        s.messages.add(line);

        s.pushToOthers(username, line);

        return "OK";
    }

    public List<String> pollMessages(String username) {
        String chatId = userToChat.get(username);
        if (chatId == null) return List.of();

        ChatSession s = sessions.get(chatId);
        if (s == null) return List.of();

        return s.drainInbox(username);
    }

    public String readAll(String username) {
        String chatId = userToChat.get(username);
        if (chatId == null) return "FAIL|reason=NotInChat";

        ChatSession s = sessions.get(chatId);
        if (s == null) return "FAIL|reason=ChatNotFound";

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String m : s.messages) {
            if (!first) sb.append("\\n");
            first = false;
            sb.append(m);
        }
        return "OK|chatId=" + chatId + ";messages=" + sb;
    }

    public synchronized String end(String username) {
        String chatId = userToChat.get(username);
        if (chatId == null) return "FAIL|reason=NotInChat";

        ChatSession s = sessions.get(chatId);
        if (s == null) return "FAIL|reason=ChatNotFound";

        s.participants.remove(username);
        userToChat.remove(username);
        busyUsers.remove(username);

        boolean aIn = userToChat.containsKey(s.userA);
        boolean bIn = userToChat.containsKey(s.userB);

        if (!aIn && !bIn) {
            sessions.remove(chatId);
        }

        notifyAvailable(username);
        return "OK";
    }

    public void subscribeAvailability(String username, ChatObserver obs) {
        observers.putIfAbsent(username, new ArrayList<>());
        observers.get(username).add(obs);
    }

    private void notifyAvailable(String username) {
        List<ChatObserver> list = observers.get(username);
        if (list == null) return;
        for (ChatObserver o : list) {
            try { o.onUserBecameAvailable(username); } catch (Exception ignored) { }
        }
    }

    private String pickAvailableUser(List<String> users) {
        if (users == null) return null;
        for (String u : users) {
            if (u != null && !u.isBlank() && !isBusy(u) && !userToChat.containsKey(u)) return u;
        }
        return null;
    }

    public Set<String> getBusyUsersSnapshot() {
        return new HashSet<>(busyUsers);
    }
}
