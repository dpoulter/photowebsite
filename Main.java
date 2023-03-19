
import java.io.IOException;
import java.nio.file.*;
import javax.imageio.metadata.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
public class Main {




    public static void main(String[] args) {

        String subPath = "1940-1949";
        String folderPath = "/Users/dalepoulter/Pictures/"+subPath;
        String commentsFile=folderPath+"/photoComments.csv";

        // Read the photcomments file and build a list of photoComments

        photoComments PhotoComments= new photoComments(commentsFile);

        //Read list of photos and build html

        String[] photos = new String[1000];

        String divRow="<div class=\"row\">\n" ;
        String webPage=divRow;
        String imageRef1=
                "    <div class=\"col-md-4\">\n" +
                "      <div class=\"thumbnail\">";
        String imageRef2=" <a href=\"/images/"+subPath+"/";

        String imageRef3="\" target=\"_blank\">\n" +
                "          <img src=\"/images/"+subPath+"/";

        String imageRef4= "\" alt=\"";

        String imageRef5="\" style=\"width:100%\">\n" + "\t\t</a>"+ "<div class=\"caption\"><p>";

        String imageRef6=
                "</p>\n" +
                "          </div>\n" +
                "        \n" +
                "      </div>\n" +
                "    </div>";

        String divCol = "";
        int colCount=0;
        int i=0;
        //System.out.println(cars[0]);

        Path dir = Paths.get(folderPath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file: stream) {
                //Metadata meta = new Metadata();

                //meta.readAndDisplayMetadata( file.toFile() );

               //System.out.println(file.getFileName());
                photos[i]=file.getFileName().toString();

                i=i+1;
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }

        for (int j=0;j<i;j++){

            //Find photcomments
            String caption = PhotoComments.getComment(photos[j]);

            //Build html
            divCol=imageRef1+photos[j]+imageRef2+photos[j]+imageRef3+photos[j]+imageRef4+photos[j]+imageRef5+caption+imageRef6;

                if (colCount % 3 == 0) {
                    webPage = webPage + "\n</div>\n" + divRow + divCol;
                } else {
                    webPage = webPage + "\n" + divCol;
                }
            colCount++;
            }
        System.out.println(webPage);

// Now outputs Opel instead of Volvo
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
