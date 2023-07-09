import java.io.BufferedReader;
import java.io.FileReader;
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

        BufferedReader csvReader = null;
        String row;
        try {
            csvReader = new BufferedReader(new FileReader(pathToCsv));

            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                // do something with the data
                if (data.length >= 2) {
                    // Only add to list if there are at least two elements after splitting
                    listComments.add(new PhotoComment(data[0], data[1]));
                } else if (data.length == 1) {
                    listComments.add(new PhotoComment(data[0], ""));
                }
            }
            
            csvReader.close();

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return listComments;
    }
}
