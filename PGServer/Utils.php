<?php

function profileEncrypt($data, $hash){
    $in = myexplode($data);
    $key = myexplode($hash);
    $out = array();
    for ($i = 0; $i < count($in); $i++) {
        $out[$i] = $in[$i] ^ $key[$i % count($key)];
    }
    $out = implode($out);
    return toBase64($out);
}

function profileDecrypt($data, $hash){
    $in = myexplode(fromBase64($data));
    $key = myexplode($hash);
    $out = array();
    for ($i = 0; $i < count($in); $i++) {
        $out[$i] = $in[$i] ^ $key[$i % count($key)];
    }
    return implode($out);
}

function genUname(){
    $seed = myexplode("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    $uname = array();
    $size = 0;
    while($size != 18){
        $uname[$size] = $seed[mt_rand(0,count($seed)-1)];
        $size++;
    }
    return implode($uname);
}

function genPass(){
    $seed = myexplode("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
    $pass = array();
    $size = 0;
    while($size != 8){
        $pass[$size] = $seed[mt_rand(0,count($seed)-1)];
        $size++;
    }
    return implode($pass);
}

function myexplode($str){
    $out = array();
    for ($i = 0; $i < strlen($str); $i++) {
        $out[$i] = substr($str, $i, 1);
    }
    return $out;
}

function getMicro(){
    return explode(' ', microtime())[1];
}

function readFileData($path){
    $file = fopen($path,"r") or die();
    $data = fread($file,filesize($path));
    fclose($file);
    return $data;
}

function isFileExist($path){
    if (file_exists($path)) {
        return 1;
    }
    return 0;
}

function toBase64($data){
    return base64_encode($data);
}

function fromBase64($data){
    return base64_decode($data);
}

function urlsafe_b64encode($string) {
    $data = base64_encode($string);
    $data = str_replace(array('+','/','='),array('-','_',''),$data);
    return $data;
}

function urlsafe_b64decode($string) {
    $data = str_replace(array('-','_'),array('+','/'),$string);
    $mod4 = strlen($data) % 4;
    if ($mod4) {
        $data .= substr('====', $mod4);
    }
    return base64_decode($data);
}

function toJson($data){
    return json_encode($data);
}

function fromJson($data){
    return json_decode($data, true);
}

function PlainDie($status = ""){
    header('Content-type: text/plain');
    die($status);
}

function sha256($data){
    return strtoupper(hash('sha256', $data));
}

function PublicKeyToBinary($publickey){
    $publickey = str_replace("-----BEGIN PUBLIC KEY-----","", $publickey);
    $publickey = str_replace("-----END PUBLIC KEY-----","", $publickey);
    $publickey = trim($publickey);
    return fromBase64($publickey);
}

function PrivateKeyToBinary($privatekey){
    $privatekey = str_replace("-----BEGIN RSA PRIVATE KEY-----","", $privatekey);
    $privatekey = str_replace("-----END RSA PRIVATE KEY-----","", $privatekey);
    $privatekey = trim($privatekey);
    return fromBase64($privatekey);
}

//api url filter
if(strpos($_SERVER['REQUEST_URI'],"Utils.php")){
    PlainDie();
}