<?php
include 'DB.php';
include 'Global.php';

if($maintenance == false){
	$conn->query("UPDATE `tokens` SET `Expiry` = `Expiry` - 1 WHERE `Expiry` > 0 AND `StartDate` IS NOT NULL");
	$conn->query("UPDATE `tokens` SET `EndDate` = CURRENT_TIMESTAMP WHERE `Expiry` = 0 AND `EndDate` IS NULL");
}