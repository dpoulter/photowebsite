# photowebsite
Java application to generate the Bootstrap HTML content that can be used in website code to display a collection of photos as thumbnails. The application reads the photos in each subfolder of a specified folder, together with that subfolder's `photoComments.csv`, and displays the photos with their comments in a grid. Each subfolder is created as a separate PHP web page (`photos <subfolder>.php`), written to the current directory.

Requires Java 10 or later.

The Main method takes 1 argument: FolderPath

For eg:
```
javac *.java
java Main "/Users/dalepoulter/Pictures/"
```

## photoComments.csv

One line per photo: the filename, a comma, then the comment. Commas in the comment are fine. Photos without a line get an empty caption.

```
BeachHoliday1.jpg,First day of our beach holiday, sunny and warm
```

## Website requirements

- Images are linked from `/images/<subfolder>/<filename>`.
- Each page includes `header.html`, which must load the Bootstrap 4.6.2 CSS to match the Bootstrap 4.6.2 JavaScript the pages use:

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css" integrity="sha384-xOolHFLEh07PJGoPkLv1IbcEPTNtaed2xpHsD9ESMhqIYd0nLMwNLD69Npy4HI+N" crossorigin="anonymous">
```
