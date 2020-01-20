<?php
include 'DB.php';
include 'Global.php';

if($maintenance == false){
	$conn->query("DELETE FROM `tokens` WHERE `Expiry` = 0 AND `EndDate` IS NOT NULL");
}