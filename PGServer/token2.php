<?php
require_once 'init.php';
date_default_timezone_set("Asia/Calcutta");
echo "Time: ".date(DATE_COOKIE);
?>
<!DOCTYPE html>
<html>
<head>
    <title>Tokens</title>
</head>
<body>
<?php
$query = $conn->query("SELECT * FROM `tokens` WHERE `StartDate` IS NULL");
if($query->num_rows < 1){
    echo "<h2>No Tokens Available</h2>";
} else {
    $res = $query->fetch_all(MYSQLI_ASSOC);
    echo "<h2>Available Tokens:</h2>";
    for ($i = 0; $i < $query->num_rows; $i++) {
        echo "Username: " . $res[$i]["Username"];
        echo " Password: " . $res[$i]["Password"];
        echo "<br>";
    }
}
?>
</body>
</html>
