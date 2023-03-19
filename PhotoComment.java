public class PhotoComment implements Comparable<PhotoComment> {
    String filename;
    String comment;

    public PhotoComment(String filename) {
        this.filename = filename;

    }

    public PhotoComment(String filename, String comment) {
        this.filename = filename;
        this.comment = comment;
    }


    public int compareTo(PhotoComment another) {
        return this.filename.compareTo(another.filename);
    }
}
