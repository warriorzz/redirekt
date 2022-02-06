<html lang="en">
<head>
    <title>Dashboard</title>
    <link href="${styleSheet}" rel="stylesheet" type="text/css">
</head>
<body>
    ${errorBanner}
    ${successBanner}
    <h1>Upload</h1>
    <form action="/dashboard/markdown" method="post" enctype="multipart/form-data">
        <input type="text" name="name" />
        <input type="file" name="file" accept="text/markdown" multiple/>
        <input type="submit" value="Upload" />
    </form>
    <h1>Redirect</h1>
    <form action="/dashboard/redirect" method="post">
        <input type="text" name="name" />
        <input type="text" name="value" />
        <input type="submit" value="Redirekt" />
    </form>
    <h1>File</h1>
    <form action="/dashboard/file" method="post" enctype="multipart/form-data">
        <input type="text" name="name" />
        <input type="file" name="file" accept="*/*" multiple/>
        <input type="submit" value="Upload" />
    </form>
</body>
</html>