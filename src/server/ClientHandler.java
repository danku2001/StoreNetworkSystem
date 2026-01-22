package server;

import common.Message;
import common.Protocol;
import server.chat.ChatManager;
import server.core.*;
import server.model.Employee;
import server.model.Role;
import server.model.Product;
import server.model.customer.Customer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final LogService logger;

    private final AuthService authService = new AuthService();
    private final InventoryService inventoryService = new InventoryService();
    private final EmployeeService employeeService = new EmployeeService();
    private final CustomerService customerService = new CustomerService();
    private final PurchaseService purchaseService = new PurchaseService();
    private final ReportService reportService = new ReportService();
    private final ServerDataStore store = ServerDataStore.getInstance();
    private final ChatManager chatManager = new ChatManager();

    private String currentUser = null;
    private int currentBranchId = -1;
    private Role currentRole = null;

    private String lastReportJson = null;

    public ClientHandler(Socket socket, LogService logger) {
        this.socket = socket;
        this.logger = logger;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {

            out.println(new Message(Protocol.PONG,
                    "server=ready;v=3;features=inventory,employees,customers,purchase,reports,chat,logs").encode());

            String line;
            while ((line = in.readLine()) != null) {
                Message msg = Message.decode(line);

                if (msg.getType() == Protocol.LOGIN) {
                    String username = extract(msg.getPayload(), "username");
                    String password = extract(msg.getPayload(), "password");

                    try {
                        Employee emp = authService.login(username, password);

                        currentUser = emp.getUsername();
                        currentBranchId = emp.getBranchId();
                        currentRole = emp.getRole();

                        chatManager.setUserBranch(currentUser, currentBranchId);

                        logger.log("LOGIN", "user=" + currentUser + " branch=" + currentBranchId + " role=" + currentRole);

                        out.println(new Message(Protocol.LOGIN_OK,
                                "role=" + currentRole + ";branchId=" + currentBranchId).encode());

                    } catch (Exception e) {
                        out.println(new Message(Protocol.LOGIN_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (currentUser == null) {
                    out.println(new Message(Protocol.LOGIN_FAIL, "reason=NotLoggedIn").encode());
                    continue;
                }

                if (msg.getType() == Protocol.LOGOUT) {
                    authService.logout(currentUser);
                    chatManager.end(currentUser);
                    logger.log("LOGOUT", "user=" + currentUser);
                    out.println(new Message(Protocol.PONG, "logout=ok;v=3").encode());
                    return;
                }

                if (msg.getType() == Protocol.INVENTORY_LIST) {
                    try {
                        Map<Integer, Integer> inv = inventoryService.getInventorySnapshot(currentBranchId);

                        StringBuilder sb = new StringBuilder();
                        boolean first = true;
                        for (Map.Entry<Integer, Integer> e : inv.entrySet()) {
                            int productId = e.getKey();
                            int qty = e.getValue();
                            Product p = store.getProduct(productId);
                            String name = (p == null) ? ("Product#" + productId) : p.getName();

                            if (!first) sb.append(";");
                            first = false;
                            sb.append("productId=").append(productId)
                                    .append(",name=").append(name)
                                    .append(",qty=").append(qty);
                        }

                        out.println(new Message(Protocol.INVENTORY_LIST_OK, sb.toString()).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.INVENTORY_LIST_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.BUY) {
                    try {
                        int productId = Integer.parseInt(extract(msg.getPayload(), "productId"));
                        int qty = Integer.parseInt(extract(msg.getPayload(), "qty"));

                        inventoryService.buy(currentBranchId, productId, qty);

                        logger.log("BUY", "user=" + currentUser + " branch=" + currentBranchId + " productId=" + productId + " qty=" + qty);

                        out.println(new Message(Protocol.BUY_OK, "ok=true").encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.BUY_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.SELL) {
                    try {
                        int productId = Integer.parseInt(extract(msg.getPayload(), "productId"));
                        int qty = Integer.parseInt(extract(msg.getPayload(), "qty"));

                        inventoryService.sell(currentBranchId, productId, qty);

                        logger.log("SELL", "user=" + currentUser + " branch=" + currentBranchId + " productId=" + productId + " qty=" + qty);

                        out.println(new Message(Protocol.SELL_OK, "ok=true").encode());
                    } catch (OutOfStockException e) {
                        out.println(new Message(Protocol.SELL_FAIL, "reason=" + e.getMessage()).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.SELL_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.CUST_ADD) {
                    try {
                        String fullName = extract(msg.getPayload(), "fullName");
                        String id = extract(msg.getPayload(), "id");
                        String phone = extract(msg.getPayload(), "phone");
                        String type = extract(msg.getPayload(), "type");

                        customerService.addCustomer(fullName, id, phone, type);

                        logger.log("CUST_ADD", "user=" + currentUser + " id=" + id + " type=" + type);

                        out.println(new Message(Protocol.CUST_ADD_OK, "ok=true").encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.CUST_ADD_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.CUST_LIST) {
                    try {
                        Collection<Customer> customers = store.listCustomers();
                        StringBuilder sb = new StringBuilder();
                        boolean first = true;
                        for (Customer c : customers) {
                            if (!first) sb.append(";");
                            first = false;
                            sb.append("fullName=").append(c.getFullName())
                                    .append(",id=").append(c.getId())
                                    .append(",phone=").append(c.getPhone())
                                    .append(",type=").append(c.getType());
                        }
                        out.println(new Message(Protocol.CUST_LIST_OK, sb.toString()).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.CUST_LIST_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.PURCHASE) {
                    try {
                        String customerId = extract(msg.getPayload(), "customerId");
                        int productId = Integer.parseInt(extract(msg.getPayload(), "productId"));
                        int qty = Integer.parseInt(extract(msg.getPayload(), "qty"));

                        double paid = purchaseService.purchase(currentBranchId, customerId, productId, qty);

                        logger.log("PURCHASE", "user=" + currentUser + " branch=" + currentBranchId +
                                " customerId=" + customerId + " productId=" + productId + " qty=" + qty + " paid=" + paid);

                        out.println(new Message(Protocol.PURCHASE_OK, "paid=" + paid).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.PURCHASE_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.EMP_ADD) {
                    try {
                        String username = extract(msg.getPayload(), "username");
                        String password = extract(msg.getPayload(), "password");
                        String roleStr = extract(msg.getPayload(), "role");
                        int branchId = Integer.parseInt(extract(msg.getPayload(), "branchId"));

                        String fullName = extract(msg.getPayload(), "fullName");
                        String id = extract(msg.getPayload(), "id");
                        String phone = extract(msg.getPayload(), "phone");
                        String bankAccount = extract(msg.getPayload(), "account");
                        String employeeNumber = extract(msg.getPayload(), "empNum");

                        employeeService.addEmployee(
                                username,
                                password,
                                roleStr,
                                branchId,
                                fullName,
                                id,
                                phone,
                                bankAccount,
                                employeeNumber
                        );

                        logger.log("EMP_ADD", "admin=" + currentUser + " username=" + username + " role=" + roleStr + " branch=" + branchId);

                        out.println(new Message(Protocol.EMP_ADD_OK, "ok=true").encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.EMP_ADD_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.EMP_LIST) {
                    try {
                        Collection<Employee> employees = store.listEmployees();
                        StringBuilder sb = new StringBuilder();
                        boolean first = true;
                        for (Employee e : employees) {
                            if (!first) sb.append(";");
                            first = false;
                            sb.append("username=").append(e.getUsername())
                                    .append(",role=").append(e.getRole())
                                    .append(",branchId=").append(e.getBranchId())
                                    .append(",fullName=").append(e.getFullName())
                                    .append(",id=").append(e.getId())
                                    .append(",phone=").append(e.getPhone())
                                    .append(",account=").append(e.getBankAccount())
                                    .append(",empNum=").append(e.getEmployeeNumber());
                        }
                        out.println(new Message(Protocol.EMP_LIST_OK, sb.toString()).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.EMP_LIST_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.REPORT_BRANCH_DAILY) {
                    try {
                        int branchId = Integer.parseInt(extract(msg.getPayload(), "branchId"));
                        String json = reportService.branchDailyJson(branchId);
                        lastReportJson = json;
                        out.println(new Message(Protocol.REPORT_BRANCH_DAILY_OK, "json=" + json).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.REPORT_BRANCH_DAILY_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.REPORT_PRODUCT) {
                    try {
                        int productId = Integer.parseInt(extract(msg.getPayload(), "productId"));
                        String json = reportService.productReportJson(productId);
                        lastReportJson = json;
                        out.println(new Message(Protocol.REPORT_PRODUCT_OK, "json=" + json).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.REPORT_PRODUCT_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.REPORT_CATEGORY) {
                    try {
                        String category = extract(msg.getPayload(), "category");
                        String json = reportService.categoryReportJson(category);
                        lastReportJson = json;
                        out.println(new Message(Protocol.REPORT_CATEGORY_OK, "json=" + json).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.REPORT_CATEGORY_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.REPORT_EXPORT_WORD) {
                    try {
                        if (lastReportJson == null || lastReportJson.isBlank()) {
                            out.println(new Message(Protocol.REPORT_EXPORT_WORD_FAIL, "reason=NoLastReport").encode());
                            continue;
                        }
                        String fileName = "report_" + System.currentTimeMillis() + ".rtf";
                        String filePath = reportService.exportJsonToWordRtf(lastReportJson, fileName);
                        out.println(new Message(Protocol.REPORT_EXPORT_WORD_OK, "file=" + filePath).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.REPORT_EXPORT_WORD_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.LOG_LIST) {
                    try {
                        out.println(new Message(Protocol.LOG_LIST_OK, "file=" + logger.getFilePath()).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.LOG_LIST_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.LOG_TAIL) {
                    try {
                        int n = 30;
                        String nStr = extract(msg.getPayload(), "n");
                        if (nStr != null && !nStr.isBlank()) n = Integer.parseInt(nStr);

                        String joined = String.join("\\n", logger.tailLines(n));
                        out.println(new Message(Protocol.LOG_TAIL_OK, "lines=" + joined).encode());
                    } catch (Exception e) {
                        out.println(new Message(Protocol.LOG_TAIL_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.CHAT_REQUEST) {
                    try {
                        int targetBranchId = Integer.parseInt(extract(msg.getPayload(), "targetBranchId"));

                        List<String> targetUsers = new ArrayList<>();
                        for (Employee e : store.listEmployees()) {
                            if (e.getBranchId() == targetBranchId && e.getUsername() != null) {
                                targetUsers.add(e.getUsername());
                            }
                        }

                        String res = chatManager.requestChat(currentUser, targetBranchId, targetUsers);

                        if (res.startsWith("OK|")) {
                            out.println(new Message(Protocol.CHAT_REQUEST_OK, res.substring(3)).encode());
                        } else {
                            out.println(new Message(Protocol.CHAT_REQUEST_FAIL, "reason=" + res).encode());
                        }

                        logger.log("CHAT_REQUEST", "from=" + currentUser + " toBranch=" + targetBranchId + " res=" + res);

                    } catch (Exception e) {
                        out.println(new Message(Protocol.CHAT_REQUEST_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.CHAT_POLL) {
                    String resMessages = chatManager.readAll(currentUser);
                    if (resMessages.startsWith("OK|")) {
                        out.println(new Message(Protocol.CHAT_POLL_OK, resMessages.substring(3)).encode());
                    } else {
                        String resReq = chatManager.pollIncomingRequest(currentUser);
                        if (resReq.startsWith("OK|")) {
                            out.println(new Message(Protocol.CHAT_POLL_OK, resReq.substring(3)).encode());
                        } else {
                            out.println(new Message(Protocol.CHAT_POLL_FAIL, "reason=" + resReq).encode());
                        }
                    }
                    continue;
                }

                if (msg.getType() == Protocol.CHAT_SEND) {
                    try {
                        String text = extract(msg.getPayload(), "text");
                        String res = chatManager.send(currentUser, text);

                        if (res.equals("OK")) {
                            out.println(new Message(Protocol.CHAT_SEND_OK, "ok=true").encode());
                        } else {
                            out.println(new Message(Protocol.CHAT_SEND_FAIL, "reason=" + res).encode());
                        }

                        logger.log("CHAT_SEND", "user=" + currentUser + " res=" + res + " text=" + text);

                    } catch (Exception e) {
                        out.println(new Message(Protocol.CHAT_SEND_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.CHAT_JOIN) {
                    try {
                        if (currentRole != Role.SHIFT_MANAGER) {
                            out.println(new Message(Protocol.CHAT_JOIN_FAIL, "reason=NoPermission").encode());
                            continue;
                        }
                        String chatId = extract(msg.getPayload(), "chatId");
                        String res = chatManager.joinAsManager(currentUser, chatId);

                        if (res.startsWith("OK|")) {
                            out.println(new Message(Protocol.CHAT_JOIN_OK, res.substring(3)).encode());
                        } else {
                            out.println(new Message(Protocol.CHAT_JOIN_FAIL, "reason=" + res).encode());
                        }

                        logger.log("CHAT_JOIN", "manager=" + currentUser + " chatId=" + chatId + " res=" + res);

                    } catch (Exception e) {
                        out.println(new Message(Protocol.CHAT_JOIN_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                if (msg.getType() == Protocol.CHAT_END) {
                    try {
                        String res = chatManager.end(currentUser);

                        if (res.equals("OK")) {
                            out.println(new Message(Protocol.CHAT_END_OK, "ok=true").encode());
                        } else {
                            out.println(new Message(Protocol.CHAT_END_FAIL, "reason=" + res).encode());
                        }

                        logger.log("CHAT_END", "user=" + currentUser + " res=" + res);

                    } catch (Exception e) {
                        out.println(new Message(Protocol.CHAT_END_FAIL, "reason=" + e.getMessage()).encode());
                    }
                    continue;
                }

                out.println(new Message(Protocol.LOGIN_FAIL, "reason=UnsupportedCommand").encode());
            }

        } catch (Exception e) {
            System.out.println("CLIENT_HANDLER_FATAL: " + e.getMessage());
        } finally {
            try {
                if (currentUser != null) authService.logout(currentUser);
            } catch (Exception ignored) { }
        }
    }

    private static String extract(String payload, String key) {
        if (payload == null) return null;

        String[] parts = payload.split(";");
        for (String p : parts) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && kv[0].trim().equals(key)) {
                return kv[1].trim();
            }
        }
        return null;
    }
}