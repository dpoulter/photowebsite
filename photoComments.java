import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class photoComments {

    //Read the comments from the input file which is a file containing a list of filenames and corresponding comments
    //of that filename  eg.  BeachHoliday1.jpg,First day of our beach holiday
    //File will be stored in PhotoComment class


    List<PhotoComment> listComments;

    public photoComments (String filename){


        listComments = readFile(filename);

        Collections.sort(listComments);



    }

    public String getComment (String filename){

        PhotoComment findPhoto = new PhotoComment(filename);

        int index = Collections.binarySearch(listComments, findPhoto);

       // System.out.println("index: " + index);

        if (index >= 0) {
            findPhoto = listComments.get(index);
           // System.out.println("Found photo comment: " + findPhoto.comment);
        }else{
            findPhoto = new PhotoComment(filename,"");
        }


        return findPhoto.comment;
    }


     private static List<PhotoComment> readFile(String pathToCsv){

        List<PhotoComment> listComments = new ArrayList<>();

        if (!Files.exists(Paths.get(pathToCsv))) {
            System.err.println("No comments file found: " + pathToCsv);
            return listComments;
        }

        String row;
        try (BufferedReader csvReader = new BufferedReader(new InputStreamReader(new FileInputStream(pathToCsv), StandardCharsets.UTF_8))) {

            boolean firstRow = true;
            while ((row = csvReader.readLine()) != null) {
                // Excel saves UTF-8 CSVs with a byte order mark at the start of the file
                if (firstRow && row.startsWith("\uFEFF")) {
                    row = row.substring(1);
                }
                firstRow = false;

                // Split on the first comma only, so comments may contain commas
                String[] data = row.split(",", 2);
                String photo = data[0].trim();
                if (photo.isEmpty()) {
                    continue;
                }
                listComments.add(new PhotoComment(photo, data.length == 2 ? data[1].trim() : ""));
            }

        } catch (java.io.IOException e) {
            System.err.println("Unable to read comments file " + pathToCsv + ": " + e);
        }

        return listComments;
    }
}
