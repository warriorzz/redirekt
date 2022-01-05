<html lang="en">
<head>
    <title>Upload</title>
    <style></style>
</head>
<body>
    <h1>Upload</h1>
    <form action="/upload" method="post" enctype="multipart/form-data">
        <input type="file" name="file" multiple/>
        <input type="submit" value="Upload" />
    </form>
</body>
</html>