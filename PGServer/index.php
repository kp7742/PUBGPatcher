<?php
require_once 'init.php';
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=no">
    <title>KMODs</title>
    <link rel="stylesheet" href="assets/bootstrap/css/bootstrap.min82ed.css">
    <link rel="stylesheet" href="assets/css/styles.minaa01.css">
</head>
<body>
    <div class="container">
        <div class="jumbotron" style="margin: 10px;">
            <h1>PUBGPatcher Server</h1><br>
			<a class="btn btn-primary shadow-lg" role="button" href="<?php echo $latestverlink; ?>">Root (<?php echo $latestver; ?>)</a>
            <div class="alert alert-success status" role="alert" style="background-color: rgb(249,249,249);margin: 50px 0 0 0;">
                <h1 style="font-size: 30px;">Changelog</h1>
                <p class="status-item"><strong>Crash Reporting Changed</strong></p>
            </div>
            <div class="alert alert-success status" role="alert" style="background-color: rgb(249,249,249);margin: 50px 0 0 0;">
                <h1 style="font-size: 30px;">Server Status</h1>
                <p class="status-item">Root: <strong>✅ ESP Safe</strong></p>
                <p class="status-item">Time: <strong>⚠<?php echo date(DATE_COOKIE)."<br>";?></strong></p>
                <p class="status-item"><strong><?php echo $maintenance ? "Current Status: <font color='red'>Maintenance</font>" : "Current Status: <font color='green'>Live</font>"."<br>";?></strong></p>
            </div>
			<div class="alert alert-success status" role="alert" style="background-color: rgb(249,249,249);margin: 50px 0 0 0;">
                <h1 style="font-size: 30px;">Free One Day Keys</h1>
				<?php
					$query = $conn->query("SELECT * FROM `tokens` WHERE `StartDate` IS NULL");
					if($query->num_rows < 1){
						echo '<p class="status-item"><strong>No Free Keys Available</strong></p>';
					} else {
						$res = $query->fetch_all(MYSQLI_ASSOC);
						for ($i = 0; $i < $query->num_rows; $i++) {
							echo '<p class="status-item"><strong>Username: ' . $res[$i]["Username"] . "<br>Password: " . $res[$i]["Password"] . "</strong></p>";
						}
					}
				?>
            </div>
        </div>
    </div>
    <script src="assets/js/jquery.min0392.js"></script>
    <script src="assets/bootstrap/js/bootstrap.mine98d.js"></script>
</body>
</html>