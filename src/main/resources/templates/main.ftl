<!DOCTYPE html>
<html lang="en">
<head>
    <title>Redirekt</title>
    <link href="${styleSheet}" rel="stylesheet" type="text/css">
    <link href="${icon}" rel="icon">
</head>
<body>
    <button onclick="login()">${main_login_github}</button>
    <script>
        function login() {
            window.location.href = 'login';
        }
    </script>
</body>
</html>