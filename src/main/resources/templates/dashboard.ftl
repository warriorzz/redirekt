<html lang="en">
<head>
    <title>Redirekt - Dashboard</title>
    <link href="${styleSheet}" rel="stylesheet" type="text/css">
    <link href="${icon}" rel="icon">
</head>
<body>
    ${errorBanner}
    ${successBanner}
    <h1>${markdown}</h1>
    <p>Add some Markdown to your page. Simply upload it right here!</p>
    <form action="/dashboard/markdown" method="post" enctype="multipart/form-data">
        <input type="text" name="name" placeholder="${name}..."/>
        <input type="file" name="file" accept="text/markdown" multiple/>
        <input type="submit" value="Upload" />
    </form>
    <h1>${redirect}</h1>
    <p>Redirect to any given page - fill in here.</p>
    <form action="/dashboard/redirect" method="post">
        <input type="text" name="name" placeholder="Name..." />
        <input type="text" name="value" placeholder="URL..." />
        <input type="submit" value="Redirekt" />
    </form>
    <h1>${file}</h1>
    <p>Share files with a simple link.</p>
    <form action="/dashboard/file" method="post" enctype="multipart/form-data">
        <input type="text" name="name" placeholder="Name..." />
        <input type="file" name="file" accept="*/*" multiple/>
        <input type="submit" value="Upload" />
    </form>
</body>
</html>