package client;

import common.Message;
import common.Protocol;

import java.util.Scanner;

public class ClientMain {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try {
            ServerConnection conn = new ServerConnection("127.0.0.1", 5000);

            Message hello = conn.read();
            System.out.println("Server: " + (hello != null ? hello.encode() : "no response"));

            System.out.print("Username: ");
            String username = sc.nextLine().trim();

            System.out.print("Password: ");
            String password = sc.nextLine().trim();

            conn.send(new Message(Protocol.LOGIN, "username=" + username + ";password=" + password));
            Message loginResp = conn.read();
            System.out.println(loginResp != null ? loginResp.encode() : "no response");

            if (loginResp == null || loginResp.getType() != Protocol.LOGIN_OK) {
                conn.close();
                return;
            }

            final boolean[] running = {true};
            final String[] lastInv = {""};

            Thread watcher = new Thread(() -> {
                try {
                    while (running[0]) {
                        Thread.sleep(2000);

                        conn.send(new Message(Protocol.INVENTORY_LIST, ""));
                        Message r = conn.read();
                        if (r != null && r.getType() == Protocol.INVENTORY_LIST_OK) {
                            String now = r.getPayload();
                            if (!now.equals(lastInv[0])) {
                                lastInv[0] = now;
                                System.out.println("\nINV_UPDATE: " + r.encode());
                                System.out.print("> ");
                            }
                        }
                    }
                } catch (Exception ignored) { }
            });
            watcher.setDaemon(true);
            watcher.start();

            while (true) {
                System.out.println("Commands:");
                System.out.println("inv, buy, sell, cust_add, cust_list, purchase, emp_add, emp_list");
                System.out.println("report_branch, report_product, report_category, export_word");
                System.out.println("log_file, log_tail");
                System.out.println("chat_request, chat_poll, chat_send, chat_end, chat_join");
                System.out.println("logout");
                System.out.print("> ");
                String cmd = sc.nextLine().trim();

                if (cmd.equalsIgnoreCase("inv")) {
                    conn.send(new Message(Protocol.INVENTORY_LIST, ""));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("buy")) {
                    System.out.print("productId: ");
                    String pid = sc.nextLine().trim();
                    System.out.print("qty: ");
                    String qty = sc.nextLine().trim();

                    conn.send(new Message(Protocol.BUY, "productId=" + pid + ";qty=" + qty));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("sell")) {
                    System.out.print("productId: ");
                    String pid = sc.nextLine().trim();
                    System.out.print("qty: ");
                    String qty = sc.nextLine().trim();

                    conn.send(new Message(Protocol.SELL, "productId=" + pid + ";qty=" + qty));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("cust_add")) {
                    System.out.print("fullName: ");
                    String fullName = sc.nextLine().trim();
                    System.out.print("id: ");
                    String id = sc.nextLine().trim();
                    System.out.print("phone: ");
                    String phone = sc.nextLine().trim();
                    System.out.print("type (NEW/RETURNING/VIP): ");
                    String type = sc.nextLine().trim();

                    conn.send(new Message(Protocol.CUST_ADD,
                            "fullName=" + fullName + ";id=" + id + ";phone=" + phone + ";type=" + type));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("cust_list")) {
                    conn.send(new Message(Protocol.CUST_LIST, ""));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("purchase")) {
                    System.out.print("customerId: ");
                    String customerId = sc.nextLine().trim();
                    System.out.print("productId: ");
                    String pid = sc.nextLine().trim();
                    System.out.print("qty: ");
                    String qty = sc.nextLine().trim();

                    conn.send(new Message(Protocol.PURCHASE,
                            "customerId=" + customerId + ";productId=" + pid + ";qty=" + qty));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("emp_add")) {
                    System.out.print("username: ");
                    String u = sc.nextLine().trim();
                    System.out.print("password (>=6, letter+digit): ");
                    String p = sc.nextLine().trim();
                    System.out.print("role (ADMIN/CASHIER/SELLER/SHIFT_MANAGER): ");
                    String r = sc.nextLine().trim();
                    System.out.print("branchId: ");
                    String b = sc.nextLine().trim();

                    System.out.print("fullName: ");
                    String fullName = sc.nextLine().trim();
                    System.out.print("id: ");
                    String id = sc.nextLine().trim();
                    System.out.print("phone: ");
                    String phone = sc.nextLine().trim();
                    System.out.print("bankAccount: ");
                    String bank = sc.nextLine().trim();
                    System.out.print("employeeNumber: ");
                    String empNo = sc.nextLine().trim();

                    conn.send(new Message(Protocol.EMP_ADD,
                            "username=" + u + ";password=" + p + ";role=" + r + ";branchId=" + b +
                                    ";fullName=" + fullName + ";id=" + id + ";phone=" + phone +
                                    ";bankAccount=" + bank + ";employeeNumber=" + empNo
                    ));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("emp_list")) {
                    conn.send(new Message(Protocol.EMP_LIST, ""));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("report_branch")) {
                    System.out.print("branchId: ");
                    String b = sc.nextLine().trim();
                    conn.send(new Message(Protocol.REPORT_BRANCH_DAILY, "branchId=" + b));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("report_product")) {
                    System.out.print("productId: ");
                    String pid = sc.nextLine().trim();
                    conn.send(new Message(Protocol.REPORT_PRODUCT, "productId=" + pid));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("report_category")) {
                    System.out.print("category: ");
                    String c = sc.nextLine().trim();
                    conn.send(new Message(Protocol.REPORT_CATEGORY, "category=" + c));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("export_word")) {
                    System.out.println("Paste json (single line):");
                    String json = sc.nextLine().trim();
                    System.out.print("fileName (example: report.rtf): ");
                    String f = sc.nextLine().trim();
                    conn.send(new Message(Protocol.REPORT_EXPORT_WORD, "json=" + json + ";fileName=" + f));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("log_file")) {
                    conn.send(new Message(Protocol.LOG_LIST, ""));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("log_tail")) {
                    System.out.print("how many lines (default 20): ");
                    String n = sc.nextLine().trim();
                    String payload = n.isBlank() ? "" : ("n=" + n);
                    conn.send(new Message(Protocol.LOG_TAIL, payload));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("chat_request")) {
                    System.out.print("targetBranchId: ");
                    String target = sc.nextLine().trim();
                    conn.send(new Message(Protocol.CHAT_REQUEST, "targetBranchId=" + target));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("chat_poll")) {
                    conn.send(new Message(Protocol.CHAT_POLL, ""));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("chat_send")) {
                    System.out.print("text: ");
                    String text = sc.nextLine();
                    conn.send(new Message(Protocol.CHAT_SEND, "text=" + text));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("chat_join")) {
                    System.out.print("chatId: ");
                    String chatId = sc.nextLine().trim();
                    conn.send(new Message(Protocol.CHAT_JOIN, "chatId=" + chatId));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("chat_end")) {
                    conn.send(new Message(Protocol.CHAT_END, ""));
                    System.out.println(conn.read().encode());
                    continue;
                }

                if (cmd.equalsIgnoreCase("logout")) {
                    running[0] = false;
                    conn.send(new Message(Protocol.LOGOUT, ""));
                    System.out.println("Server: " + conn.read().encode());
                    conn.close();
                    break;
                }

                System.out.println("Unknown command");
            }

        } catch (Exception e) {
            System.out.println("CLIENT_ERROR: " + e.getMessage());
        }
    }
}
