import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Main {

    // Only files with these extensions are treated as photos
    static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "tif", "tiff"));

    public static void main(String[] args) {
        if(args.length != 1){
            System.err.println("Usage: java Main [folderPath]");
            System.exit(1);
        }

        Path root = Paths.get(args[0]).toAbsolutePath().normalize();

        // Each immediate subdirectory of the folderPath becomes one web page
        List<Path> subFolders;
        try (Stream<Path> stream = Files.list(root)) {
            subFolders = stream.filter(Files::isDirectory).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Unable to read folder " + root + ": " + e);
            System.exit(1);
            return;
        }

        for (Path path : subFolders) {
            String subPath = path.getFileName().toString();
            String commentsFile = path.resolve("photoComments.csv").toString();

            System.out.println("Generating page for " + subPath);

            //Generate first section of webpage- which is a PHP page- modify as required.
            //The PHP block must come before any output, otherwise header() fails with "headers already sent".
            //Bootstrap JS is 4.6.2; header.html must load the matching Bootstrap 4.6.2 CSS.

            String webPage = "<?php\n" +
            "header(\"Cache-Control: no-cache, no-store, must-revalidate\"); // HTTP 1.1.\n" +
            "header(\"Pragma: no-cache\"); // HTTP 1.0.\n" +
            "header(\"Expires: 0\"); // Proxies.\n" +
            "?>\n" +
            "<!doctype html>\n" +
            "<html lang=\"en\">\n" +

            "<?php require(\"header.html\");?>\n" +

            "<body class=\"bg-light\">\n" +

            "<script src=\"https://cdn.jsdelivr.net/npm/jquery@3.5.1/dist/jquery.slim.min.js\" integrity=\"sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj\" crossorigin=\"anonymous\"></script>\n" +
            "<script src=\"https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js\" integrity=\"sha384-9/reFTGAW83EW2RDu2S0VKaIzap3H66lZH81PoYlFhbGU+6BZp6G7niu735Sk7lN\" crossorigin=\"anonymous\"></script>\n" +
            "<script src=\"https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.min.js\" integrity=\"sha384-+sLIOodYLS7CIrQpBjl+C7nPvqq+FbNUBDunl/OZv93DB7Ln/533i8e/mZXLi/P+\" crossorigin=\"anonymous\"></script>\n" +

            "<section class=\"jumbotron text-center\">\n" +
                    "<div class=\"container\">\n" +
                    "<h1 class=\"jumbotron-heading\">" + escapeHtml(subPath) + "</h1>\n" +
                    "<p class=\"lead text-muted\">Click on the images to enlarge them.</p>\n" +
                    "</div>\n" +
                    "</section>\n" +
            "<div class=\"container\">";

            webPage = webPage + "\n" + generateForFolder(path, subPath, commentsFile) +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>";

            // Write the webpage to a file
            String outFile = "photos " + subPath + ".php";
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8))) {
                out.println(webPage);
                System.out.println("Wrote " + outFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String generateForFolder(Path dir, String subPath, String commentsFile) {

        // Read the photo comments file
        PhotoComments comments = new PhotoComments(commentsFile);

        //Read list of photos (sorted so the page order is stable) and build html
        List<String> photos = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            photos = stream.filter(Files::isRegularFile)
                    .map(file -> file.getFileName().toString())
                    .filter(Main::isImage)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException | UncheckedIOException x) {
            System.err.println("Unable to read folder " + dir + ": " + x);
        }

        // One Bootstrap 4 row of cards; the grid wraps them 1 per row on phones, 2 on tablets, 3 on desktops
        StringBuilder webPage = new StringBuilder("<div class=\"row\">\n");
        String urlBase = "/images/" + encodeUrl(subPath) + "/";

        for (String photo : photos) {

            //Find photo comment
            String caption = comments.getComment(photo);
            String url = escapeHtml(urlBase + encodeUrl(photo));

            //Build html
            webPage.append(
                    "  <div class=\"col-sm-6 col-md-4 mb-4\">\n" +
                    "    <div class=\"card h-100 shadow-sm\">\n" +
                    "      <a href=\"" + url + "\" target=\"_blank\">\n" +
                    "        <img class=\"card-img-top\" src=\"" + url + "\" alt=\"" + escapeHtml(photo) + "\" loading=\"lazy\">\n" +
                    "      </a>\n");
            if (!caption.isEmpty()) {
                webPage.append(
                    "      <div class=\"card-body\">\n" +
                    "        <p class=\"card-text\">" + escapeHtml(caption) + "</p>\n" +
                    "      </div>\n");
            }
            webPage.append(
                    "    </div>\n" +
                    "  </div>\n");
        }
        return webPage.append("</div>\n").toString();
    }

    static boolean isImage(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0) {
            return false; // no extension, or a hidden file such as .DS_Store
        }
        return IMAGE_EXTENSIONS.contains(filename.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    // Encode a single path segment; URLEncoder is for query strings so turn "+" back into "%20"
    static String encodeUrl(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
