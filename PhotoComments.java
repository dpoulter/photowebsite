import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PhotoComments {

    //Read the comments from the input file which is a file containing a list of filenames and corresponding comments
    //of that filename  eg.  BeachHoliday1.jpg,First day of our beach holiday

    private final Map<String, String> comments;

    public PhotoComments(String filename) {
        comments = readFile(filename);
    }

    // Returns the comment for a photo, or an empty string if it has none
    public String getComment(String filename) {
        return comments.getOrDefault(filename, "");
    }

    private static Map<String, String> readFile(String pathToCsv) {

        Map<String, String> comments = new HashMap<>();

        if (!Files.exists(Paths.get(pathToCsv))) {
            System.err.println("No comments file found: " + pathToCsv);
            return comments;
        }

        String row;
        try (BufferedReader csvReader = new BufferedReader(new InputStreamReader(new FileInputStream(pathToCsv), StandardCharsets.UTF_8))) {

            boolean firstRow = true;
            while ((row = csvReader.readLine()) != null) {
                // Excel saves UTF-8 CSVs with a byte order mark at the start of the file
                if (firstRow && row.startsWith("﻿")) {
                    row = row.substring(1);
                }
                firstRow = false;

                // Split on the first comma only, so comments may contain commas
                String[] data = row.split(",", 2);
                String photo = data[0].trim();
                if (photo.isEmpty()) {
                    continue;
                }
                String comment = data.length == 2 ? data[1].trim() : "";
                if (comments.put(photo, comment) != null) {
                    System.err.println("Duplicate entry for " + photo + " in " + pathToCsv + "; using the last one");
                }
            }

        } catch (IOException e) {
            System.err.println("Unable to read comments file " + pathToCsv + ": " + e);
        }

        return comments;
    }
}
