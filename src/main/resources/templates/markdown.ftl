<!DOCTYPE html>
<html lang="en">
<head>
    <title>${title}</title>
    <link href="${styleSheet}" rel="stylesheet" type="text/css">
    <link href="${icon}" rel="icon">
    <meta charset="UTF-8">
    <style>
        .markdown-body {
            box-sizing: border-box;
            min-width: 200px;
            max-width: 980px;
            margin: 0 auto;
            padding: 45px;
        }

        @media (max-width: 767px) {
            .markdown-body {
                padding: 15px;
            }
        }
    </style>
</head>
<body class="markdown-body">
${content}
</body>
</html>