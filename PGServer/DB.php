<?php
//api url filter
if(strpos($_SERVER['REQUEST_URI'],"DB.php")){
    require_once 'Utils.php';
    PlainDie();
}

$conn = new mysqli("localhost", "zrzhmnff", "ir7VA5V40-ut)T", "zrzhmnff_tokenserver");
if($conn->connect_error != null){
    die($conn->connect_error);
}