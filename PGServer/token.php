<?php
//api url filter
if(strpos($_SERVER['REQUEST_URI'],"token.php") || !isset($_GET['secret'])){
    require_once 'Utils.php';
    PlainDie();
}
include 'init.php';
$expiry = 1440;
?>
<!DOCTYPE html>
<html>
<head>
    <title>Generated Tokens</title>
</head>
<body>
<h2>One Day Tokens(10):</h2>
<?php
for ($i = 0; $i < 10; $i++) {
    $uname = genUname();
    $pass = genPass();
    //$conn->query("INSERT INTO `tokens` (`Username`, `Password`, `StartDate`, `EndDate`, `Expiry`) VALUES ('".$uname."', '".$pass."', NULL, NULL, ".$expiry.")");
    echo "Username: " . $uname;
    echo " Password: " . $pass;
    echo "<br>";
}
?>
</body>
</html>
