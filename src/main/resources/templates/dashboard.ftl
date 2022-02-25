<html lang="en">
<head>
    <title>Redirekt - ${dashboard_dashboard}</title>
    <link href="${styleSheet}" rel="stylesheet" type="text/css">
    <link href="${icon}" rel="icon">
    <meta charset="UTF-8">
</head>
<body>
    ${errorBanner}
    ${successBanner}
    <h1>${dashboard_markdown}</h1>
    <p>${dashboard_markdown_text}</p>
    <form action="/dashboard/markdown" method="post" enctype="multipart/form-data">
        <input type="text" name="name" placeholder="${dashboard_name}..."/>
        <input type="file" name="file" accept="text/markdown" multiple/>
        <input type="submit" value="${dashboard_upload}" />
    </form>
    <h1>${dashboard_redirect}</h1>
    <p>${dashboard_redirect_text}</p>
    <form action="/dashboard/redirect" method="post">
        <input type="text" name="name" placeholder="${dashboard_name}" />
        <input type="text" name="value" placeholder="${dashboard_url}" />
        <input type="submit" value="${dashboard_redirekt}" />
    </form>
    <h1>${dashboard_file}</h1>
    <p>${dashboard_file_text}</p>
    <form action="/dashboard/file" method="post" enctype="multipart/form-data">
        <input type="text" name="name" placeholder="${dashboard_name}" />
        <input type="file" name="file" accept="*/*" multiple/>
        <input type="submit" value="${dashboard_upload}" />
    </form>
</body>
</html>