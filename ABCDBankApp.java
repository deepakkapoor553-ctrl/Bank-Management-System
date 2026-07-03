import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.*;

// ═══════════════════════════════════════════════════════════
//  ABCD BANK  |  Premium Java Swing MIS  (Professional Theme)
//  LOGIN CREDENTIALS:
//    admin    / admin123      → ADMIN
//    manager  / manager123    → MANAGER
//    officer  / officer123    → OFFICER
//    clerk    / clerk123      → CLERK
//  Compile   : javac ABCDBankApp.java
//  Run       : java  ABCDBankApp
// ═══════════════════════════════════════════════════════════
public class ABCDBankApp {

    // ── Enhanced Professional Palette ────────────────────────
    static final Color C_NAVY      = new Color(0x07,0x0D,0x1A);
    static final Color C_DARK      = new Color(0x0A,0x12,0x22);
    static final Color C_CARD      = new Color(0x0F,0x1B,0x2E);
    static final Color C_PANEL     = new Color(0x14,0x24,0x3A);
    static final Color C_BORDER    = new Color(0x1E,0x35,0x52);
    static final Color C_BORDER2   = new Color(0x28,0x48,0x6E);

    // Accent Colors - Rich & Vibrant
    static final Color C_RED       = new Color(0xE5,0x32,0x35);
    static final Color C_RED_D     = new Color(0xC0,0x1C,0x1E);
    static final Color C_RED_L     = new Color(0xFF,0x6B,0x6B);
    static final Color C_GOLD      = new Color(0xF5,0xC5,0x42);
    static final Color C_GOLD_D    = new Color(0xD4,0xA0,0x17);
    static final Color C_CYAN      = new Color(0x00,0xD4,0xFF);
    static final Color C_CYAN_D    = new Color(0x00,0xA8,0xCC);
    static final Color C_PURPLE    = new Color(0x9B,0x59,0xF5);
    static final Color C_PURPLE_D  = new Color(0x7C,0x3A,0xED);
    static final Color C_TEAL      = new Color(0x00,0xE5,0xC3);
    static final Color C_ORANGE    = new Color(0xFF,0x7B,0x2C);

    // Text Colors
    static final Color C_TPRIM     = new Color(0xF0,0xF6,0xFF);
    static final Color C_TSEC      = new Color(0x8A,0xA8,0xC8);
    static final Color C_TMUT      = new Color(0x4A,0x6A,0x8A);

    // Status Colors
    static final Color C_GREEN     = new Color(0x2E,0xE8,0x9A);
    static final Color C_GREEN_D   = new Color(0x1A,0xC4,0x7A);
    static final Color C_AMBER     = new Color(0xFF,0xB8,0x30);
    static final Color C_BLUE      = new Color(0x3D,0xA5,0xFF);

    // Table
    static final Color C_ROWALT    = new Color(0x0C,0x18,0x28);
    static final Color C_SEL       = new Color(0x1A,0x35,0x58);

    // ── Fonts ────────────────────────────────────────────────
    static final Font FB  = new Font("SansSerif", Font.PLAIN,  13);
    static final Font FS  = new Font("SansSerif", Font.PLAIN,  11);
    static final Font FB2 = new Font("SansSerif", Font.BOLD,   12);

    // ── Data ─────────────────────────────────────────────────
    static List<Client>           clients  = new ArrayList<>();
    static List<BankAccount>      accounts = new ArrayList<>();
    static List<FixedDeposit>     fds      = new ArrayList<>();
    static List<RecurringDeposit> rds      = new ArrayList<>();
    static List<MutualFund>       mfs      = new ArrayList<>();
    static List<Loan>             loans    = new ArrayList<>();
    static int CC = 6, AC = 8;

    // ── App globals ──────────────────────────────────────────
    static JFrame     mainFrame;
    static JPanel     contentArea;
    static CardLayout contentLayout = new CardLayout();
    static List<JLabel> navItems    = new ArrayList<>();

    static final NumberFormat NF = NumberFormat.getNumberInstance(new Locale("en","IN"));
    static { NF.setMinimumFractionDigits(2); NF.setMaximumFractionDigits(2); }
    static String rs(double v){ return "\u20B9"+NF.format(v); }

    // ── Role System ──────────────────────────────────────────
    static String currentRole = "ADMIN";

    static final java.util.Map<String, String[]> STAFF_CREDENTIALS = new java.util.HashMap<String, String[]>(){{
        put("admin",   new String[]{"admin123",   "ADMIN"});
        put("manager", new String[]{"manager123", "MANAGER"});
        put("officer", new String[]{"officer123", "OFFICER"});
        put("clerk",   new String[]{"clerk123",   "CLERK"});
    }};

    static boolean canAccess(String section){
        switch(currentRole){
            case "ADMIN":   return true;
            case "MANAGER": return !section.equals("staff");
            case "OFFICER": return !section.equals("staff");
            case "CLERK":   return section.equals("dashboard")||section.equals("clients")||
                                   section.equals("accounts")||section.equals("transactions")||
                                   section.equals("fds")||section.equals("rds")||section.equals("mfs")||
                                   section.equals("loans")||section.equals("inbox")||section.equals("cards")||
                                   section.equals("daily");
            default: return false;
        }
    }
    static boolean canWrite(){ return !currentRole.equals("CLERK"); }
    static boolean canFreezeAccounts(){ return currentRole.equals("ADMIN")||currentRole.equals("MANAGER"); }
    static boolean canRegisterClient(){ return !currentRole.equals("CLERK"); }
    static boolean canEditDeleteClient(){ return !currentRole.equals("CLERK"); }
    static boolean canManageInvestments(){ return !currentRole.equals("CLERK"); }
    static boolean isAdmin(){ return currentRole.equals("ADMIN"); }

    static final Color[] ROLE_COLORS = {
        new Color(0xE5,0x32,0x35),
        new Color(0xF5,0xC5,0x42),
        new Color(0x3D,0xA5,0xFF),
        new Color(0x2E,0xE8,0x9A)
    };
    static final String[] ROLES = {"ADMIN","MANAGER","OFFICER","CLERK"};

    static JLabel sidebarRoleLabel;
    static JPanel sidebarPill;
    static JPanel sidebarRoleSwitcher;
    static boolean isAdminSession = false;

    // ════════════════════════════════════════════════════════
    //  DATABASE LAYER
    // ════════════════════════════════════════════════════════
    static final String DB_DIR = System.getProperty("user.home") + File.separator + "ABCDBankData";
    static final String F_CLIENTS = DB_DIR + File.separator + "clients.csv";
    static final String F_ACCS    = DB_DIR + File.separator + "accounts.csv";
    static final String F_TXNS    = DB_DIR + File.separator + "transactions.csv";
    static final String F_FDS     = DB_DIR + File.separator + "fds.csv";
    static final String F_RDS     = DB_DIR + File.separator + "rds.csv";
    static final String F_MFS     = DB_DIR + File.separator + "mfs.csv";
    static final String F_LOANS   = DB_DIR + File.separator + "loans.csv";
    static final String F_META    = DB_DIR + File.separator + "meta.csv";
    static final String F_MSGS    = DB_DIR + File.separator + "messages.csv";
    static final String F_ATMCARDS  = DB_DIR + File.separator + "atm_cards.csv";
    static final String F_CCARDS    = DB_DIR + File.separator + "credit_cards.csv";
    static final String F_CHEQUES   = DB_DIR + File.separator + "cheque_books.csv";
    static final String F_STAFF     = DB_DIR + File.separator + "staff.csv";

    static List<InternalMessage> messages = new ArrayList<>();
    static JLabel inboxBadge;

    // Card & Cheque Services data
    static List<ATMCard>      atmCards    = new ArrayList<>();
    static List<CreditCard>   creditCards = new ArrayList<>();
    static List<ChequeBook>   chequeBooks = new ArrayList<>();
    static int CARD_CTR = 1, CCARD_CTR = 1, CHQ_CTR = 1;
    static List<StaffMember> staffMembers = new ArrayList<>();
    static int STAFF_CTR = 10;
    static void saveDatabase() {
        try {
            new File(DB_DIR).mkdirs();
            writeCSV(F_META, List.of("CC,AC\n" + CC + "," + AC));
            StringBuilder sb = new StringBuilder();
            sb.append("id,name,phone,email,city,pan,aadhar,occupation,annualIncome,kyc,riskProfile,status,openedOn,accountIds\n");
            for (Client c : clients)
                sb.append(esc(c.id)).append(",").append(esc(c.name)).append(",").append(esc(c.phone))
                  .append(",").append(esc(c.email)).append(",").append(esc(c.city))
                  .append(",").append(esc(c.pan)).append(",").append(esc(c.aadhar))
                  .append(",").append(esc(c.occupation)).append(",").append(c.annualIncome)
                  .append(",").append(esc(c.kyc)).append(",").append(esc(c.riskProfile))
                  .append(",").append(esc(c.status)).append(",").append(esc(c.openedOn))
                  .append(",").append(esc(String.join(";", c.accountIds))).append("\n");
            writeRaw(F_CLIENTS, sb.toString());
            sb.setLength(0);
            sb.append("id,clientId,type,balance,minBal,rate,status,openedOn,nominee\n");
            for (BankAccount a : accounts)
                sb.append(esc(a.id)).append(",").append(esc(a.clientId)).append(",").append(esc(a.type))
                  .append(",").append(a.balance).append(",").append(a.minBal).append(",").append(a.rate)
                  .append(",").append(esc(a.status)).append(",").append(esc(a.openedOn))
                  .append(",").append(esc(a.nominee)).append("\n");
            writeRaw(F_ACCS, sb.toString());
            sb.setLength(0);
            sb.append("accountId,type,amount,desc,balAfter,dt\n");
            for (BankAccount a : accounts)
                for (Transaction t : a.transactions)
                    sb.append(esc(a.id)).append(",").append(esc(t.type)).append(",").append(t.amount)
                      .append(",").append(esc(t.desc)).append(",").append(t.balAfter)
                      .append(",").append(esc(t.dt)).append("\n");
            writeRaw(F_TXNS, sb.toString());
            sb.setLength(0);
            sb.append("id,clientId,linkedAccId,principal,rate,months,startDate,matDate,type,status\n");
            for (FixedDeposit f : fds)
                sb.append(esc(f.id)).append(",").append(esc(f.clientId)).append(",").append(esc(f.linkedAccId))
                  .append(",").append(f.principal).append(",").append(f.rate).append(",").append(f.months)
                  .append(",").append(esc(f.startDate)).append(",").append(esc(f.matDate))
                  .append(",").append(esc(f.type)).append(",").append(esc(f.status)).append("\n");
            writeRaw(F_FDS, sb.toString());
            sb.setLength(0);
            sb.append("id,clientId,linkedAccId,inst,rate,months,paid,totalDep,startDate,matDate,status,lastPayDate\n");
            for (RecurringDeposit r : rds)
                sb.append(esc(r.id)).append(",").append(esc(r.clientId)).append(",").append(esc(r.linkedAccId))
                  .append(",").append(r.inst).append(",").append(r.rate).append(",").append(r.months)
                  .append(",").append(r.paid).append(",").append(r.totalDep)
                  .append(",").append(esc(r.startDate)).append(",").append(esc(r.matDate))
                  .append(",").append(esc(r.status)).append(",").append(esc(r.lastPayDate==null?"":r.lastPayDate)).append("\n");
            writeRaw(F_RDS, sb.toString());
            sb.setLength(0);
            sb.append("id,clientId,name,cat,amc,invested,nav,units,curVal,isSIP,sipAmt,purchaseDate,status\n");
            for (MutualFund m : mfs)
                sb.append(esc(m.id)).append(",").append(esc(m.clientId)).append(",").append(esc(m.name))
                  .append(",").append(esc(m.cat)).append(",").append(esc(m.amc))
                  .append(",").append(m.invested).append(",").append(m.nav).append(",").append(m.units)
                  .append(",").append(m.curVal).append(",").append(m.isSIP ? "1":"0")
                  .append(",").append(m.sipAmt).append(",").append(esc(m.purchaseDate))
                  .append(",").append(esc(m.status)).append("\n");
            writeRaw(F_MFS, sb.toString());
            sb.setLength(0);
            sb.append("id,clientId,linkedAccId,type,principal,rate,tenureMonths,emiAmt,paidEmis,outstandingBal,disbursedDate,closureDate,status\n");
            for (Loan l : loans)
                sb.append(esc(l.id)).append(",").append(esc(l.clientId)).append(",").append(esc(l.linkedAccId))
                  .append(",").append(esc(l.type)).append(",").append(l.principal)
                  .append(",").append(l.rate).append(",").append(l.tenureMonths)
                  .append(",").append(l.emiAmt).append(",").append(l.paidEmis)
                  .append(",").append(l.outstandingBal).append(",").append(esc(l.disbursedDate))
                  .append(",").append(esc(l.closureDate)).append(",").append(esc(l.status)).append("\n");
            writeRaw(F_LOANS, sb.toString());
            sb.setLength(0);
            sb.append("id,fromRole,toRole,subject,body,timestamp,read\n");
            for(InternalMessage m : messages)
                sb.append(esc(m.id)).append(",").append(esc(m.fromRole)).append(",").append(esc(m.toRole))
                  .append(",").append(esc(m.subject)).append(",").append(esc(m.body))
                  .append(",").append(esc(m.timestamp)).append(",").append(m.read?"1":"0").append("\n");
            writeRaw(F_MSGS, sb.toString());
            // ATM Cards
            sb.setLength(0);
            sb.append("id,clientId,accountId,cardNumber,network,type,pin,issueDate,expiryDate,status,dailyLimit\n");
            for(ATMCard c: atmCards)
                sb.append(esc(c.id)).append(",").append(esc(c.clientId)).append(",").append(esc(c.accountId))
                  .append(",").append(esc(c.cardNumber)).append(",").append(esc(c.network))
                  .append(",").append(esc(c.type)).append(",").append(esc(c.pin))
                  .append(",").append(esc(c.issueDate)).append(",").append(esc(c.expiryDate))
                  .append(",").append(esc(c.status)).append(",").append(c.dailyLimit).append("\n");
            writeRaw(F_ATMCARDS, sb.toString());
            // Credit Cards
            sb.setLength(0);
            sb.append("id,clientId,accountId,cardNumber,network,cardType,issueDate,expiryDate,creditLimit,outstanding,minDue,dueDate,status\n");
            for(CreditCard c: creditCards)
                sb.append(esc(c.id)).append(",").append(esc(c.clientId)).append(",").append(esc(c.accountId))
                  .append(",").append(esc(c.cardNumber)).append(",").append(esc(c.network))
                  .append(",").append(esc(c.cardType)).append(",").append(esc(c.issueDate))
                  .append(",").append(esc(c.expiryDate)).append(",").append(c.creditLimit)
                  .append(",").append(c.outstanding).append(",").append(c.minDue)
                  .append(",").append(esc(c.dueDate)).append(",").append(esc(c.status)).append("\n");
            writeRaw(F_CCARDS, sb.toString());
            // Cheque Books
            sb.setLength(0);
            sb.append("id,clientId,accountId,bookNumber,leaves,startCheque,endCheque,issueDate,status,usedLeaves\n");
            for(ChequeBook c: chequeBooks)
                sb.append(esc(c.id)).append(",").append(esc(c.clientId)).append(",").append(esc(c.accountId))
                  .append(",").append(esc(c.bookNumber)).append(",").append(c.leaves)
                  .append(",").append(esc(c.startCheque)).append(",").append(esc(c.endCheque))
                  .append(",").append(esc(c.issueDate)).append(",").append(esc(c.status))
                  .append(",").append(c.usedLeaves).append("\n");
            writeRaw(F_CHEQUES, sb.toString());

            // Staff Members
            sb.setLength(0);
            sb.append("id,username,password,role,branch,fullName,email,phone,permissions,createdOn,status\n");
            for(StaffMember s: staffMembers)
                sb.append(esc(s.id)).append(",").append(esc(s.username)).append(",")
                .append(esc(s.password)).append(",").append(esc(s.role)).append(",")
                .append(esc(s.branch)).append(",").append(esc(s.fullName)).append(",")
                .append(esc(s.email)).append(",").append(esc(s.phone)).append(",")
                .append(esc(String.join(";",s.permissions))).append(",")
                .append(esc(s.createdOn)).append(",").append(esc(s.status)).append("\n");
            writeRaw(F_STAFF, sb.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            toast("⚠ Auto-Save Error: " + ex.getMessage());
        }
    }

    static boolean loadDatabase() {
        File dir = new File(DB_DIR);
        if (!dir.exists() || !new File(F_CLIENTS).exists()) return false;
        try {
            List<String[]> meta = readCSV(F_META);
            if (!meta.isEmpty()) { CC = Integer.parseInt(meta.get(0)[0]); AC = Integer.parseInt(meta.get(0)[1]); }
            clients.clear();
            for (String[] r : readCSV(F_CLIENTS)) {
                if (r.length < 14) continue;
                Client c = new Client(r[0],r[1],r[2],r[3],r[4],r[5],r[6],r[7],
                    parseDbl(r[8]),r[9],r[10],r[12]);
                c.status = r[11];
                if (r[13] != null && !r[13].isEmpty())
                    for (String ai : r[13].split(";")) if (!ai.isEmpty()) c.accountIds.add(ai);
                clients.add(c);
            }
            accounts.clear();
            for (String[] r : readCSV(F_ACCS)) {
                if (r.length < 9) continue;
                BankAccount a = new BankAccount(r[0],r[1],r[2],parseDbl(r[3]),parseDbl(r[4]),parseDbl(r[5]),r[7]);
                a.status  = r[6];
                a.nominee = r[8];
                accounts.add(a);
            }
            for (String[] r : readCSV(F_TXNS)) {
                if (r.length < 6) continue;
                BankAccount a = findA(r[0]); if (a == null) continue;
                a.transactions.add(new Transaction(r[1], parseDbl(r[2]), r[3], parseDbl(r[4]), r[5]));
            }
            fds.clear();
            for (String[] r : readCSV(F_FDS)) {
                if (r.length < 10) continue;
                FixedDeposit f = new FixedDeposit(r[0],r[1],r[2],parseDbl(r[3]),parseDbl(r[4]),
                    (int)parseDbl(r[5]),r[6],r[7],r[8]);
                f.status = r[9];
                fds.add(f);
            }
            rds.clear();
            for (String[] r : readCSV(F_RDS)) {
                if (r.length < 11) continue;
                RecurringDeposit rd = new RecurringDeposit(r[0],r[1],r[2],parseDbl(r[3]),parseDbl(r[4]),
                    (int)parseDbl(r[5]),r[8],r[9]);
                rd.paid = (int)parseDbl(r[6]); rd.totalDep = parseDbl(r[7]); rd.status = r[10];
                rd.lastPayDate = (r.length > 11 && r[11] != null) ? r[11] : "";
                rds.add(rd);
            }
            mfs.clear();
            for (String[] r : readCSV(F_MFS)) {
                if (r.length < 13) continue;
                MutualFund m = new MutualFund(r[0],r[1],r[2],r[3],r[4],
                    parseDbl(r[5]),parseDbl(r[6]),parseDbl(r[7]),r[11]);
                m.curVal = parseDbl(r[8]); m.isSIP = "1".equals(r[9]);
                m.sipAmt = parseDbl(r[10]); m.status = r[12];
                mfs.add(m);
            }
            loans.clear();
            if (new File(F_LOANS).exists()) {
                for (String[] r : readCSV(F_LOANS)) {
                    if (r.length < 13) continue;
                    Loan l = new Loan(r[0],r[1],r[2],r[3],parseDbl(r[4]),parseDbl(r[5]),
                        (int)parseDbl(r[6]),r[10]);
                    l.emiAmt         = parseDbl(r[7]);
                    l.paidEmis       = (int)parseDbl(r[8]);
                    l.outstandingBal = parseDbl(r[9]);
                    l.closureDate    = r[11];
                    l.status         = r[12];
                    loans.add(l);
                }
            }
            loadMessages();
            // ATM Cards
            atmCards.clear();
            if(new File(F_ATMCARDS).exists()){
                for(String[] r: readCSV(F_ATMCARDS)){
                    if(r.length<11) continue;
                    ATMCard c=new ATMCard(r[0],r[1],r[2],r[3],r[4],r[5],r[6],r[7],r[8]);
                    c.status=r[9]; c.dailyLimit=parseDbl(r[10]); atmCards.add(c);
                }
            }
            // Credit Cards
            creditCards.clear();
            if(new File(F_CCARDS).exists()){
                for(String[] r: readCSV(F_CCARDS)){
                    if(r.length<13) continue;
                    CreditCard c=new CreditCard(r[0],r[1],r[2],r[3],r[4],r[5],r[6],r[7],parseDbl(r[8]));
                    c.outstanding=parseDbl(r[9]); c.minDue=parseDbl(r[10]); c.dueDate=r[11]; c.status=r[12]; creditCards.add(c);
                }
            }
            // Cheque Books
            chequeBooks.clear();
            if(new File(F_CHEQUES).exists()){
                for(String[] r: readCSV(F_CHEQUES)){
                    if(r.length<10) continue;
                    ChequeBook c=new ChequeBook(r[0],r[1],r[2],r[3],(int)parseDbl(r[4]),r[5],r[6],r[7]);
                    c.status=r[8]; c.usedLeaves=(int)parseDbl(r[9]); chequeBooks.add(c);
                }
            }

            // Staff Members
            staffMembers.clear();
            if(new File(F_STAFF).exists()){
                for(String[] r: readCSV(F_STAFF)){
                    if(r.length<11) continue;
                    StaffMember s=new StaffMember(r[0],r[1],r[2],r[3],r[4],r[5],r[6],r[7]);
                    if(r[8]!=null&&!r[8].isEmpty())
                        for(String perm:r[8].split(";"))
                            if(!perm.isEmpty()) s.permissions.add(perm);
                    s.createdOn=r[9]; s.status=r[10];
                    staffMembers.add(s);
                }
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    static void loadMessages(){
        messages.clear();
        try {
            if(!new File(F_MSGS).exists()) return;
            for(String[] r : readCSV(F_MSGS)){
                if(r.length<7) continue;
                InternalMessage m=new InternalMessage(r[0],r[1],r[2],r[3],r[4],r[5]);
                m.read="1".equals(r[6]);
                messages.add(m);
            }
        } catch(Exception ignored){}
    }

    static String esc(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    static void writeRaw(String path, String data) throws IOException { Files.writeString(Path.of(path), data); }
    static void writeCSV(String path, List<String> lines) throws IOException { Files.writeString(Path.of(path), String.join("\n", lines)); }
    static List<String[]> readCSV(String path) throws IOException {
        List<String[]> result = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(path));
        if (lines.size() <= 1) return result;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            result.add(parseCSVLine(line));
        }
        return result;
    }
    static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQ = false; StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQ && i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                else inQ = !inQ;
            } else if (c == ',' && !inQ) { fields.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        fields.add(cur.toString());
        return fields.toArray(new String[0]);
    }

    static void copyToClipboard(String text) {
        java.awt.datatransfer.StringSelection ss = new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
    }

    static String buildClientCopyText(Client c) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════\n");
        sb.append("  ABCD BANK  ─  CLIENT INFORMATION\n");
        sb.append("═══════════════════════════════════\n");
        sb.append("Client ID      : ").append(c.id).append("\n");
        sb.append("Full Name      : ").append(c.name).append("\n");
        sb.append("Phone          : ").append(c.phone).append("\n");
        sb.append("Email          : ").append(c.email).append("\n");
        sb.append("City           : ").append(c.city).append("\n");
        sb.append("PAN            : ").append(c.pan).append("\n");
        sb.append("Aadhar         : ").append(c.aadhar).append("\n");
        sb.append("Occupation     : ").append(c.occupation).append("\n");
        sb.append("Annual Income  : ").append(rs(c.annualIncome)).append("\n");
        sb.append("KYC Status     : ").append(c.kyc).append("\n");
        sb.append("Risk Profile   : ").append(c.riskProfile).append("\n");
        sb.append("Member Since   : ").append(c.openedOn).append("\n");
        sb.append("Accounts       : ").append(String.join(", ", c.accountIds)).append("\n");
        sb.append("═══════════════════════════════════\n");
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════
    //  MAIN
    // ════════════════════════════════════════════════════════
    public static void main(String[] a){
        setupLAF();
        SwingUtilities.invokeLater(ABCDBankApp::showSplash);
    }

    static void setupLAF(){
        try{ UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }catch(Exception ignored){}
        UIManager.put("Button.focus",                new Color(0,0,0,0));
        UIManager.put("TextField.caretForeground",   C_TPRIM);
        UIManager.put("ComboBox.background",         C_PANEL);
        UIManager.put("ComboBox.foreground",         C_TPRIM);
        UIManager.put("ComboBox.selectionBackground",C_RED);
        UIManager.put("ComboBox.selectionForeground",Color.WHITE);
        UIManager.put("List.background",             C_PANEL);
        UIManager.put("List.foreground",             C_TPRIM);
        UIManager.put("ScrollBar.background",        C_CARD);
        UIManager.put("ScrollBar.thumb",             C_BORDER2);
        UIManager.put("ScrollBar.track",             C_CARD);
        UIManager.put("ScrollBar.width",             8);
        UIManager.put("Table.background",            C_CARD);
        UIManager.put("Table.foreground",            C_TPRIM);
        UIManager.put("Table.gridColor",             C_BORDER);
        UIManager.put("Table.selectionBackground",   C_SEL);
        UIManager.put("Table.selectionForeground",   C_TPRIM);
        UIManager.put("TableHeader.background",      C_PANEL);
        UIManager.put("TableHeader.foreground",      C_TMUT);
        UIManager.put("OptionPane.background",       C_CARD);
        UIManager.put("OptionPane.messageForeground",C_TPRIM);
        UIManager.put("Panel.background",            C_CARD);
    }

    // ════════════════════════════════════════════════════════
    //  SPLASH  (Enhanced)
    // ════════════════════════════════════════════════════════
    static void showSplash(){
        JWindow w = new JWindow(); w.setSize(580,400); w.setLocationRelativeTo(null);
        JPanel p = new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Deep space gradient background
                g2.setPaint(new GradientPaint(0,0,C_DARK,0,getHeight(),new Color(0x05,0x0E,0x1C)));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
                // Radial glow top-right
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(480,0),320,
                    new float[]{0f,1f},new Color[]{new Color(0xE5,0x32,0x35,60),new Color(0,0,0,0)}));
                g2.fillRect(0,0,getWidth(),getHeight());
                // Radial glow bottom-left
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(80,getHeight()),240,
                    new float[]{0f,1f},new Color[]{new Color(0x00,0xD4,0xFF,40),new Color(0,0,0,0)}));
                g2.fillRect(0,0,getWidth(),getHeight());
                // Grid dots decoration
                g2.setColor(new Color(0xFF,0xFF,0xFF,8));
                for(int x=20;x<getWidth();x+=30) for(int y=20;y<getHeight();y+=30) g2.fillOval(x,y,2,2);
                // Top gradient bar
                g2.setPaint(new GradientPaint(0,0,C_RED_D,290,0,C_GOLD));
                g2.fillRect(0,0,getWidth(),4);
                // Bottom accent line
                g2.setPaint(new GradientPaint(0,0,new Color(0x00,0xD4,0xFF,120),getWidth(),0,new Color(0x9B,0x59,0xF5,120)));
                g2.fillRect(0,getHeight()-2,getWidth(),2);
            }
        };
        p.setBackground(C_DARK);

        // Animated logo
        JLabel logo=L("ABCD Bank",new Font("Serif",Font.BOLD,52),C_TPRIM);
        logo.setBounds(0,70,580,68); logo.setHorizontalAlignment(SwingConstants.CENTER); p.add(logo);

        JLabel tag=L("MANAGEMENT INFORMATION SYSTEM",new Font("SansSerif",Font.BOLD,11),C_GOLD);
        tag.setBounds(0,148,580,20); tag.setHorizontalAlignment(SwingConstants.CENTER); p.add(tag);

        // Decorative divider
        JPanel div=new JPanel(){@Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setPaint(new GradientPaint(0,0,new Color(0,0,0,0),140,0,C_CYAN));
            g2.fillRect(0,0,140,1);
            g2.setPaint(new GradientPaint(0,0,C_CYAN,80,0,new Color(0,0,0,0)));
            g2.fillRect(140,0,80,1);
        }};
        div.setOpaque(false); div.setBounds(170,178,240,1); p.add(div);

        JProgressBar bar=new JProgressBar(0,100); bar.setBounds(160,210,260,6);
        bar.setBorderPainted(false); bar.setBackground(C_BORDER); bar.setForeground(C_CYAN); p.add(bar);

        JLabel status=L("Initialising\u2026",FS,C_TMUT);
        status.setBounds(0,226,580,18); status.setHorizontalAlignment(SwingConstants.CENTER); p.add(status);

        JLabel dbLbl=L("",FS,C_GREEN);
        dbLbl.setBounds(0,252,580,18); dbLbl.setHorizontalAlignment(SwingConstants.CENTER); p.add(dbLbl);

        // Feature pills
        String[] pills={"Role-Based Access","File Database","Internal Messaging","Loan Management"};
        Color[] pillC={C_RED,C_GOLD,C_CYAN,C_GREEN};
        int px=30;
        for(int i=0;i<pills.length;i++){
            final Color pc=pillC[i];
            JLabel pill=new JLabel(pills[i]){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(pc.getRed(),pc.getGreen(),pc.getBlue(),28));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    g2.setColor(new Color(pc.getRed(),pc.getGreen(),pc.getBlue(),80));
                    g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                    super.paintComponent(g);
                }
            };
            pill.setFont(new Font("SansSerif",Font.BOLD,9)); pill.setForeground(pc); pill.setOpaque(false);
            pill.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
            FontMetrics fm=pill.getFontMetrics(pill.getFont());
            int pw=fm.stringWidth(pills[i])+24;
            pill.setBounds(px,292,pw,22); p.add(pill); px+=pw+8;
        }

         JLabel copy=L("Established 1985  \u00B7  Trusted Since Forever  \u00B7  v3.2",FS,C_TMUT);
        copy.setBounds(0,360,580,18); copy.setHorizontalAlignment(SwingConstants.CENTER); p.add(copy);
        JLabel amrit=L("made by amrit",new Font("SansSerif",Font.PLAIN,9),C_TMUT);
        amrit.setBounds(0,376,580,14); amrit.setHorizontalAlignment(SwingConstants.CENTER); p.add(amrit);

        w.setContentPane(p);
        try{ w.setShape(new RoundRectangle2D.Double(0,0,580,400,24,24)); }catch(Exception ignored){}
        w.setVisible(true);
        String[] msgs={"Initialising core modules\u2026","Connecting database\u2026","Loading security layer\u2026",
            "Fetching client data\u2026","Syncing accounts\u2026","Loading FDs & RDs\u2026",
            "MF portfolio sync\u2026","Loading loan records\u2026","Rendering UI\u2026"};
        int[] step={0};
        javax.swing.Timer t=new javax.swing.Timer(280,null);
        t.addActionListener(e->{
            if(step[0]<msgs.length){
                status.setText(msgs[step[0]]); bar.setValue((step[0]+1)*100/msgs.length);
                bar.setForeground(step[0]%3==0?C_CYAN:step[0]%3==1?C_GOLD:C_GREEN);
                if(step[0]==1){
                    boolean loaded=loadDatabase();
                    if(loaded){ dbLbl.setText("\u2714  Database loaded from "+DB_DIR); }
                    else       { seedData(); dbLbl.setText("\u2714  Fresh database \u2014 seed data loaded"); }
                }
                step[0]++;
            } else { t.stop(); w.dispose(); showLogin(); }
        });
        t.start();
    }

    // ════════════════════════════════════════════════════════
    //  LOGIN  (Centered on maximize)
    // ════════════════════════════════════════════════════════
    static void showLogin(){
        JFrame f=new JFrame("ABCD Bank \u2014 Staff Login");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.setSize(520,640);
        f.setMinimumSize(new Dimension(420,560));
        f.setLocationRelativeTo(null); f.setResizable(true);

        // Root uses GridBagLayout so card stays centered on maximize
        JPanel root=new JPanel(new GridBagLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Deep background
                g2.setColor(C_DARK); g2.fillRect(0,0,getWidth(),getHeight());
                // Radial glow top-right corner
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(getWidth(),0),Math.max(getWidth(),getHeight())*0.6f,
                    new float[]{0f,1f},new Color[]{new Color(0xE5,0x32,0x35,45),new Color(0,0,0,0)}));
                g2.fillRect(0,0,getWidth(),getHeight());
                // Radial glow bottom-left corner
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(0,getHeight()),Math.max(getWidth(),getHeight())*0.5f,
                    new float[]{0f,1f},new Color[]{new Color(0x00,0xD4,0xFF,35),new Color(0,0,0,0)}));
                g2.fillRect(0,0,getWidth(),getHeight());
                // Dot grid
                g2.setColor(new Color(0xFF,0xFF,0xFF,6));
                for(int x=0;x<getWidth();x+=32) for(int y=0;y<getHeight();y+=32) g2.fillOval(x,y,2,2);
            }
        };
        root.setBackground(C_DARK);

        // ── Login Card ──────────────────────────────────────
        JPanel card=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Card background
                g2.setColor(C_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),22,22);
                // Subtle inner glow
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(getWidth()/2f,0),getWidth()*0.8f,
                    new float[]{0f,1f},new Color[]{new Color(0xE5,0x32,0x35,18),new Color(0,0,0,0)}));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),22,22);
                // Border
                g2.setColor(C_BORDER2); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,22,22);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(400,560));
        card.setMinimumSize(new Dimension(400,560));
        card.setMaximumSize(new Dimension(400,560));

        // Top gradient bar
        JPanel topBar=new JPanel(){@Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setPaint(new GradientPaint(0,0,C_RED_D,200,0,C_GOLD,true));
            g2.fillRoundRect(0,0,getWidth(),4,4,4);}};
        topBar.setOpaque(false); topBar.setBounds(0,0,400,4); card.add(topBar);

        // Bank name + icon
        JPanel brandIcon=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,C_RED_D,44,44,C_RED));
                g2.fillOval(0,0,44,44);
                g2.setColor(new Color(255,255,255,200));
                g2.setFont(new Font("Serif",Font.BOLD,18));
                g2.drawString("AB",9,29);
            }
            @Override public Dimension getPreferredSize(){ return new Dimension(44,44); }
        };
        brandIcon.setOpaque(false); brandIcon.setBounds(178,28,44,44); card.add(brandIcon);

        JLabel brand=L("ABCD Bank",new Font("Serif",Font.BOLD,34),C_TPRIM);
        brand.setBounds(0,80,400,46); brand.setHorizontalAlignment(SwingConstants.CENTER); card.add(brand);

        JLabel sub=L("STAFF PORTAL",new Font("SansSerif",Font.BOLD,10),C_GOLD);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        sub.setBorder(BorderFactory.createEmptyBorder(3,14,3,14));
        // Draw badge manually
        JPanel subBadge=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF5,0xC5,0x42,28)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(0xF5,0xC5,0x42,80)); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
            }
        };
        subBadge.setOpaque(false); sub.setBounds(0,0,100,24); subBadge.add(sub);
        subBadge.setBounds(150,130,100,24); card.add(subBadge);

        // Quick credentials panel
        JPanel hintPanel=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_PANEL); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(C_BORDER2); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
            }
        };
        hintPanel.setOpaque(false); hintPanel.setBounds(24,162,352,116);

        JLabel quickFill=L("QUICK LOGIN \u2014 click any row",new Font("SansSerif",Font.BOLD,8),C_TMUT);
        quickFill.setBounds(10,6,240,12); hintPanel.add(quickFill);

        String[][] creds = {
            {"\uD83D\uDC51","admin",   "admin123",   "ADMIN"},
            {"\uD83D\uDCBC","manager", "manager123", "MANAGER"},
            {"\uD83D\uDC64","officer", "officer123", "OFFICER"},
            {"\uD83D\uDCCB","clerk",   "clerk123",   "CLERK"}
        };
        Font hfb = new Font("SansSerif",Font.BOLD,10);
        Font hf  = new Font("SansSerif",Font.PLAIN,10);
        int hy = 22;
        for(int i=0;i<creds.length;i++){
            final String u=creds[i][1], pw2=creds[i][2];
            final Color rc=ROLE_COLORS[i];
            final int rowY=hy; final int rowH=22;

            JLabel ico=new JLabel(creds[i][0]); ico.setFont(new Font("SansSerif",Font.PLAIN,11));
            ico.setBounds(8,hy+2,18,18); hintPanel.add(ico);
            JLabel uL=new JLabel(creds[i][1]); uL.setFont(hfb); uL.setForeground(C_TPRIM);
            uL.setBounds(28,hy+2,60,18); hintPanel.add(uL);
            JLabel sep1=new JLabel("/"); sep1.setFont(hf); sep1.setForeground(C_TMUT);
            sep1.setBounds(88,hy+2,10,18); hintPanel.add(sep1);
            JLabel pw=new JLabel(creds[i][2]); pw.setFont(hf); pw.setForeground(C_TSEC);
            pw.setBounds(98,hy+2,80,18); hintPanel.add(pw);

            JLabel roleL=new JLabel(creds[i][3]){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),40));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                    super.paintComponent(g);
                }
            };
            roleL.setFont(hfb); roleL.setForeground(rc); roleL.setOpaque(false);
            roleL.setBorder(BorderFactory.createEmptyBorder(1,6,1,6));
            roleL.setBounds(186,hy+3,72,16); hintPanel.add(roleL);
            hy+=24;
        }
        card.add(hintPanel);

        // Row click fill
        int[] qy2={22};
        for(String[] cr : creds){
            final String u=cr[1], pw2=cr[2]; final int rowY=qy2[0]; final int rowH=22;
            hintPanel.addMouseListener(new MouseAdapter(){
                JTextField userF2,passF2;
                public void mouseClicked(MouseEvent e){
                    if(e.getY()>=rowY && e.getY()<rowY+rowH){
                        // will set via closure below
                    }
                }
            });
            qy2[0]+=24;
        }
        hintPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Username
        JLabel uLbl=L("USERNAME",new Font("SansSerif",Font.BOLD,9),new Color(0x8A,0xA8,0xC8));
        uLbl.setBounds(24,288,352,14); card.add(uLbl);
        JTextField uF=mkField("Enter username"); uF.setBounds(24,304,352,42); card.add(uF);

        // Password
        JLabel pLbl=L("PASSWORD",new Font("SansSerif",Font.BOLD,9),new Color(0x8A,0xA8,0xC8));
        pLbl.setBounds(24,358,352,14); card.add(pLbl);
        JPasswordField pF=new JPasswordField(); styleField(pF,"Enter password"); pF.setBounds(24,374,352,42); card.add(pF);

        JLabel err=L("",FS,C_RED_L); err.setBounds(24,424,352,18); err.setHorizontalAlignment(SwingConstants.CENTER); card.add(err);

        // Colorful Sign In button
        JButton btn=new JButton("Sign In to Portal"){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h=getModel().isRollover();
                if(h){
                    g2.setPaint(new GradientPaint(0,0,new Color(0xFF,0x60,0x20),getWidth(),getHeight(),new Color(0xC0,0x1C,0x1E)));
                } else {
                    g2.setPaint(new GradientPaint(0,0,C_RED_D,getWidth(),getHeight(),C_RED));
                }
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                // Shine overlay
                g2.setPaint(new GradientPaint(0,0,new Color(255,255,255,40),0,getHeight()/2,new Color(255,255,255,0)));
                g2.fillRoundRect(0,0,getWidth(),getHeight()/2,12,12);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif",Font.BOLD,14)); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBounds(24,450,352,48); card.add(btn);

        JLabel ver=L("v3.2  \u00B7  \u00A9 2025 ABCD Bank  |  All Rights Reserved",new Font("SansSerif",Font.PLAIN,9),C_TMUT);
        ver.setBounds(0,510,400,14); ver.setHorizontalAlignment(SwingConstants.CENTER); card.add(ver);

        // Bottom accent bar
        JPanel botBar=new JPanel(){@Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setPaint(new GradientPaint(0,0,new Color(0x00,0xD4,0xFF,100),200,0,new Color(0x9B,0x59,0xF5,100),true));
            g2.fillRoundRect(0,0,getWidth(),3,3,3);}};
        botBar.setOpaque(false); botBar.setBounds(0,557,400,3); card.add(botBar);

        // Quick-fill mouse handler (directly on hintPanel, using field references)
        int[] qy3={22};
        for(String[] cr : creds){
            final String u=cr[1], pw2=cr[2]; final int rowY=qy3[0]; final int rowH=22;
            hintPanel.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    if(e.getY()>=rowY && e.getY()<rowY+rowH){ uF.setText(u); pF.setText(pw2); }
                }
            });
            qy3[0]+=24;
        }

        // GridBagConstraints: centered at all sizes
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0; gbc.gridy=0; gbc.anchor=GridBagConstraints.CENTER;
        root.add(card, gbc);
        f.setContentPane(root);

        Runnable go=()->{
            String user = uF.getText().trim().toLowerCase();
            String pass = new String(pF.getPassword());
            String[] cred = STAFF_CREDENTIALS.get(user);
            if(cred != null && cred[0].equals(pass)){
                currentRole = cred[1];
                isAdminSession = "admin".equals(user);
                f.dispose();
                showMainApp();
            } else {
                err.setText("\u2717  Incorrect username or password");
                pF.setText(""); uF.requestFocus();
            }
        };
        btn.addActionListener(e->go.run());
        pF.addActionListener(e->go.run());
        uF.addActionListener(e->pF.requestFocus());
        f.setVisible(true);
    }

    // ════════════════════════════════════════════════════════
    //  MAIN APP
    // ════════════════════════════════════════════════════════
    static void showMainApp(){
        navItems.clear();
        mainFrame=new JFrame("ABCD Bank \u2014 Management System  ["+currentRole+"]");
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){ saveDatabase(); mainFrame.dispose(); System.exit(0); }
        });
        mainFrame.setSize(1380,900); mainFrame.setMinimumSize(new Dimension(1100,720));
        mainFrame.setLocationRelativeTo(null);
        JPanel root=new JPanel(new BorderLayout()); root.setBackground(C_NAVY);
        JPanel sidebarPanel = buildSidebar();
        JScrollPane sidebarScroll = new JScrollPane(sidebarPanel);
        sidebarScroll.setBorder(BorderFactory.createEmptyBorder());
        sidebarScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebarScroll.getViewport().setBackground(C_CARD);
        sidebarScroll.getVerticalScrollBar().setBackground(C_CARD);
        sidebarScroll.getVerticalScrollBar().setForeground(C_BORDER2);
        sidebarScroll.setPreferredSize(new Dimension(262,0));
        root.add(sidebarScroll,BorderLayout.WEST);
        contentArea=new JPanel(contentLayout); contentArea.setBackground(C_NAVY);
        contentArea.add(buildDashboard(),    "dashboard");
        contentArea.add(buildClients(),      "clients");
        contentArea.add(buildAccounts(),     "accounts");
        contentArea.add(buildTransactions(), "transactions");
        contentArea.add(buildFDs(),          "fds");
        contentArea.add(buildRDs(),          "rds");
        contentArea.add(buildMFs(),          "mfs");
        contentArea.add(buildLoans(),        "loans");
        contentArea.add(buildCards(),        "cards");
        contentArea.add(buildDailySummary(), "daily");
        contentArea.add(buildRegister(),     "register");
        contentArea.add(buildStaff(),        "staff");
        contentArea.add(buildInbox(),        "inbox");
        JScrollPane sp=new JScrollPane(contentArea);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(C_NAVY);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(sp,BorderLayout.CENTER);
        
        mainFrame.setContentPane(root); mainFrame.setVisible(true);
        // ── Transparent brand overlay on glass pane ──────────
        mainFrame.setGlassPane(new JComponent(){
            @Override protected void paintComponent(Graphics g){}
            @Override public boolean isOpaque(){ return false; }
        });
        JComponent glass = (JComponent) mainFrame.getGlassPane();
        glass.setVisible(true);
        glass.setOpaque(false);
        glass.setBackground(new Color(0,0,0,0));
        glass.setLayout(null);
        JLabel brand = new JLabel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(new Font("SansSerif",Font.PLAIN,8));
                g2.setColor(new Color(0x4A,0x6A,0x8A,120));
                g2.drawString("developed by",0,10);
                g2.setFont(new Font("SansSerif",Font.BOLD,10));
                g2.setColor(new Color(0x3D,0xA5,0xFF,150));
                g2.drawString("AMRIT",0,22);
               
            }
        };
        brand.setOpaque(false);
        brand.setBackground(new Color(0,0,0,0));
        glass.add(brand);
        glass.addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e){
                brand.setBounds(glass.getWidth()-130, glass.getHeight()-30, 120, 28);
            }
        });
        brand.setBounds(mainFrame.getWidth()-130, mainFrame.getHeight()-30, 120, 28);
        showSection("dashboard",0);
        refreshInboxBadge();
    }

    // ════════════════════════════════════════════════════════
    //  SIDEBAR  (Enhanced with colorful nav items)
    // ════════════════════════════════════════════════════════
    static JPanel buildSidebar(){
        JPanel sb=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,new Color(0x0A,0x14,0x24),getWidth(),getHeight(),new Color(0x0F,0x1A,0x2C)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        sb.setBackground(C_CARD);
        sb.setPreferredSize(new Dimension(262,960));
        sb.setBorder(BorderFactory.createMatteBorder(0,0,0,1,C_BORDER));

        // ── Brand ──────────────────────────────────────────
        JPanel brand=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setColor(new Color(0x0A,0x14,0x24)); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setPaint(new GradientPaint(0,getHeight()-1,C_RED_D,140,getHeight()-1,C_GOLD));
                g2.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        brand.setBackground(C_CARD); brand.setBounds(0,0,262,74);

        JPanel topAccent=new JPanel(){@Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setPaint(new GradientPaint(0,0,C_RED_D,130,0,C_GOLD));
            g2.fillRect(0,0,getWidth(),3);}};
        topAccent.setOpaque(false); topAccent.setBounds(0,0,262,3); brand.add(topAccent);

        // Logo circle
        JPanel logoCirc=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,C_RED_D,36,36,C_RED));
                g2.fillOval(0,0,36,36);
                g2.setColor(new Color(255,255,255,200));
                g2.setFont(new Font("Serif",Font.BOLD,14)); g2.drawString("AB",6,24);
            }
        };
        logoCirc.setOpaque(false); logoCirc.setBounds(14,18,36,36); brand.add(logoCirc);

        JLabel bn=L("ABCD Bank",new Font("Serif",Font.BOLD,20),C_TPRIM); bn.setBounds(58,16,194,26); brand.add(bn);
        JLabel bs=L("MANAGEMENT SYSTEM",new Font("SansSerif",Font.BOLD,8),C_GOLD); bs.setBounds(58,44,194,14); brand.add(bs);
        sb.add(brand);

        // ── Role Pill ──────────────────────────────────────
        sidebarPill=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color rc=roleColor();
                g2.setColor(new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),18));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),60));
                g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
            }
        };
        sidebarPill.setOpaque(false); sidebarPill.setBounds(10,84,242,54);

        JPanel av=new JPanel(){@Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Color rc=roleColor();
            g2.setPaint(new GradientPaint(0,0,rc.darker(),32,32,rc)); g2.fillOval(0,0,32,32);
            g2.setColor(Color.WHITE); g2.setFont(new Font("SansSerif",Font.BOLD,10));
            String ini=currentRole.substring(0,Math.min(2,currentRole.length()));
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(ini,(32-fm.stringWidth(ini))/2,(32+fm.getAscent()-fm.getDescent())/2-1);}};
        av.setOpaque(false); av.setBounds(10,11,32,32); sidebarPill.add(av);

        JLabel uN=L("ABCD Bank Staff",new Font("SansSerif",Font.BOLD,11),C_TPRIM); uN.setBounds(52,10,178,18); sidebarPill.add(uN);
        sidebarRoleLabel=L(currentRole,new Font("SansSerif",Font.BOLD,9),roleColor()); sidebarRoleLabel.setBounds(52,28,178,14); sidebarPill.add(sidebarRoleLabel);
        sb.add(sidebarPill);

        // ── Role Switcher ──────────────────────────────────
        sidebarRoleSwitcher = new JPanel(null);
        sidebarRoleSwitcher.setOpaque(false);
        sidebarRoleSwitcher.setBounds(10,146,242,0);
        if(isAdminSession){
            sidebarRoleSwitcher.setBounds(10,146,242,60);
            JLabel swTitle=L("SWITCH VIEW AS",new Font("SansSerif",Font.BOLD,8),C_TMUT);
            swTitle.setBounds(6,2,230,12); sidebarRoleSwitcher.add(swTitle);
            int bx=0;
            for(int i=0;i<ROLES.length;i++){
                final String role=ROLES[i]; final Color rc=ROLE_COLORS[i]; final int fi=i;
                JLabel btn=new JLabel(role){
                    @Override protected void paintComponent(Graphics g){
                        Graphics2D g2=(Graphics2D)g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                        boolean active=currentRole.equals(role);
                        g2.setColor(active?new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),55):new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),18));
                        g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                        if(active){g2.setColor(rc);g2.setStroke(new BasicStroke(1.5f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);}
                        super.paintComponent(g);
                    }
                };
                btn.setFont(new Font("SansSerif",Font.BOLD,9)); btn.setForeground(rc);
                btn.setHorizontalAlignment(SwingConstants.CENTER); btn.setOpaque(false);
                btn.setBounds(bx,18,58,30); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.addMouseListener(new MouseAdapter(){
                    public void mouseClicked(MouseEvent e){ applyRole(role); }
                    public void mouseEntered(MouseEvent e){ btn.setForeground(Color.WHITE); btn.repaint(); }
                    public void mouseExited (MouseEvent e){ btn.setForeground(rc); btn.repaint(); }
                });
                sidebarRoleSwitcher.add(btn); bx+=60;
            }
        }
        sb.add(sidebarRoleSwitcher);

        // Nav items with icons and color accents per section
        Object[][] navDef={
            {null,"MAIN"},
            {"dashboard",  "\u25A3  Dashboard",        C_CYAN},
            {null,"BANKING"},
            {"clients",    "\uD83D\uDC65  Clients",    C_BLUE},
            {"accounts",   "\uD83C\uDFE6  Accounts",   C_GOLD},
            {"transactions","\u21C4  Transactions",    C_GREEN},
            {null,"INVESTMENTS"},
            {"fds",        "\uD83D\uDCC8  Fixed Deposits",  C_AMBER},
            {"rds",        "\uD83D\uDD04  Recurring Deposits",C_TEAL},
            {"mfs",        "\uD83D\uDCE6  Mutual Funds",    C_PURPLE},
            {null,"CREDIT"},
            {"loans",      "\uD83C\uDFE6  Loans",      C_ORANGE},
            {"cards",      "\uD83D\uDCB3  Cards & Cheques", new Color(0xFF,0x6B,0xB5)},
            {null,"OPERATIONS"},
            {"daily",      "\uD83D\uDCCA  Daily Summary",   C_GREEN},
            {"register",   "\uFF0B  New Registration", C_GREEN},
            {"staff",      "\uD83D\uDD10  Staff Management",C_RED},
            {"inbox",      "\u2709  Internal Inbox",   C_CYAN},
        };

        int y= isAdminSession ? 214 : 150; int navIdx=0;
        for(Object[] item:navDef){
            if(item[0]==null){
                JLabel h=L((String)item[1],new Font("SansSerif",Font.BOLD,9),C_TMUT);
                h.setBounds(20,y,220,15); sb.add(h); y+=22;
            } else {
                final String id=(String)item[0]; final int ni=navIdx++; final Color accent=(Color)item[2];
                JLabel n=new JLabel((String)item[1]){
                    @Override protected void paintComponent(Graphics g){
                        Graphics2D g2=(Graphics2D)g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                        if(Boolean.TRUE.equals(getClientProperty("active"))){
                            g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),30));
                            g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                            // Left accent bar
                            g2.setPaint(new GradientPaint(0,0,accent,0,getHeight(),accent.darker()));
                            g2.fillRoundRect(0,4,3,getHeight()-8,3,3);
                        } else if(Boolean.TRUE.equals(getClientProperty("hover"))){
                            g2.setColor(new Color(255,255,255,8));
                            g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                        }
                        super.paintComponent(g);
                    }
                };
                n.setFont(new Font("SansSerif",Font.PLAIN,13)); n.setForeground(C_TSEC); n.setOpaque(false);
                n.setBounds(6,y,250,34); n.setBorder(BorderFactory.createEmptyBorder(0,14,0,0));
                n.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                n.addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){
                        n.putClientProperty("hover",true);
                        if(!Boolean.TRUE.equals(n.getClientProperty("active")))
                            n.setForeground(canAccess(id)?C_TPRIM:C_TMUT);
                        n.repaint();
                    }
                    public void mouseExited(MouseEvent e){
                        n.putClientProperty("hover",false);
                        if(!Boolean.TRUE.equals(n.getClientProperty("active")))
                            n.setForeground(canAccess(id)?C_TSEC:C_TMUT);
                        n.repaint();
                    }
                    public void mouseClicked(MouseEvent e){
                        if(!canAccess(id)){
                            toast("\uD83D\uDD12  "+currentRole+" role cannot access "+id.substring(0,1).toUpperCase()+id.substring(1));
                            return;
                        }
                        showSection(id,ni);
                    }
                });
                navItems.add(n); sb.add(n); y+=36;
            }
        }

        // Inbox badge
        inboxBadge=new JLabel("");
        inboxBadge.setFont(new Font("SansSerif",Font.BOLD,9));
        inboxBadge.setForeground(Color.WHITE); inboxBadge.setOpaque(false);
        inboxBadge.setBounds(208,y-36+8,40,16);
        inboxBadge.setHorizontalAlignment(SwingConstants.CENTER);
        sb.add(inboxBadge);

        // Auto-save indicator
        JPanel dbPill=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x2E,0xE8,0x9A,20)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(0x2E,0xE8,0x9A,60)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
            }
        };
        dbPill.setOpaque(false); dbPill.setBounds(12,824,238,20);
        JLabel dbStatus=new JLabel("\u25CF  Auto-Save ON  \u2014  "+DB_DIR);
        dbStatus.setFont(new Font("SansSerif",Font.PLAIN,9)); dbStatus.setForeground(C_GREEN);
        dbStatus.setBounds(0,0,238,20); dbPill.add(dbStatus); sb.add(dbPill);

        JLabel logout=L("\u23FB  Sign Out",new Font("SansSerif",Font.PLAIN,12),C_TSEC);
        logout.setBounds(18,850,230,28); logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ logout.setForeground(C_RED_L); }
            public void mouseExited (MouseEvent e){ logout.setForeground(C_TSEC); }
            public void mouseClicked(MouseEvent e){ saveDatabase(); isAdminSession=false; mainFrame.dispose(); showLogin(); }
        });
        sb.add(logout);
        JLabel madeBy=L("made by amrit",new Font("SansSerif",Font.PLAIN,9),C_TMUT);
        madeBy.setBounds(18,878,230,14);
        sb.add(madeBy);
        return sb;
    }

    static Color roleColor(){
        for(int i=0;i<ROLES.length;i++) if(ROLES[i].equals(currentRole)) return ROLE_COLORS[i];
        return C_GOLD;
    }

    static void applyRole(String role){
        if(!isAdminSession){ toast("\uD83D\uDD12  Only the Admin account can switch roles."); return; }
        currentRole=role;
        if(sidebarRoleLabel!=null){ sidebarRoleLabel.setText(currentRole); sidebarRoleLabel.setForeground(roleColor()); }
        if(sidebarPill!=null) sidebarPill.repaint();
        if(sidebarRoleSwitcher!=null) sidebarRoleSwitcher.repaint();
        for(JLabel n:navItems){ n.setForeground(C_TSEC); n.repaint(); }
        if(mainFrame!=null) mainFrame.setTitle("ABCD Bank \u2014 Management System  ["+currentRole+"]");
        showSection("dashboard",0); refreshNavAccessibility(); refreshInboxBadge();
        toast("\uD83D\uDD04  Now viewing as  "+role);
    }

    static void refreshNavAccessibility(){
        String[] ids={"dashboard","clients","accounts","transactions","fds","rds","mfs","loans","cards","register","staff","inbox"};
        for(int i=0;i<navItems.size()&&i<ids.length;i++){
            boolean ok=canAccess(ids[i]);
            navItems.get(i).setForeground(ok?C_TSEC:C_TMUT);
            navItems.get(i).setCursor(Cursor.getPredefinedCursor(ok?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR));
        }
    }

    static void showSection(String id,int ni){
        if(!canAccess(id)){ toast("\uD83D\uDD12  "+currentRole+" role cannot access this section."); return; }
        contentLayout.show(contentArea,id);
        for(int i=0;i<navItems.size();i++){
            JLabel n=navItems.get(i); boolean a=(i==ni);
            n.putClientProperty("active",a);
            n.setForeground(a?C_TPRIM:(canAccess(id)?C_TSEC:C_TMUT));
            n.repaint();
        }
        switch(id){
            case "dashboard":    refreshDashboard();    break;
            case "clients":      refreshClients();      break;
            case "accounts":     refreshAccounts();     break;
            case "transactions": refreshTransactions(); break;
            case "fds":          refreshFDs();          break;
            case "rds":          refreshRDs();          break;
            case "mfs":          refreshMFs();          break;
            case "loans":        refreshLoans();        break;
            case "cards":        refreshCards();        break;
            case "daily":        refreshDailySummary(); break;
            case "inbox":        refreshInbox();        break;
        }
    }

    // ════════════════════════════════════════════════════════
    //  DASHBOARD  (Enhanced color stat cards)
    // ════════════════════════════════════════════════════════
    static JLabel[] dS=new JLabel[8];
    static DefaultTableModel dashTxnModel;
    static JPanel dashRolePanel;
    static JLabel dashRoleTitle;
    static JLabel[] dashRoleCards=new JLabel[4];

    static JPanel buildDashboard(){
        JPanel p=page("Portfolio Overview");

        dashRolePanel=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(C_BORDER2); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                Color rc=roleColor();
                g2.setPaint(new GradientPaint(0,0,rc,0,getHeight(),new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),30)));
                g2.fillRoundRect(0,0,4,getHeight(),4,4);
            }
        };
        dashRolePanel.setOpaque(false); dashRolePanel.setAlignmentX(0);
        dashRolePanel.setMaximumSize(new Dimension(99999,72));
        dashRolePanel.setPreferredSize(new Dimension(0,72));
        dashRolePanel.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));

        JLabel icon=new JLabel("\uD83D\uDD10"); icon.setFont(new Font("SansSerif",Font.PLAIN,18)); icon.setBounds(18,18,28,34); dashRolePanel.add(icon);
        dashRoleTitle=L("Viewing as  "+currentRole,new Font("SansSerif",Font.BOLD,12),C_TPRIM); dashRoleTitle.setBounds(52,14,260,18); dashRolePanel.add(dashRoleTitle);
        JLabel sub2=L("ADMIN can preview any role \u2014 nav restrictions apply instantly",new Font("SansSerif",Font.PLAIN,10),C_TMUT); sub2.setBounds(52,34,360,16); dashRolePanel.add(sub2);

        JPanel roleCards=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,14)); roleCards.setOpaque(false); roleCards.setBounds(420,0,600,72);
        String[] roleIcons={"\uD83D\uDC51","\uD83D\uDCBC","\uD83D\uDC64","\uD83D\uDCCB"};
        for(int i=0;i<ROLES.length;i++){
            final String role=ROLES[i]; final Color rc=ROLE_COLORS[i]; final int idx=i;
            dashRoleCards[i]=new JLabel(roleIcons[i]+" "+role){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean active=currentRole.equals(role);
                    if(active){ g2.setColor(new Color(rc.getRed(),rc.getGreen(),rc.getBlue(),55)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.setColor(rc); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); }
                    else { g2.setColor(new Color(255,255,255,8)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.setColor(C_BORDER2); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); }
                    super.paintComponent(g);
                }
            };
            dashRoleCards[i].setFont(new Font("SansSerif",Font.BOLD,11));
            dashRoleCards[i].setForeground(currentRole.equals(role)?rc:C_TSEC);
            dashRoleCards[i].setBorder(BorderFactory.createEmptyBorder(6,14,6,14)); dashRoleCards[i].setOpaque(false);
            dashRoleCards[i].setCursor(Cursor.getPredefinedCursor(isAdminSession?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR));
            dashRoleCards[i].addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){ applyRole(role); }
                public void mouseEntered(MouseEvent e){ if(isAdminSession) dashRoleCards[idx].setForeground(rc); }
                public void mouseExited (MouseEvent e){ dashRoleCards[idx].setForeground(currentRole.equals(role)?rc:C_TSEC); }
            });
            roleCards.add(dashRoleCards[i]);
        }
        dashRolePanel.add(roleCards);
        dashRolePanel.addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e){ roleCards.setBounds(Math.max(420,dashRolePanel.getWidth()-620),0,620,72); }
        });
        if(isAdminSession) p.add(dashRolePanel);

        // Stat cards with unique colors
        JPanel row1=new JPanel(new GridLayout(1,4,16,0));
        row1.setOpaque(false); row1.setAlignmentX(0);
        row1.setMaximumSize(new Dimension(99999,120));
        row1.setBorder(BorderFactory.createEmptyBorder(0,0,16,0));
        String[] sL={"Registered Clients","Active Accounts","Active FDs","Active Loans"};
        Color[]  sC={C_RED,C_GOLD,C_GREEN,C_BLUE};
        String[] sI={"\uD83D\uDC65","\uD83C\uDFE6","\uD83D\uDCC8","\uD83C\uDFE6"};
        for(int i=0;i<4;i++){ dS[i]=L("\u2014",new Font("Serif",Font.BOLD,30),C_TPRIM); row1.add(statCard(sL[i],dS[i],sC[i],sI[i])); }
        p.add(row1);

        // Balance cards
        JPanel row2=new JPanel(new GridLayout(1,4,16,0));
        row2.setOpaque(false); row2.setAlignmentX(0);
        row2.setMaximumSize(new Dimension(99999,96));
        row2.setBorder(BorderFactory.createEmptyBorder(0,0,22,0));
        String[] bL={"Total Bank Balance","FD Principal","MF Market Value","Loan Book"};
        Color[]  bC={C_CYAN,C_GOLD,C_PURPLE,C_ORANGE};
        for(int i=0;i<4;i++){ dS[4+i]=L("\u2014",new Font("Serif",Font.BOLD,15),C_TPRIM); row2.add(balCard(bL[i],dS[4+i],bC[i])); }
        p.add(row2);

        dashTxnModel=new DefaultTableModel(new String[]{"Date & Time","Account","Client","Type","Amount","Description"},0){ public boolean isCellEditable(int r,int c){return false;}};
        JTable t=mkTable(dashTxnModel); setW(t,165,130,155,80,125,215);
        t.getColumnModel().getColumn(3).setCellRenderer(badge());
        p.add(tblCard("RECENT TRANSACTIONS",t));
        return p;
    }

    static void refreshDashboard(){
        if(dashRolePanel!=null) dashRolePanel.setVisible(isAdminSession);
        for(int i=0;i<ROLES.length;i++){
            if(dashRoleCards[i]!=null){
                Color rc=ROLE_COLORS[i]; boolean active=currentRole.equals(ROLES[i]);
                dashRoleCards[i].setForeground(active?rc:C_TSEC);
                dashRoleCards[i].setCursor(Cursor.getPredefinedCursor(isAdminSession?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR));
                dashRoleCards[i].repaint();
            }
        }
        if(dashRoleTitle!=null) dashRoleTitle.setText("Viewing as  "+currentRole);
        dS[0].setText(String.valueOf(clients.size()));
        dS[1].setText(String.valueOf(accounts.stream().filter(a->"ACTIVE".equals(a.status)).count()));
        dS[2].setText(String.valueOf(fds.stream().filter(f->"ACTIVE".equals(f.status)).count()));
        dS[3].setText(String.valueOf(loans.stream().filter(l->"ACTIVE".equals(l.status)).count()));
        dS[4].setText(rs(accounts.stream().mapToDouble(a->a.balance).sum()));
        dS[5].setText(rs(fds.stream().filter(f->"ACTIVE".equals(f.status)).mapToDouble(f->f.principal).sum()));
        dS[6].setText(rs(mfs.stream().filter(m->"ACTIVE".equals(m.status)).mapToDouble(m->m.curVal).sum()));
        dS[7].setText(rs(loans.stream().filter(l->"ACTIVE".equals(l.status)).mapToDouble(l->l.outstandingBal).sum()));
        dashTxnModel.setRowCount(0);
        List<Object[]> rows=new ArrayList<>();
        for(BankAccount a:accounts){ Client c=findC(a.clientId); for(Transaction t:a.transactions) rows.add(new Object[]{t.dt,a.id,c!=null?c.name:"?",t.type,rs(t.amount),t.desc}); }
        rows.sort((a,b)->b[0].toString().compareTo(a[0].toString()));
        rows.stream().limit(10).forEach(dashTxnModel::addRow);
    }

    // ════════════════════════════════════════════════════════
    //  CLIENTS
    // ════════════════════════════════════════════════════════
    static DefaultTableModel clientModel;
    static JLabel cntBadge;
    static JTable clientTable;

    static JPanel buildClients(){
        JPanel p=page("Client Management");
        JPanel sb=new JPanel(new BorderLayout(0,0)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(C_BORDER2); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
            }
        };
        sb.setOpaque(false); sb.setAlignmentX(0);
        sb.setMaximumSize(new Dimension(99999,58));
        sb.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));

        JPanel left=new JPanel(new BorderLayout(10,0)); left.setOpaque(false); left.setBorder(BorderFactory.createEmptyBorder(14,18,14,14));
        JLabel mag=new JLabel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CYAN); g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawOval(1,1,15,15); g2.drawLine(14,14,20,20);
            }
            @Override public Dimension getPreferredSize(){return new Dimension(22,22);}
        };
        left.add(mag,BorderLayout.WEST);
        JTextField search=new JTextField(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                if(getText().isEmpty()&&!isFocusOwner()){
                    Graphics2D g2=(Graphics2D)g; g2.setColor(C_TMUT); g2.setFont(getFont());
                    Insets ins=getInsets();
                    g2.drawString("Search by name, Client ID, phone or PAN\u2026",ins.left,ins.top+g2.getFontMetrics().getAscent());
                }
            }
        };
        search.setBackground(C_CARD); search.setForeground(C_TPRIM);
        search.setCaretColor(C_TPRIM); search.setFont(FB); search.setBorder(BorderFactory.createEmptyBorder());
        left.add(search,BorderLayout.CENTER);
        sb.add(left,BorderLayout.CENTER);

        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,13)); right.setOpaque(false); right.setBorder(BorderFactory.createEmptyBorder(0,0,0,14));
        right.add(new JLabel(){@Override protected void paintComponent(Graphics g){g.setColor(C_BORDER2);g.fillRect(0,3,1,24);}@Override public Dimension getPreferredSize(){return new Dimension(1,30);}});

        String[] chipTxt={"ALL","VERIFIED","PENDING"};
        Color[]  chipClr={C_TSEC,C_GREEN,C_AMBER};
        String[] fs={"all"};
        JLabel[] chips=new JLabel[3];
        for(int i=0;i<3;i++){
            final String fv=chipTxt[i].toLowerCase(); final Color fc=chipClr[i];
            chips[i]=new JLabel(chipTxt[i]){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if(fs[0].equals(fv)){
                        g2.setColor(new Color(fc.getRed(),fc.getGreen(),fc.getBlue(),38)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                        g2.setColor(new Color(fc.getRed(),fc.getGreen(),fc.getBlue(),120)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                    }
                    super.paintComponent(g);
                }
            };
            chips[i].setFont(new Font("SansSerif",Font.BOLD,10)); chips[i].setForeground(fc); chips[i].setOpaque(false);
            chips[i].setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
            chips[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final JLabel[] ref=chips;
            chips[i].addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){ fs[0]=fv; for(JLabel c:ref)c.repaint(); filterCL(search.getText().trim().toLowerCase(),fs[0]); }
            });
            right.add(chips[i]);
        }
        cntBadge=new JLabel("0 clients"){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE5,0x32,0x35,30)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                super.paintComponent(g);
            }
        };
        cntBadge.setFont(new Font("SansSerif",Font.BOLD,10)); cntBadge.setForeground(C_RED_L); cntBadge.setOpaque(false);
        cntBadge.setBorder(BorderFactory.createEmptyBorder(3,10,3,10));
        right.add(cntBadge);
        
        sb.add(right,BorderLayout.EAST);
        p.add(sb);

        clientModel=new DefaultTableModel(new String[]{"Client ID","Full Name","Phone","City","KYC Status","Risk Profile","Member Since"},0){ public boolean isCellEditable(int r,int c){return false;}};
        clientTable=mkTable(clientModel); setW(clientTable,98,200,116,116,94,100,100);
        clientTable.getColumnModel().getColumn(4).setCellRenderer(badge());
        clientTable.getColumnModel().getColumn(5).setCellRenderer(badge());
        ctxMenu(clientTable,
            new String[]{"Edit Client Info","Delete Client","View Details","\uD83D\uDCCB  Copy Client Info"},
            new ActionListener[]{
                e->{ int r=clientTable.getSelectedRow(); if(r>=0) editClientDlg((String)clientModel.getValueAt(r,0)); },
                e->{ int r=clientTable.getSelectedRow(); if(r>=0) deleteClient((String)clientModel.getValueAt(r,0)); },
                e->{ int r=clientTable.getSelectedRow(); if(r>=0) viewClientDetails((String)clientModel.getValueAt(r,0)); },
                e->{ int r=clientTable.getSelectedRow(); if(r>=0){ Client c=findC((String)clientModel.getValueAt(r,0)); if(c!=null){ copyToClipboard(buildClientCopyText(c)); toast("\uD83D\uDCCB  Client info copied to clipboard!"); }}}
            });
        clientTable.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ if(e.getClickCount()==2){ int r=clientTable.getSelectedRow(); if(r>=0) viewClientDetails((String)clientModel.getValueAt(r,0)); }}
        });
        p.add(tblCard("ALL CLIENTS  (right-click to edit/delete/copy \u2502 double-click to view)",clientTable));
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){filterCL(search.getText().trim().toLowerCase(),fs[0]);}
            public void removeUpdate(javax.swing.event.DocumentEvent e){filterCL(search.getText().trim().toLowerCase(),fs[0]);}
            public void changedUpdate(javax.swing.event.DocumentEvent e){}
        });
        return p;
    }

    static void refreshClients(){ filterCL("","all"); }
    static void filterCL(String q,String kyc){
        clientModel.setRowCount(0);
        for(Client c:clients){
            boolean tm=q.isEmpty()||(c.id+" "+c.name+" "+c.phone+" "+c.pan).toLowerCase().contains(q);
            boolean km="all".equals(kyc)||c.kyc.equalsIgnoreCase(kyc);
            if(tm&&km) clientModel.addRow(new Object[]{c.id,c.name,c.phone,c.city,c.kyc,c.riskProfile,c.openedOn});
        }
        if(cntBadge!=null) cntBadge.setText(clientModel.getRowCount()+" clients");
    }

    static void showAddClientDlg(){
        if(!canRegisterClient()){toast("\uD83D\uDD12  "+currentRole+" cannot register clients.");return;}
        JDialog d=dlg("Register New Client",510,570); JPanel f=dlgForm(d);
        JTextField[] tf={mkField("Full Name"),mkField("Phone Number"),mkField("Email Address"),mkField("City"),mkField("PAN (10 chars)"),mkField("Aadhar Number"),mkField("Occupation"),mkField("Annual Income (\u20B9)")};
        String[] labs={"Full Name","Phone","Email","City","PAN Number","Aadhar Number","Occupation","Annual Income (\u20B9)"};
        for(int i=0;i<labs.length;i+=2){
            if(i+1<labs.length) f.add(twoCol(fRow(labs[i],tf[i]),fRow(labs[i+1],tf[i+1])));
            else f.add(fRow(labs[i],tf[i]));
        }
        JComboBox<String> kyc=mkCombo(new String[]{"VERIFIED","PENDING"});
        JComboBox<String> risk=mkCombo(new String[]{"LOW","MODERATE","HIGH"});
        f.add(twoCol(fRow("KYC Status",kyc),fRow("Risk Profile",risk)));
        f.add(Box.createVerticalStrut(12));
        RedButton save=new RedButton("Register Client"); save.setAlignmentX(0.5f); f.add(save);
        save.addActionListener(e->{
            String name=tf[0].getText().trim(),phone=tf[1].getText().trim(),pan=tf[4].getText().trim().toUpperCase();
            if(name.isEmpty()||phone.isEmpty()||pan.length()!=10){showErr(d,"Name, phone & valid 10-char PAN required.");return;}
            String id="CLT"+String.format("%05d",CC++);
            clients.add(new Client(id,name,phone,tf[2].getText().trim(),tf[3].getText().trim(),pan,tf[5].getText().trim(),tf[6].getText().trim(),parseDbl(tf[7].getText()),(String)kyc.getSelectedItem(),(String)risk.getSelectedItem(),LocalDate.now().toString()));
            saveDatabase(); d.dispose(); refreshClients(); toast("Client "+id+" registered \u2014 auto-saved!");
        });
        d.setVisible(true);
    }

    static void editClientDlg(String clientId){
        if(!canEditDeleteClient()){toast("\uD83D\uDD12  "+currentRole+" cannot edit clients.");return;}
        Client c=findC(clientId); if(c==null){toast("Client not found.");return;}
        JDialog d=dlg("Edit Client \u2014 "+clientId+" ["+c.kyc+"]",510,610); JPanel f=dlgForm(d);
        JPanel banner=new JPanel(new BorderLayout()); banner.setOpaque(false); banner.setAlignmentX(0); banner.setMaximumSize(new Dimension(700,38));
        banner.setBorder(BorderFactory.createCompoundBorder(new LineBorder("PENDING".equals(c.kyc)?C_AMBER:C_BORDER2,1,true),BorderFactory.createEmptyBorder(7,14,7,14)));
        String statusMsg="PENDING".equals(c.kyc)?"\u26A0  PENDING client \u2014 all fields editable + KYC can be approved":"\u2139  VERIFIED client \u2014 contact details & profile editable";
        banner.add(L(statusMsg,FS,"PENDING".equals(c.kyc)?C_AMBER:C_TSEC),BorderLayout.WEST); f.add(banner); f.add(Box.createVerticalStrut(10));
        JTextField nameF=mkField("Full Name"); nameF.setText(c.name);
        JTextField phoneF=mkField("Phone Number"); phoneF.setText(c.phone);
        JTextField emailF=mkField("Email Address"); emailF.setText(c.email);
        JTextField cityF=mkField("City"); cityF.setText(c.city);
        JTextField panF=mkField("PAN (10 chars)"); panF.setText(c.pan);
        JTextField aadharF=mkField("Aadhar Number"); aadharF.setText(c.aadhar);
        JTextField occF=mkField("Occupation"); occF.setText(c.occupation);
        JTextField incF=mkField("Annual Income (\u20B9)"); incF.setText(String.valueOf((long)c.annualIncome));
        if(!"PENDING".equals(c.kyc)){ panF.setEditable(false); panF.setForeground(C_TMUT); aadharF.setEditable(false); aadharF.setForeground(C_TMUT); }
        f.add(twoCol(fRow("Full Name",nameF),fRow("Phone",phoneF)));
        f.add(twoCol(fRow("Email",emailF),fRow("City",cityF)));
        f.add(twoCol(fRow("PAN Number"+(!"PENDING".equals(c.kyc)?" (locked)":""),panF),fRow("Aadhar"+(!"PENDING".equals(c.kyc)?" (locked)":""),aadharF)));
        f.add(twoCol(fRow("Occupation",occF),fRow("Annual Income (\u20B9)",incF)));
        JComboBox<String> kycC=mkCombo("PENDING".equals(c.kyc)?new String[]{"PENDING","VERIFIED"}:new String[]{"VERIFIED","PENDING"}); kycC.setSelectedItem(c.kyc);
        JComboBox<String> riskC=mkCombo(new String[]{"LOW","MODERATE","HIGH"}); riskC.setSelectedItem(c.riskProfile);
        f.add(twoCol(fRow("KYC Status",kycC),fRow("Risk Profile",riskC)));
        f.add(Box.createVerticalStrut(14));
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER,14,0)); btnRow.setOpaque(false); btnRow.setAlignmentX(0.5f);
        RedButton saveBtn=new RedButton("  \u2713  Save Changes  "); btnRow.add(saveBtn); f.add(btnRow);
        saveBtn.addActionListener(e->{
            String name=nameF.getText().trim(),phone=phoneF.getText().trim(),pan=panF.getText().trim().toUpperCase();
            if(name.isEmpty()||phone.isEmpty()||pan.length()!=10){showErr(d,"Name, phone & valid 10-char PAN are required."); return;}
            c.name=name; c.phone=phone; c.email=emailF.getText().trim(); c.city=cityF.getText().trim();
            c.pan=pan; c.aadhar=aadharF.getText().trim(); c.occupation=occF.getText().trim();
            c.annualIncome=parseDbl(incF.getText()); c.kyc=(String)kycC.getSelectedItem(); c.riskProfile=(String)riskC.getSelectedItem();
            saveDatabase(); d.dispose(); refreshClients(); toast("\u2714 Client "+clientId+" updated \u2014 auto-saved!");
        });
        d.setVisible(true);
    }

    static void deleteClient(String clientId){
        if(!canEditDeleteClient()){toast("\uD83D\uDD12  "+currentRole+" cannot delete clients.");return;}
        Client c=findC(clientId); if(c==null){toast("Client not found.");return;}
        if(!"PENDING".equals(c.kyc)){ JOptionPane.showMessageDialog(mainFrame,"Only PENDING clients can be deleted.\n\n"+c.name+" has KYC status: "+c.kyc+".","Delete Restricted",JOptionPane.WARNING_MESSAGE); return; }
        boolean hasBalance=c.accountIds.stream().map(ABCDBankApp::findA).anyMatch(a->a!=null&&a.balance>0);
        int confirm=JOptionPane.showConfirmDialog(mainFrame,"Delete client "+clientId+" \u2014 "+c.name+"?"+(hasBalance?"\n\u26A0  This client has accounts with balance.":"")+"\n\nThis action CANNOT be undone.","Confirm Delete",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(confirm!=JOptionPane.YES_OPTION) return;
        for(String aId:new ArrayList<>(c.accountIds)){ fds.removeIf(fv->fv.linkedAccId.equals(aId)); rds.removeIf(r->r.linkedAccId.equals(aId)); accounts.removeIf(a->a.id.equals(aId)); }
        mfs.removeIf(m->m.clientId.equals(clientId)); loans.removeIf(l->l.clientId.equals(clientId)); clients.remove(c);
        saveDatabase(); refreshClients(); refreshDashboard(); toast("\uD83D\uDDD1  Client "+clientId+" deleted \u2014 auto-saved.");
    }

    static void viewClientDetails(String clientId){
        Client c=findC(clientId); if(c==null) return;
        JDialog d=dlg("Client Profile \u2014 "+clientId,560,580); JPanel f=dlgForm(d);
        JPanel hc=new JPanel(null); hc.setOpaque(false); hc.setAlignmentX(0); hc.setMaximumSize(new Dimension(700,70)); hc.setPreferredSize(new Dimension(0,70));
        JPanel av2=new JPanel(){@Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0,0,C_RED_D,48,48,C_CYAN)); g2.fillOval(0,0,48,48);
            g2.setColor(Color.WHITE); g2.setFont(new Font("SansSerif",Font.BOLD,14));
            String ini=c.name.isEmpty()?"?":c.name.substring(0,Math.min(2,c.name.length())).toUpperCase();
            FontMetrics fm=g2.getFontMetrics(); g2.drawString(ini,(48-fm.stringWidth(ini))/2,(48+fm.getAscent()-fm.getDescent())/2-2);}};
        av2.setOpaque(false); av2.setBounds(0,10,48,48); hc.add(av2);
        JLabel nm=L(c.name,new Font("Serif",Font.BOLD,18),C_TPRIM); nm.setBounds(60,12,430,24); hc.add(nm);
        JLabel id2=L(c.id+"  |  Member since "+c.openedOn,FS,C_TSEC); id2.setBounds(60,38,430,16); hc.add(id2);
        f.add(hc); f.add(Box.createVerticalStrut(8));
        JLabel hint=L("\uD83D\uDCCB  Click any field below and drag to select text, then Ctrl+C to copy",new Font("SansSerif",Font.PLAIN,10),C_TMUT);
        hint.setAlignmentX(0); hint.setMaximumSize(new Dimension(700,18)); f.add(hint); f.add(Box.createVerticalStrut(8));
        String[][] details={{"Client ID",c.id},{"Full Name",c.name},{"Phone",c.phone},{"Email",c.email},{"City",c.city},{"PAN Number",c.pan},{"Aadhar Number",c.aadhar},{"Occupation",c.occupation},{"Annual Income",rs(c.annualIncome)},{"KYC Status",c.kyc},{"Risk Profile",c.riskProfile},{"Accounts",String.join(", ",c.accountIds)}};
        JPanel grid=new JPanel(new GridLayout(0,2,12,8)); grid.setOpaque(false); grid.setAlignmentX(0); grid.setMaximumSize(new Dimension(700,9999));
        for(String[] row:details){
            JPanel kv=new JPanel(new BorderLayout(4,2)); kv.setOpaque(false);
            kv.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(5,10,5,10)));
            JLabel keyLbl=L(row[0],new Font("SansSerif",Font.BOLD,9),C_TMUT); keyLbl.setBorder(BorderFactory.createEmptyBorder(0,0,2,0)); kv.add(keyLbl,BorderLayout.NORTH);
            JTextArea ta=new JTextArea(row[1]); ta.setEditable(false); ta.setLineWrap(true); ta.setWrapStyleWord(true);
            ta.setBackground(new Color(0,0,0,0)); ta.setForeground(C_TPRIM); ta.setFont(FB); ta.setOpaque(false);
            ta.setBorder(BorderFactory.createEmptyBorder()); ta.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            ta.setHighlighter(new javax.swing.text.DefaultHighlighter());
            ta.setSelectionColor(new Color(C_CYAN.getRed(),C_CYAN.getGreen(),C_CYAN.getBlue(),80)); ta.setSelectedTextColor(C_TPRIM);
            kv.add(ta,BorderLayout.CENTER); grid.add(kv);
        }
        f.add(grid); f.add(Box.createVerticalStrut(16));
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER,10,0)); btnRow.setOpaque(false); btnRow.setAlignmentX(0.5f);
        RedButton editBtn=new RedButton("  \u270E  Edit Client  "); editBtn.addActionListener(e->{ d.dispose(); editClientDlg(clientId); }); btnRow.add(editBtn);
        RedButton copyAllBtn=new RedButton("  \uD83D\uDCCB  Copy All Info  "); copyAllBtn.addActionListener(e->{ copyToClipboard(buildClientCopyText(c)); toast("\uD83D\uDCCB  All client info copied!"); }); btnRow.add(copyAllBtn);
        if("PENDING".equals(c.kyc)){
            JButton delBtn=new JButton("  \uD83D\uDDD1  Delete  "); delBtn.setFont(FB2); delBtn.setForeground(C_RED_L); delBtn.setBackground(new Color(198,40,40,60)); delBtn.setBorderPainted(false); delBtn.setFocusPainted(false); delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            delBtn.addActionListener(e->{ d.dispose(); deleteClient(clientId); }); btnRow.add(delBtn);
        }
        f.add(btnRow); d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════
    //  ACCOUNTS
    // ════════════════════════════════════════════════════════
    static DefaultTableModel accModel;

    static JPanel buildAccounts(){
        JPanel p=page("Account Management");
        p.add(actBar("Open New Account",e->showOpenAccDlg()));
        accModel=new DefaultTableModel(new String[]{"Account No","Type","Client","Balance","Min Balance","Rate","Status","Opened"},0){ public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl=mkTable(accModel); setW(tbl,148,80,175,128,108,55,84,100);
        tbl.getColumnModel().getColumn(1).setCellRenderer(badge()); tbl.getColumnModel().getColumn(6).setCellRenderer(badge());
        ctxMenu(tbl,new String[]{"Freeze / Unfreeze","Deposit Cash","Withdraw Cash"},new ActionListener[]{
            e->{int r=tbl.getSelectedRow();if(r<0)return; if(!canFreezeAccounts()){toast("\uD83D\uDD12  "+currentRole+" cannot freeze/unfreeze accounts.");return;} BankAccount a=findA((String)accModel.getValueAt(r,0));if(a==null)return; a.status="ACTIVE".equals(a.status)?"FROZEN":"ACTIVE";saveDatabase();refreshAccounts();toast("Account "+a.id+" \u2192 "+a.status);},
            e->{int r=tbl.getSelectedRow();if(r>=0){if(!canWrite()){toast("\uD83D\uDD12  "+currentRole+" cannot deposit.");return;} showDepDlg((String)accModel.getValueAt(r,0));}},
            e->{int r=tbl.getSelectedRow();if(r>=0){if(!canWrite()){toast("\uD83D\uDD12  "+currentRole+" cannot withdraw.");return;} showWitDlg((String)accModel.getValueAt(r,0));}}
        });
        p.add(tblCard("ALL ACCOUNTS",tbl));
        return p;
    }

    static void refreshAccounts(){
        accModel.setRowCount(0);
        for(BankAccount a:accounts){ Client c=findC(a.clientId); accModel.addRow(new Object[]{a.id,a.type,c!=null?c.name:"?",rs(a.balance),rs(a.minBal),a.rate+"%",a.status,a.openedOn}); }
    }

    static void showOpenAccDlg(){
        if(!canWrite()){toast("\uD83D\uDD12  "+currentRole+" cannot open accounts.");return;}
        JDialog d=dlg("Open New Account",440,400); JPanel f=dlgForm(d);
        JTextField cidF=mkField("e.g. CLT00001");
        JComboBox<String> typeC=mkCombo(new String[]{"SAVINGS \u2014 Min \u20B91,000 | 3.5%","CURRENT \u2014 Min \u20B95,000 | 0%","SALARY \u2014 No min | 2.5%"});
        JTextField depF=mkField("Initial Deposit Amount"),nomF=mkField("Nominee Name (optional)");
        f.add(fRow("Client ID",cidF)); f.add(fRow("Account Type",typeC)); f.add(twoCol(fRow("Initial Deposit (\u20B9)",depF),fRow("Nominee",nomF)));
        f.add(Box.createVerticalStrut(12)); RedButton save=new RedButton("Open Account"); save.setAlignmentX(0.5f); f.add(save);
        save.addActionListener(e->{
            Client c=findC(cidF.getText().trim().toUpperCase()); if(c==null){showErr(d,"Client not found.");return;}
            int ti=typeC.getSelectedIndex(); String type=ti==0?"SAVINGS":ti==1?"CURRENT":"SALARY";
            double min=ti==0?1000:ti==1?5000:0,rate=ti==0?3.5:ti==2?2.5:0; String pre=ti==0?"SB":ti==1?"CA":"SL";
            double dep=parseDbl(depF.getText()); if(dep<min){showErr(d,"Min deposit: "+rs(min));return;}
            String aId=pre+String.format("%09d",AC++);
            BankAccount acc=new BankAccount(aId,c.id,type,dep,min,rate,LocalDate.now().toString());
            acc.nominee=nomF.getText().trim(); acc.addTxn("CREDIT",dep,"Account Opening"); accounts.add(acc); c.accountIds.add(aId); saveDatabase();
            d.dispose(); refreshAccounts(); toast("Account "+aId+" opened \u2014 auto-saved!");
        });
        d.setVisible(true);
    }

    static void showDepDlg(String id){
        BankAccount a=findA(id); if(a==null||!"ACTIVE".equals(a.status))return;
        JDialog d=dlg("Deposit \u2014 "+id,390,270); JPanel f=dlgForm(d);
        f.add(infoRow("Current Balance",rs(a.balance)));
        JTextField amtF=mkField("Amount (\u20B9)"); f.add(fRow("Deposit Amount",amtF)); f.add(Box.createVerticalStrut(12));
        RedButton b=new RedButton("Deposit"); b.setAlignmentX(0.5f); f.add(b);
        b.addActionListener(e->{ double amt=parseDbl(amtF.getText()); if(amt<=0){showErr(d,"Invalid amount.");return;} a.balance+=amt; a.addTxn("CREDIT",amt,"Cash Deposit"); saveDatabase(); d.dispose(); refreshAccounts(); refreshTransactions(); toast("Deposited "+rs(amt)+" \u2192 Balance: "+rs(a.balance)); });
        d.setVisible(true);
    }

    static void showWitDlg(String id){
        BankAccount a=findA(id); if(a==null||!"ACTIVE".equals(a.status))return;
        double avail=a.balance-a.minBal;
        JDialog d=dlg("Withdraw \u2014 "+id,390,290); JPanel f=dlgForm(d);
        f.add(infoRow("Balance",rs(a.balance))); f.add(infoRow("Available",rs(avail)));
        JTextField amtF=mkField("Amount (\u20B9)"); f.add(fRow("Withdraw Amount",amtF)); f.add(Box.createVerticalStrut(12));
        RedButton b=new RedButton("Withdraw"); b.setAlignmentX(0.5f); f.add(b);
        b.addActionListener(e->{ double amt=parseDbl(amtF.getText()); if(amt<=0||amt>avail){showErr(d,"Invalid or insufficient.");return;} a.balance-=amt; a.addTxn("DEBIT",amt,"Cash Withdrawal"); saveDatabase(); d.dispose(); refreshAccounts(); refreshTransactions(); toast("Withdrawn "+rs(amt)+" \u2192 Balance: "+rs(a.balance)); });
        d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════
    //  TRANSACTIONS
    // ════════════════════════════════════════════════════════
    static DefaultTableModel txnModel;

    static JPanel buildTransactions(){
        JPanel p=page("Transactions");
        p.add(actBar("\u21C4  Transfer Funds",e->showTransferDlg()));
        txnModel=new DefaultTableModel(new String[]{"Date & Time","Account","Client","Type","Amount","Balance After","Description"},0){ public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl=mkTable(txnModel); setW(tbl,158,134,148,75,118,118,200);
        tbl.getColumnModel().getColumn(3).setCellRenderer(badge());
        p.add(tblCard("TRANSACTION LEDGER",tbl));
        return p;
    }

    static void refreshTransactions(){
        txnModel.setRowCount(0);
        List<Object[]> rows=new ArrayList<>();
        for(BankAccount a:accounts){ Client c=findC(a.clientId); for(Transaction t:a.transactions) rows.add(new Object[]{t.dt,a.id,c!=null?c.name:"?",t.type,rs(t.amount),rs(t.balAfter),t.desc}); }
        rows.sort((a,b)->b[0].toString().compareTo(a[0].toString())); rows.forEach(txnModel::addRow);
    }

    static void showTransferDlg(){
        if(!canWrite()){toast("\uD83D\uDD12  "+currentRole+" cannot transfer funds.");return;}
        JDialog d=dlg("Fund Transfer",440,410); JPanel f=dlgForm(d);
        JTextField frF=mkField("From Account"),toF=mkField("To Account"),amF=mkField("Amount (\u20B9)"),rmF=mkField("Remarks (optional)");
        f.add(twoCol(fRow("From Account",frF),fRow("To Account",toF))); f.add(fRow("Amount (\u20B9)",amF)); f.add(fRow("Remarks",rmF));
        f.add(Box.createVerticalStrut(12)); RedButton b=new RedButton("Transfer Funds"); b.setAlignmentX(0.5f); f.add(b);
        b.addActionListener(e->{
            BankAccount fr=findA(frF.getText().trim().toUpperCase()); BankAccount to=findA(toF.getText().trim().toUpperCase());
            if(fr==null||!"ACTIVE".equals(fr.status)){showErr(d,"Source account invalid.");return;}
            if(to==null||!"ACTIVE".equals(to.status)){showErr(d,"Destination account invalid.");return;}
            double amt=parseDbl(amF.getText()),avail=fr.balance-fr.minBal;
            if(amt<=0||amt>avail){showErr(d,"Insufficient. Available: "+rs(avail));return;}
            String desc=rmF.getText().trim(),now=LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
            fr.balance-=amt; fr.transactions.add(0,new Transaction("DEBIT",amt,"Transfer to "+to.id+(desc.isEmpty()?"":" \u2014 "+desc),fr.balance,now));
            to.balance+=amt; to.transactions.add(0,new Transaction("CREDIT",amt,"Transfer from "+fr.id+(desc.isEmpty()?"":" \u2014 "+desc),to.balance,now));
            saveDatabase(); d.dispose(); refreshTransactions(); refreshAccounts(); toast(rs(amt)+" transferred: "+fr.id+" \u2192 "+to.id);
        });
        d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════
    //  FIXED DEPOSITS
    // ════════════════════════════════════════════════════════
    static DefaultTableModel fdModel;

    static JPanel buildFDs(){
        JPanel p=page("Fixed Deposits");
        RoundPanel rates=new RoundPanel(C_PANEL,10);
        rates.setBorder(new LineBorder(C_BORDER2,1,true)); rates.setLayout(new FlowLayout(FlowLayout.LEFT,18,9));
        rates.setAlignmentX(0); rates.setMaximumSize(new Dimension(99999,44));
        rates.add(L("FD RATES \u2192",new Font("SansSerif",Font.BOLD,9),C_TMUT));
        String[][] rr={{"6m","6.5%"},{"12m","7.0%"},{"24m","7.1%"},{"36m","7.5%"},{"48m","7.25%"},{"60m","7.25%"},{"84m","6.8%"}};
        for(String[] r:rr){ JPanel rp=new JPanel(new FlowLayout(FlowLayout.LEFT,3,0)); rp.setOpaque(false); rp.add(L(r[0],FS,C_TMUT)); rp.add(L(r[1],new Font("SansSerif",Font.BOLD,11),C_GOLD)); rates.add(rp); }
        rates.add(L("\u2022 Sr. Citizens +0.5%",FS,C_GREEN));
        p.add(rates); p.add(Box.createVerticalStrut(12));
        p.add(actBar("\uFF0B  Open Fixed Deposit",e->showOpenFDDlg()));
        fdModel=new DefaultTableModel(new String[]{"FD ID","Client","Principal","Rate","Tenure","Maturity Amt","Matures On","Type","Status"},0){ public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl=mkTable(fdModel); setW(tbl,92,168,118,60,72,132,108,118,82);
        tbl.getColumnModel().getColumn(8).setCellRenderer(badge());
        ctxMenu(tbl,new String[]{"Break FD \u2014 ACTIVE only (premature, 1% penalty)","Renew FD \u2014 MATURED only"},new ActionListener[]{
            e->{int r=tbl.getSelectedRow();if(r>=0)breakFD((String)fdModel.getValueAt(r,0));},
            e->{int r=tbl.getSelectedRow();if(r>=0)renewFD((String)fdModel.getValueAt(r,0));}
        });
        p.add(tblCard("ALL FIXED DEPOSITS  (right-click: Break=ACTIVE | Renew=MATURED)",tbl));
        return p;
    }

    static void refreshFDs(){
        fdModel.setRowCount(0);
        for(FixedDeposit f:fds){ Client c=findC(f.clientId); double mat=f.principal*Math.pow(1+f.rate/100.0/4,4.0*f.months/12.0); fdModel.addRow(new Object[]{f.id,c!=null?c.name:"?",rs(f.principal),f.rate+"%",f.months+"m",rs(mat),f.matDate,f.type,f.status}); }
    }

    static void showOpenFDDlg(){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot create FDs.");return;}
        JDialog d=dlg("Open Fixed Deposit",450,440); JPanel f=dlgForm(d);
        JTextField cidF=mkField("Client ID"),accF=mkField("Linked Account No"),priF=mkField("Min \u20B910,000");
        JComboBox<String> tenC=mkCombo(new String[]{"6 months \u2014 6.5%","12 months \u2014 7.0%","24 months \u2014 7.1%","36 months \u2014 7.5%","48 months \u2014 7.25%","60 months \u2014 7.25%","84 months \u2014 6.8%"});
        JComboBox<String> typC=mkCombo(new String[]{"CUMULATIVE","NON_CUMULATIVE"});
        f.add(twoCol(fRow("Client ID",cidF),fRow("Linked Account",accF))); f.add(fRow("Principal Amount (\u20B9)",priF)); f.add(twoCol(fRow("Tenure",tenC),fRow("FD Type",typC)));
        f.add(Box.createVerticalStrut(12)); RedButton b=new RedButton("Create Fixed Deposit"); b.setAlignmentX(0.5f); f.add(b);
        b.addActionListener(e->{
            int[] tens={6,12,24,36,48,60,84}; double[] rats={6.5,7.0,7.1,7.5,7.25,7.25,6.8}; int ti=tenC.getSelectedIndex();
            Client c=findC(cidF.getText().trim().toUpperCase()); if(c==null){showErr(d,"Client not found.");return;}
            BankAccount acc=findA(accF.getText().trim().toUpperCase()); if(acc==null||!acc.clientId.equals(c.id)){showErr(d,"Invalid account.");return;}
            double pri=parseDbl(priF.getText()); if(pri<10000){showErr(d,"Min FD \u20B910,000.");return;}
            if(acc.balance<pri){showErr(d,"Insufficient balance.");return;}
            String fId="FD"+String.format("%07d",fds.size()+1); String mat=LocalDate.now().plusMonths(tens[ti]).toString();
            acc.balance-=pri; acc.addTxn("DEBIT",pri,"FD Created \u2014 "+fId);
            fds.add(new FixedDeposit(fId,c.id,acc.id,pri,rats[ti],tens[ti],LocalDate.now().toString(),mat,(String)typC.getSelectedItem()));
            saveDatabase(); d.dispose(); refreshFDs(); refreshAccounts(); toast("FD "+fId+" created! Matures: "+mat);
        });
        d.setVisible(true);
    }

    static void breakFD(String id){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot break FDs.");return;}
        FixedDeposit f=fds.stream().filter(x->x.id.equals(id)&&"ACTIVE".equals(x.status)).findFirst().orElse(null);
        if(f==null){toast("Only ACTIVE FDs can be broken.");return;}
        double pen=f.principal*0.01,pay=f.principal-pen;
        if(JOptionPane.showConfirmDialog(mainFrame,"Premature Closure \u2014 "+id+"\n\nPrincipal   : "+rs(f.principal)+"\nPenalty (1%): "+rs(pen)+"\nPayout      : "+rs(pay)+"\n\nConfirm?","Break FD",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        BankAccount a=findA(f.linkedAccId); if(a!=null){a.balance+=pay; a.addTxn("CREDIT",pay,"FD Premature Closure \u2014 "+id);}
        fds.remove(f); saveDatabase(); refreshFDs(); refreshAccounts(); refreshDashboard(); toast("FD "+id+" removed. "+rs(pay)+" credited (1% penalty deducted).");
    }

    static void renewFD(String id){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot renew FDs.");return;}
        FixedDeposit f=fds.stream().filter(x->x.id.equals(id)&&"MATURED".equals(x.status)).findFirst().orElse(null);
        if(f==null){toast("Only MATURED FDs can be renewed.");return;}
        double mat=f.principal*Math.pow(1+f.rate/100.0/4,4.0*f.months/12.0); String nMat=LocalDate.parse(f.matDate).plusMonths(f.months).toString();
        if(JOptionPane.showConfirmDialog(mainFrame,"Renew Fixed Deposit \u2014 "+id+"\n\nOld Principal : "+rs(f.principal)+"\nNew Principal : "+rs(mat)+"\nNew Maturity  : "+nMat+"\n\nOld FD will be removed. Confirm?","Renew FD",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        String nId="FD"+String.format("%07d",fds.size()+1); fds.remove(f);
        fds.add(new FixedDeposit(nId,f.clientId,f.linkedAccId,mat,f.rate,f.months,LocalDate.now().toString(),nMat,f.type));
        saveDatabase(); refreshFDs(); refreshDashboard(); toast("FD renewed! Old FD removed \u2014 New FD "+nId+" active. Matures: "+nMat);
    }

    // ════════════════════════════════════════════════════════
    //  RECURRING DEPOSITS
    // ════════════════════════════════════════════════════════
    static DefaultTableModel rdModel;

    static JPanel buildRDs(){
        JPanel p=page("Recurring Deposits");
        p.add(actBar("\uFF0B  Open Recurring Deposit",e->showOpenRDDlg()));
        rdModel=new DefaultTableModel(new String[]{"RD ID","Client","Monthly Inst.","Rate","Tenure","Paid","Total Dep.","Maturity Amt","Next Due","Status"},0){ public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl=mkTable(rdModel); setW(tbl,92,148,110,55,65,65,110,128,108,80);
        tbl.getColumnModel().getColumn(8).setCellRenderer(badge()); tbl.getColumnModel().getColumn(9).setCellRenderer(badge());
        ctxMenu(tbl,new String[]{"Pay Monthly Installment"},new ActionListener[]{ e->{int r=tbl.getSelectedRow();if(r>=0)payRD((String)rdModel.getValueAt(r,0));}});
        p.add(tblCard("ALL RECURRING DEPOSITS  (right-click to pay \u2502 one installment per month)",tbl));
        return p;
    }

    static void refreshRDs(){
        rdModel.setRowCount(0);
        for(RecurringDeposit r:rds){
            Client c=findC(r.clientId); double mr=r.rate/100.0/12.0;
            double mat=r.inst*(Math.pow(1+mr,r.months)-1)/mr*(1+mr);
            String nextDue;
            if("MATURED".equals(r.status)||"CLOSED".equals(r.status)){ nextDue="\u2014"; }
            else {
                String todayYM=LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                boolean paidThisMonth=r.lastPayDate!=null&&!r.lastPayDate.isEmpty()&&r.lastPayDate.substring(0,7).equals(todayYM);
                nextDue=paidThisMonth?LocalDate.now().withDayOfMonth(1).plusMonths(1).format(DateTimeFormatter.ofPattern("01-MMM-yyyy")):"\u26A0 Due Now";
            }
            rdModel.addRow(new Object[]{r.id,c!=null?c.name:"?",rs(r.inst),r.rate+"%",r.months+"m",r.paid+"/"+r.months,rs(r.totalDep),rs(mat),nextDue,r.status});
        }
    }

    static void showOpenRDDlg(){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot create RDs.");return;}
        JDialog d=dlg("Open Recurring Deposit",440,390); JPanel f=dlgForm(d);
        JTextField cidF=mkField("Client ID"),accF=mkField("Linked Account No"),instF=mkField("Min \u20B9500/month");
        JComboBox<String> tenC=mkCombo(new String[]{"6 months \u2014 6.2%","12 months \u2014 6.2%","24 months \u2014 6.5%","36 months \u2014 6.7%","60 months \u2014 6.5%"});
        f.add(twoCol(fRow("Client ID",cidF),fRow("Linked Account",accF))); f.add(twoCol(fRow("Monthly Installment (\u20B9)",instF),fRow("Tenure",tenC)));
        f.add(Box.createVerticalStrut(12)); RedButton b=new RedButton("Create RD"); b.setAlignmentX(0.5f); f.add(b);
        b.addActionListener(e->{
            int[] tens={6,12,24,36,60}; double[] rats={6.2,6.2,6.5,6.7,6.5}; int ti=tenC.getSelectedIndex();
            Client c=findC(cidF.getText().trim().toUpperCase()); if(c==null){showErr(d,"Client not found.");return;}
            BankAccount acc=findA(accF.getText().trim().toUpperCase()); if(acc==null||!acc.clientId.equals(c.id)){showErr(d,"Invalid account.");return;}
            double inst=parseDbl(instF.getText()); if(inst<500){showErr(d,"Min \u20B9500.");return;}
            String rId="RD"+String.format("%07d",rds.size()+1); String mat=LocalDate.now().plusMonths(tens[ti]).toString();
            rds.add(new RecurringDeposit(rId,c.id,acc.id,inst,rats[ti],tens[ti],LocalDate.now().toString(),mat));
            saveDatabase(); d.dispose(); refreshRDs(); toast("RD "+rId+" created! Matures: "+mat);
        });
        d.setVisible(true);
    }

    static void payRD(String id){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot pay RD installments.");return;}
        RecurringDeposit rd=rds.stream().filter(x->x.id.equals(id)&&"ACTIVE".equals(x.status)).findFirst().orElse(null);
        if(rd==null){toast("Active RD not found.");return;}
        if(rd.paid>=rd.months){toast("All installments paid. RD is complete.");return;}
        String todayYM=LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        if(rd.lastPayDate!=null&&!rd.lastPayDate.isEmpty()){ String lastYM=rd.lastPayDate.substring(0,7); if(lastYM.equals(todayYM)){ String nextDue=LocalDate.now().withDayOfMonth(1).plusMonths(1).format(DateTimeFormatter.ofPattern("01-MMM-yyyy")); JOptionPane.showMessageDialog(mainFrame,"Installment for "+LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))+" already paid on "+rd.lastPayDate+".\n\nNext installment due: "+nextDue,"Already Paid This Month",JOptionPane.INFORMATION_MESSAGE); return; } }
        BankAccount acc=findA(rd.linkedAccId); if(acc==null){toast("Linked account not found.");return;}
        if(acc.balance<rd.inst){ JOptionPane.showMessageDialog(mainFrame,"Insufficient balance in account "+rd.linkedAccId+".","Insufficient Balance",JOptionPane.WARNING_MESSAGE); return; }
        String today=LocalDate.now().toString(); acc.balance-=rd.inst;
        acc.addTxn("DEBIT",rd.inst,"RD Installment #"+(rd.paid+1)+" \u2014 "+id);
        rd.paid++; rd.totalDep+=rd.inst; rd.lastPayDate=today;
        if(rd.paid>=rd.months){ rd.status="MATURED"; double mr=rd.rate/100.0/12.0; double mat=rd.inst*(Math.pow(1+mr,rd.months)-1)/mr*(1+mr); acc.balance+=mat; acc.addTxn("CREDIT",mat,"RD Maturity Credit \u2014 "+id); saveDatabase(); refreshRDs(); refreshAccounts(); toast("\uD83C\uDF89  RD Matured! "+rs(mat)+" credited to "+rd.linkedAccId); }
        else { String nextDue=LocalDate.now().withDayOfMonth(1).plusMonths(1).format(DateTimeFormatter.ofPattern("01-MMM-yyyy")); saveDatabase(); refreshRDs(); refreshAccounts(); toast("Installment #"+rd.paid+" paid: "+rs(rd.inst)+"  |  "+rd.paid+"/"+rd.months+" done  |  Next due: "+nextDue); }
    }

    // ════════════════════════════════════════════════════════
    //  MUTUAL FUNDS
    // ════════════════════════════════════════════════════════
    static DefaultTableModel mfModel;
    static JLabel mfInv,mfCur,mfRet;

    static JPanel buildMFs(){
        JPanel p=page("Mutual Funds");
        JPanel sumRow=new JPanel(new GridLayout(1,3,16,0)); sumRow.setOpaque(false); sumRow.setAlignmentX(0);
        sumRow.setMaximumSize(new Dimension(99999,120)); sumRow.setBorder(BorderFactory.createEmptyBorder(0,0,16,0));
        mfInv=L("\u2014",new Font("Serif",Font.BOLD,26),C_GOLD);
        mfCur=L("\u2014",new Font("Serif",Font.BOLD,26),C_TPRIM);
        mfRet=L("\u2014",new Font("Serif",Font.BOLD,26),C_GREEN);
        sumRow.add(statCard("Active Invested",mfInv,C_GOLD,"\uD83D\uDCB0"));
        sumRow.add(statCard("Active Market Value",mfCur,C_CYAN,"\uD83D\uDCC8"));
        sumRow.add(statCard("Active Returns",mfRet,C_GREEN,"\u2197"));
        p.add(sumRow);
        p.add(actBar("\uFF0B  Add Investment",e->showAddMFDlg()));
        mfModel=new DefaultTableModel(new String[]{"MF ID","Fund Name","Client","Category","Invested","Curr Value","Returns %","SIP","Status"},0){ public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl=mkTable(mfModel); setW(tbl,90,210,148,162,118,118,92,88,82);
        tbl.getColumnModel().getColumn(8).setCellRenderer(badge());
        ctxMenu(tbl,new String[]{"Update NAV","Redeem Fund"},new ActionListener[]{
            e->{int r=tbl.getSelectedRow();if(r>=0)showUpdateNAVDlg((String)mfModel.getValueAt(r,0));},
            e->{int r=tbl.getSelectedRow();if(r>=0)redeemMF((String)mfModel.getValueAt(r,0));}
        });
        p.add(tblCard("PORTFOLIO  (right-click to update NAV or redeem)",tbl));
        return p;
    }

    static void refreshMFs(){
        double inv=mfs.stream().filter(m->"ACTIVE".equals(m.status)).mapToDouble(m->m.invested).sum();
        double cur=mfs.stream().filter(m->"ACTIVE".equals(m.status)).mapToDouble(m->m.curVal).sum();
        double ret=cur-inv;
        mfInv.setText(rs(inv)); mfCur.setText(rs(cur));
        mfRet.setText((ret>=0?"+":"")+rs(ret)); mfRet.setForeground(ret>=0?C_GREEN:C_RED_L);
        mfModel.setRowCount(0);
        for(MutualFund m:mfs){
            Client c=findC(m.clientId); double r="REDEEMED".equals(m.status)?0:(m.curVal-m.invested);
            double pct="REDEEMED".equals(m.status)?0:(m.invested>0?r/m.invested*100:0);
            mfModel.addRow(new Object[]{m.id,m.name,c!=null?c.name:"?",m.cat,rs(m.invested),"REDEEMED".equals(m.status)?rs(0):rs(m.curVal),"REDEEMED".equals(m.status)?"\u2014":String.format("%s%.2f%%",pct>=0?"+":"",pct),m.isSIP?"SIP "+rs(m.sipAmt):"\u2014",m.status});
        }
    }

    static void showAddMFDlg(){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot add MF investments.");return;}
        JDialog d=dlg("Add MF Investment",460,440); JPanel f=dlgForm(d);
        JTextField cidF=mkField("Client ID"),amtF=mkField("Amount (\u20B9)"),navF=mkField("Current NAV (\u20B9)"),sipF=mkField("SIP Amount (\u20B9, 0=none)");
        JComboBox<String> fundC=mkCombo(new String[]{"HDFC Top 100 Fund","SBI Bluechip Fund","Mirae Asset Emerging Bluechip","ICICI Prudential Liquid Fund"});
        f.add(twoCol(fRow("Client ID",cidF),fRow("Fund",fundC))); f.add(twoCol(fRow("Investment (\u20B9)",amtF),fRow("NAV (\u20B9)",navF))); f.add(fRow("Monthly SIP (\u20B9, 0=no SIP)",sipF));
        f.add(Box.createVerticalStrut(12)); RedButton b=new RedButton("Add Investment"); b.setAlignmentX(0.5f); f.add(b);
        b.addActionListener(e->{
            Client c=findC(cidF.getText().trim().toUpperCase()); if(c==null){showErr(d,"Client not found.");return;}
            double amt=parseDbl(amtF.getText()),nav=parseDbl(navF.getText()),sip=parseDbl(sipF.getText());
            if(amt<=0||nav<=0){showErr(d,"Invalid values.");return;}
            String[] cats={"Equity - Large Cap","Equity - Large Cap","Equity - Large & Mid Cap","Debt - Liquid"};
            String[] amcs={"HDFC AMC","SBI Funds","Mirae AMC","ICICI Prudential"}; int fi=fundC.getSelectedIndex();
            String mId="MF"+String.format("%07d",mfs.size()+1);
            MutualFund mf=new MutualFund(mId,c.id,(String)fundC.getSelectedItem(),cats[fi],amcs[fi],amt,nav,amt/nav,LocalDate.now().toString());
            mf.isSIP=sip>0; mf.sipAmt=sip; mfs.add(mf); saveDatabase(); d.dispose(); refreshMFs(); toast("MF "+mId+" added!");
        });
        d.setVisible(true);
    }

    static void showUpdateNAVDlg(String mfId){
        MutualFund m=mfs.stream().filter(x->x.id.equals(mfId)).findFirst().orElse(null); if(m==null)return;
        if("REDEEMED".equals(m.status)){toast("Cannot update NAV of a redeemed fund.");return;}
        JDialog d=dlg("Update NAV \u2014 "+mfId,390,270); JPanel f=dlgForm(d);
        f.add(infoRow("Current NAV",rs(m.nav))); f.add(infoRow("Current Value",rs(m.curVal)));
        JTextField nF=mkField("New NAV (\u20B9)"); f.add(fRow("New NAV",nF)); f.add(Box.createVerticalStrut(12));
        RedButton b=new RedButton("Update NAV"); b.setAlignmentX(0.5f); f.add(b);
        b.addActionListener(e->{ double nav=parseDbl(nF.getText()); if(nav<=0){showErr(d,"Invalid NAV.");return;} m.nav=nav; m.curVal=nav*m.units; saveDatabase(); d.dispose(); refreshMFs(); toast("NAV updated to "+rs(nav)+". New value: "+rs(m.curVal)); });
        d.setVisible(true);
    }

    static void redeemMF(String id){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot redeem MFs.");return;}
        MutualFund m=mfs.stream().filter(x->x.id.equals(id)&&"ACTIVE".equals(x.status)).findFirst().orElse(null);
        if(m==null){toast("Active fund not found.");return;}
        if(JOptionPane.showConfirmDialog(mainFrame,"Redeem "+m.name+"?\nCurrent Value: "+rs(m.curVal)+"\nUnits: "+String.format("%.4f",m.units),"Redeem Fund",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        BankAccount acc=accounts.stream().filter(a->a.clientId.equals(m.clientId)&&"ACTIVE".equals(a.status)).findFirst().orElse(null);
        if(acc!=null){ acc.balance+=m.curVal; acc.addTxn("CREDIT",m.curVal,"MF Redemption \u2014 "+m.name+" ("+m.id+")"); }
        double rv=m.curVal; m.status="REDEEMED"; m.curVal=0; m.units=0;
        saveDatabase(); refreshMFs(); refreshAccounts(); refreshDashboard(); toast("Redeemed "+rs(rv)+" from "+m.name+(acc!=null?" \u2192 "+acc.id:""));
    }

    // ════════════════════════════════════════════════════════
    //  LOANS
    // ════════════════════════════════════════════════════════
    static DefaultTableModel loanModel;
    static JLabel loanTotal,loanOutstanding,loanCount;

    static JPanel buildLoans(){
        JPanel p=page("Loan Management");
        RoundPanel rates=new RoundPanel(C_PANEL,10); rates.setBorder(new LineBorder(C_BORDER2,1,true)); rates.setLayout(new FlowLayout(FlowLayout.LEFT,18,9));
        rates.setAlignmentX(0); rates.setMaximumSize(new Dimension(99999,44)); rates.add(L("LOAN RATES \u2192",new Font("SansSerif",Font.BOLD,9),C_TMUT));
        String[][] lr={{"Home Loan","8.5%"},{"Personal","12.5%"},{"Car Loan","9.2%"},{"Education","10.0%"},{"Gold Loan","7.5%"},{"Business","13.0%"}};
        for(String[] r:lr){ JPanel rp=new JPanel(new FlowLayout(FlowLayout.LEFT,3,0)); rp.setOpaque(false); rp.add(L(r[0],FS,C_TMUT)); rp.add(L(r[1],new Font("SansSerif",Font.BOLD,11),C_RED_L)); rates.add(rp); }
        p.add(rates); p.add(Box.createVerticalStrut(12));
        JPanel sumRow=new JPanel(new GridLayout(1,3,16,0)); sumRow.setOpaque(false); sumRow.setAlignmentX(0);
        sumRow.setMaximumSize(new Dimension(99999,96)); sumRow.setBorder(BorderFactory.createEmptyBorder(0,0,16,0));
        loanTotal=L("\u2014",new Font("Serif",Font.BOLD,15),C_TPRIM);
        loanOutstanding=L("\u2014",new Font("Serif",Font.BOLD,15),C_RED_L);
        loanCount=L("\u2014",new Font("Serif",Font.BOLD,15),C_GREEN);
        sumRow.add(balCard("Total Loan Disbursed",loanTotal,C_GOLD));
        sumRow.add(balCard("Outstanding Balance",loanOutstanding,C_RED));
        sumRow.add(balCard("Active Loans",loanCount,C_GREEN));
        p.add(sumRow);
        p.add(actBar("\uFF0B  Disburse New Loan",e->showDisburseLoanDlg()));
        loanModel=new DefaultTableModel(new String[]{"Loan ID","Client","Type","Principal","Rate","Tenure","EMI","Paid","Outstanding","Status"},0){ public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl=mkTable(loanModel); setW(tbl,92,152,98,108,55,60,108,70,118,82);
        tbl.getColumnModel().getColumn(9).setCellRenderer(badge());
        ctxMenu(tbl,new String[]{"Pay EMI","Foreclose Loan","View Schedule"},new ActionListener[]{
            e->{ int r=tbl.getSelectedRow(); if(r>=0) payLoanEMI((String)loanModel.getValueAt(r,0)); },
            e->{ int r=tbl.getSelectedRow(); if(r>=0) forecloseLoan((String)loanModel.getValueAt(r,0)); },
            e->{ int r=tbl.getSelectedRow(); if(r>=0) showLoanSchedule((String)loanModel.getValueAt(r,0)); }
        });
        p.add(tblCard("ALL LOANS  (right-click to pay EMI / foreclose / view schedule)",tbl));
        return p;
    }

    static void refreshLoans(){
        loanTotal.setText(rs(loans.stream().mapToDouble(l->l.principal).sum()));
        loanOutstanding.setText(rs(loans.stream().filter(l->"ACTIVE".equals(l.status)).mapToDouble(l->l.outstandingBal).sum()));
        loanCount.setText(String.valueOf(loans.stream().filter(l->"ACTIVE".equals(l.status)).count()));
        loanModel.setRowCount(0);
        for(Loan l:loans){ Client c=findC(l.clientId); loanModel.addRow(new Object[]{l.id,c!=null?c.name:"?",l.type,rs(l.principal),l.rate+"%",l.tenureMonths+"m",rs(l.emiAmt),l.paidEmis+"/"+l.tenureMonths,rs(l.outstandingBal),l.status}); }
    }

    static void showDisburseLoanDlg(){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot disburse loans.");return;}
        JDialog d=dlg("Disburse New Loan",470,470); JPanel f=dlgForm(d);
        JTextField cidF=mkField("Client ID"),accF=mkField("Linked Account"),principalF=mkField("Principal Amount (\u20B9, min \u20B910,000)");
        JComboBox<String> typeC=mkCombo(new String[]{"Home Loan \u2014 8.5%","Personal Loan \u2014 12.5%","Car Loan \u2014 9.2%","Education Loan \u2014 10.0%","Gold Loan \u2014 7.5%","Business Loan \u2014 13.0%"});
        JComboBox<String> tenureC=mkCombo(new String[]{"12 months","24 months","36 months","60 months","84 months","120 months","180 months","240 months"});
        JLabel emiPreview=L("EMI: \u2014  (select type & fill amount)",FS,C_GOLD); emiPreview.setAlignmentX(0); emiPreview.setMaximumSize(new Dimension(700,22));
        Runnable calcEMI=()->{
            double[] rats={8.5,12.5,9.2,10.0,7.5,13.0}; int[] tens={12,24,36,60,84,120,180,240};
            int ti=typeC.getSelectedIndex(),ten=tens[tenureC.getSelectedIndex()]; double rate=rats[ti],pri=parseDbl(principalF.getText());
            if(pri>0){ double mr=rate/100.0/12.0; double emi=pri*mr*Math.pow(1+mr,ten)/(Math.pow(1+mr,ten)-1); emiPreview.setText("EMI: "+rs(emi)+"  |  Total Payable: "+rs(emi*ten)+"  |  Interest: "+rs(emi*ten-pri)); }
            else emiPreview.setText("EMI: \u2014  (enter amount above)");
        };
        f.add(twoCol(fRow("Client ID",cidF),fRow("Linked Account",accF))); f.add(fRow("Loan Type",typeC)); f.add(twoCol(fRow("Principal (\u20B9)",principalF),fRow("Tenure",tenureC))); f.add(fRow("",emiPreview));
        f.add(Box.createVerticalStrut(14)); RedButton b=new RedButton("Disburse Loan"); b.setAlignmentX(0.5f); f.add(b);
        typeC.addActionListener(e2->calcEMI.run()); tenureC.addActionListener(e2->calcEMI.run());
        principalF.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){ public void insertUpdate(javax.swing.event.DocumentEvent e){calcEMI.run();} public void removeUpdate(javax.swing.event.DocumentEvent e){calcEMI.run();} public void changedUpdate(javax.swing.event.DocumentEvent e){} });
        b.addActionListener(e->{
            double[] rats={8.5,12.5,9.2,10.0,7.5,13.0}; int[] tens={12,24,36,60,84,120,180,240}; String[] typeNames={"Home Loan","Personal Loan","Car Loan","Education Loan","Gold Loan","Business Loan"};
            int ti=typeC.getSelectedIndex(),ten=tens[tenureC.getSelectedIndex()]; double rate=rats[ti],pri=parseDbl(principalF.getText());
            Client c=findC(cidF.getText().trim().toUpperCase()); if(c==null){showErr(d,"Client not found.");return;}
            BankAccount acc=findA(accF.getText().trim().toUpperCase()); if(acc==null||!acc.clientId.equals(c.id)){showErr(d,"Invalid linked account.");return;}
            if(!"ACTIVE".equals(acc.status)){showErr(d,"Account is not ACTIVE.");return;}
            if(pri<10000){showErr(d,"Min loan amount is \u20B910,000.");return;}
            double mr=rate/100.0/12.0,emi=pri*mr*Math.pow(1+mr,ten)/(Math.pow(1+mr,ten)-1);
            String lId="LN"+String.format("%07d",loans.size()+1);
            Loan loan=new Loan(lId,c.id,acc.id,typeNames[ti],pri,rate,ten,LocalDate.now().toString());
            loan.emiAmt=emi; loan.outstandingBal=pri; loans.add(loan); acc.balance+=pri; acc.addTxn("CREDIT",pri,"Loan Disbursed \u2014 "+lId);
            saveDatabase(); d.dispose(); refreshLoans(); refreshAccounts(); refreshDashboard(); toast("Loan "+lId+" disbursed! EMI: "+rs(emi)+"/month for "+ten+" months.");
        });
        d.setVisible(true);
    }

    static void payLoanEMI(String id){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot process loan EMIs.");return;}
        Loan l=loans.stream().filter(x->x.id.equals(id)&&"ACTIVE".equals(x.status)).findFirst().orElse(null); if(l==null){toast("Active loan not found.");return;}
        BankAccount acc=findA(l.linkedAccId); if(acc==null){toast("Linked account not found.");return;}
        if(acc.balance<l.emiAmt){ toast("\u26A0  Insufficient balance in "+acc.id+". Need "+rs(l.emiAmt)+", have "+rs(acc.balance)); return; }
        double mr=l.rate/100.0/12.0,interestComp=l.outstandingBal*mr,principalComp=l.emiAmt-interestComp;
        l.outstandingBal=Math.max(0,l.outstandingBal-principalComp); l.paidEmis++;
        acc.balance-=l.emiAmt; acc.addTxn("DEBIT",l.emiAmt,"Loan EMI #"+l.paidEmis+" \u2014 "+id+" (P:"+rs(principalComp)+" I:"+rs(interestComp)+")");
        if(l.paidEmis>=l.tenureMonths||l.outstandingBal<1){ l.outstandingBal=0; l.status="CLOSED"; l.closureDate=LocalDate.now().toString(); toast("Loan "+id+" CLOSED! All "+l.paidEmis+" EMIs paid."); }
        else toast("EMI #"+l.paidEmis+" paid: "+rs(l.emiAmt)+" | P:"+rs(principalComp)+" | I:"+rs(interestComp)+"\nOutstanding: "+rs(l.outstandingBal));
        saveDatabase(); refreshLoans(); refreshAccounts(); refreshDashboard();
    }

    static void forecloseLoan(String id){
        if(!canManageInvestments()){toast("\uD83D\uDD12  "+currentRole+" cannot foreclose loans.");return;}
        Loan l=loans.stream().filter(x->x.id.equals(id)&&"ACTIVE".equals(x.status)).findFirst().orElse(null); if(l==null){toast("Active loan not found.");return;}
        double penalty=l.outstandingBal*0.02,totalDue=l.outstandingBal+penalty;
        BankAccount acc=findA(l.linkedAccId); if(acc==null){toast("Linked account not found.");return;}
        if(acc.balance<totalDue){ JOptionPane.showMessageDialog(mainFrame,"Insufficient balance for foreclosure.\nRequired: "+rs(totalDue)+"\nAccount balance: "+rs(acc.balance),"Foreclosure Declined",JOptionPane.WARNING_MESSAGE); return; }
        if(JOptionPane.showConfirmDialog(mainFrame,"Foreclose Loan "+id+"?\nOutstanding: "+rs(l.outstandingBal)+"\nPenalty (2%): "+rs(penalty)+"\nTotal: "+rs(totalDue)+"\n\nConfirm?","Foreclose Loan",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        acc.balance-=totalDue; acc.addTxn("DEBIT",totalDue,"Loan Foreclosure \u2014 "+id+" (incl. 2% penalty)");
        l.outstandingBal=0; l.status="CLOSED"; l.closureDate=LocalDate.now().toString();
        saveDatabase(); refreshLoans(); refreshAccounts(); refreshDashboard(); toast("Loan "+id+" foreclosed! Charged: "+rs(totalDue));
    }

    static void showLoanSchedule(String id){
        Loan l=loans.stream().filter(x->x.id.equals(id)).findFirst().orElse(null); if(l==null)return;
        Client c=findC(l.clientId); JDialog d=dlg("Loan Schedule \u2014 "+id,620,520); JPanel f=dlgForm(d);
        JPanel hdr=new JPanel(new GridLayout(2,4,10,6)); hdr.setOpaque(false); hdr.setAlignmentX(0); hdr.setMaximumSize(new Dimension(700,76)); hdr.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));
        String[][] info={{"Client",c!=null?c.name:"?"},{"Type",l.type},{"Principal",rs(l.principal)},{"Rate",l.rate+"%"},{"Tenure",l.tenureMonths+"m"},{"EMI",rs(l.emiAmt)},{"Paid",l.paidEmis+"/"+l.tenureMonths},{"Outstanding",rs(l.outstandingBal)}};
        for(String[] kv:info){ JPanel kp=new JPanel(new BorderLayout()); kp.setOpaque(false); kp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(4,8,4,8))); kp.add(L(kv[0],new Font("SansSerif",Font.BOLD,9),C_TMUT),BorderLayout.NORTH); kp.add(L(kv[1],FS,C_TPRIM),BorderLayout.CENTER); hdr.add(kp); }
        f.add(hdr);
        DefaultTableModel schModel=new DefaultTableModel(new String[]{"EMI #","Opening Bal","EMI","Principal","Interest","Closing Bal","Status"},0){ public boolean isCellEditable(int r,int c2){return false;}};
        JTable sch=mkTable(schModel); setW(sch,55,112,112,100,100,112,68); sch.getColumnModel().getColumn(6).setCellRenderer(badge());
        double bal=l.principal; double mr=l.rate/100.0/12.0;
        for(int i=1;i<=l.tenureMonths;i++){ double interest=bal*mr,principal=l.emiAmt-interest; if(principal>bal)principal=bal; double closing=Math.max(0,bal-principal); String status=i<=l.paidEmis?"PAID":(i==l.paidEmis+1?"ACTIVE":"PENDING"); schModel.addRow(new Object[]{i,rs(bal),rs(l.emiAmt),rs(principal),rs(interest),rs(closing),status}); bal=closing; if(bal<1)break; }
        f.add(tblCard("AMORTIZATION SCHEDULE",sch)); d.setVisible(true);
    }
      // ════════════════════════════════════════════════════════
//  DAILY TRANSACTION SUMMARY
// ════════════════════════════════════════════════════════
static DefaultTableModel dailyModel;
static JLabel dailyTotalCredit, dailyTotalDebit, dailyNetFlow, dailyTxnCount;
static JLabel dailyDateLbl;

static JPanel buildDailySummary(){
    JPanel p = page("Daily Transaction Summary");

    // ── Date selector bar ───────────────────────────────
    JPanel dateBar = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
    dateBar.setOpaque(false); dateBar.setAlignmentX(0);
    dateBar.setMaximumSize(new Dimension(99999,48));
    dateBar.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));

    JLabel dateLblTitle = L("SELECT DATE:", new Font("SansSerif",Font.BOLD,10), C_TMUT);
    dateBar.add(dateLblTitle);

    // Date field
    JTextField dateField = mkField("YYYY-MM-DD");
    dateField.setText(LocalDate.now().toString());
    dateField.setPreferredSize(new Dimension(140,36));
    dateField.setMaximumSize(new Dimension(140,36));
    dateField.setMinimumSize(new Dimension(140,36));
    dateBar.add(dateField);

    RedButton todayBtn = new RedButton("  Today  ");
    todayBtn.setPreferredSize(new Dimension(90,36));
    dateBar.add(todayBtn);

    RedButton searchBtn = new RedButton("  \uD83D\uDD0D  Search  ");
    searchBtn.setPreferredSize(new Dimension(110,36));
    dateBar.add(searchBtn);

    dailyDateLbl = L("  Showing: "+LocalDate.now().toString(),
        new Font("SansSerif",Font.PLAIN,11), C_TSEC);
    dateBar.add(dailyDateLbl);
    p.add(dateBar);

    // ── Stat cards row ──────────────────────────────────
    JPanel row = new JPanel(new GridLayout(1,4,16,0));
    row.setOpaque(false); row.setAlignmentX(0);
    row.setMaximumSize(new Dimension(99999,115));
    row.setBorder(BorderFactory.createEmptyBorder(0,0,18,0));

    dailyTotalCredit = L("\u2014", new Font("Serif",Font.BOLD,22), C_GREEN);
    dailyTotalDebit  = L("\u2014", new Font("Serif",Font.BOLD,22), C_RED_L);
    dailyNetFlow     = L("\u2014", new Font("Serif",Font.BOLD,22), C_CYAN);
    dailyTxnCount    = L("\u2014", new Font("Serif",Font.BOLD,30), C_GOLD);

    row.add(statCard("Total Credited Today",  dailyTotalCredit, C_GREEN,  "\u2B06"));
    row.add(statCard("Total Debited Today",   dailyTotalDebit,  C_RED,    "\u2B07"));
    row.add(statCard("Net Cash Flow",         dailyNetFlow,     C_CYAN,   "\u21C6"));
    row.add(statCard("Total Transactions",    dailyTxnCount,    C_GOLD,   "\uD83D\uDCB0"));
    p.add(row);

    // ── Summary breakdown panel ─────────────────────────
    JPanel breakdown = new JPanel(new GridLayout(1,2,16,0));
    breakdown.setOpaque(false); breakdown.setAlignmentX(0);
    breakdown.setMaximumSize(new Dimension(99999,90));
    breakdown.setBorder(BorderFactory.createEmptyBorder(0,0,18,0));

    // Credit breakdown card
    JPanel creditCard2 = new JPanel(null){
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(C_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
            g2.setColor(C_BORDER2); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
            g2.setPaint(new GradientPaint(0,0,C_GREEN,0,getHeight(),
                new Color(C_GREEN.getRed(),C_GREEN.getGreen(),C_GREEN.getBlue(),40)));
            g2.fillRoundRect(0,0,4,getHeight(),4,4);
        }
    };
    creditCard2.setOpaque(false);
    creditCard2.setPreferredSize(new Dimension(200,85));
    JLabel cTitle = L("CREDITS BREAKDOWN",new Font("SansSerif",Font.BOLD,9),C_TMUT);
    cTitle.setBounds(14,10,300,14); creditCard2.add(cTitle);
    JLabel cDepLbl  = L("Cash Deposits : \u2014", new Font("SansSerif",Font.PLAIN,11), C_TSEC);
    JLabel cTransLbl= L("Transfers In  : \u2014", new Font("SansSerif",Font.PLAIN,11), C_TSEC);
    JLabel cOtherLbl= L("Other Credits : \u2014", new Font("SansSerif",Font.PLAIN,11), C_TSEC);
    cDepLbl.setBounds(14,28,280,16);  creditCard2.add(cDepLbl);
    cTransLbl.setBounds(14,46,280,16); creditCard2.add(cTransLbl);
    cOtherLbl.setBounds(14,64,280,16); creditCard2.add(cOtherLbl);
    breakdown.add(creditCard2);

    // Debit breakdown card
    JPanel debitCard2 = new JPanel(null){
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(C_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
            g2.setColor(C_BORDER2); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
            g2.setPaint(new GradientPaint(0,0,C_RED,0,getHeight(),
                new Color(C_RED.getRed(),C_RED.getGreen(),C_RED.getBlue(),40)));
            g2.fillRoundRect(0,0,4,getHeight(),4,4);
        }
    };
    debitCard2.setOpaque(false);
    debitCard2.setPreferredSize(new Dimension(200,85));
    JLabel dTitle   = L("DEBITS BREAKDOWN",new Font("SansSerif",Font.BOLD,9),C_TMUT);
    dTitle.setBounds(14,10,300,14); debitCard2.add(dTitle);
    JLabel dWithLbl = L("Cash Withdrawals : \u2014", new Font("SansSerif",Font.PLAIN,11), C_TSEC);
    JLabel dTransLbl= L("Transfers Out    : \u2014", new Font("SansSerif",Font.PLAIN,11), C_TSEC);
    JLabel dOtherLbl= L("Other Debits     : \u2014", new Font("SansSerif",Font.PLAIN,11), C_TSEC);
    dWithLbl.setBounds(14,28,280,16);  debitCard2.add(dWithLbl);
    dTransLbl.setBounds(14,46,280,16); debitCard2.add(dTransLbl);
    dOtherLbl.setBounds(14,64,280,16); debitCard2.add(dOtherLbl);
    breakdown.add(debitCard2);
    p.add(breakdown);

    // ── Transaction table ────────────────────────────────
    dailyModel = new DefaultTableModel(new String[]{
        "Time","Account","Client","Type","Amount","Description","Running Balance"}, 0){
        public boolean isCellEditable(int r,int c){ return false; }
    };
    JTable tbl = mkTable(dailyModel);
    setW(tbl, 100, 140, 160, 75, 120, 230, 130);
    tbl.getColumnModel().getColumn(3).setCellRenderer(badge());
    p.add(tblCard("ALL TRANSACTIONS  \u2014  "+LocalDate.now().toString(), tbl));

    // ── Button actions ───────────────────────────────────
    Runnable doSearch = () -> {
        String dateStr = dateField.getText().trim();
        try {
            LocalDate.parse(dateStr); // validate format
        } catch(Exception ex){
            toast("\u26A0  Invalid date format. Use YYYY-MM-DD");
            return;
        }
        refreshDailySummaryForDate(dateStr,
            dailyTotalCredit, dailyTotalDebit, dailyNetFlow, dailyTxnCount,
            cDepLbl, cTransLbl, cOtherLbl,
            dWithLbl, dTransLbl, dOtherLbl, tbl);
        dailyDateLbl.setText("  Showing: "+dateStr);
    };

    todayBtn.addActionListener(e -> {
        dateField.setText(LocalDate.now().toString());
        doSearch.run();
    });
    searchBtn.addActionListener(e -> doSearch.run());
    dateField.addActionListener(e -> doSearch.run());

    // Store refs for refresh
    p.putClientProperty("dateField",     dateField);
    p.putClientProperty("cDepLbl",       cDepLbl);
    p.putClientProperty("cTransLbl",     cTransLbl);
    p.putClientProperty("cOtherLbl",     cOtherLbl);
    p.putClientProperty("dWithLbl",      dWithLbl);
    p.putClientProperty("dTransLbl",     dTransLbl);
    p.putClientProperty("dOtherLbl",     dOtherLbl);
    p.putClientProperty("tbl",           tbl);
    return p;
}

static void refreshDailySummary(){
    // Find the daily panel and refresh with today
    if(contentArea==null) return;
    for(Component c: contentArea.getComponents()){
        if(c instanceof JPanel){
            JPanel p=(JPanel)c;
            JTextField df=(JTextField)p.getClientProperty("dateField");
            if(df==null) continue;
            String dateStr = df.getText().trim();
            if(dateStr.isEmpty()) dateStr = LocalDate.now().toString();
            JLabel cD=(JLabel)p.getClientProperty("cDepLbl");
            JLabel cT=(JLabel)p.getClientProperty("cTransLbl");
            JLabel cO=(JLabel)p.getClientProperty("cOtherLbl");
            JLabel dW=(JLabel)p.getClientProperty("dWithLbl");
            JLabel dT=(JLabel)p.getClientProperty("dTransLbl");
            JLabel dO=(JLabel)p.getClientProperty("dOtherLbl");
            JTable tbl=(JTable)p.getClientProperty("tbl");
            if(tbl==null) continue;
            refreshDailySummaryForDate(dateStr,
                dailyTotalCredit, dailyTotalDebit, dailyNetFlow, dailyTxnCount,
                cD,cT,cO,dW,dT,dO,tbl);
        }
    }
}

static void refreshDailySummaryForDate(
        String dateStr,
        JLabel totalCredit, JLabel totalDebit, JLabel netFlow, JLabel txnCount,
        JLabel cDepLbl, JLabel cTransLbl, JLabel cOtherLbl,
        JLabel dWithLbl, JLabel dTransLbl, JLabel dOtherLbl,
        JTable tbl){

    // Collect all transactions for the date
    List<Object[]> rows = new ArrayList<>();
    double sumCredit=0, sumDebit=0;
    double cashDeposit=0, transferIn=0, otherCredit=0;
    double cashWithdraw=0, transferOut=0, otherDebit=0;

    for(BankAccount a : accounts){
        Client cl = findC(a.clientId);
        for(Transaction t : a.transactions){
            // Match date — t.dt format: "dd-MMM-yyyy HH:mm"
            String txnDate = "";
            try{
                // Parse "dd-MMM-yyyy HH:mm" → LocalDate
                java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
                LocalDate txnLD = java.time.LocalDateTime.parse(t.dt, fmt).toLocalDate();
                txnDate = txnLD.toString();
            } catch(Exception ex){
                // fallback: check if dt starts with dateStr
                txnDate = t.dt.length()>=10 ? t.dt.substring(0,10) : t.dt;
            }

            if(!txnDate.equals(dateStr)) continue;

            // Extract time portion
            String timePart = t.dt.contains(" ") ?
                t.dt.substring(t.dt.indexOf(" ")+1) : t.dt;

            rows.add(new Object[]{
                timePart,
                a.id,
                cl!=null?cl.name:"?",
                t.type,
                rs(t.amount),
                t.desc,
                rs(t.balAfter)
            });

            if("CREDIT".equals(t.type)){
                sumCredit += t.amount;
                String d2 = t.desc.toLowerCase();
                if(d2.contains("deposit"))        cashDeposit  += t.amount;
                else if(d2.contains("transfer"))  transferIn   += t.amount;
                else                              otherCredit  += t.amount;
            } else {
                sumDebit += t.amount;
                String d2 = t.desc.toLowerCase();
                if(d2.contains("withdrawal"))     cashWithdraw += t.amount;
                else if(d2.contains("transfer"))  transferOut  += t.amount;
                else                              otherDebit   += t.amount;
            }
        }
    }

    // Sort by time
    rows.sort((a,b)->a[0].toString().compareTo(b[0].toString()));

    // Update stat cards
    double net = sumCredit - sumDebit;
    totalCredit.setText(rs(sumCredit));
    totalDebit.setText(rs(sumDebit));
    netFlow.setText((net>=0?"+":"")+rs(net));
    netFlow.setForeground(net>=0 ? C_GREEN : C_RED_L);
    txnCount.setText(String.valueOf(rows.size()));

    // Update breakdowns
    cDepLbl.setText( "Cash Deposits : "+rs(cashDeposit));
    cTransLbl.setText("Transfers In  : "+rs(transferIn));
    cOtherLbl.setText("Other Credits : "+rs(otherCredit));
    dWithLbl.setText( "Cash Withdrawals : "+rs(cashWithdraw));
    dTransLbl.setText("Transfers Out    : "+rs(transferOut));
    dOtherLbl.setText("Other Debits     : "+rs(otherDebit));

    // Update table
    DefaultTableModel m = (DefaultTableModel)tbl.getModel();
    m.setRowCount(0);
    rows.forEach(m::addRow);

    // Color the amount column green/red
    tbl.getColumnModel().getColumn(4).setCellRenderer((table,v,sel,foc,row,col)->{
        String type = table.getValueAt(row,3).toString();
        JLabel lbl = new JLabel(v!=null?v.toString():"");
        lbl.setOpaque(true);
        lbl.setFont(new Font("SansSerif",Font.BOLD,12));
        lbl.setBorder(BorderFactory.createEmptyBorder(0,14,0,14));
        lbl.setForeground("CREDIT".equals(type)?C_GREEN:C_RED_L);
        lbl.setBackground(sel?C_SEL:row%2==0?C_CARD:C_ROWALT);
        return lbl;
    });
}
    // ════════════════════════════════════════════════════════
    //  CARDS & CHEQUE SERVICES
    // ════════════════════════════════════════════════════════
    static final Color C_PINK   = new Color(0xFF,0x6B,0xB5);
    static final Color C_PINK_D = new Color(0xD4,0x40,0x8A);

    static DefaultTableModel atmModel, ccModel, chqModel;

    static JPanel buildCards(){
        JPanel p = page("Cards & Cheque Services");

        // ── Summary stat row ────────────────────────────────
        JPanel sumRow = new JPanel(new GridLayout(1,3,16,0));
        sumRow.setOpaque(false); sumRow.setAlignmentX(0);
        sumRow.setMaximumSize(new Dimension(99999,115));
        sumRow.setBorder(BorderFactory.createEmptyBorder(0,0,18,0));
        JLabel sAtm  = L("\u2014",new Font("Serif",Font.BOLD,30),C_TPRIM);
        JLabel sCred = L("\u2014",new Font("Serif",Font.BOLD,30),C_TPRIM);
        JLabel sChq  = L("\u2014",new Font("Serif",Font.BOLD,30),C_TPRIM);
        sumRow.add(statCard("ATM Cards Issued",  sAtm,  C_PINK,   "\uD83D\uDCB3"));
        sumRow.add(statCard("Credit Cards Active",sCred, C_CYAN,   "\uD83D\uDCB8"));
        sumRow.add(statCard("Cheque Books Issued",sChq,  C_PURPLE, "\uD83D\uDCDD"));
        p.add(sumRow);

        // ── Tabbed panel ────────────────────────────────────
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        tabBar.setOpaque(false); tabBar.setAlignmentX(0);
        tabBar.setMaximumSize(new Dimension(99999,44));
        tabBar.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        JPanel tabContent = new JPanel(new CardLayout());
        tabContent.setBackground(C_NAVY); tabContent.setAlignmentX(0);

        // Build three sub-panels
        JPanel atmPanel  = buildATMPanel();
        JPanel ccPanel   = buildCCPanel();
        JPanel chqPanel  = buildChqPanel();
        tabContent.add(atmPanel,  "atm");
        tabContent.add(ccPanel,   "cc");
        tabContent.add(chqPanel,  "chq");
        CardLayout tcl = (CardLayout) tabContent.getLayout();

        String[] tabNames = {"\uD83D\uDCB3  ATM / Debit Cards", "\uD83D\uDCB8  Credit Cards", "\uD83D\uDCDD  Cheque Books"};
        Color[]  tabCols  = {C_PINK, C_CYAN, C_PURPLE};
        String[] tabKeys  = {"atm","cc","chq"};
        JLabel[] tabs     = new JLabel[3];
        int[]    active   = {0};

        for(int i=0;i<3;i++){
            final int fi=i; final Color tc=tabCols[i]; final String key=tabKeys[i];
            tabs[i]=new JLabel("  "+tabNames[i]+"  "){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if(active[0]==fi){
                        g2.setColor(C_CARD); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                        // bottom accent
                        g2.setPaint(new GradientPaint(0,getHeight()-3,tc,getWidth(),getHeight()-3,tc.darker()));
                        g2.fillRect(0,getHeight()-3,getWidth(),3);
                        setForeground(tc);
                    } else {
                        g2.setColor(new Color(255,255,255,5)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                        setForeground(Boolean.TRUE.equals(getClientProperty("hover"))?C_TPRIM:C_TSEC);
                    }
                    super.paintComponent(g);
                }
            };
            tabs[i].setFont(new Font("SansSerif",Font.BOLD,12));
            tabs[i].setBorder(BorderFactory.createEmptyBorder(8,18,8,18));
            tabs[i].setOpaque(false); tabs[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabs[i].addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){ tabs[fi].putClientProperty("hover",true); tabs[fi].repaint(); }
                public void mouseExited (MouseEvent e){ tabs[fi].putClientProperty("hover",false); tabs[fi].repaint(); }
                public void mouseClicked(MouseEvent e){
                    active[0]=fi;
                    for(JLabel t:tabs) t.repaint();
                    tcl.show(tabContent,key);
                }
            });
            tabBar.add(tabs[i]);
        }
        // Active initial
        tabs[0].setForeground(C_PINK);

        JPanel tabWrap = new JPanel(new BorderLayout(0,0));
        tabWrap.setOpaque(false); tabWrap.setAlignmentX(0);
        tabWrap.setMaximumSize(new Dimension(99999,9999));

        // Tab bar container with bottom border
        JPanel tabBarWrap = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                g.setColor(C_CARD); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(C_BORDER2); g.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        tabBarWrap.setOpaque(false);
        tabBarWrap.add(tabBar,BorderLayout.WEST);

        // Summary labels (update on tab switch)
        tabWrap.add(tabBarWrap,BorderLayout.NORTH);
        tabWrap.add(tabContent,BorderLayout.CENTER);
        p.add(tabWrap);

        // Store refs for refresh
        p.putClientProperty("sAtm",  sAtm);
        p.putClientProperty("sCred", sCred);
        p.putClientProperty("sChq",  sChq);
        return p;
    }

    static JPanel cardsRootPanel; // stored ref for refresh label updates

    // ── ATM / Debit Card Sub-Panel ───────────────────────────
    static JPanel buildATMPanel(){
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(C_NAVY); p.setBorder(BorderFactory.createEmptyBorder(18,0,0,0));

        // Info banner
        JPanel info = infoStrip(
            "\uD83D\uDCB3  Debit cards linked directly to savings/current accounts.  Daily ATM limit: configurable.  PIN set at issuance.",
            C_PINK);
        p.add(info); p.add(Box.createVerticalStrut(14));

        p.add(actBar("\uFF0B  Issue New ATM / Debit Card", e -> showIssueATMDlg()));
        atmModel = new DefaultTableModel(new String[]{
            "Card ID","Client","Account","Card Number","Network","Type","Daily Limit","Issued On","Expiry","Status"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl = mkTable(atmModel);
        setW(tbl, 90,148,120,160,80,80,100,100,90,80);
        tbl.getColumnModel().getColumn(9).setCellRenderer(badge());
        tbl.getColumnModel().getColumn(4).setCellRenderer(badge());
        ctxMenu(tbl, new String[]{"Block Card","Unblock Card","Change Daily Limit","View Details"},
            new ActionListener[]{
                e->{ int r=tbl.getSelectedRow(); if(r>=0) blockUnblockATM((String)atmModel.getValueAt(r,0),true);  },
                e->{ int r=tbl.getSelectedRow(); if(r>=0) blockUnblockATM((String)atmModel.getValueAt(r,0),false); },
                e->{ int r=tbl.getSelectedRow(); if(r>=0) changeATMLimit((String)atmModel.getValueAt(r,0)); },
                e->{ int r=tbl.getSelectedRow(); if(r>=0) viewATMDetails((String)atmModel.getValueAt(r,0)); }
            });
        p.add(tblCard("ALL ATM / DEBIT CARDS  (right-click for actions)", tbl));
        return p;
    }

    static void refreshATMTable(){
        if(atmModel==null) return;
        atmModel.setRowCount(0);
        for(ATMCard c: atmCards){
            Client cl=findC(c.clientId);
            atmModel.addRow(new Object[]{c.id, cl!=null?cl.name:"?", c.accountId,
                c.cardNumber, c.network, c.type, rs(c.dailyLimit),
                c.issueDate, c.expiryDate, c.status});
        }
    }

    static void showIssueATMDlg(){
        if(!canWrite()){ toast("\uD83D\uDD12  "+currentRole+" cannot issue cards."); return; }
        JDialog d = dlg("Issue ATM / Debit Card", 480, 460);
        JPanel f = dlgForm(d);
       JTextField cidF = mkField("Client ID");
        JComboBox<String> accountCombo = new JComboBox<>();
        JLabel statusLbl = L("Enter Client ID then press Tab",FS,C_TMUT);
        statusLbl.setAlignmentX(0); statusLbl.setMaximumSize(new Dimension(700,18));
        JComboBox<String> netC  = mkCombo(new String[]{"VISA","MasterCard","RuPay"});
        JComboBox<String> typeC = mkCombo(new String[]{"Classic","Gold","Platinum","Titanium"});
        JTextField pinF   = mkField("4-digit PIN");
        JTextField limitF = mkField("Daily ATM Limit (\u20B9)");
        cidF.addFocusListener(new FocusAdapter(){
            public void focusLost(FocusEvent e){ loadAccountsForClient(cidF.getText(),accountCombo,statusLbl); }
        });
        cidF.addActionListener(e->loadAccountsForClient(cidF.getText(),accountCombo,statusLbl));
        f.add(fRow("Client ID", cidF));
        f.add(statusLbl); f.add(Box.createVerticalStrut(4));
        f.add(fRow("Select Account", accountCombo));
        f.add(twoCol(fRow("Card Network", netC), fRow("Card Type", typeC)));
        
        f.add(twoCol(fRow("4-digit PIN", pinF), fRow("Daily ATM Limit (\u20B9)", limitF)));
        f.add(Box.createVerticalStrut(8));

        // Preview card art
        JPanel prevCard = new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,new Color(0x1A,0x1A,0x2E),getWidth(),getHeight(),new Color(0x6C,0x3D,0xE0)));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.setColor(new Color(255,255,255,18)); g2.setStroke(new BasicStroke(60f)); g2.drawOval(-40,-40,140,140);
                g2.setPaint(new GradientPaint(0,0,C_PINK_D,getWidth(),0,C_PINK));
                g2.setStroke(new BasicStroke(1f)); g2.fillRect(0,getHeight()-34,getWidth(),14);
                g2.setColor(new Color(255,215,0)); g2.fillRoundRect(14,20,32,24,6,6);
                g2.setColor(new Color(255,255,255,200)); g2.setFont(new Font("Monospaced",Font.BOLD,13));
                g2.drawString("ABCD BANK DEBIT",14,62);
                g2.setFont(new Font("Monospaced",Font.BOLD,15));
                g2.drawString("**** **** **** ????",14,84);
                g2.setFont(new Font("SansSerif",Font.PLAIN,9)); g2.setColor(new Color(255,255,255,160));
                g2.drawString("VALID THRU  MM/YY",14,104);
                g2.setFont(new Font("SansSerif",Font.BOLD,11)); g2.setColor(C_TPRIM);
                g2.drawString((String)(netC.getSelectedItem()),getWidth()-70,getHeight()-14);
            }
        };
        prevCard.setOpaque(false);
        prevCard.setPreferredSize(new Dimension(280,175));
        prevCard.setMinimumSize(new Dimension(280,175));
        prevCard.setMaximumSize(new Dimension(280,175));
        prevCard.setAlignmentX(0); f.add(prevCard); f.add(Box.createVerticalStrut(14));
        netC.addActionListener(e2->prevCard.repaint()); typeC.addActionListener(e2->prevCard.repaint());

        RedButton issue = new RedButton("  \uD83D\uDCB3  Issue Card  ");
        issue.setAlignmentX(0.5f); f.add(issue);
        issue.addActionListener(e -> {
            Client cl = findC(cidF.getText().trim().toUpperCase());
            if(cl==null){ showErr(d,"Client not found."); return; }
            String accId = getSelectedAccountId(accountCombo);
            if(accId.isEmpty()){ showErr(d,"Please select an account."); return; }
            BankAccount acc = findA(accId);
            if(acc==null||!acc.clientId.equals(cl.id)){ showErr(d,"Invalid account."); return; }
            String pin = pinF.getText().trim();
            if(pin.length()!=4||!pin.matches("\\d+")){ showErr(d,"PIN must be exactly 4 digits."); return; }
            double lim = parseDbl(limitF.getText());
            if(lim<=0){ showErr(d,"Enter a valid daily limit."); return; }
            // Generate card number
            String cardNum = String.format("%04d %04d %04d %04d",
                1000+(int)(Math.random()*8999),1000+(int)(Math.random()*8999),
                1000+(int)(Math.random()*8999),1000+(int)(Math.random()*8999));
            String cId   = "ATM"+String.format("%05d",CARD_CTR++);
            String exp   = LocalDate.now().plusYears(5).format(DateTimeFormatter.ofPattern("MM/yy"));
            ATMCard card = new ATMCard(cId,cl.id,acc.id,cardNum,
                (String)netC.getSelectedItem(),(String)typeC.getSelectedItem(),
                pin, LocalDate.now().toString(), exp);
            card.dailyLimit = lim;
            atmCards.add(card);
            saveDatabase(); d.dispose(); refreshCards();
            toast("\uD83D\uDCB3  Card "+cId+" issued to "+cl.name+"!  Card No: "+cardNum.substring(cardNum.length()-9));
        });
        d.setVisible(true);
    }

    static void blockUnblockATM(String id, boolean block){
        ATMCard c = atmCards.stream().filter(x->x.id.equals(id)).findFirst().orElse(null);
        if(c==null) return;
        if(block&&"BLOCKED".equals(c.status)){ toast("Card already blocked."); return; }
        if(!block&&"ACTIVE".equals(c.status)){ toast("Card is already active."); return; }
        c.status = block ? "BLOCKED" : "ACTIVE";
        saveDatabase(); refreshCards();
        toast((block?"\uD83D\uDEAB  Card BLOCKED: ":"\u2705  Card UNBLOCKED: ")+id);
    }

    static void changeATMLimit(String id){
        ATMCard c = atmCards.stream().filter(x->x.id.equals(id)).findFirst().orElse(null);
        if(c==null) return;
        JDialog d = dlg("Change Daily Limit \u2014 "+id, 380, 240);
        JPanel f = dlgForm(d);
        f.add(infoRow("Current Daily Limit", rs(c.dailyLimit)));
        JTextField nF = mkField("New Limit (\u20B9)"); f.add(fRow("New Daily ATM Limit (\u20B9)", nF));
        f.add(Box.createVerticalStrut(12));
        RedButton b = new RedButton("Update Limit"); b.setAlignmentX(0.5f); f.add(b);
        b.addActionListener(e -> {
            double lim = parseDbl(nF.getText());
            if(lim<=0){ showErr(d,"Invalid limit."); return; }
            c.dailyLimit = lim; saveDatabase(); d.dispose(); refreshCards();
            toast("Daily limit updated to "+rs(lim)+" for card "+id);
        });
        d.setVisible(true);
    }

    static void viewATMDetails(String id){
        ATMCard c = atmCards.stream().filter(x->x.id.equals(id)).findFirst().orElse(null);
        if(c==null) return;
        Client cl = findC(c.clientId);
        JDialog d = dlg("ATM Card Details \u2014 "+id, 500, 440);
        JPanel f = dlgForm(d);

        // Visual card
        JPanel cardArt = new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = "BLOCKED".equals(c.status)?new Color(0x3A,0x1A,0x1A):new Color(0x1A,0x1A,0x2E);
                Color c2 = "BLOCKED".equals(c.status)?new Color(0x8B,0x2A,0x2A):new Color(0x6C,0x3D,0xE0);
                g2.setPaint(new GradientPaint(0,0,c1,getWidth(),getHeight(),c2));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(255,255,255,16)); g2.setStroke(new BasicStroke(50f)); g2.drawOval(-30,-30,120,120);
                g2.setPaint(new GradientPaint(0,0,"BLOCKED".equals(c.status)?C_RED_D:C_PINK_D,getWidth(),0,"BLOCKED".equals(c.status)?C_RED:C_PINK));
                g2.fillRect(0,getHeight()-32,getWidth(),12);
                g2.setColor(new Color(255,215,0)); g2.fillRoundRect(14,18,34,26,6,6);
                g2.setColor(new Color(255,255,255,200)); g2.setFont(new Font("Monospaced",Font.BOLD,14));
                g2.drawString("ABCD BANK  \u2022  "+c.type.toUpperCase(),14,64);
                g2.setFont(new Font("Monospaced",Font.BOLD,18)); g2.drawString(c.cardNumber,14,90);
                g2.setFont(new Font("SansSerif",Font.PLAIN,10)); g2.setColor(new Color(255,255,255,160));
                g2.drawString("VALID THRU  "+c.expiryDate,14,112);
                g2.setFont(new Font("SansSerif",Font.BOLD,12)); g2.setColor(C_TPRIM);
                g2.drawString(c.network,getWidth()-70,getHeight()-14);
                if("BLOCKED".equals(c.status)){
                    g2.setColor(new Color(255,80,80,140)); g2.setFont(new Font("SansSerif",Font.BOLD,28));
                    g2.rotate(Math.toRadians(-20),getWidth()/2,getHeight()/2);
                    g2.drawString("BLOCKED",getWidth()/2-60,getHeight()/2+10);
                }
            }
        };
        cardArt.setOpaque(false); cardArt.setAlignmentX(0);
        cardArt.setPreferredSize(new Dimension(280,175));
        cardArt.setMinimumSize(new Dimension(280,175));
        cardArt.setMaximumSize(new Dimension(280,175));
        f.add(cardArt); f.add(Box.createVerticalStrut(14));

        String[][] rows = {
            {"Card ID",id},{"Client",cl!=null?cl.name:"?"},{"Account",c.accountId},
            {"Network",c.network},{"Type",c.type},{"Daily Limit",rs(c.dailyLimit)},
            {"Issued On",c.issueDate},{"Expiry",c.expiryDate},{"Status",c.status}
        };
        JPanel grid = new JPanel(new GridLayout(0,2,10,8)); grid.setOpaque(false);
        grid.setAlignmentX(0); grid.setMaximumSize(new Dimension(700,9999));
        for(String[] row:rows){
            JPanel kp=new JPanel(new BorderLayout()); kp.setOpaque(false);
            kp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(5,10,5,10)));
            kp.add(L(row[0],new Font("SansSerif",Font.BOLD,9),C_TMUT),BorderLayout.NORTH);
            kp.add(L(row[1],FB,C_TPRIM),BorderLayout.CENTER); grid.add(kp);
        }
        f.add(grid);
        d.setVisible(true);
    }

    // ── Credit Card Sub-Panel ────────────────────────────────
    static JPanel buildCCPanel(){
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(C_NAVY); p.setBorder(BorderFactory.createEmptyBorder(18,0,0,0));
        p.add(infoStrip(
            "\uD83D\uDCB8  Credit cards provide revolving credit.  Min due = 5% of outstanding.  Billing cycle: monthly.",
            C_CYAN));
        p.add(Box.createVerticalStrut(14));
        p.add(actBar("\uFF0B  Issue New Credit Card", e -> showIssueCCDlg()));
        ccModel = new DefaultTableModel(new String[]{
            "Card ID","Client","Account","Card Number","Network","Card Type","Credit Limit","Outstanding","Min Due","Due Date","Status"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl = mkTable(ccModel);
        setW(tbl,90,148,108,160,80,90,110,110,90,90,80);
        tbl.getColumnModel().getColumn(10).setCellRenderer(badge());
        tbl.getColumnModel().getColumn(4).setCellRenderer(badge());
        ctxMenu(tbl, new String[]{"Pay Outstanding","Block Card","Unblock Card","View Card"},
            new ActionListener[]{
                e->{ int r=tbl.getSelectedRow(); if(r>=0) payCreditCard((String)ccModel.getValueAt(r,0));   },
                e->{ int r=tbl.getSelectedRow(); if(r>=0) blockUnblockCC((String)ccModel.getValueAt(r,0),true);  },
                e->{ int r=tbl.getSelectedRow(); if(r>=0) blockUnblockCC((String)ccModel.getValueAt(r,0),false); },
                e->{ int r=tbl.getSelectedRow(); if(r>=0) viewCCDetails((String)ccModel.getValueAt(r,0)); }
            });
        p.add(tblCard("ALL CREDIT CARDS  (right-click: Pay / Block / View)", tbl));
        return p;
    }

    static void refreshCCTable(){
        if(ccModel==null) return;
        ccModel.setRowCount(0);
        for(CreditCard c: creditCards){
            Client cl=findC(c.clientId);
            ccModel.addRow(new Object[]{c.id,cl!=null?cl.name:"?",c.accountId,c.cardNumber,
                c.network,c.cardType,rs(c.creditLimit),rs(c.outstanding),rs(c.minDue),c.dueDate,c.status});
        }
    }

    static void showIssueCCDlg(){
        if(!canWrite()){ toast("\uD83D\uDD12  "+currentRole+" cannot issue credit cards."); return; }
        JDialog d = dlg("Issue Credit Card", 480, 440);
        JPanel f = dlgForm(d);
        JTextField cidF = mkField("Client ID");
        JComboBox<String> accountCombo = new JComboBox<>();
        JLabel statusLbl = L("Enter Client ID then press Tab",FS,C_TMUT);
        statusLbl.setAlignmentX(0); statusLbl.setMaximumSize(new Dimension(700,18));
        JComboBox<String> netC   = mkCombo(new String[]{"VISA","MasterCard","AmericanExpress","RuPay"});
        JComboBox<String> typeC  = mkCombo(new String[]{"Standard","Gold","Platinum","Signature"});
        JTextField limitF = mkField("Credit Limit (\u20B9, e.g. 100000)");
        cidF.addFocusListener(new FocusAdapter(){
            public void focusLost(FocusEvent e){ loadAccountsForClient(cidF.getText(),accountCombo,statusLbl); }
        });
        cidF.addActionListener(e->loadAccountsForClient(cidF.getText(),accountCombo,statusLbl));
        f.add(fRow("Client ID", cidF));
        f.add(statusLbl); f.add(Box.createVerticalStrut(4));
        f.add(fRow("Select Account", accountCombo));
        f.add(twoCol(fRow("Card Network", netC), fRow("Card Type", typeC)));
        f.add(fRow("Credit Limit (\u20B9, min \u20B910,000)", limitF));
        f.add(Box.createVerticalStrut(10));

        // Credit card preview
        JPanel prev = new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,new Color(0x0D,0x24,0x3A),getWidth(),getHeight(),new Color(0x00,0x80,0x80)));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.setColor(new Color(255,255,255,14)); g2.fillOval(getWidth()-80,-30,120,120);
                g2.setPaint(new GradientPaint(0,getHeight()-32,C_GOLD_D,getWidth(),getHeight()-32,C_GOLD));
                g2.fillRect(0,getHeight()-32,getWidth(),12);
                g2.setColor(new Color(255,215,0)); g2.fillRoundRect(14,18,34,26,6,6);
                g2.setFont(new Font("SansSerif",Font.BOLD,10)); g2.setColor(new Color(255,215,0,200));
                g2.drawString((String)typeC.getSelectedItem()+" CREDIT",14,62);
                g2.setFont(new Font("Monospaced",Font.BOLD,17)); g2.setColor(new Color(255,255,255,200));
                g2.drawString("**** **** **** ????",14,86);
                g2.setFont(new Font("SansSerif",Font.PLAIN,10)); g2.setColor(new Color(255,255,255,140));
                g2.drawString("VALID THRU  MM/YY",14,110);
                g2.setFont(new Font("SansSerif",Font.BOLD,12)); g2.setColor(C_GOLD);
                g2.drawString((String)netC.getSelectedItem(),getWidth()-82,getHeight()-14);
            }
        };
        prev.setOpaque(false); prev.setAlignmentX(0);
        prev.setPreferredSize(new Dimension(280,175));
        prev.setMinimumSize(new Dimension(280,175));
        prev.setMaximumSize(new Dimension(280,175));
        f.add(prev); f.add(Box.createVerticalStrut(14));
        netC.addActionListener(e2->prev.repaint()); typeC.addActionListener(e2->prev.repaint());

        RedButton issue = new RedButton("  \uD83D\uDCB8  Issue Credit Card  ");
        issue.setAlignmentX(0.5f); f.add(issue);
        issue.addActionListener(e -> {
            Client cl = findC(cidF.getText().trim().toUpperCase());
            if(cl==null){ showErr(d,"Client not found."); return; }
            String accId = getSelectedAccountId(accountCombo);
            if(accId.isEmpty()){ showErr(d,"Please select an account."); return; }
            BankAccount acc = findA(accId);
            if(acc==null||!acc.clientId.equals(cl.id)){ showErr(d,"Invalid account."); return; }
            if(!"VERIFIED".equals(cl.kyc)){ showErr(d,"KYC must be VERIFIED to issue credit card."); return; }
            double lim = parseDbl(limitF.getText());
            if(lim<10000){ showErr(d,"Minimum credit limit is \u20B910,000."); return; }
            String cardNum = String.format("%04d %04d %04d %04d",
                4000+(int)(Math.random()*999),1000+(int)(Math.random()*8999),
                1000+(int)(Math.random()*8999),1000+(int)(Math.random()*8999));
            String cId  = "CC"+String.format("%05d",CCARD_CTR++);
            String exp  = LocalDate.now().plusYears(5).format(DateTimeFormatter.ofPattern("MM/yy"));
            String due  = LocalDate.now().plusDays(25).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
            CreditCard cc = new CreditCard(cId,cl.id,acc.id,cardNum,
                (String)netC.getSelectedItem(),(String)typeC.getSelectedItem(),
                LocalDate.now().toString(), exp, lim);
            cc.dueDate = due; creditCards.add(cc);
            saveDatabase(); d.dispose(); refreshCards();
            toast("\uD83D\uDCB8  Credit Card "+cId+" issued!  Limit: "+rs(lim));
        });
        d.setVisible(true);
    }

    static void payCreditCard(String id){
        CreditCard c = creditCards.stream().filter(x->x.id.equals(id)).findFirst().orElse(null);
        if(c==null) return;
        if(c.outstanding<=0){ toast("No outstanding dues on this card."); return; }
        JDialog d = dlg("Pay Credit Card Bill \u2014 "+id, 400, 310);
        JPanel f = dlgForm(d);
        f.add(infoRow("Total Outstanding",  rs(c.outstanding)));
        f.add(infoRow("Minimum Due",        rs(c.minDue)));
        f.add(infoRow("Payment Due Date",   c.dueDate));
        JTextField amtF = mkField("Amount (\u20B9)");
        f.add(fRow("Payment Amount (\u20B9)", amtF));
        JTextField accF2 = mkField("Debit from Account No");
        f.add(fRow("Debit from Account", accF2));
        f.add(Box.createVerticalStrut(12));
        RedButton b = new RedButton("Pay Now"); b.setAlignmentX(0.5f); f.add(b);
        b.addActionListener(e -> {
            double amt = parseDbl(amtF.getText());
            if(amt<=0||amt>c.outstanding){ showErr(d,"Invalid amount. Max: "+rs(c.outstanding)); return; }
            BankAccount acc = findA(accF2.getText().trim().toUpperCase());
            if(acc==null||!"ACTIVE".equals(acc.status)){ showErr(d,"Invalid or inactive account."); return; }
            if(acc.balance<amt){ showErr(d,"Insufficient balance: "+rs(acc.balance)); return; }
            acc.balance-=amt; acc.addTxn("DEBIT",amt,"Credit Card Payment \u2014 "+id);
            c.outstanding-=amt; c.minDue=Math.max(0,c.outstanding*0.05);
            if(c.outstanding<=0){ c.outstanding=0; c.minDue=0; }
            saveDatabase(); d.dispose(); refreshCards(); refreshAccounts();
            toast("\u2705  Paid "+rs(amt)+" towards "+id+". Remaining: "+rs(c.outstanding));
        });
        d.setVisible(true);
    }

    static void blockUnblockCC(String id, boolean block){
        CreditCard c = creditCards.stream().filter(x->x.id.equals(id)).findFirst().orElse(null);
        if(c==null) return;
        if(block&&"BLOCKED".equals(c.status)){ toast("Card already blocked."); return; }
        if(!block&&"ACTIVE".equals(c.status)){  toast("Card is already active."); return; }
        c.status = block?"BLOCKED":"ACTIVE";
        saveDatabase(); refreshCards();
        toast((block?"\uD83D\uDEAB  Credit Card BLOCKED: ":"\u2705  Credit Card UNBLOCKED: ")+id);
    }

    static void viewCCDetails(String id){
        CreditCard c = creditCards.stream().filter(x->x.id.equals(id)).findFirst().orElse(null);
        if(c==null) return;
        Client cl = findC(c.clientId);
        JDialog d = dlg("Credit Card Details \u2014 "+id, 500, 480);
        JPanel f = dlgForm(d);

        JPanel cardArt = new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean blocked = "BLOCKED".equals(c.status);
                Color ca = blocked?new Color(0x3A,0x10,0x10):new Color(0x0D,0x24,0x3A);
                Color cb = blocked?new Color(0x7A,0x28,0x28):new Color(0x00,0x80,0x80);
                g2.setPaint(new GradientPaint(0,0,ca,getWidth(),getHeight(),cb));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(255,255,255,14)); g2.fillOval(getWidth()-80,-30,120,120);
                g2.setPaint(new GradientPaint(0,getHeight()-32,blocked?C_RED_D:C_GOLD_D,getWidth(),getHeight()-32,blocked?C_RED:C_GOLD));
                g2.fillRect(0,getHeight()-32,getWidth(),12);
                g2.setColor(new Color(255,215,0)); g2.fillRoundRect(14,18,34,26,6,6);
                g2.setFont(new Font("SansSerif",Font.BOLD,10)); g2.setColor(new Color(255,215,0,200));
                g2.drawString(c.cardType.toUpperCase()+" CREDIT  \u2022  ABCD BANK",14,62);
                g2.setFont(new Font("Monospaced",Font.BOLD,18)); g2.setColor(new Color(255,255,255,200));
                g2.drawString(c.cardNumber,14,90);
                g2.setFont(new Font("SansSerif",Font.PLAIN,10)); g2.setColor(new Color(255,255,255,140));
                g2.drawString("VALID THRU  "+c.expiryDate+"   "+cl!=null?cl.name.toUpperCase():"",14,114);
                g2.setFont(new Font("SansSerif",Font.BOLD,12)); g2.setColor(C_GOLD);
                g2.drawString(c.network,getWidth()-82,getHeight()-14);
                if(blocked){
                    g2.setColor(new Color(255,60,60,120)); g2.setFont(new Font("SansSerif",Font.BOLD,26));
                    g2.rotate(Math.toRadians(-18),getWidth()/2,getHeight()/2);
                    g2.drawString("BLOCKED",getWidth()/2-56,getHeight()/2+10);
                }
            }
        };
        cardArt.setOpaque(false); cardArt.setAlignmentX(0);
        cardArt.setPreferredSize(new Dimension(280,175));
        cardArt.setMinimumSize(new Dimension(280,175));
        cardArt.setMaximumSize(new Dimension(280,175));
        f.add(cardArt); f.add(Box.createVerticalStrut(14));

        // Usage bar
        double usedPct = c.creditLimit>0?c.outstanding/c.creditLimit*100:0;
        JPanel usageBar = new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BORDER2); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                int filled=(int)(getWidth()*usedPct/100);
                Color barC = usedPct>80?C_RED:usedPct>50?C_AMBER:C_GREEN;
                g2.setPaint(new GradientPaint(0,0,barC.darker(),filled,0,barC));
                g2.fillRoundRect(0,0,filled,getHeight(),8,8);
            }
        };
        usageBar.setOpaque(false); usageBar.setAlignmentX(0);
        usageBar.setPreferredSize(new Dimension(460,12));
        usageBar.setMinimumSize(new Dimension(100,12));
        usageBar.setMaximumSize(new Dimension(99999,12));
        JLabel usageLbl = L(String.format("Credit Usage: %.1f%%  |  Used: %s  |  Available: %s",
            usedPct, rs(c.outstanding), rs(c.creditLimit-c.outstanding)),
            new Font("SansSerif",Font.PLAIN,10), usedPct>80?C_RED_L:usedPct>50?C_AMBER:C_GREEN);
        usageLbl.setAlignmentX(0); usageLbl.setMaximumSize(new Dimension(99999,18));
        f.add(usageBar); f.add(Box.createVerticalStrut(4)); f.add(usageLbl); f.add(Box.createVerticalStrut(10));

        String[][] rows={
            {"Card ID",id},{"Client",cl!=null?cl.name:"?"},{"Account",c.accountId},
            {"Network",c.network},{"Card Type",c.cardType},{"Credit Limit",rs(c.creditLimit)},
            {"Outstanding",rs(c.outstanding)},{"Minimum Due",rs(c.minDue)},
            {"Due Date",c.dueDate},{"Issued",c.issueDate},{"Expiry",c.expiryDate},{"Status",c.status}
        };
        JPanel grid=new JPanel(new GridLayout(0,2,10,8)); grid.setOpaque(false);
        grid.setAlignmentX(0); grid.setMaximumSize(new Dimension(700,9999));
        for(String[] row:rows){
            JPanel kp=new JPanel(new BorderLayout()); kp.setOpaque(false);
            kp.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(5,10,5,10)));
            kp.add(L(row[0],new Font("SansSerif",Font.BOLD,9),C_TMUT),BorderLayout.NORTH);
            kp.add(L(row[1],FB,C_TPRIM),BorderLayout.CENTER); grid.add(kp);
        }
        f.add(grid); d.setVisible(true);
    }

    // ── Cheque Book Sub-Panel ────────────────────────────────
    static JPanel buildChqPanel(){
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(C_NAVY); p.setBorder(BorderFactory.createEmptyBorder(18,0,0,0));
        p.add(infoStrip(
            "\uD83D\uDCDD  Cheque books issued in lots of 10, 20, 25 or 50 leaves.  First cheque number assigned sequentially.",
            C_PURPLE));
        p.add(Box.createVerticalStrut(14));
        p.add(actBar("\uFF0B  Issue Cheque Book", e -> showIssueChequeBookDlg()));
        chqModel = new DefaultTableModel(new String[]{
            "Book ID","Client","Account","Book No","Leaves","Cheque From","Cheque To","Used","Issued On","Status"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl = mkTable(chqModel);
        setW(tbl,90,148,108,90,70,110,110,60,100,80);
        tbl.getColumnModel().getColumn(9).setCellRenderer(badge());
        ctxMenu(tbl, new String[]{"Mark Leaf Used","Stop Cheque Book","Reorder Cheque Book"},
            new ActionListener[]{
                e->{ int r=tbl.getSelectedRow(); if(r>=0) useChequLeaf((String)chqModel.getValueAt(r,0)); },
                e->{ int r=tbl.getSelectedRow(); if(r>=0) stopChequeBook((String)chqModel.getValueAt(r,0)); },
                e->{ int r=tbl.getSelectedRow(); if(r>=0) reorderChequeBook((String)chqModel.getValueAt(r,0)); }
            });
        p.add(tblCard("ALL CHEQUE BOOKS  (right-click to mark leaf used / stop / reorder)", tbl));
        return p;
    }

    static void refreshChqTable(){
        if(chqModel==null) return;
        chqModel.setRowCount(0);
        for(ChequeBook c: chequeBooks){
            Client cl=findC(c.clientId);
            chqModel.addRow(new Object[]{c.id,cl!=null?cl.name:"?",c.accountId,
                c.bookNumber,c.leaves,c.startCheque,c.endCheque,
                c.usedLeaves+"/"+c.leaves,c.issueDate,c.status});
        }
    }

    static void showIssueChequeBookDlg(){
        if(!canWrite()){ toast("\uD83D\uDD12  "+currentRole+" cannot issue cheque books."); return; }
        JDialog d = dlg("Issue Cheque Book", 460, 400);
        JPanel f = dlgForm(d);
        JTextField cidF = mkField("Client ID");
        JComboBox<String> accountCombo = new JComboBox<>();
        JLabel statusLbl = L("Enter Client ID then press Tab",FS,C_TMUT);
        statusLbl.setAlignmentX(0); statusLbl.setMaximumSize(new Dimension(700,18));
        JComboBox<String> leavesC = mkCombo(new String[]{"10 Leaves","20 Leaves","25 Leaves","50 Leaves"});
        cidF.addFocusListener(new FocusAdapter(){
            public void focusLost(FocusEvent e){ loadAccountsForClient(cidF.getText(),accountCombo,statusLbl); }
        });
        cidF.addActionListener(e->loadAccountsForClient(cidF.getText(),accountCombo,statusLbl));
        f.add(fRow("Client ID", cidF));
        f.add(statusLbl); f.add(Box.createVerticalStrut(4));
        f.add(fRow("Select Account", accountCombo));
        f.add(fRow("Number of Leaves", leavesC));
        f.add(Box.createVerticalStrut(10));

        // Cheque book preview
        JPanel prev = new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Book spine
                g2.setColor(new Color(0x1E,0x35,0x52)); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                // Cheque pages
                for(int i=4;i>=0;i--){
                    int ox=i*4, oy=i*3;
                    g2.setColor(new Color(0xF8,0xF5,0xE8)); g2.fillRoundRect(ox+20,oy+8,getWidth()-40,getHeight()-24,8,8);
                    g2.setColor(new Color(0xCC,0xCC,0xCC)); g2.drawRoundRect(ox+20,oy+8,getWidth()-40,getHeight()-24,8,8);
                }
                // Top cheque
                g2.setColor(new Color(0xF8,0xF8,0xF0)); g2.fillRoundRect(40,20,getWidth()-60,getHeight()-40,8,8);
                g2.setColor(C_BORDER2); g2.drawRoundRect(40,20,getWidth()-60,getHeight()-40,8,8);
                // Top color stripe
                g2.setPaint(new GradientPaint(40,20,new Color(0x1A,0x3A,0x6A),getWidth()-20,20,new Color(0x2A,0x5A,0xAA)));
                g2.fillRoundRect(40,20,getWidth()-60,18,8,8);
                g2.setColor(Color.WHITE); g2.setFont(new Font("SansSerif",Font.BOLD,8));
                g2.drawString("ABCD BANK",54,32);
                g2.setColor(new Color(0x33,0x33,0x33)); g2.setFont(new Font("SansSerif",Font.PLAIN,8));
                g2.drawString("PAY ____________________",52,52);
                g2.drawString("AMOUNT \u20B9 _______________",52,68);
                g2.setColor(C_TMUT); g2.drawString("Cheque No: 000001  |  MICR",52,84);
                g2.setColor(new Color(0x1A,0x3A,0x6A)); g2.setFont(new Font("Monospaced",Font.BOLD,9));
                g2.drawString("\u2015\u2015 MICR BAND \u2015\u2015",52,96);
            }
        };
        prev.setOpaque(false); prev.setAlignmentX(0);
        prev.setPreferredSize(new Dimension(280,175));
        prev.setMinimumSize(new Dimension(280,175));
        prev.setMaximumSize(new Dimension(280,175));
        f.add(prev); f.add(Box.createVerticalStrut(14));

        RedButton issue = new RedButton("  \uD83D\uDCDD  Issue Cheque Book  ");
        issue.setAlignmentX(0.5f); f.add(issue);
        issue.addActionListener(e -> {
            Client cl = findC(cidF.getText().trim().toUpperCase());
            if(cl==null){ showErr(d,"Client not found."); return; }
            String accId = getSelectedAccountId(accountCombo);
            if(accId.isEmpty()){ showErr(d,"Please select an account."); return; }
            BankAccount acc = findA(accId);
            if(acc==null||!acc.clientId.equals(cl.id)){ showErr(d,"Invalid account."); return; }
            if(!"SAVINGS".equals(acc.type)&&!"CURRENT".equals(acc.type)){
                showErr(d,"Cheque books only for Savings / Current accounts."); return; }
            int[] leafOpts = {10,20,25,50};
            int lv = leafOpts[leavesC.getSelectedIndex()];
            String cId  = "CHQ"+String.format("%05d",CHQ_CTR++);
            String bNum = "BK"+String.format("%06d",CHQ_CTR);
            // Determine start cheque number
            int maxEnd = chequeBooks.stream()
                .filter(x->x.accountId.equals(acc.id))
                .mapToInt(x->{ try{return Integer.parseInt(x.endCheque);}catch(Exception ex){return 0;} })
                .max().orElse(0);
            String startNo = String.format("%06d", maxEnd+1);
            String endNo   = String.format("%06d", maxEnd+lv);
            ChequeBook cb = new ChequeBook(cId,cl.id,acc.id,bNum,lv,startNo,endNo,LocalDate.now().toString());
            chequeBooks.add(cb);
            saveDatabase(); d.dispose(); refreshCards();
            toast("\uD83D\uDCDD  Cheque Book "+cId+" issued!  Cheques "+startNo+" to "+endNo);
        });
        d.setVisible(true);
    }

    static void useChequLeaf(String id){
        ChequeBook c = chequeBooks.stream().filter(x->x.id.equals(id)&&"ACTIVE".equals(x.status)).findFirst().orElse(null);
        if(c==null){ toast("Active cheque book not found."); return; }
        if(c.usedLeaves>=c.leaves){ toast("All leaves used. Please reorder."); return; }
        c.usedLeaves++;
        if(c.usedLeaves>=c.leaves) c.status="EXHAUSTED";
        saveDatabase(); refreshCards();
        toast("Cheque leaf marked used. "+c.usedLeaves+"/"+c.leaves+" leaves used."+
            (c.usedLeaves>=c.leaves?" Book exhausted \u2014 please reorder.":""));
    }

    static void stopChequeBook(String id){
        ChequeBook c = chequeBooks.stream().filter(x->x.id.equals(id)).findFirst().orElse(null);
        if(c==null) return;
        if("STOPPED".equals(c.status)){ toast("Cheque book already stopped."); return; }
        if(JOptionPane.showConfirmDialog(mainFrame,
            "Stop cheque book "+id+"?\nAll remaining "+( c.leaves-c.usedLeaves)+" unused cheques will be invalidated.",
            "Stop Cheque Book",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        c.status="STOPPED";
        saveDatabase(); refreshCards();
        toast("\uD83D\uDEAB  Cheque book "+id+" stopped. Unused leaves invalidated.");
    }

    static void reorderChequeBook(String id){
        ChequeBook old = chequeBooks.stream().filter(x->x.id.equals(id)).findFirst().orElse(null);
        if(old==null) return;
        if("ACTIVE".equals(old.status)&&old.usedLeaves<old.leaves/2){
            toast("Cannot reorder \u2014 old book still has "+(old.leaves-old.usedLeaves)+" unused leaves."); return; }
        if(JOptionPane.showConfirmDialog(mainFrame,
            "Reorder cheque book for account "+old.accountId+"?\nSame "+old.leaves+" leaves, new serial numbers.",
            "Reorder Cheque Book",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        int maxEnd = chequeBooks.stream().filter(x->x.accountId.equals(old.accountId))
            .mapToInt(x->{try{return Integer.parseInt(x.endCheque);}catch(Exception ex){return 0;}}).max().orElse(0);
        String startNo = String.format("%06d", maxEnd+1);
        String endNo   = String.format("%06d", maxEnd+old.leaves);
        String nId = "CHQ"+String.format("%05d",CHQ_CTR++);
        String bNum= "BK"+String.format("%06d",CHQ_CTR);
        chequeBooks.add(new ChequeBook(nId,old.clientId,old.accountId,bNum,old.leaves,startNo,endNo,LocalDate.now().toString()));
        saveDatabase(); refreshCards();
        toast("\uD83D\uDCDD  New cheque book "+nId+" issued. Cheques "+startNo+" \u2192 "+endNo);
    }

    // ── Unified refresh for cards page ───────────────────────
    static void refreshCards(){
        refreshATMTable();
        refreshCCTable();
        refreshChqTable();
        // Update summary counters in page (find by traversal)
        if(contentArea==null) return;
        for(Component c: contentArea.getComponents()){
            if(c instanceof JPanel){
                JLabel sa=(JLabel)((JPanel)c).getClientProperty("sAtm");
                JLabel sc=(JLabel)((JPanel)c).getClientProperty("sCred");
                JLabel sq=(JLabel)((JPanel)c).getClientProperty("sChq");
                if(sa!=null) sa.setText(String.valueOf(atmCards.size()));
                if(sc!=null) sc.setText(String.valueOf(creditCards.stream().filter(x->"ACTIVE".equals(x.status)).count()));
                if(sq!=null) sq.setText(String.valueOf(chequeBooks.stream().filter(x->"ACTIVE".equals(x.status)).count()));
            }
        }
    }

    // ── Info strip helper ────────────────────────────────────
    static JPanel infoStrip(String msg, Color accent){
        JPanel strip = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),18));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),70));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.setPaint(new GradientPaint(0,0,accent,0,getHeight(),new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),40)));
                g2.fillRoundRect(0,0,4,getHeight(),4,4);
            }
        };
        strip.setOpaque(false); strip.setAlignmentX(0);
        strip.setMaximumSize(new Dimension(99999,36));
        strip.setBorder(BorderFactory.createEmptyBorder(7,16,7,16));
        JLabel lbl = L(msg, new Font("SansSerif",Font.PLAIN,11), C_TSEC);
        strip.add(lbl, BorderLayout.CENTER);
        return strip;
    }

    // ════════════════════════════════════════════════════════
    //  NEW REGISTRATION
    // ════════════════════════════════════════════════════════
    static JPanel buildRegister(){
        JPanel outer=page("New Customer Registration");
        RoundPanel card=new RoundPanel(C_CARD,14);
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(26,28,30,28)));
        card.setAlignmentX(0); card.setMaximumSize(new Dimension(760,9999));
        card.add(stepLbl("STEP 1 \u2014 PERSONAL DETAILS")); card.add(Box.createVerticalStrut(12));
        JTextField nameF=mkField("Full Name"),phoneF=mkField("Mobile Number"),emailF=mkField("Email Address"),dobF=mkField("Date of Birth (YYYY-MM-DD)"),panF=mkField("PAN (10 chars)"),aadharF=mkField("12-digit Aadhar"),cityF=mkField("City"),occF=mkField("Occupation"),incF=mkField("Annual Income (\u20B9)");
        card.add(twoCol(fRow("Full Name",nameF),fRow("Mobile",phoneF)));
        card.add(twoCol(fRow("Email",emailF),fRow("Date of Birth",dobF)));
        card.add(twoCol(fRow("PAN Number",panF),fRow("Aadhar Number",aadharF)));
        card.add(twoCol(fRow("City",cityF),fRow("Occupation",occF)));
        card.add(fRow("Annual Income (\u20B9)",incF));
        card.add(Box.createVerticalStrut(18));
        card.add(stepLbl("STEP 2 \u2014 ACCOUNT DETAILS")); card.add(Box.createVerticalStrut(12));
        JComboBox<String> typeC=mkCombo(new String[]{"SAVINGS \u2014 Min \u20B91,000 | 3.5% p.a.","CURRENT \u2014 Min \u20B95,000 | 0%","SALARY \u2014 No min | 2.5% p.a."});
        JTextField depF=mkField("Initial Deposit (\u20B9)"),nomF=mkField("Nominee Name");
        card.add(fRow("Account Type",typeC)); card.add(twoCol(fRow("Initial Deposit (\u20B9)",depF),fRow("Nominee Name",nomF)));
        card.add(Box.createVerticalStrut(18));
        card.add(stepLbl("STEP 3 \u2014 DOCUMENTATION CHECKLIST")); card.add(Box.createVerticalStrut(10));
        JCheckBox[] chks={mkChk("KYC Documents Collected"),mkChk("Photographs Collected"),mkChk("Account Opening Form Signed"),mkChk("FATCA / Self Declaration Signed")};
        JPanel cg=new JPanel(new GridLayout(2,2,14,8)); cg.setOpaque(false); cg.setAlignmentX(0); cg.setMaximumSize(new Dimension(700,72));
        for(JCheckBox c:chks) cg.add(c); card.add(cg); card.add(Box.createVerticalStrut(20));
        RedButton createBtn=new RedButton("  \u2713  Create Customer Account"); createBtn.setAlignmentX(0); createBtn.setMaximumSize(new Dimension(270,46)); card.add(createBtn);
        outer.add(card);
        createBtn.addActionListener(e->{
            String name=nameF.getText().trim(),phone=phoneF.getText().trim(),pan=panF.getText().trim().toUpperCase();
            if(name.isEmpty()||phone.isEmpty()||pan.length()!=10){toast("Name, phone & valid PAN required.");return;}
            for(JCheckBox c:chks){if(!c.isSelected()){toast("All documentation must be confirmed.");return;}}
            int ti=typeC.getSelectedIndex(); double[] mins={1000,5000,0}; double[] rats={3.5,0,2.5}; String[] pres={"SB","CA","SL"}; String[] types={"SAVINGS","CURRENT","SALARY"};
            double dep=parseDbl(depF.getText()); if(dep<mins[ti]){toast("Min deposit for "+types[ti]+": "+rs(mins[ti]));return;}
            String cid="CLT"+String.format("%05d",CC++);
            clients.add(new Client(cid,name,phone,emailF.getText().trim(),cityF.getText().trim(),pan,aadharF.getText().trim(),occF.getText().trim(),parseDbl(incF.getText()),"VERIFIED","MODERATE",LocalDate.now().toString()));
            String aId=pres[ti]+String.format("%09d",AC++);
            BankAccount acc=new BankAccount(aId,cid,types[ti],dep,mins[ti],rats[ti],LocalDate.now().toString());
            acc.nominee=nomF.getText().trim(); acc.addTxn("CREDIT",dep,"Account Opening"); accounts.add(acc);
            clients.stream().filter(c2->c2.id.equals(cid)).findFirst().ifPresent(c2->c2.accountIds.add(aId));
            for(JTextField tf:new JTextField[]{nameF,phoneF,emailF,dobF,panF,aadharF,cityF,occF,incF,depF,nomF})tf.setText("");
            for(JCheckBox c:chks)c.setSelected(false); saveDatabase(); toast("\u2713  Account "+aId+" created for "+name+" ("+cid+") \u2014 auto-saved!");
        });
        return outer;
    }

    // ════════════════════════════════════════════════════════
    //  STAFF
    // ════════════════════════════════════════════════════════
     static JPanel buildStaff(){
    JPanel p = page("Staff Management");
    if(!canAccess("staff")){
        RoundPanel denied=new RoundPanel(C_CARD,14);
        denied.setLayout(new BoxLayout(denied,BoxLayout.Y_AXIS));
        denied.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_RED,1,true),
            BorderFactory.createEmptyBorder(30,30,30,30)));
        denied.setAlignmentX(0); denied.setMaximumSize(new Dimension(500,120));
        denied.add(L("\uD83D\uDD12  Access Denied",new Font("Serif",Font.BOLD,20),C_RED_L));
        denied.add(Box.createVerticalStrut(10));
        denied.add(L("Only ADMIN and MANAGER can access Staff Management.",FB,C_TSEC));
        p.add(denied); return p;
    }

    // ── Action bar ──────────────────────────────────────
    if(isAdmin()){
        JPanel aBar=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        aBar.setOpaque(false); aBar.setAlignmentX(0);
        aBar.setMaximumSize(new Dimension(99999,48));
        aBar.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));
        RedButton addBtn=new RedButton("  \uFF0B  Hire New Staff  ");
        addBtn.addActionListener(e->showHireStaffDlg());
        aBar.add(addBtn);
        p.add(aBar);
    }

    // ── Default staff table ─────────────────────────────
    DefaultTableModel sm=new DefaultTableModel(
        new String[]{"Username","Password","Role","Access Level","Branch"},0){
        public boolean isCellEditable(int r,int c){return false;}};
    sm.addRow(new Object[]{"admin",  "admin123",   "ADMIN",   "Full System Access","All Branches"});
    sm.addRow(new Object[]{"manager","manager123", "MANAGER", "No Staff Mgmt",     "All Branches"});
    sm.addRow(new Object[]{"officer","officer123", "OFFICER", "No Freeze / No Staff","Regional"});
    sm.addRow(new Object[]{"clerk",  "clerk123",   "CLERK",   "View Only",         "Local Branch"});
    JTable st=mkTable(sm);
    setW(st,100,110,90,180,120);
    st.getColumnModel().getColumn(2).setCellRenderer(badge());
    p.add(tblCard("DEFAULT SYSTEM STAFF",st));
    p.add(Box.createVerticalStrut(16));

    // ── Custom hired staff table ────────────────────────
    DefaultTableModel hm=new DefaultTableModel(
        new String[]{"Staff ID","Full Name","Username","Role","Branch",
                     "Permissions","Phone","Status","Joined"},0){
        public boolean isCellEditable(int r,int c){return false;}};
    JTable ht=mkTable(hm);
    setW(ht,90,160,110,100,110,240,110,80,100);
    ht.getColumnModel().getColumn(3).setCellRenderer(badge());
    ht.getColumnModel().getColumn(7).setCellRenderer(badge());

    if(isAdmin()){
        ctxMenu(ht,
            new String[]{"Edit Staff","Deactivate / Activate","Reset Password",
                         "View Permissions","Delete Staff"},
            new ActionListener[]{
                e->{ int r=ht.getSelectedRow();
                     if(r>=0) editStaffDlg((String)hm.getValueAt(r,0)); },
                e->{ int r=ht.getSelectedRow();
                     if(r>=0) toggleStaffStatus((String)hm.getValueAt(r,0),hm); },
                e->{ int r=ht.getSelectedRow();
                     if(r>=0) resetStaffPassword((String)hm.getValueAt(r,0)); },
                e->{ int r=ht.getSelectedRow();
                     if(r>=0) viewStaffPermissions((String)hm.getValueAt(r,0)); },
                e->{ int r=ht.getSelectedRow();
                     if(r>=0) deleteStaff((String)hm.getValueAt(r,0),hm); }
            });
    }

    refreshHiredStaffTable(hm);
    p.add(tblCard("HIRED STAFF  (right-click to manage)",ht));
    p.add(Box.createVerticalStrut(16));

    // ── Permissions matrix ──────────────────────────────
    DefaultTableModel pm=new DefaultTableModel(
        new String[]{"Role","Clients","Accounts","FD/RD/MF",
                     "Loans","Transactions","Staff Mgmt","Register","Daily Summary"},0){
        public boolean isCellEditable(int r,int c){return false;}};
    pm.addRow(new Object[]{"CLERK",     "View","View","View","View","View","No","Yes","Yes"});
    pm.addRow(new Object[]{"CASHIER",   "View","Txn Only","No","No","View","No","No","Yes"});
    pm.addRow(new Object[]{"FORM_FILLER","View","No","No","No","No","No","Yes","No"});
    pm.addRow(new Object[]{"OFFICER",   "Full","Create","Full","Full","Full","No","Yes","No"});
    pm.addRow(new Object[]{"MANAGER",   "Full","Full+Freeze","Full","Full","Full","View","Yes","No"});
    pm.addRow(new Object[]{"ADMIN",     "Full","Full","Full","Full","Full","Full","Yes","Yes"});
    JTable pt=mkTable(pm);
    setW(pt,100,80,110,90,80,110,90,80,110);
    p.add(tblCard("ROLE PERMISSIONS MATRIX",pt));
    p.add(Box.createVerticalStrut(16));

    // ── DB info ─────────────────────────────────────────
    RoundPanel dbInfo=new RoundPanel(C_PANEL,10);
    dbInfo.setLayout(new BoxLayout(dbInfo,BoxLayout.Y_AXIS));
    dbInfo.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(C_BORDER2,1,true),
        BorderFactory.createEmptyBorder(14,18,14,18)));
    dbInfo.setAlignmentX(0); dbInfo.setMaximumSize(new Dimension(99999,100));
    dbInfo.add(L("DATABASE INFORMATION",new Font("SansSerif",Font.BOLD,10),C_TMUT));
    dbInfo.add(Box.createVerticalStrut(10));
    dbInfo.add(infoRow("Storage Type","CSV flat-file  (no external driver needed)"));
    dbInfo.add(infoRow("Database Path",new File(DB_DIR).getAbsolutePath()));
    dbInfo.add(infoRow("Auto-save","On every create / edit / delete / transaction"));
    p.add(dbInfo);
    return p;
}

static void refreshHiredStaffTable(DefaultTableModel hm){
    hm.setRowCount(0);
    for(StaffMember s: staffMembers){
        hm.addRow(new Object[]{
            s.id, s.fullName, s.username, s.role, s.branch,
            String.join(", ", s.permissions),
            s.phone, s.status, s.createdOn
        });
    }
}

// ── All available permissions ────────────────────────
static final String[] ALL_PERMISSIONS = {
    "View Clients", "Add Clients", "Edit Clients", "Delete Clients",
    "View Accounts", "Open Accounts", "Deposit", "Withdraw", "Transfer",
    "View FD", "Create FD", "Break FD",
    "View RD", "Create RD", "Pay RD",
    "View MF", "Add MF", "Redeem MF",
    "View Loans", "Disburse Loans", "Pay EMI",
    "View Transactions", "View Daily Summary",
    "Issue ATM Card", "Issue Credit Card", "Issue Cheque Book",
    "Register Customer", "View Staff", "Send Internal Email"
};

static final java.util.Map<String,String[]> ROLE_DEFAULT_PERMISSIONS =
    new java.util.HashMap<String,String[]>(){{
        put("CASHIER",     new String[]{"View Clients","View Accounts",
                                        "Deposit","Withdraw","Transfer",
                                        "View Transactions","View Daily Summary"});
        put("CLERK",       new String[]{"View Clients","View Accounts",
                                        "View FD","View RD","View MF",
                                        "View Loans","View Transactions",
                                        "View Daily Summary","Register Customer",
                                        "Send Internal Email"});
        put("FORM_FILLER", new String[]{"View Clients","Add Clients",
                                        "Register Customer"});
        put("OFFICER",     new String[]{"View Clients","Add Clients","Edit Clients",
                                        "View Accounts","Open Accounts",
                                        "Deposit","Withdraw","Transfer",
                                        "View FD","Create FD","Break FD",
                                        "View RD","Create RD","Pay RD",
                                        "View MF","Add MF","Redeem MF",
                                        "View Loans","Disburse Loans","Pay EMI",
                                        "View Transactions","Register Customer",
                                        "Send Internal Email"});
        put("MANAGER",     new String[]{"View Clients","Add Clients","Edit Clients",
                                        "View Accounts","Open Accounts",
                                        "Deposit","Withdraw","Transfer",
                                        "View FD","Create FD","Break FD",
                                        "View RD","Create RD","Pay RD",
                                        "View MF","Add MF","Redeem MF",
                                        "View Loans","Disburse Loans","Pay EMI",
                                        "View Transactions","View Staff",
                                        "Register Customer","Send Internal Email"});
        put("CUSTOM",      new String[]{});
    }};

static void showHireStaffDlg(){
    if(!isAdmin()){ toast("\uD83D\uDD12  Only ADMIN can hire staff."); return; }
    JDialog d = dlg("Hire New Staff Member", 620, 720);
    JPanel f  = dlgForm(d);

    f.add(stepLbl("PERSONAL DETAILS"));
    f.add(Box.createVerticalStrut(10));
    JTextField fullNameF = mkField("Full Name");
    JTextField emailF    = mkField("Email Address");
    JTextField phoneF    = mkField("Phone Number");
    JTextField branchF   = mkField("Branch / Department");
    f.add(twoCol(fRow("Full Name",    fullNameF), fRow("Phone",  phoneF)));
    f.add(twoCol(fRow("Email",        emailF),    fRow("Branch", branchF)));
    f.add(Box.createVerticalStrut(14));

    f.add(stepLbl("LOGIN CREDENTIALS"));
    f.add(Box.createVerticalStrut(10));
    JTextField usernameF = mkField("Username (unique)");
    JTextField passwordF = mkField("Password");
    f.add(twoCol(fRow("Username", usernameF), fRow("Password", passwordF)));
    f.add(Box.createVerticalStrut(14));

    f.add(stepLbl("ROLE & PERMISSIONS"));
    f.add(Box.createVerticalStrut(10));
    String[] roles = {"CASHIER","CLERK","FORM_FILLER","OFFICER","MANAGER","CUSTOM"};
    JComboBox<String> roleC = mkCombo(roles);
    f.add(fRow("Select Role  (auto-fills permissions below)", roleC));
    f.add(Box.createVerticalStrut(10));

    JLabel permLbl = L("PERMISSIONS  —  auto-selected by role, customise freely",
        new Font("SansSerif",Font.BOLD,9), C_TMUT);
    permLbl.setAlignmentX(0); permLbl.setMaximumSize(new Dimension(700,16));
    f.add(permLbl); f.add(Box.createVerticalStrut(6));

    JPanel permGrid = new JPanel(new GridLayout(0,3,8,6));
    permGrid.setOpaque(false); permGrid.setAlignmentX(0);
    permGrid.setMaximumSize(new Dimension(700,9999));
    permGrid.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(C_BORDER2,1,true),
        BorderFactory.createEmptyBorder(10,12,10,12)));

    JCheckBox[] permBoxes = new JCheckBox[ALL_PERMISSIONS.length];
    for(int i=0;i<ALL_PERMISSIONS.length;i++){
        permBoxes[i] = mkChk(ALL_PERMISSIONS[i]);
        permGrid.add(permBoxes[i]);
    }
    f.add(permGrid); f.add(Box.createVerticalStrut(6));

    JPanel quickBtns = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
    quickBtns.setOpaque(false); quickBtns.setAlignmentX(0);
    quickBtns.setMaximumSize(new Dimension(700,36));
    RedButton selAll = new RedButton("  Select All  ");
    RedButton clrAll = new RedButton("  Clear All  ");
    selAll.addActionListener(e->{ for(JCheckBox cb:permBoxes) cb.setSelected(true); });
    clrAll.addActionListener(e->{ for(JCheckBox cb:permBoxes) cb.setSelected(false); });
    quickBtns.add(selAll); quickBtns.add(clrAll);
    f.add(quickBtns); f.add(Box.createVerticalStrut(14));

    // Auto-fill on role change
    roleC.addActionListener(e->{
        String selRole = (String)roleC.getSelectedItem();
        String[] defaults = ROLE_DEFAULT_PERMISSIONS.getOrDefault(selRole, new String[]{});
        java.util.Set<String> defSet = new java.util.HashSet<>(Arrays.asList(defaults));
        for(int i=0;i<ALL_PERMISSIONS.length;i++)
            permBoxes[i].setSelected(defSet.contains(ALL_PERMISSIONS[i]));
    });
    // Trigger initial fill
    roleC.setSelectedIndex(0);
    roleC.getActionListeners()[0].actionPerformed(null);

    RedButton hireBtn = new RedButton("  \u2713  Hire Staff Member  ");
    hireBtn.setAlignmentX(0.5f); f.add(hireBtn);

    hireBtn.addActionListener(e->{
        String fullName = fullNameF.getText().trim();
        String username = usernameF.getText().trim().toLowerCase();
        String password = passwordF.getText().trim();
        String branch   = branchF.getText().trim();
        String phone    = phoneF.getText().trim();
        String email    = emailF.getText().trim();
        String role     = (String)roleC.getSelectedItem();

        if(fullName.isEmpty()){ showErr(d,"Full Name is required."); return; }
        if(username.isEmpty()){ showErr(d,"Username is required."); return; }
        if(password.isEmpty()){ showErr(d,"Password is required."); return; }
        if(branch.isEmpty()){   showErr(d,"Branch is required."); return; }

        boolean exists = STAFF_CREDENTIALS.containsKey(username) ||
            staffMembers.stream().anyMatch(s->s.username.equals(username));
        if(exists){ showErr(d,"Username '"+username+"' already exists."); return; }

        List<String> perms = new ArrayList<>();
        for(int i=0;i<ALL_PERMISSIONS.length;i++)
            if(permBoxes[i].isSelected()) perms.add(ALL_PERMISSIONS[i]);
        if(perms.isEmpty()){ showErr(d,"Assign at least one permission."); return; }

        String sId = "STF"+String.format("%05d",STAFF_CTR++);
        StaffMember sm = new StaffMember(sId,username,password,role,branch,fullName,email,phone);
        sm.permissions.addAll(perms);
        staffMembers.add(sm);
        saveDatabase(); d.dispose();
        // Rebuild staff panel
        for(int i=0;i<contentArea.getComponentCount();i++){
            if(contentArea.getComponent(i) instanceof JPanel){
                JPanel panel=(JPanel)contentArea.getComponent(i);
                if(panel.getClientProperty("pageTitle")!=null&&
                   panel.getClientProperty("pageTitle").equals("staff")){
                    contentArea.remove(i); break;
                }
            }
        }
        contentArea.add(buildStaff(),"staff");
        showSection("staff",
            java.util.Arrays.asList("dashboard","clients","accounts","transactions",
                "fds","rds","mfs","loans","cards","daily","register","staff","inbox")
                .indexOf("staff"));
        toast("\uD83D\uDC64  "+fullName+" hired as "+role+"!  Login: "+username);
    });
    d.setVisible(true);
}

static void editStaffDlg(String staffId){
    if(!isAdmin()){ toast("\uD83D\uDD12  Only ADMIN can edit staff."); return; }
    StaffMember s = staffMembers.stream()
        .filter(x->x.id.equals(staffId)).findFirst().orElse(null);
    if(s==null) return;

    JDialog d = dlg("Edit Staff \u2014 "+staffId, 620, 680);
    JPanel f  = dlgForm(d);

    f.add(stepLbl("PERSONAL DETAILS"));
    f.add(Box.createVerticalStrut(10));
    JTextField fullNameF = mkField("Full Name");   fullNameF.setText(s.fullName);
    JTextField emailF    = mkField("Email");        emailF.setText(s.email);
    JTextField phoneF    = mkField("Phone");        phoneF.setText(s.phone);
    JTextField branchF   = mkField("Branch");       branchF.setText(s.branch);
    f.add(twoCol(fRow("Full Name", fullNameF), fRow("Phone",  phoneF)));
    f.add(twoCol(fRow("Email",     emailF),    fRow("Branch", branchF)));
    f.add(Box.createVerticalStrut(14));

    f.add(stepLbl("ROLE & PERMISSIONS"));
    f.add(Box.createVerticalStrut(10));
    String[] roles = {"CASHIER","CLERK","FORM_FILLER","OFFICER","MANAGER","CUSTOM"};
    JComboBox<String> roleC = mkCombo(roles);
    roleC.setSelectedItem(s.role);
    f.add(fRow("Role", roleC));
    f.add(Box.createVerticalStrut(10));

    JPanel permGrid = new JPanel(new GridLayout(0,3,8,6));
    permGrid.setOpaque(false); permGrid.setAlignmentX(0);
    permGrid.setMaximumSize(new Dimension(700,9999));
    permGrid.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(C_BORDER2,1,true),
        BorderFactory.createEmptyBorder(10,12,10,12)));

    JCheckBox[] permBoxes = new JCheckBox[ALL_PERMISSIONS.length];
    java.util.Set<String> curPerms = new java.util.HashSet<>(s.permissions);
    for(int i=0;i<ALL_PERMISSIONS.length;i++){
        permBoxes[i] = mkChk(ALL_PERMISSIONS[i]);
        permBoxes[i].setSelected(curPerms.contains(ALL_PERMISSIONS[i]));
        permGrid.add(permBoxes[i]);
    }
    f.add(permGrid); f.add(Box.createVerticalStrut(6));

    JPanel quickBtns=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
    quickBtns.setOpaque(false); quickBtns.setAlignmentX(0);
    quickBtns.setMaximumSize(new Dimension(700,36));
    RedButton selAll=new RedButton("  Select All  ");
    RedButton clrAll=new RedButton("  Clear All  ");
    selAll.addActionListener(e->{ for(JCheckBox cb:permBoxes) cb.setSelected(true); });
    clrAll.addActionListener(e->{ for(JCheckBox cb:permBoxes) cb.setSelected(false); });
    quickBtns.add(selAll); quickBtns.add(clrAll);
    f.add(quickBtns); f.add(Box.createVerticalStrut(14));

    roleC.addActionListener(e->{
        String selRole=(String)roleC.getSelectedItem();
        if("CUSTOM".equals(selRole)) return;
        String[] defaults=ROLE_DEFAULT_PERMISSIONS.getOrDefault(selRole,new String[]{});
        java.util.Set<String> defSet=new java.util.HashSet<>(Arrays.asList(defaults));
        for(int i=0;i<ALL_PERMISSIONS.length;i++)
            permBoxes[i].setSelected(defSet.contains(ALL_PERMISSIONS[i]));
    });

    RedButton saveBtn=new RedButton("  \u2713  Save Changes  ");
    saveBtn.setAlignmentX(0.5f); f.add(saveBtn);

    saveBtn.addActionListener(e->{
        s.fullName = fullNameF.getText().trim();
        s.email    = emailF.getText().trim();
        s.phone    = phoneF.getText().trim();
        s.branch   = branchF.getText().trim();
        s.role     = (String)roleC.getSelectedItem();
        s.permissions.clear();
        for(int i=0;i<ALL_PERMISSIONS.length;i++)
            if(permBoxes[i].isSelected()) s.permissions.add(ALL_PERMISSIONS[i]);
        saveDatabase(); d.dispose();
        contentArea.add(buildStaff(),"staff");
        showSection("staff",
            java.util.Arrays.asList("dashboard","clients","accounts","transactions",
                "fds","rds","mfs","loans","cards","daily","register","staff","inbox")
                .indexOf("staff"));
        toast("\u2714  "+s.fullName+" updated!");
    });
    d.setVisible(true);
}

static void toggleStaffStatus(String staffId, DefaultTableModel hm){
    StaffMember s = staffMembers.stream()
        .filter(x->x.id.equals(staffId)).findFirst().orElse(null);
    if(s==null) return;
    s.status = "ACTIVE".equals(s.status) ? "INACTIVE" : "ACTIVE";
    saveDatabase(); refreshHiredStaffTable(hm);
    toast(("ACTIVE".equals(s.status)?"\u2705  Activated: ":"\uD83D\uDEAB  Deactivated: ")+s.fullName);
}

static void resetStaffPassword(String staffId){
    StaffMember s = staffMembers.stream()
        .filter(x->x.id.equals(staffId)).findFirst().orElse(null);
    if(s==null) return;
    JDialog d = dlg("Reset Password \u2014 "+s.fullName, 400, 260);
    JPanel f  = dlgForm(d);
    f.add(infoRow("Staff",    s.fullName));
    f.add(infoRow("Username", s.username));
    f.add(Box.createVerticalStrut(8));
    JTextField newPassF = mkField("New Password");
    f.add(fRow("New Password", newPassF));
    f.add(Box.createVerticalStrut(12));
    RedButton b = new RedButton("  Reset Password  ");
    b.setAlignmentX(0.5f); f.add(b);
    b.addActionListener(e->{
        String np = newPassF.getText().trim();
        if(np.isEmpty()){ showErr(d,"Enter a new password."); return; }
        s.password = np;
        saveDatabase(); d.dispose();
        toast("\uD83D\uDD11  Password reset for "+s.username);
    });
    d.setVisible(true);
}

static void viewStaffPermissions(String staffId){
    StaffMember s = staffMembers.stream()
        .filter(x->x.id.equals(staffId)).findFirst().orElse(null);
    if(s==null) return;
    JDialog d = dlg("Permissions \u2014 "+s.fullName, 520, 540);
    JPanel f  = dlgForm(d);
    f.add(infoRow("Staff ID",  s.id));
    f.add(infoRow("Full Name", s.fullName));
    f.add(infoRow("Role",      s.role));
    f.add(infoRow("Branch",    s.branch));
    f.add(Box.createVerticalStrut(10));
    JLabel pl = L("ASSIGNED PERMISSIONS  ("+s.permissions.size()+" of "+ALL_PERMISSIONS.length+")",
        new Font("SansSerif",Font.BOLD,9), C_TMUT);
    pl.setAlignmentX(0); pl.setMaximumSize(new Dimension(700,16)); f.add(pl);
    f.add(Box.createVerticalStrut(8));
    JPanel permGrid=new JPanel(new GridLayout(0,2,10,6));
    permGrid.setOpaque(false); permGrid.setAlignmentX(0);
    permGrid.setMaximumSize(new Dimension(700,9999));
    permGrid.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(C_BORDER2,1,true),
        BorderFactory.createEmptyBorder(10,12,10,12)));
    for(String perm: ALL_PERMISSIONS){
        boolean has = s.permissions.contains(perm);
        JLabel pl2 = L((has?"\u2705  ":"\u274C  ")+perm,
            new Font("SansSerif",Font.PLAIN,11), has?C_GREEN:C_TMUT);
        permGrid.add(pl2);
    }
    f.add(permGrid);
    d.setVisible(true);
}

static void deleteStaff(String staffId, DefaultTableModel hm){
    StaffMember s = staffMembers.stream()
        .filter(x->x.id.equals(staffId)).findFirst().orElse(null);
    if(s==null) return;
    if(JOptionPane.showConfirmDialog(mainFrame,
        "Delete "+s.fullName+" ("+s.username+")?\nThis cannot be undone.",
        "Delete Staff", JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION) return;
    staffMembers.remove(s);
    saveDatabase(); refreshHiredStaffTable(hm);
    toast("\uD83D\uDDD1  "+s.fullName+" removed from staff.");
}

    // ════════════════════════════════════════════════════════
    //  INTERNAL EMAIL / INBOX
    // ════════════════════════════════════════════════════════
    static DefaultTableModel inboxModel;
    static JLabel inboxUnreadLbl;

    static String[] allowedRecipients(){
        switch(currentRole){
            case "ADMIN":   return new String[]{"MANAGER","OFFICER","CLERK"};
            case "MANAGER": return new String[]{"ADMIN","OFFICER","CLERK"};
            case "OFFICER": return new String[]{"MANAGER"};
            case "CLERK":   return new String[]{"MANAGER"};
            default:        return new String[]{};
        }
    }

    static JPanel buildInbox(){
        JPanel p=page("Internal Email");
        JPanel aBar=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); aBar.setOpaque(false); aBar.setAlignmentX(0);
        aBar.setMaximumSize(new Dimension(99999,48)); aBar.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));
        RedButton composeBtn=new RedButton("  \u270F  Compose Email  "); composeBtn.addActionListener(e->showComposeDlg()); aBar.add(composeBtn);
        final boolean[] showSent={false};
        JLabel inboxTab=new JLabel("  \uD83D\uDCE5  Inbox  "); JLabel sentTab=new JLabel("  \uD83D\uDCE4  Sent  ");
        for(JLabel tab:new JLabel[]{inboxTab,sentTab}){ tab.setFont(new Font("SansSerif",Font.BOLD,11)); tab.setForeground(C_TSEC); tab.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(4,10,4,10))); tab.setOpaque(false); tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        inboxTab.setForeground(C_CYAN); aBar.add(inboxTab); aBar.add(sentTab);
        inboxUnreadLbl=new JLabel(""); inboxUnreadLbl.setFont(new Font("SansSerif",Font.BOLD,10)); inboxUnreadLbl.setForeground(C_TPRIM); aBar.add(inboxUnreadLbl);
        p.add(aBar);
        inboxModel=new DefaultTableModel(new String[]{"","From","To","Subject","Date/Time","Status"},0){ public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl=mkTable(inboxModel); setW(tbl,22,100,100,280,148,70); tbl.getColumnModel().getColumn(5).setCellRenderer(badge());
        tbl.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){ if(e.getClickCount()==2){ int r=tbl.getSelectedRow(); if(r>=0) openMessageDlg(r,showSent[0]); } }});
        inboxTab.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){ showSent[0]=false; inboxTab.setForeground(C_CYAN); sentTab.setForeground(C_TSEC); refreshInboxTable(false); }});
        sentTab.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){ showSent[0]=true; sentTab.setForeground(C_CYAN); inboxTab.setForeground(C_TSEC); refreshInboxTable(true); }});
        ctxMenu(tbl,new String[]{"Open Message","Delete"},new ActionListener[]{
            e->{ int r=tbl.getSelectedRow(); if(r>=0) openMessageDlg(r,showSent[0]); },
            e->{ int r=tbl.getSelectedRow(); if(r>=0) deleteMessage(r,showSent[0]); }
        });
        p.add(tblCard("MESSAGES  \u2014  double-click to open \u2502 right-click to delete",tbl));
        JLabel note=L("Sending rules:  ADMIN \u21C4 MANAGER (both ways)  \u2022  OFFICER \u2192 MANAGER only  \u2022  CLERK \u2192 MANAGER only",new Font("SansSerif",Font.PLAIN,10),C_TMUT);
        note.setAlignmentX(0); note.setMaximumSize(new Dimension(99999,20)); note.setBorder(BorderFactory.createEmptyBorder(8,0,0,0)); p.add(note);
        return p;
    }

    static void refreshInbox(){ if(inboxModel==null) return; refreshInboxTable(false); refreshInboxBadge(); }

    static void refreshInboxTable(boolean showSent){
        if(inboxModel==null) return; inboxModel.setRowCount(0);
        List<InternalMessage> filtered=messages.stream().filter(m->showSent?m.fromRole.equals(currentRole):m.toRole.equals(currentRole)).sorted((a,b)->b.timestamp.compareTo(a.timestamp)).collect(Collectors.toList());
        for(InternalMessage m:filtered){ String dot=(!showSent&&!m.read)?"\u25CF":""; String status=showSent?"SENT":(m.read?"READ":"UNREAD"); inboxModel.addRow(new Object[]{dot,m.fromRole,m.toRole,m.subject,m.timestamp,status}); }
        long unread=messages.stream().filter(m->m.toRole.equals(currentRole)&&!m.read).count();
        if(inboxUnreadLbl!=null) inboxUnreadLbl.setText(unread>0?" \u2014  "+unread+" unread":"  \u2014  All read");
    }

    static void refreshInboxBadge(){
        if(inboxBadge==null) return;
        long unread=messages.stream().filter(m->m.toRole.equals(currentRole)&&!m.read).count();
        if(unread>0){ inboxBadge.setText(String.valueOf(unread)); inboxBadge.setOpaque(true); inboxBadge.setBackground(C_RED); inboxBadge.setBorder(BorderFactory.createEmptyBorder(1,5,1,5)); }
        else { inboxBadge.setText(""); inboxBadge.setOpaque(false); inboxBadge.setBorder(BorderFactory.createEmptyBorder()); }
        inboxBadge.repaint();
    }

    static void openMessageDlg(int tableRow,boolean showSent){
        List<InternalMessage> filtered=messages.stream().filter(m->showSent?m.fromRole.equals(currentRole):m.toRole.equals(currentRole)).sorted((a,b)->b.timestamp.compareTo(a.timestamp)).collect(Collectors.toList());
        if(tableRow>=filtered.size()) return;
        InternalMessage m=filtered.get(tableRow);
        if(!showSent&&!m.read){ m.read=true; saveDatabase(); refreshInboxBadge(); refreshInboxTable(showSent); }
        JDialog d=dlg((showSent?"Sent: ":"From: ")+m.fromRole+" \u2192 "+m.toRole,520,400); JPanel f=dlgForm(d);
        JPanel meta=new JPanel(new GridLayout(3,2,10,6)); meta.setOpaque(false); meta.setAlignmentX(0); meta.setMaximumSize(new Dimension(700,100));
        meta.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(10,14,10,14)));
        String[][] mfields={{"From",m.fromRole},{"To",m.toRole},{"Date",m.timestamp},{"Subject",m.subject}};
        for(String[] kv:mfields){ JPanel kp=new JPanel(new BorderLayout()); kp.setOpaque(false); kp.add(L(kv[0],new Font("SansSerif",Font.BOLD,9),C_TMUT),BorderLayout.NORTH); kp.add(L(kv[1],FB,C_TPRIM),BorderLayout.CENTER); meta.add(kp); }
        f.add(meta); f.add(Box.createVerticalStrut(12));
        JLabel bodyLbl=L("MESSAGE",new Font("SansSerif",Font.BOLD,9),C_TMUT); bodyLbl.setAlignmentX(0); f.add(bodyLbl); f.add(Box.createVerticalStrut(4));
        JTextArea body=new JTextArea(m.body); body.setEditable(false); body.setLineWrap(true); body.setWrapStyleWord(true);
        body.setForeground(C_TPRIM); body.setFont(FB); body.setBackground(C_PANEL); body.setOpaque(true); body.setCaretColor(C_TPRIM);
        body.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(10,12,10,12)));
        JScrollPane bodyScroll=new JScrollPane(body); bodyScroll.setAlignmentX(0); bodyScroll.setMaximumSize(new Dimension(700,9999)); bodyScroll.setBorder(BorderFactory.createEmptyBorder()); bodyScroll.getViewport().setBackground(C_PANEL); f.add(bodyScroll); f.add(Box.createVerticalStrut(14));
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER,10,0)); btnRow.setOpaque(false); btnRow.setAlignmentX(0.5f);
        if(!showSent){ RedButton replyBtn=new RedButton("  \u21A9  Reply  "); replyBtn.addActionListener(e->{ d.dispose(); showComposeDlg(m.fromRole,"Re: "+m.subject); }); btnRow.add(replyBtn); }
        RedButton closeBtn=new RedButton("  Close  "); closeBtn.addActionListener(e->d.dispose()); btnRow.add(closeBtn); f.add(btnRow);
        d.setVisible(true);
    }

    static void deleteMessage(int tableRow,boolean showSent){
        List<InternalMessage> filtered=messages.stream().filter(m->showSent?m.fromRole.equals(currentRole):m.toRole.equals(currentRole)).sorted((a,b)->b.timestamp.compareTo(a.timestamp)).collect(Collectors.toList());
        if(tableRow>=filtered.size()) return; InternalMessage m=filtered.get(tableRow);
        if(JOptionPane.showConfirmDialog(mainFrame,"Delete this message?\nFrom: "+m.fromRole+"  To: "+m.toRole+"\nSubject: "+m.subject,"Delete Message",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        messages.remove(m); saveDatabase(); refreshInboxTable(showSent); refreshInboxBadge(); toast("\uD83D\uDDD1  Message deleted.");
    }

    static void showComposeDlg(){ showComposeDlg(null,null); }
    static void showComposeDlg(String preselectedTo,String preselectedSubject){
        String[] recipients=allowedRecipients(); if(recipients.length==0){ toast("\uD83D\uDD12  "+currentRole+" has no allowed email recipients."); return; }
        JDialog d=dlg("Compose Email",500,440); JPanel f=dlgForm(d);
        JPanel fromRow=new JPanel(new BorderLayout()); fromRow.setOpaque(false); fromRow.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(6,12,6,12))); fromRow.add(L("FROM",new Font("SansSerif",Font.BOLD,9),C_TMUT),BorderLayout.NORTH); fromRow.add(L(currentRole,FB,C_TPRIM),BorderLayout.CENTER); fromRow.setAlignmentX(0); fromRow.setMaximumSize(new Dimension(700,50)); f.add(fromRow); f.add(Box.createVerticalStrut(8));
        JComboBox<String> toCombo=mkCombo(recipients); if(preselectedTo!=null){ for(String r:recipients) if(r.equals(preselectedTo)) toCombo.setSelectedItem(r); } f.add(fRow("TO",toCombo)); f.add(Box.createVerticalStrut(4));
        JTextField subjectF=mkField("Subject"); if(preselectedSubject!=null) subjectF.setText(preselectedSubject); f.add(fRow("SUBJECT",subjectF)); f.add(Box.createVerticalStrut(8));
        JLabel bodyLbl=L("MESSAGE BODY",new Font("SansSerif",Font.BOLD,9),C_TMUT); bodyLbl.setAlignmentX(0); f.add(bodyLbl); f.add(Box.createVerticalStrut(4));
        JTextArea bodyArea=new JTextArea(6,40); bodyArea.setLineWrap(true); bodyArea.setWrapStyleWord(true); bodyArea.setForeground(C_TPRIM); bodyArea.setFont(FB); bodyArea.setBackground(C_PANEL); bodyArea.setOpaque(true); bodyArea.setCaretColor(C_TPRIM);
        bodyArea.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(8,10,8,10)));
        JScrollPane bodyScroll=new JScrollPane(bodyArea); bodyScroll.setAlignmentX(0); bodyScroll.setMaximumSize(new Dimension(700,9999)); bodyScroll.setPreferredSize(new Dimension(0,140)); bodyScroll.setBorder(BorderFactory.createEmptyBorder()); bodyScroll.getViewport().setBackground(C_PANEL); f.add(bodyScroll); f.add(Box.createVerticalStrut(10));
        RedButton sendBtn=new RedButton("  \u2709  Send Email  "); sendBtn.setAlignmentX(0.5f);
        sendBtn.addActionListener(e->{ String to=(String)toCombo.getSelectedItem(); String subject=subjectF.getText().trim(); String body=bodyArea.getText().trim(); if(subject.isEmpty()){showErr(d,"Please enter a subject."); return;} if(body.isEmpty()){showErr(d,"Please enter a message body."); return;} String ts=LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")); String mid="MSG"+String.format("%06d",messages.size()+1); messages.add(new InternalMessage(mid,currentRole,to,subject,body,ts)); saveDatabase(); d.dispose(); refreshInboxBadge(); toast("\u2709  Email sent to "+to+"!"); });
        f.add(sendBtn); d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════
    //  TOAST  (Enhanced)
    // ════════════════════════════════════════════════════════
    static void toast(String msg){
        JWindow tw=new JWindow(mainFrame);
        JPanel tp=new JPanel(new FlowLayout(FlowLayout.LEFT,14,10)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_PANEL); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(C_BORDER2); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                // Left accent
                g2.setPaint(new GradientPaint(0,4,C_CYAN,0,getHeight()-4,C_GOLD));
                g2.fillRoundRect(0,0,4,getHeight(),4,4);
            }
        };
        tp.setOpaque(false); tp.add(L("  "+msg,FB,C_TPRIM));
        tw.setContentPane(tp); tw.pack(); tw.setSize(Math.max(tw.getWidth(),340),48);
        if(mainFrame!=null){ Point loc=mainFrame.getLocation(); Dimension sz=mainFrame.getSize(); tw.setLocation(loc.x+sz.width-tw.getWidth()-22,loc.y+sz.height-tw.getHeight()-44); }
        try{tw.setShape(new RoundRectangle2D.Double(0,0,tw.getWidth(),48,14,14));}catch(Exception ignored){}
        tw.setVisible(true);
        javax.swing.Timer t=new javax.swing.Timer(2800,e->tw.dispose()); t.setRepeats(false); t.start();
    }

    // ════════════════════════════════════════════════════════
    //  UI BUILDER HELPERS
    // ════════════════════════════════════════════════════════
    static JPanel page(String title){
        JPanel p=new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(C_NAVY); p.setBorder(BorderFactory.createEmptyBorder(28,32,32,32));
        JPanel hdr=new JPanel(new BorderLayout()); hdr.setOpaque(false); hdr.setAlignmentX(0); hdr.setMaximumSize(new Dimension(99999,40));
        JLabel tl=new JLabel(title); tl.setFont(new Font("Serif",Font.BOLD,26)); tl.setForeground(C_TPRIM); hdr.add(tl,BorderLayout.WEST);
        String ds=LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"));
        JLabel dl=L(ds,FS,C_TMUT); dl.setHorizontalAlignment(SwingConstants.RIGHT); hdr.add(dl,BorderLayout.EAST);
        p.add(hdr); p.add(Box.createVerticalStrut(8));
        JPanel sep=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setPaint(new GradientPaint(0,0,C_CYAN,180,0,C_GOLD,false));
                g2.fillRect(0,0,Math.min(getWidth(),360),2);
            }
        };
        sep.setOpaque(false); sep.setAlignmentX(0); sep.setMaximumSize(new Dimension(99999,2)); sep.setPreferredSize(new Dimension(0,2));
        p.add(sep); p.add(Box.createVerticalStrut(20));
        return p;
    }

    // Enhanced stat card with icon
    static JPanel statCard(String label,JLabel valLbl,Color accent,String icon){
        RoundPanel card=new RoundPanel(C_CARD,14);
        card.setLayout(null); card.setBorder(new LineBorder(C_BORDER2,1,true));
        card.setPreferredSize(new Dimension(200,115)); card.setMinimumSize(new Dimension(140,115)); card.setMaximumSize(new Dimension(99999,115));
        // Top accent bar
        JPanel bar=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,accent,0,getHeight(),new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),40)));
                g2.fillRoundRect(0,0,4,getHeight(),4,4);
            }
        };
        bar.setOpaque(false); bar.setBounds(0,16,4,83); card.add(bar);
        // Glow background
        JPanel glow=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(getWidth(),0),getWidth()*1.2f,
                    new float[]{0f,1f},new Color[]{new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),12),new Color(0,0,0,0)}));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        glow.setOpaque(false); glow.setBounds(0,0,300,115); card.add(glow);
        JLabel lbl=L(label.toUpperCase(),new Font("SansSerif",Font.BOLD,9),C_TMUT); lbl.setBounds(16,14,220,14); card.add(lbl);
        valLbl.setFont(new Font("Serif",Font.BOLD,30)); valLbl.setBounds(16,34,220,44); card.add(valLbl);
        if(icon!=null){ JLabel ico=new JLabel(icon); ico.setFont(new Font("SansSerif",Font.PLAIN,16)); ico.setForeground(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),80)); ico.setBounds(200,10,36,36); card.add(ico); }
        return card;
    }

    static JPanel statCard(String label,JLabel valLbl,Color accent){ return statCard(label,valLbl,accent,null); }

    static JPanel balCard(String label,JLabel valLbl,Color accent){
        RoundPanel card=new RoundPanel(C_CARD,12);
        card.setLayout(null); card.setBorder(new LineBorder(C_BORDER2,1,true));
        card.setPreferredSize(new Dimension(180,90)); card.setMinimumSize(new Dimension(130,90)); card.setMaximumSize(new Dimension(99999,90));
        JPanel top=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setPaint(new GradientPaint(0,0,accent,getWidth()/2,0,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),0)));
                g2.fillRect(0,0,getWidth(),3);
            }
        };
        top.setOpaque(false); top.setBounds(0,0,800,3); card.add(top);
        JLabel ll=L(label.toUpperCase(),new Font("SansSerif",Font.BOLD,8),C_TMUT); ll.setBounds(14,10,220,13); card.add(ll);
        valLbl.setFont(new Font("Serif",Font.BOLD,15)); valLbl.setBounds(14,26,220,30); card.add(valLbl);
        JLabel cur=L("INR",new Font("SansSerif",Font.PLAIN,8),C_TMUT); cur.setBounds(14,60,36,11); card.add(cur);
        return card;
    }

    static JScrollPane tblCard(String title,JTable tbl){
        tbl.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,10));
        tbl.getTableHeader().setBackground(C_PANEL); tbl.getTableHeader().setForeground(C_TMUT);
        tbl.getTableHeader().setPreferredSize(new Dimension(0,36));
        tbl.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_BORDER));
        JPanel titleHdr=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                g.setColor(C_PANEL); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(C_BORDER2); g.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        titleHdr.setOpaque(false); titleHdr.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
        titleHdr.add(L(title,new Font("SansSerif",Font.BOLD,10),C_TSEC),BorderLayout.WEST);
        JLabel cnt=new JLabel("0 records"){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x00,0xD4,0xFF,28)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                super.paintComponent(g);
            }
        };
        cnt.setFont(new Font("SansSerif",Font.BOLD,9)); cnt.setForeground(C_CYAN); cnt.setOpaque(false); cnt.setBorder(BorderFactory.createEmptyBorder(3,9,3,9));
        titleHdr.add(cnt,BorderLayout.EAST);
        tbl.getModel().addTableModelListener(e->cnt.setText(tbl.getRowCount()+" records")); cnt.setText(tbl.getRowCount()+" records");
        JPanel combined=new JPanel(new BorderLayout(0,0)); combined.setBackground(C_PANEL); combined.add(titleHdr,BorderLayout.NORTH); combined.add(tbl.getTableHeader(),BorderLayout.CENTER);
        JScrollPane sp=new JScrollPane(tbl); sp.setBorder(BorderFactory.createLineBorder(C_BORDER2)); sp.getViewport().setBackground(C_CARD); sp.setBackground(C_CARD); sp.setAlignmentX(0); sp.setColumnHeaderView(combined);
        return sp;
    }

    static JPanel actBar(String label,ActionListener al){
        JPanel bar=new JPanel(new BorderLayout()); bar.setOpaque(false); bar.setAlignmentX(0); bar.setMaximumSize(new Dimension(99999,50)); bar.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));
        RedButton btn=new RedButton("  "+label+"  "); btn.setPreferredSize(new Dimension(260,38)); btn.addActionListener(al); bar.add(btn,BorderLayout.EAST);
        return bar;
    }

    static JPanel twoCol(JPanel a,JPanel b){
        JPanel row=new JPanel(new GridLayout(1,2,16,0)); row.setOpaque(false); row.setAlignmentX(0); row.setMaximumSize(new Dimension(99999,72)); row.add(a); row.add(b); return row;
    }

    static JPanel fRow(String label,JComponent field){
        JPanel p=new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setOpaque(false); p.setAlignmentX(0); p.setMaximumSize(new Dimension(700,68));
        JLabel lbl=L(label,new Font("SansSerif",Font.BOLD,10),C_TSEC); lbl.setBorder(BorderFactory.createEmptyBorder(0,2,4,0)); lbl.setAlignmentX(0);
        field.setAlignmentX(0); field.setMaximumSize(new Dimension(700,38)); p.add(lbl); p.add(field); p.setBorder(BorderFactory.createEmptyBorder(0,0,8,0)); return p;
    }

    static JPanel infoRow(String label,String val){
        JPanel p=new JPanel(new BorderLayout()); p.setOpaque(false); p.setAlignmentX(0); p.setMaximumSize(new Dimension(700,30)); p.setBorder(BorderFactory.createEmptyBorder(2,0,4,0));
        p.add(L(label,FS,C_TMUT),BorderLayout.WEST); p.add(L(val,new Font("SansSerif",Font.BOLD,12),C_GOLD),BorderLayout.EAST); return p;
    }

    static JLabel stepLbl(String text){
        JLabel l=new JLabel(text); l.setFont(new Font("SansSerif",Font.BOLD,10)); l.setForeground(C_CYAN);
        l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,C_BORDER2),BorderFactory.createEmptyBorder(0,0,8,0)));
        l.setAlignmentX(0); l.setMaximumSize(new Dimension(700,26)); return l;
    }

    // ════════════════════════════════════════════════════════
    //  TABLE HELPERS
    // ════════════════════════════════════════════════════════
    static JTable mkTable(DefaultTableModel model){
        JTable t=new JTable(model);
        t.setBackground(C_CARD); t.setForeground(C_TPRIM); t.setFont(FB);
        t.setRowHeight(40); t.setGridColor(C_BORDER); t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(0,1));
        t.setSelectionBackground(C_SEL); t.setSelectionForeground(C_TPRIM);
        t.setFocusable(false); t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.setDefaultRenderer(Object.class,(tbl,v,sel,foc,row,col)->{
            JLabel l=new JLabel(v!=null?v.toString():""); l.setOpaque(true); l.setFont(FB);
            l.setBorder(BorderFactory.createEmptyBorder(0,14,0,14));
            l.setBackground(sel?C_SEL:row%2==0?C_CARD:C_ROWALT); l.setForeground(C_TPRIM); return l;
        });
        return t;
    }

    static TableCellRenderer badge(){
        return (tbl,v,sel,foc,row,col)->{
            String val=v!=null?v.toString():""; Color bg,fg;
            switch(val){
                case "ACTIVE":case"VERIFIED":case"CREDIT":case"Full":case"PAID":      bg=new Color(0x2E,0xE8,0x9A,32); fg=C_GREEN; break;
                case "FROZEN":case"PENDING":case"DEBIT":case"View":case"Create":      bg=new Color(0x3D,0xA5,0xFF,32); fg=C_BLUE; break;
                case "MATURED":case"MODERATE":                                         bg=new Color(0xFF,0xB8,0x30,32); fg=C_AMBER; break;
                case "CLOSED":case"REDEEMED":case"No":                                 bg=new Color(100,100,100,32);    fg=C_TMUT; break;
                case "EXHAUSTED":case"STOPPED":                                        bg=new Color(0xFF,0x7B,0x2C,32); fg=C_ORANGE; break;
                case "HIGH":case"ADMIN":                                               bg=new Color(0xE5,0x32,0x35,32); fg=C_RED_L; break;
                case "LOW":                                                            bg=new Color(0x2E,0xE8,0x9A,32); fg=C_GREEN; break;
                case "SAVINGS":case"SALARY":                                          bg=new Color(0x00,0xD4,0xFF,28); fg=C_CYAN; break;
                case "CURRENT":                                                        bg=new Color(0xF5,0xC5,0x42,28); fg=C_GOLD; break;
                case "MANAGER":                                                        bg=new Color(0xF5,0xC5,0x42,32); fg=C_GOLD; break;
                case "OFFICER":                                                        bg=new Color(0x3D,0xA5,0xFF,32); fg=C_BLUE; break;
                case "CLERK":                                                          bg=new Color(0x2E,0xE8,0x9A,32); fg=C_GREEN; break;
                case "READ":                                                           bg=new Color(100,100,100,32);    fg=C_TMUT; break;
                case "UNREAD":                                                         bg=new Color(0x00,0xD4,0xFF,32); fg=C_CYAN; break;
                case "SENT":                                                           bg=new Color(0x9B,0x59,0xF5,32); fg=C_PURPLE; break;
                default:
                    if(val.contains("Due Now")){ bg=new Color(0xFF,0xB8,0x30,32); fg=C_AMBER; }
                    else { bg=new Color(0x14,0x24,0x3A); fg=C_TSEC; }
                    break;
            }
            Color rowBg=sel?C_SEL:row%2==0?C_CARD:C_ROWALT;
            JLabel badge=new JLabel(val,SwingConstants.CENTER){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    super.paintComponent(g);
                }
            };
            badge.setFont(new Font("SansSerif",Font.BOLD,10)); badge.setForeground(fg); badge.setOpaque(false); badge.setBorder(BorderFactory.createEmptyBorder(3,9,3,9));
            JPanel wrap=new JPanel(new FlowLayout(FlowLayout.LEFT,14,9)); wrap.setBackground(rowBg); wrap.setOpaque(true); wrap.add(badge); return wrap;
        };
    }

    static void setW(JTable t,int... ws){ for(int i=0;i<ws.length&&i<t.getColumnCount();i++) t.getColumnModel().getColumn(i).setPreferredWidth(ws[i]); }

    // ════════════════════════════════════════════════════════
    //  DIALOG HELPERS
    // ════════════════════════════════════════════════════════
    // Helper to make a window-control button (─ □ ×)
    static JLabel mkWinBtn(String symbol, Color hoverFg, Color normalFg, Color hoverBg){
        JLabel btn = new JLabel(symbol){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h=Boolean.TRUE.equals(getClientProperty("hover"));
                g2.setColor(h ? hoverBg : new Color(255,255,255,18));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif",Font.BOLD,13));
        btn.setForeground(normalFg);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(28,22));
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ btn.putClientProperty("hover",true);  btn.setForeground(hoverFg);  btn.repaint(); }
            public void mouseExited (MouseEvent e){ btn.putClientProperty("hover",false); btn.setForeground(normalFg); btn.repaint(); }
        });
        return btn;
    }

    static JDialog dlg(String title,int w,int h){
        JDialog d=new JDialog(mainFrame,title,true);
        d.setUndecorated(true);
        d.setSize(w,h); d.setMinimumSize(new Dimension(360,260));
        d.setLocationRelativeTo(mainFrame); d.setResizable(true);

        JPanel root=new JPanel(new BorderLayout()); root.setBackground(C_CARD);

        // ── Header ──────────────────────────────────────────
        JPanel hdr=new JPanel(new BorderLayout()); hdr.setBackground(C_CARD);
        hdr.setPreferredSize(new Dimension(w,46));

        // Top gradient accent bar
        JPanel bar=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setPaint(new GradientPaint(0,0,C_CYAN,getWidth()/2,0,C_GOLD));
                g2.fillRect(0,0,getWidth(),2);
            }
        };
        bar.setOpaque(false); bar.setPreferredSize(new Dimension(0,2));

        JLabel tl=L(title,new Font("Serif",Font.BOLD,15),C_TPRIM);
        tl.setBorder(BorderFactory.createEmptyBorder(0,20,0,8));

        // ── Window control buttons: [─]  [□]  [×] left→right ──
        JPanel ctrlPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,3,6));
        ctrlPanel.setOpaque(false);

        // Minimize button  ─
        JLabel minBtn = mkWinBtn("\u2012", C_GOLD, C_TSEC, new Color(80,70,20,80));
        minBtn.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                d.setVisible(false);
                JWindow stub=new JWindow();
                String shortTitle=title.substring(0,Math.min(title.length(),28));
                JLabel restore=new JLabel("  \uD83D\uDCCB  "+shortTitle+"   \u25B2 restore  ");
                restore.setFont(new Font("SansSerif",Font.PLAIN,11));
                restore.setForeground(C_TPRIM); restore.setBackground(C_PANEL); restore.setOpaque(true);
                restore.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1),BorderFactory.createEmptyBorder(6,12,6,12)));
                stub.setContentPane(restore); stub.pack();
                if(mainFrame!=null){
                    Point ml=mainFrame.getLocation(); Dimension ms=mainFrame.getSize();
                    stub.setLocation(ml.x+ms.width-stub.getWidth()-24, ml.y+ms.height-stub.getHeight()-48);
                }
                stub.setVisible(true);
                restore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                restore.addMouseListener(new MouseAdapter(){
                    public void mouseClicked(MouseEvent e2){ stub.dispose(); d.setVisible(true); d.toFront(); }
                    public void mouseEntered(MouseEvent e2){ restore.setBackground(C_BORDER2); }
                    public void mouseExited (MouseEvent e2){ restore.setBackground(C_PANEL); }
                });
            }
        });

        // Maximize / Restore button  □
        final boolean[] maximized={false};
        final int[] savedBounds={w,h,0,0};
        JLabel maxBtn = mkWinBtn("\u25A1", C_CYAN, C_TSEC, new Color(0,80,80,80));
        maxBtn.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if(!maximized[0]){
                    savedBounds[0]=d.getWidth(); savedBounds[1]=d.getHeight();
                    savedBounds[2]=d.getX();     savedBounds[3]=d.getY();
                    GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
                    Rectangle screen=ge.getMaximumWindowBounds();
                    d.setBounds(screen); maximized[0]=true; maxBtn.setText("\u2750");
                } else {
                    d.setBounds(savedBounds[2],savedBounds[3],savedBounds[0],savedBounds[1]);
                    maximized[0]=false; maxBtn.setText("\u25A1");
                }
                maxBtn.repaint();
            }
        });

        // Close button  ×
        JLabel closeBtn = mkWinBtn("\u00D7", Color.WHITE, new Color(0xFF,0x8A,0x8A), new Color(0xC0,0x1C,0x1E,160));
        closeBtn.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ d.dispose(); }
        });

        // Order: [─] [□] [×]  — min is leftmost, close is rightmost
        ctrlPanel.add(minBtn);
        ctrlPanel.add(maxBtn);
        ctrlPanel.add(closeBtn);
        ctrlPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,8));

        JPanel titleRow=new JPanel(new BorderLayout());
        titleRow.setOpaque(false); titleRow.setPreferredSize(new Dimension(0,44));
        titleRow.add(tl,BorderLayout.CENTER);
        titleRow.add(ctrlPanel,BorderLayout.EAST);

        JPanel hdrInner=new JPanel(new BorderLayout());
        hdrInner.setOpaque(false);
        hdrInner.add(bar,BorderLayout.NORTH);
        hdrInner.add(titleRow,BorderLayout.CENTER);

        hdr.add(hdrInner,BorderLayout.CENTER);
        root.add(hdr,BorderLayout.NORTH);
        d.setContentPane(root);
        return d;
    }

    static JPanel dlgForm(JDialog d){
        JPanel f=new JPanel(); f.setLayout(new BoxLayout(f,BoxLayout.Y_AXIS)); f.setBackground(C_CARD); f.setBorder(BorderFactory.createEmptyBorder(16,22,22,22));
        JScrollPane sp=new JScrollPane(f); sp.setBorder(BorderFactory.createEmptyBorder()); sp.getViewport().setBackground(C_CARD); sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        d.getContentPane().add(sp,BorderLayout.CENTER); return f;
    }

    static void showErr(JDialog d,String msg){ JOptionPane.showMessageDialog(d,msg,"Error",JOptionPane.ERROR_MESSAGE); }

    static void ctxMenu(JTable tbl,String[] labels,ActionListener[] actions){
        JPopupMenu pop=new JPopupMenu(); pop.setBackground(C_PANEL);
        for(int i=0;i<labels.length;i++){
            JMenuItem mi=new JMenuItem(labels[i]); mi.setBackground(C_PANEL); mi.setForeground(C_TPRIM); mi.setFont(FB); mi.setBorder(BorderFactory.createEmptyBorder(6,14,6,14)); mi.addActionListener(actions[i]); pop.add(mi);
        }
        tbl.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e){ int r=tbl.rowAtPoint(e.getPoint()); if(r>=0)tbl.setRowSelectionInterval(r,r); if(SwingUtilities.isRightMouseButton(e))pop.show(tbl,e.getX(),e.getY()); }
        });
    }

    // ════════════════════════════════════════════════════════
    //  FIELD HELPERS
    // ════════════════════════════════════════════════════════
    static JTextField mkField(String ph){
        JTextField tf=new JTextField(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                if(getText().isEmpty()&&!isFocusOwner()){ Graphics2D g2=(Graphics2D)g; g2.setColor(C_TMUT); g2.setFont(getFont()); Insets ins=getInsets(); g2.drawString(ph,ins.left+4,ins.top+g2.getFontMetrics().getAscent()); }
            }
        };
        styleField(tf,ph); return tf;
    }
    static void addCopyPasteMenu(JTextField tf){
    JPopupMenu menu = new JPopupMenu();
    menu.setBackground(C_PANEL);

    JMenuItem cut   = new JMenuItem("Cut");
    JMenuItem copy  = new JMenuItem("Copy");
    JMenuItem paste = new JMenuItem("Paste");
    JMenuItem selAll= new JMenuItem("Select All");
    JMenuItem clear = new JMenuItem("Clear");

    for(JMenuItem mi : new JMenuItem[]{cut,copy,paste,selAll,clear}){
        mi.setBackground(C_PANEL);
        mi.setForeground(C_TPRIM);
        mi.setFont(FB);
        mi.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));
        menu.add(mi);
    }

    cut.addActionListener(e -> {
        if(tf.getSelectedText()!=null){
            copyToClipboard(tf.getSelectedText());
            tf.replaceSelection("");
        }
    });
    copy.addActionListener(e -> {
        String sel = tf.getSelectedText();
        if(sel==null||sel.isEmpty()) sel = tf.getText();
        copyToClipboard(sel);
    });
    paste.addActionListener(e -> {
        try{
            String clip = (String) java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
            if(clip!=null) tf.replaceSelection(clip);
        }catch(Exception ignored){}
    });
    selAll.addActionListener(e -> tf.selectAll());
    clear.addActionListener(e -> tf.setText(""));

    tf.addMouseListener(new MouseAdapter(){
        public void mousePressed(MouseEvent e){ if(SwingUtilities.isRightMouseButton(e)) menu.show(tf,e.getX(),e.getY()); }
        public void mouseReleased(MouseEvent e){ if(SwingUtilities.isRightMouseButton(e)) menu.show(tf,e.getX(),e.getY()); }
    });
}

    static void styleField(JTextField tf,String ph){
        tf.setBackground(C_PANEL); tf.setForeground(C_TPRIM); tf.setCaretColor(C_TPRIM); tf.setFont(FB);
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(6,10,6,10)));
        tf.addFocusListener(new FocusAdapter(){
            public void focusGained(FocusEvent e){ tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_CYAN,1,true),BorderFactory.createEmptyBorder(6,10,6,10))); }
            public void focusLost(FocusEvent e){  tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER2,1,true),BorderFactory.createEmptyBorder(6,10,6,10))); }
        });
        addCopyPasteMenu(tf);
    }

    static JComboBox<String> mkCombo(String[] items){
        JComboBox<String> cb=new JComboBox<>(items); cb.setBackground(C_PANEL); cb.setForeground(C_TPRIM); cb.setFont(FB); cb.setBorder(new LineBorder(C_BORDER2,1,true));
        cb.setRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList<?> list,Object v,int i,boolean sel,boolean foc){
                JLabel l=(JLabel)super.getListCellRendererComponent(list,v,i,sel,foc);
                l.setBackground(sel?new Color(0x1A,0x35,0x58):C_PANEL); l.setForeground(C_TPRIM); l.setBorder(BorderFactory.createEmptyBorder(5,10,5,10)); return l;
            }
        });
        return cb;
    }

    static JCheckBox mkChk(String text){
        JCheckBox cb=new JCheckBox(text); cb.setBackground(C_CARD); cb.setForeground(C_TSEC); cb.setFont(FB); cb.setFocusPainted(false); return cb;
    }

    static JLabel L(String text,Font font,Color color){ JLabel l=new JLabel(text); l.setFont(font); l.setForeground(color); return l; }
    static JLabel L(String text,Font font,String hexColor){ return L(text,font,Color.decode(hexColor)); }

    // ════════════════════════════════════════════════════════
    //  CUSTOM COMPONENTS
    // ════════════════════════════════════════════════════════
    static class RedButton extends JButton {
        RedButton(String text){
            super(text); setFont(FB2); setForeground(Color.WHITE); setFocusPainted(false); setBorderPainted(false); setOpaque(false); setContentAreaFilled(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            boolean h=getModel().isRollover();
            g2.setPaint(new GradientPaint(0,0,h?new Color(0xFF,0x60,0x20):C_RED_D,getWidth(),getHeight(),h?C_RED_L:C_RED));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
            // Shine effect
            g2.setPaint(new GradientPaint(0,0,new Color(255,255,255,35),0,getHeight()/2,new Color(255,255,255,0)));
            g2.fillRoundRect(0,0,getWidth(),getHeight()/2,10,10);
            super.paintComponent(g);
        }
    }

    static class RoundPanel extends JPanel {
        final Color bg; final int r;
        RoundPanel(Color bg,int r){ this.bg=bg; this.r=r; setOpaque(false); }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),r,r);
        }
    }

    // ════════════════════════════════════════════════════════
    //  DATA MODELS
    // ════════════════════════════════════════════════════════
    static class Client {
        String id,name,phone,email,city,pan,aadhar,occupation,kyc,riskProfile,status,openedOn;
        double annualIncome; List<String> accountIds=new ArrayList<>();
        Client(String id,String name,String phone,String email,String city,String pan,String aadhar,String occ,double income,String kyc,String risk,String opened){
            this.id=id;this.name=name;this.phone=phone;this.email=email;this.city=city;this.pan=pan;this.aadhar=aadhar;this.occupation=occ;this.annualIncome=income;this.kyc=kyc;this.riskProfile=risk;this.status="ACTIVE";this.openedOn=opened;
        }
    }

    static class BankAccount {
        String id,clientId,type,status,openedOn,nominee=""; double balance,minBal,rate; List<Transaction> transactions=new ArrayList<>();
        BankAccount(String id,String cid,String type,double bal,double min,double rate,String opened){ this.id=id;this.clientId=cid;this.type=type;this.balance=bal;this.minBal=min;this.rate=rate;this.status="ACTIVE";this.openedOn=opened; }
        void addTxn(String type,double amt,String desc){ transactions.add(0,new Transaction(type,amt,desc,balance,LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")))); }
    }

    static class Transaction { String type,desc,dt; double amount,balAfter; Transaction(String t,double a,String d,double b,String dt){type=t;amount=a;desc=d;balAfter=b;this.dt=dt;} }

    static class FixedDeposit {
        String id,clientId,linkedAccId,type,status,startDate,matDate; double principal,rate; int months;
        FixedDeposit(String id,String cid,String acc,double p,double r,int m,String s,String mat,String type){ this.id=id;this.clientId=cid;this.linkedAccId=acc;this.principal=p;this.rate=r;this.months=m;this.startDate=s;this.matDate=mat;this.type=type;this.status="ACTIVE"; }
    }

    static class RecurringDeposit {
        String id,clientId,linkedAccId,status,startDate,matDate,lastPayDate; double inst,rate,totalDep; int months,paid;
        RecurringDeposit(String id,String cid,String acc,double inst,double rate,int months,String s,String mat){ this.id=id;this.clientId=cid;this.linkedAccId=acc;this.inst=inst;this.rate=rate;this.months=months;this.startDate=s;this.matDate=mat;this.status="ACTIVE";this.lastPayDate=""; }
    }

    static class MutualFund {
        String id,clientId,name,cat,amc,status,purchaseDate; double invested,curVal,nav,units,sipAmt; boolean isSIP;
        MutualFund(String id,String cid,String name,String cat,String amc,double inv,double nav,double units,String date){ this.id=id;this.clientId=cid;this.name=name;this.cat=cat;this.amc=amc;this.invested=inv;this.nav=nav;this.units=units;this.curVal=nav*units;this.purchaseDate=date;this.status="ACTIVE";this.isSIP=false;this.sipAmt=0; }
    }

    static class Loan {
        String id,clientId,linkedAccId,type,status,disbursedDate,closureDate; double principal,rate,emiAmt,outstandingBal; int tenureMonths,paidEmis;
        Loan(String id,String cid,String accId,String type,double principal,double rate,int tenure,String disbursedDate){ this.id=id;this.clientId=cid;this.linkedAccId=accId;this.type=type;this.principal=principal;this.rate=rate;this.tenureMonths=tenure;this.disbursedDate=disbursedDate;this.closureDate="";this.status="ACTIVE";this.paidEmis=0;this.outstandingBal=principal; }
    }

    static class StaffMember {
    String id, username, password, role, branch, fullName, email, phone;
    List<String> permissions = new ArrayList<>();
    String createdOn, status;
    StaffMember(String id, String username, String password, String role,
                String branch, String fullName, String email, String phone){
        this.id=id; this.username=username; this.password=password;
        this.role=role; this.branch=branch; this.fullName=fullName;
        this.email=email; this.phone=phone;
        this.createdOn=LocalDate.now().toString();
        this.status="ACTIVE";
    }
}

    static class InternalMessage {
        String id,fromRole,toRole,subject,body,timestamp; boolean read;
        InternalMessage(String id,String from,String to,String subject,String body,String ts){ this.id=id;this.fromRole=from;this.toRole=to;this.subject=subject;this.body=body;this.timestamp=ts;this.read=false; }
    }

    // ════════════════════════════════════════════════════════
    //  FIND HELPERS
    // ════════════════════════════════════════════════════════
    static Client      findC(String id){ return clients.stream().filter(c->c.id.equals(id)).findFirst().orElse(null); }
    static BankAccount findA(String id){ return accounts.stream().filter(a->a.id.equals(id)).findFirst().orElse(null); }
    static double parseDbl(String s){ try{return Double.parseDouble(s.trim().replace(",",""));}catch(Exception e){return 0;} }
    
    static void loadAccountsForClient(String clientId, JComboBox<String> accountCombo, JLabel statusLabel) {
    accountCombo.removeAllItems();
    accountCombo.addItem("-- Select Account --");
    if (clientId == null || clientId.trim().isEmpty()) {
        if (statusLabel != null) statusLabel.setText("Enter a Client ID");
        return;
    }
    Client c = findC(clientId.trim().toUpperCase());
    if (c == null) {
        if (statusLabel != null) statusLabel.setText("❌ Client not found");
        return;
    }
    int count = 0;
    for (String accId : c.accountIds) {
        BankAccount a = findA(accId);
        if (a != null) {
            accountCombo.addItem(accId + " | " + a.type + " | Bal: " + rs(a.balance));
            count++;
        }
    }
    if (statusLabel != null) {
        if (count == 0) statusLabel.setText("⚠️ No accounts found");
        else statusLabel.setText("✅ " + count + " account(s) loaded  —  " + c.name);
    }
}

static String getSelectedAccountId(JComboBox<String> accountCombo) {
    String selected = (String) accountCombo.getSelectedItem();
    if (selected == null || selected.startsWith("--")) return "";
    return selected.split("\\|")[0].trim();
}
    // ════════════════════════════════════════════════════════
    //  SEED DATA
    // ════════════════════════════════════════════════════════
    static void seedData(){
        clients.add(new Client("CLT00001","Mr. Vikram Singh",  "9988776655","vikram.singh@gmail.com",  "Delhi",    "AABCS1234D","1234 5678 9012","Business",    2500000,"VERIFIED","HIGH",    "2019-03-15"));
        clients.add(new Client("CLT00002","Mrs. Meera Nair",   "8877665544","meera.nair@gmail.com",    "Bengaluru","BBBMN4567F","9876 5432 1098","Salaried",    1200000,"VERIFIED","MODERATE","2020-06-01"));
        clients.add(new Client("CLT00003","Dr. Arjun Kapoor",  "7766554433","dr.kapoor@clinic.com",    "Mumbai",   "CCCAK7890G","4567 8901 2345","Professional",3600000,"VERIFIED","MODERATE","2018-01-10"));
        clients.add(new Client("CLT00004","Ms. Deepa Menon",   "6655443322","deepa.menon@company.com", "Chennai",  "DDDLM2345H","7890 1234 5678","Salaried",     900000,"PENDING", "LOW",     "2022-09-12"));
        clients.add(new Client("CLT00005","Mr. Rohit Joshi",   "5544332211","rohit.joshi@business.com","Jaipur",   "EEERI9012J","3456 7890 1234","Business",    4800000,"VERIFIED","HIGH",    "2017-07-20"));
        BankAccount a1=new BankAccount("SB000000001","CLT00001","SAVINGS", 456750,1000,3.5,"2019-03-15"); a1.nominee="Anita Singh";
        BankAccount a2=new BankAccount("CA000000002","CLT00001","CURRENT",1250000,5000,0,  "2019-03-16");
        BankAccount a3=new BankAccount("SB000000003","CLT00002","SAVINGS", 189500,1000,3.5,"2020-06-01"); a3.nominee="Suresh Nair";
        BankAccount a4=new BankAccount("SB000000004","CLT00003","SAVINGS", 872000,1000,3.5,"2018-01-10");
        BankAccount a5=new BankAccount("SB000000005","CLT00004","SAVINGS",  45200, 500,2.5,"2022-09-12");
        BankAccount a6=new BankAccount("SB000000006","CLT00005","SAVINGS",2340000,1000,3.5,"2017-07-20");
        BankAccount a7=new BankAccount("CA000000007","CLT00005","CURRENT",5600000,5000,0,  "2017-07-21");
        a1.addTxn("CREDIT",50000,"Salary Credit Apr"); a1.addTxn("DEBIT",15000,"Bill Payment"); a1.addTxn("CREDIT",12500,"Interest Credit");
        a2.addTxn("CREDIT",500000,"Business Receipt"); a2.addTxn("DEBIT",200000,"Rent Payment");
        a3.addTxn("CREDIT",75000,"Salary Credit"); a3.addTxn("DEBIT",8000,"Grocery");
        a4.addTxn("CREDIT",250000,"Consultation Fees");
        accounts.addAll(Arrays.asList(a1,a2,a3,a4,a5,a6,a7));
        clients.get(0).accountIds.addAll(Arrays.asList("SB000000001","CA000000002"));
        clients.get(1).accountIds.add("SB000000003");
        clients.get(2).accountIds.add("SB000000004");
        clients.get(3).accountIds.add("SB000000005");
        clients.get(4).accountIds.addAll(Arrays.asList("SB000000006","CA000000007"));
        fds.add(new FixedDeposit("FD0000001","CLT00001","SB000000001", 500000,7.5, 36,"2022-04-01","2025-04-01","CUMULATIVE"));
        fds.add(new FixedDeposit("FD0000002","CLT00003","SB000000004",1000000,7.1, 24,"2023-01-01","2025-01-01","CUMULATIVE"));
        fds.add(new FixedDeposit("FD0000003","CLT00005","SB000000006",2000000,7.25,60,"2021-07-01","2026-07-01","NON_CUMULATIVE"));
        FixedDeposit fd4=new FixedDeposit("FD0000004","CLT00002","SB000000003",200000,6.9,12,"2023-03-01","2024-03-01","CUMULATIVE"); fd4.status="MATURED"; fds.add(fd4);
        RecurringDeposit rd1=new RecurringDeposit("RD0000001","CLT00002","SB000000003",5000, 6.5,24,"2023-01-01","2025-01-01"); rd1.paid=15;rd1.totalDep=75000;
        RecurringDeposit rd2=new RecurringDeposit("RD0000002","CLT00004","SB000000005",2000, 6.2,12,"2023-09-01","2024-09-01"); rd2.paid=7; rd2.totalDep=14000;
        RecurringDeposit rd3=new RecurringDeposit("RD0000003","CLT00005","SB000000006",25000,6.7,36,"2022-07-01","2025-07-01"); rd3.paid=21;rd3.totalDep=525000;
        rds.addAll(Arrays.asList(rd1,rd2,rd3));
        MutualFund mf1=new MutualFund("MF0000001","CLT00001","HDFC Top 100 Fund",           "Equity - Large Cap",      "HDFC AMC",        500000,78.45, 6500,"2021-06-15"); mf1.isSIP=true;mf1.sipAmt=10000;mf1.curVal=510000;
        MutualFund mf2=new MutualFund("MF0000002","CLT00003","SBI Bluechip Fund",            "Equity - Large Cap",      "SBI Funds",       300000,68.30, 4500,"2020-03-01"); mf2.curVal=350000;
        MutualFund mf3=new MutualFund("MF0000003","CLT00005","Mirae Asset Emerging Bluechip","Equity - Large & Mid Cap","Mirae AMC",      1000000,95.20,11000,"2019-01-10"); mf3.isSIP=true;mf3.sipAmt=25000;mf3.curVal=1250000;
        MutualFund mf4=new MutualFund("MF0000004","CLT00002","ICICI Prudential Liquid Fund", "Debt - Liquid",           "ICICI Prudential",150000,310.50, 490,"2023-09-01"); mf4.curVal=157500;
        mfs.addAll(Arrays.asList(mf1,mf2,mf3,mf4));
        Loan l1=new Loan("LN0000001","CLT00001","SB000000001","Home Loan",2500000,8.5,240,"2022-01-15"); double mr1=8.5/100.0/12.0; l1.emiAmt=2500000*mr1*Math.pow(1+mr1,240)/(Math.pow(1+mr1,240)-1); l1.paidEmis=38; l1.outstandingBal=2500000*(1-38.0/240.0); loans.add(l1);
        Loan l2=new Loan("LN0000002","CLT00002","SB000000003","Car Loan",800000,9.2,60,"2023-06-01"); double mr2=9.2/100.0/12.0; l2.emiAmt=800000*mr2*Math.pow(1+mr2,60)/(Math.pow(1+mr2,60)-1); l2.paidEmis=9; l2.outstandingBal=800000*(1-9.0/60.0); loans.add(l2);
        Loan l3=new Loan("LN0000003","CLT00005","SB000000006","Business Loan",5000000,13.0,84,"2020-07-20"); double mr3=13.0/100.0/12.0; l3.emiAmt=5000000*mr3*Math.pow(1+mr3,84)/(Math.pow(1+mr3,84)-1); l3.paidEmis=84; l3.outstandingBal=0; l3.status="CLOSED"; l3.closureDate="2027-07-20"; loans.add(l3);
        // Seed ATM cards
        ATMCard atm1=new ATMCard("ATM00001","CLT00001","SB000000001","4532 1234 5678 9010","VISA","Gold","1234","2022-03-15","03/27"); atm1.dailyLimit=50000; atmCards.add(atm1);
        ATMCard atm2=new ATMCard("ATM00002","CLT00002","SB000000003","5200 8765 4321 0987","MasterCard","Classic","5678","2023-06-01","06/28"); atm2.dailyLimit=25000; atmCards.add(atm2);
        ATMCard atm3=new ATMCard("ATM00003","CLT00003","SB000000004","6070 1122 3344 5566","RuPay","Platinum","9012","2021-01-10","01/26"); atm3.dailyLimit=100000; atmCards.add(atm3);
        ATMCard atm4=new ATMCard("ATM00004","CLT00005","SB000000006","4716 9988 7766 5544","VISA","Titanium","3456","2020-07-20","07/25"); atm4.dailyLimit=200000; atm4.status="BLOCKED"; atmCards.add(atm4);
        CARD_CTR=5;
        // Seed credit cards
        CreditCard cc1=new CreditCard("CC00001","CLT00001","CA000000002","4111 5555 2222 7777","VISA","Platinum","2021-03-15","03/26",500000); cc1.outstanding=125000; cc1.minDue=6250; cc1.dueDate=LocalDate.now().plusDays(8).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")); creditCards.add(cc1);
        CreditCard cc2=new CreditCard("CC00002","CLT00003","SB000000004","5425 6789 0123 4321","MasterCard","Gold","2020-01-10","01/25",300000); cc2.outstanding=45000; cc2.minDue=2250; cc2.dueDate=LocalDate.now().plusDays(15).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")); creditCards.add(cc2);
        CreditCard cc3=new CreditCard("CC00003","CLT00005","CA000000007","3782 822463 10005","AmericanExpress","Signature","2019-07-20","07/24",1000000); cc3.outstanding=0; cc3.minDue=0; cc3.dueDate="\u2014"; cc3.status="BLOCKED"; creditCards.add(cc3);
        CCARD_CTR=4;
        // Seed cheque books
        ChequeBook chq1=new ChequeBook("CHQ00001","CLT00001","SB000000001","BK000001",25,"000001","000025","2022-03-15"); chq1.usedLeaves=14; chequeBooks.add(chq1);
        ChequeBook chq2=new ChequeBook("CHQ00002","CLT00001","CA000000002","BK000002",50,"000026","000075","2022-03-15"); chq2.usedLeaves=30; chequeBooks.add(chq2);
        ChequeBook chq3=new ChequeBook("CHQ00003","CLT00003","SB000000004","BK000003",10,"000001","000010","2021-01-10"); chq3.usedLeaves=10; chq3.status="EXHAUSTED"; chequeBooks.add(chq3);
        ChequeBook chq4=new ChequeBook("CHQ00004","CLT00005","CA000000007","BK000004",25,"000001","000025","2020-07-20"); chq4.usedLeaves=5; chequeBooks.add(chq4);
        CHQ_CTR=5;
    }

    // ════════════════════════════════════════════════════════
    //  DATA MODELS — CARDS & CHEQUES
    // ════════════════════════════════════════════════════════
    static class ATMCard {
        String id, clientId, accountId, cardNumber, network, type, pin, issueDate, expiryDate;
        String status = "ACTIVE";
        double dailyLimit = 25000;
        ATMCard(String id,String cid,String aid,String num,String net,String type,String pin,String issued,String exp){
            this.id=id; this.clientId=cid; this.accountId=aid; this.cardNumber=num;
            this.network=net; this.type=type; this.pin=pin; this.issueDate=issued; this.expiryDate=exp;
        }
    }

    static class CreditCard {
        String id, clientId, accountId, cardNumber, network, cardType, issueDate, expiryDate, dueDate;
        String status = "ACTIVE";
        double creditLimit, outstanding = 0, minDue = 0;
        CreditCard(String id,String cid,String aid,String num,String net,String type,String issued,String exp,double limit){
            this.id=id; this.clientId=cid; this.accountId=aid; this.cardNumber=num;
            this.network=net; this.cardType=type; this.issueDate=issued; this.expiryDate=exp;
            this.creditLimit=limit; this.dueDate="N/A";
        }
    }

    static class ChequeBook {
        String id, clientId, accountId, bookNumber, startCheque, endCheque, issueDate;
        String status = "ACTIVE";
        int leaves, usedLeaves = 0;
        ChequeBook(String id,String cid,String aid,String bnum,int leaves,String start,String end,String issued){
            this.id=id; this.clientId=cid; this.accountId=aid; this.bookNumber=bnum;
            this.leaves=leaves; this.startCheque=start; this.endCheque=end; this.issueDate=issued;
        }
    }
}
