
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
        if(args.length != 1){
            System.err.println("Usage: java Main [folderPath]");
            System.exit(1);
        }
    
        try {
            // Walk through each subdirectory in the folderPath
            Files.walk(Paths.get(args[0]))
                    .filter(Files::isDirectory)
                    .forEach(path -> {
                        //String subPath = path.toString();
                        String subPath = path.getFileName().toString();

                        if(!path.toString().equals(args[0]))
                        {
                            System.out.println("subPath != arg[0]: subPath="+subPath+" args[0]="+args[0]);

                            String commentsFile = path.toString() + "/photoComments.csv";
        
                            //Generate first section of webpage- which is a PHP page- modify as required.  
        
                            String webPage = "<!doctype html>" +
                            "<html lang=\"en\">" +
                            "<?php" +
                            "+header(\"Cache-Control: no-cache, no-store, must-revalidate\"); // HTTP 1.1." +
                            "header(\"Pragma: no-cache\"); // HTTP 1.0." +
                            "header(\"Expires: 0\"); // Proxies." +
                            "?>" +
        
                            "<?php require(\"header.html\");?>" + 
        
                            "<body class=\"bg-light\">" +
        
                            "<script src=\"https://code.jquery.com/jquery-3.3.1.slim.min.js\" integrity=\"sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo\" crossorigin=\"anonymous\"></script>" +
                            "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.6/umd/popper.min.js\" integrity=\"sha384-wHAiFfRlMFy6i5SRaxvfOCifBUQy1xHdJ/yoi7FRNXMRBu5WHdZYu1hA6ZOblgut\" crossorigin=\"anonymous\"></script>" +
                            "<script src=\"https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js\" integrity=\"sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM\" crossorigin=\"anonymous\"></script>" +
        
                            "<section class=\"jumbotron text-center\">" +
                                    "<div class=\"container\">" +
                                    "<h1 class=\"jumbotron-heading\">" + subPath + "</h1>" +
                                    "<p class=\"lead text-muted\">Click on the images to enlarge them.</p>" +
                                    "</div>" +
                                    "</section>" +
                            "<div class=\"container\">";

                            System.out.println("subPath = "+subPath);
                            System.out.println("commentsFile = "+commentsFile);
        
                            webPage = webPage + "\n" + generate_for_folder(args[0],subPath,commentsFile) + 
                                        "</div>" +
                                        "</body>" +
                                        "</html>";
                            System.out.println(webPage);

                            // Write the webpage to a file
                            try (PrintWriter out = new PrintWriter(new FileWriter("photos " + subPath + ".php"))) {
                                out.println(webPage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String generate_for_folder(String folderPath, String subPath, String commentsFile) {

        //commentsFile = folderPath + "/" + commentsFile;

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

        Path dir = Paths.get(folderPath+"/"+subPath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file: stream) {
                //Metadata meta = new Metadata();

                //meta.readAndDisplayMetadata( file.toFile() );

               System.out.println("Filename="+file.getFileName().toString());

               if(!(file.getFileName().toString().equals("photoComments.csv")||file.getFileName()==null)){
                photos[i]=file.getFileName().toString();
                i=i+1;
               }
                
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
        return (webPage);
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
