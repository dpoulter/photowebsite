
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import javax.imageio.metadata.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import org.w3c.dom.*;
import java.io.*;
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

            String webPage = "<?php\n" +
            "header(\"Cache-Control: no-cache, no-store, must-revalidate\"); // HTTP 1.1.\n" +
            "header(\"Pragma: no-cache\"); // HTTP 1.0.\n" +
            "header(\"Expires: 0\"); // Proxies.\n" +
            "?>\n" +
            "<!doctype html>\n" +
            "<html lang=\"en\">\n" +

            "<?php require(\"header.html\");?>\n" +

            "<body class=\"bg-light\">\n" +

            "<script src=\"https://code.jquery.com/jquery-3.3.1.slim.min.js\" integrity=\"sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo\" crossorigin=\"anonymous\"></script>\n" +
            "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.6/umd/popper.min.js\" integrity=\"sha384-wHAiFfRlMFy6i5SRaxvfOCifBUQy1xHdJ/yoi7FRNXMRBu5WHdZYu1hA6ZOblgut\" crossorigin=\"anonymous\"></script>\n" +
            "<script src=\"https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js\" integrity=\"sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM\" crossorigin=\"anonymous\"></script>\n" +

            "<section class=\"jumbotron text-center\">\n" +
                    "<div class=\"container\">\n" +
                    "<h1 class=\"jumbotron-heading\">" + escapeHtml(subPath) + "</h1>\n" +
                    "<p class=\"lead text-muted\">Click on the images to enlarge them.</p>\n" +
                    "</div>\n" +
                    "</section>\n" +
            "<div class=\"container\">";

            webPage = webPage + "\n" + generate_for_folder(path, subPath, commentsFile) +
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

    public static String generate_for_folder(Path dir, String subPath, String commentsFile) {

        // Read the photcomments file and build a list of photoComments
        photoComments PhotoComments= new photoComments(commentsFile);

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

        String divRow="<div class=\"row\">\n" ;
        String webPage=divRow;
        String urlBase = "/images/" + encodeUrl(subPath) + "/";
        int colCount=0;

        for (String photo : photos) {

            //Find photcomments
            String caption = PhotoComments.getComment(photo);
            String url = escapeHtml(urlBase + encodeUrl(photo));

            //Build html
            String divCol =
                    "    <div class=\"col-md-4\">\n" +
                    "      <div class=\"thumbnail\">\n" +
                    "        <a href=\"" + url + "\" target=\"_blank\">\n" +
                    "          <img src=\"" + url + "\" alt=\"" + escapeHtml(photo) + "\" style=\"width:100%\">\n" +
                    "        </a>\n" +
                    "        <div class=\"caption\"><p>" + escapeHtml(caption) + "</p>\n" +
                    "        </div>\n" +
                    "      </div>\n" +
                    "    </div>";

            // Start a new row after every 3 photos
            if (colCount > 0 && colCount % 3 == 0) {
                webPage = webPage + "\n</div>\n" + divRow + divCol;
            } else {
                webPage = webPage + "\n" + divCol;
            }
            colCount++;
        }
        return webPage + "\n</div>\n";
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



    void readAndDisplayMetadata( Path file ) {
        try {


            ImageInputStream iis = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

            if (readers.hasNext()) {

                // pick the first available ImageReader
                ImageReader reader = readers.next();

                // attach source to the reader
                reader.setInput(iis, true);

                // read metadata of first image
                IIOMetadata metadata = reader.getImageMetadata(0);

                String[] names = metadata.getMetadataFormatNames();
                int length = names.length;
                for (int i = 0; i < length; i++) {
                    System.out.println( "Format name: " + names[ i ] );
                    displayMetadata(metadata.getAsTree(names[i]));
                }
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }



    void displayMetadata(Node root) {
        displayMetadata(root, 0);
    }

    void indent(int level) {
        for (int i = 0; i < level; i++)
            System.out.print("    ");
    }

    void displayMetadata(Node node, int level) {
        // print open tag of element
        indent(level);
        System.out.print("<" + node.getNodeName());
        NamedNodeMap map = node.getAttributes();
        if (map != null) {

            // print attribute values
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                System.out.print(" " + attr.getNodeName() +
                        "=\"" + attr.getNodeValue() + "\"");
            }
        }

        Node child = node.getFirstChild();
        if (child == null) {
            // no children, so close element and return
            System.out.println("/>");
            return;
        }

        // children, so close current tag
        System.out.println(">");
        while (child != null) {
            // print children recursively
            displayMetadata(child, level + 1);
            child = child.getNextSibling();
        }

        // print close tag of element
        indent(level);
        System.out.println("</" + node.getNodeName() + ">");
    }
}
