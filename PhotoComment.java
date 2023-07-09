public class PhotoComment implements Comparable<PhotoComment> {
    String filename;
    String comment;

    public PhotoComment(String filename) {
        if (!(filename == null)) {
            this.filename = filename;
            this.comment = ""; // assign an empty string to prevent NullPointerException
        }
    }

    public PhotoComment(String filename, String comment) {
        if (!(filename == null)) {
            this.filename = filename;
            this.comment = comment != null ? comment : ""; // use the provided comment if it's not null, otherwise use an empty string
        }
    }

    public int compareTo(PhotoComment another) {
        if (another == null || another.filename == null) {
            return 1; // current object is considered greater than the null object
        } 
        if (this.filename == null) {
            return -1; // current object is considered less than the non-null object
        }
        // if none of them are null, compare normally
        return this.filename.compareTo(another.filename);
    }
    
}
