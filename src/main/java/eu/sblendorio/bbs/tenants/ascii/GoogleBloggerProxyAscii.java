package eu.sblendorio.bbs.tenants.ascii;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.BloggerScopes;
import com.google.api.services.blogger.model.Post;
import com.google.api.services.blogger.model.PostList;
import com.rometools.utils.IO;
import eu.sblendorio.bbs.core.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static eu.sblendorio.bbs.core.Utils.*;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.math.NumberUtils.*;

@Hidden
public class GoogleBloggerProxyAscii extends AsciiThread {

    String HR_TOP;

    @Override
    public void initBbs() throws Exception {
        HR_TOP = StringUtils.repeat('-', getScreenColumns() - 1);
    }

    protected String blogUrl = "https://blogger.googleblog.com";
    protected byte[] logo = LOGO_BLOGGER;
    protected int pageSize = 8;
    protected int screenLines = 19;

    protected static final String CRED_FILE_PATH = System.getProperty("user.home") + File.separator + "credentials.json";

    protected GoogleCredential credential;
    protected Blogger blogger;
    protected String blogId;

    protected Map<Integer, Post> posts = null;

    protected static class PageTokens {
        Stack<String> tokens = new Stack<>();

        String prev = null;
        String curr = null;
        String next = null;
        int page = 1;

        public void reset() {
            prev=null; curr=null; next=null; page=1; tokens.clear();
        }
    }

    protected PageTokens pageTokens = new PageTokens();

    private String originalBlogUrl;

    public GoogleBloggerProxyAscii() {
        super();
    }

    public GoogleBloggerProxyAscii(String blogUrl) {
        this();
        this.blogUrl = blogUrl;
    }

    public GoogleBloggerProxyAscii(String blogUrl, byte[] logo) {
        this();
        this.blogUrl = blogUrl;
        this.logo = logo;
    }

    public void init() throws IOException {
        try {
            originalBlogUrl = blogUrl;
            cls();

            this.credential = GoogleCredential
                .fromStream(new FileInputStream(CRED_FILE_PATH))
                .createScoped(Arrays.asList(BloggerScopes.BLOGGER));

            this.blogger = new Blogger.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                .setApplicationName("Apple-1 BBS Builder - Blogger Proxy - " + this.getClass().getSimpleName())
                .build();

            changeBlogIdByUrl(this.blogUrl);
            pageTokens.reset();
        } catch (IOException e) {
            exitWithError();
        }
    }

    protected void changeBlogIdByUrl(String url) throws IOException {
        this.blogUrl = url;
        blogId = blogger.blogs().getByUrl(blogUrl).execute().getId();
    }

    @Override
    public void doLoop() throws Exception {
        init();
        log("Blogger entering (" + blogUrl + ")");
        listPosts();
        while (true) {
            log("Blogger waiting for input");
            print("#");
            print(" [");
            print("N+-");
            print("]Page [");
            print("H");
            print("]elp [");
            print("R");
            print("]eload [");
            print(".");
            print("]");
            print("Q");
            print("uit> ");
            resetInput();
            flush(); String inputRaw = readLine();
            String input = lowerCase(trim(inputRaw));
            if (".".equals(input) || "exit".equals(input) || "quit".equals(input) || "q".equals(input)) {
                break;
            } else if ("help".equals(input) || "h".equals(input)) {
                help();
                listPosts();
                continue;
            } else if ("n".equals(input) || "N".equals(input) || "+".equals(input)) {
                pageTokens.tokens.push(pageTokens.prev);
                pageTokens.prev = pageTokens.curr;
                pageTokens.curr = pageTokens.next;
                pageTokens.next = null;
                ++pageTokens.page;
                posts = null;
                listPosts();
                continue;
            } else if ("-".equals(input) && pageTokens.page > 1) {
                pageTokens.next = pageTokens.curr;
                pageTokens.curr = pageTokens.prev;
                pageTokens.prev = pageTokens.tokens.pop();
                --pageTokens.page;
                posts = null;
                listPosts();
                continue;
            } else if ("--".equals(input) && pageTokens.page > 1) {
                pageTokens.reset();
                posts = null;
                listPosts();
                continue;
            } else if ("r".equals(input) || "reload".equals(input) || "refresh".equals(input)) {
                posts = null;
                listPosts();
                continue;
            } else if (posts.containsKey(toInt(input))) {
                displayPost(toInt(input));
            } else if ("".equals(input)) {
                listPosts();
                continue;
            } else if ("clients".equals(input)) {
                listClients();
                continue;
            } else if (substring(input,0,5).equalsIgnoreCase("send ")) {
                long client = toLong(input.replaceAll("^send ([0-9]+).*$", "$1"));
                String message = input.replaceAll("^send [0-9]+ (.*)$", "$1");
                if (getClients().containsKey(client) && isNotBlank(message)) {
                    log("Sending '"+message+"' to #"+client);
                    int exitCode = send(client, message);
                    log("Message sent, exitCode="+exitCode+".");
                }
            } else if (substring(input,0,5).equalsIgnoreCase("name ")) {
                String newName = defaultString(input.replaceAll("^name ([^\\s]+).*$", "$1"));
                changeClientName(newName);
            } else if (substring(input, 0, 8).equalsIgnoreCase("connect ")) {
                final String oldBlogUrl = blogUrl;
                final byte[] oldLogo = logo;
                String newUrl = trim(input.replaceAll("^connect ([^\\s]+).*$", "$1"));
                if (newUrl.indexOf('.') == -1) newUrl += ".blogspot.com";
                if (!newUrl.matches("(?is)^http.*")) newUrl = "https://" + newUrl;
                log("new blogUrl: "+newUrl);
                try {
                    changeBlogIdByUrl(newUrl);
                    pageTokens.reset();
                    posts = null;
                    listPosts();
                } catch (Exception e) {
                    log("BLOGGER FAILED: " + e.getClass().getName() + ": " + e.getMessage());
                    logo = oldLogo;
                    changeBlogIdByUrl(oldBlogUrl);
                    pageTokens.reset();
                    posts = null;
                    listPosts();
                }
            }
        }
        flush();
    }

    protected Map<Integer, Post> getPosts() throws IOException {
        Map<Integer, Post> result = new LinkedHashMap<>();

        Blogger.Posts.List action = blogger.posts().list(blogId).setPageToken(pageTokens.curr);
        action.setFields("items(author/displayName,id,content,published,title,url),nextPageToken");
        action.setMaxResults(Long.valueOf(pageSize));
        PostList list = action.execute();

        for (int i=0; i<list.getItems().size(); ++i) {
            Post post = list.getItems().get(i);
            result.put(i+1+(pageSize*(pageTokens.page-1)), post);
        }

        pageTokens.next = list.getNextPageToken();
        return result;
    }

    protected void listPosts() throws IOException {
        cls();
        drawLogo();
        if (posts == null) {
            posts = getPosts();
        }
        for (Map.Entry<Integer, Post> entry: posts.entrySet()) {
            int i = entry.getKey();
            Post post = entry.getValue();
            print(i + ".");
            final int nCols = getScreenColumns() - 3;
            final int iLen = nCols-String.valueOf(i).length();
            String line = WordUtils.wrap(filterPrintable(HtmlUtils.htmlClean(post.getTitle())), iLen, "\r", true);
            println(line.replaceAll("\r", newlineString() + " " + repeat(" ", nCols-iLen)));
        }
        newline();
    }

    protected List<String> wordWrap(String s) {
        String[] cleaned = filterPrintableWithNewline(HtmlUtils.htmlClean(s)).split("\n");
        List<String> result = new ArrayList<>();
        for (String item: cleaned) {
            String[] wrappedLine = WordUtils
                .wrap(item, getScreenColumns() - 1, "\n", true)
                .split("\n");
            result.addAll(asList(wrappedLine));
        }
        return result;
    }

    protected void help() throws IOException {
        cls();
        drawLogo();
        println();
        println();
        println("Press any key to go back to posts");
        readKey();
    }

    protected void displayPost(int n) throws IOException {
        cls();
        drawLogo();
        final Post p = posts.get(n);
        String content = p.getContent()
            .replaceAll("(?is)<style>.*</style>", EMPTY)
            .replaceAll("(?is)<script .*</script>", EMPTY)
            .replaceAll("(?is)^[\\s\\n\\r]+|^\\s*(/?<(br|div|figure|iframe|img|p|h[0-9])[^>]*>\\s*)+", EMPTY)
            .replaceAll("(?is)^(<[^>]+>(\\s|\n|\r)*)+", EMPTY);
        final String head = p.getTitle() +
            "<br>" +
            HR_TOP +
            "<br>";
        List<String> rows = wordWrap(head);
        List<String> article = wordWrap(
            p.getPublished().toStringRfc3339().replaceAll("^(\\d\\d\\d\\d).(\\d\\d).(\\d\\d).*","$3/$2/$1") +
                " - " +
                content);
        rows.addAll(article);

        int page = 1;
        int j = 0;
        boolean forward = true;
        while (j < rows.size()) {
            if (j>0 && j % screenLines == 0 && forward) {
                println();
                print("-PAGE " + page + "-  SPACE=NEXT  -=PREV  .=EXIT");

                resetInput(); int ch = readKey();
                if (ch == '.') {
                    listPosts();
                    return;
                }
                if (ch == '-' && page > 1) {
                    j -= (screenLines *2);
                    --page;
                    forward = false;
                    cls();
                    drawLogo();
                    continue;
                } else {
                    ++page;
                }
                cls();
                drawLogo();
            }
            String row = rows.get(j);
            println(row);
            forward = true;
            ++j;
        }
        println();
    }

    protected static final byte[] LOGO_BLOGGER = "Blogger".getBytes(StandardCharsets.ISO_8859_1);

    protected void drawLogo() {
        if (!equalsDomain(blogUrl, originalBlogUrl)) {
            final String normDomain = normalizeDomain(blogUrl);
            print(normDomain);
        } else {
            write(logo);
        }
        newline();
        newline();
    }

    protected void listClients() {
        cls();
        println("You are #" + getClientId() + ": "+getClientName() + " [" + getClientClass().getSimpleName() + "]");
        newline();
        for (Map.Entry<Long, BbsThread> entry: clients.entrySet())
            if (entry.getKey() != getClientId())
                println("#" + entry.getKey() +": "+entry.getValue().getClientName() + " [" + entry.getValue().getClientClass().getSimpleName() + "]");
        println();
    }

    protected void exitWithError() throws IOException {
        log("Missing file " + CRED_FILE_PATH + " on the server's filesystem");
        cls();
        drawLogo();
        newline();
        print(" "); print("       "); println(" Missing Google credentials on");
        print(" "); print(" ERROR "); println(" server's filesystem. Contact");
        print(" "); print("       "); println(" the system administrator.");
        newline();
        flush();
        throw new BbsIOException("Missing file " + CRED_FILE_PATH + " on the server's filesystem");
    }

    @Override
    public void receive(long sender, Object message) {
        log("--------------------------------");
        log("From "+getClients().get(sender).getClientName()+": " +message);
        log("--------------------------------");
        println();
        println("--------------------------------");
        println("From "+getClients().get(sender).getClientName()+": " +message);
        println("--------------------------------");
    }

}
