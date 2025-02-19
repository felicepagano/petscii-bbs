package eu.sblendorio.bbs.tenants.ascii;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxmind.db.Reader;
import eu.sblendorio.bbs.core.AsciiThread;
import eu.sblendorio.bbs.core.BbsThread;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

public class MenuApple1 extends AsciiThread {

    public MenuApple1() {
        this(false);
    }

    public MenuApple1(boolean echo) {
        super();
        setLocalEcho(echo);
    }

    public static class GeoData {
        public final String city;
        public final String cityGeonameId;
        public final String country;
        public final Double latitude;
        public final Double longitude;
        public final String timeZone;
        public GeoData(final String city, final String cityGeonameId, final String country, final Double latitude, final Double longitude, final String timeZone) {
            this.city = city;
            this.cityGeonameId = cityGeonameId;
            this.country = country;
            this.latitude = latitude;
            this.longitude = longitude;
            this.timeZone = timeZone;
        }
    }

    private static final String MAXMIND_DB = System.getProperty("user.home") + File.separator + "GeoLite2-City.mmdb";
    private Reader maxmindReader;
    private JsonNode maxmindResponse;
    private GeoData geoData;

    public void init() throws IOException {
        try {
            File maxmindDb = new File(MAXMIND_DB);
            maxmindReader = new Reader(maxmindDb);
            maxmindResponse = maxmindReader.get(socket.getInetAddress());
            maxmindReader.close();

            geoData = new GeoData(
                maxmindResponse.get("city").get("names").get("en").asText(),
                maxmindResponse.get("city").get("geoname_id").asText(),
                maxmindResponse.get("country").get("names").get("en").asText(),
                maxmindResponse.get("location").get("latitude").asDouble(),
                maxmindResponse.get("location").get("longitude").asDouble(),
                maxmindResponse.get("location").get("time_zone").asText()
            );
            log("Location: " + geoData.city + ", " + geoData.country);
        } catch (Exception e) {
            maxmindResponse = null;
            geoData = null;
            log("Error retrieving GeoIP data: " + e.getClass().getName());
        }
    }

    public void logo() throws Exception {
        readTextFile("apple1/intro-menu.txt").forEach(this::println);
        flush();
    }

    protected String banner() { return "BBS for Apple I - by F. Sblendorio 2020"; }

    @Override
    public void doLoop() throws Exception {
        init();
        String sp = (getScreenColumns() > 40) ? "                    " : "";
        logo();
        while (true) {
            log("Starting Apple1 / main menu");
            println();
            println();
            println();
            println();
            println();
            println(banner());
            println();
            println("International News   "+sp+"  Game Room");
            println("------------------   "+sp+"  ---------------");
            println("A - CNN News         "+sp+"  N - TIC TAC TOE");
            println("B - BBC News         "+sp+"  O - Connect Four");
            println("C - Indie Retro News "+sp+"  P - Zork I");
            println("D - VCF News         "+sp+"  Q - Zork II");
            println("E - The 8-Bit Guy    "+sp+"  R - Zork III");
            println("                     "+sp+"  S - Hitchhiker's");
            println("Italian News");
            println("-----------------");
            println("F - Televideo RAI");
            println("G - Wired Italia");
            println("H - Disinformatico");
            println("I - Il Post           "+sp+"  Services");
            println("J - Fatto Quotidiano  "+sp+"  ----------");
            println("K - Retrocampus       "+sp+"  T - Chat");
            println("L - Butac.it          "+sp+"  U - Private Msg");
            println("M - Facta.news        "+sp+"  . - Logout");

             //final String line = geoData != null ? "Connected from "+geoData.city+", "+geoData.country : EMPTY;
            final String line = "(C) F. Sblendorio in 2018, 2019";

            flush();
            boolean validKey;
            do {
                validKey = true;
                log("Menu. Waiting for key pressed.");
                println();
                print("> ");
                resetInput();
                String choice = readLine();
                resetInput();
                choice = StringUtils.lowerCase(choice);
                log("Menu. Choice = "+ choice);
                BbsThread subThread;
                if (".".equals(choice)) {
                    newline();
                    newline();
                    println("Disconnected.");
                    return;
                }
                else if ("a".equals(choice)) subThread = new CnnAscii();
                else if ("b".equals(choice)) subThread = new BbcAscii();
                else if ("c".equals(choice)) subThread = new IndieRetroNewsAscii();
                else if ("d".equals(choice)) subThread = new VcfedAscii();
                else if ("e".equals(choice)) subThread = new The8BitGuyAscii();
                else if ("f".equals(choice)) subThread = new TelevideoRaiAscii();
                else if ("g".equals(choice)) subThread = new WiredItaliaAscii();
                else if ("h".equals(choice)) subThread = new DisinformaticoAscii();
                else if ("i".equals(choice)) subThread = new IlPostAscii();
                else if ("j".equals(choice)) subThread = new IlFattoQuotidianoAscii();
                else if ("k".equals(choice)) subThread = new RetroCampusAscii();
                else if ("l".equals(choice)) subThread = new ButacAscii();
                else if ("m".equals(choice)) subThread = new FactaNewsAscii();
                else if ("n".equals(choice)) subThread = new TicTacToeAscii();
                else if ("o".equals(choice)) subThread = new Connect4Ascii();
                else if ("p".equals(choice)) subThread = new ZorkMachineAscii("zmpp/zork1.z3");
                else if ("q".equals(choice)) subThread = new ZorkMachineAscii("zmpp/zork2.z3");
                else if ("r".equals(choice)) subThread = new ZorkMachineAscii("zmpp/zork3.z3");
                else if ("s".equals(choice)) subThread = new ZorkMachineAscii("zmpp/hitchhiker-r60.z3");
                else if ("t".equals(choice)) subThread = new ChatA1();
                else if ("u".equals(choice)) subThread = new PrivateMessagesAscii();
                else {
                    validKey = false;
                    subThread = null;
                }
                if (subThread != null) {
                    if (subThread instanceof AsciiThread) {
                        ((AsciiThread) subThread).clsBytes = this.clsBytes;
                        ((AsciiThread) subThread).screenColumns = this.screenColumns;
                        ((AsciiThread) subThread).screenRows = this.screenRows;
                    }
                    launch(subThread);
                }
            } while (!validKey);
        }
    }
}