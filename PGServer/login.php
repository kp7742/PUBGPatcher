<?php
//api url filter
if(strpos($_SERVER['REQUEST_URI'],"login.php") || !isset($_POST['token'])){
    require_once 'Utils.php';
    PlainDie();
}

include 'init.php';

//initialization
$crypter = Crypter::init();
$privatekey = readFileData("Keys/PrivateKey.prk");

function tokenResponse($data){
    global $crypter, $privatekey;
    $data = toJson($data);
    $datahash = sha256($data);
    $acktoken = array(
        "Data" => profileEncrypt($data, $datahash),
        "Sign" => toBase64($crypter->signByPrivate($privatekey, $data)),
        "Hash" => $datahash
    );
    return toBase64(toJson($acktoken));
}

//token data
$token = fromBase64($_POST['token']);
$tokarr = fromJson($token, true);

//Data section decrypter
$encdata = $tokarr['Data'];
$decdata = trim($crypter->decryptByPrivate($privatekey, fromBase64($encdata)));
$data = fromJson($decdata);

//Hash Validator
$tokhash = $tokarr['Hash'];
$newhash = sha256($encdata);

if (strcmp($tokhash, $newhash) == 0) {
    PlainDie();
}

if($maintenance){
    $ackdata = array(
        "Status" => "Failed",
        "MessageString" => "Server is in Maintenance",
        "SubscriptionLeft" => "0",
        "CurrVersion" => $latestver
    );
    PlainDie(tokenResponse($ackdata));
}

//Username Validator
$uname = $data["uname"];
if($uname == null || preg_match("([A-Z0-9]+)", $uname) === 0){
    $ackdata = array(
        "Status" => "Failed",
        "MessageString" => "Invalid Username",
        "SubscriptionLeft" => "0",
        "CurrVersion" => $latestver
    );
    PlainDie(tokenResponse($ackdata));
}

//Password Validator
$pass = $data["pass"];
if($pass == null || !preg_match("([a-zA-Z0-9]+)", $pass) === 0){
    $ackdata = array(
        "Status" => "Failed",
        "MessageString" => "Invalid Password",
        "SubscriptionLeft" => "0",
        "CurrVersion" => $latestver
    );
    PlainDie(tokenResponse($ackdata));
}

$query = $conn->query("SELECT * FROM `tokens` WHERE `Username` = '".$uname."' AND `Password` = '".$pass."'");
if($query->num_rows < 1){
    $ackdata = array(
        "Status" => "Failed",
        "MessageString" => "Username And Password Are Wrong",
        "SubscriptionLeft" => "0",
        "CurrVersion" => $latestver
    );
    PlainDie(tokenResponse($ackdata));
}

$res = $query->fetch_assoc();
if($res["StartDate"] == NULL){
    $query = $conn->query("UPDATE `tokens` SET `StartDate` = CURRENT_TIMESTAMP WHERE `Username` = '".$uname."' AND `Password` = '".$pass."'");
}

if($res["UID"] == NULL){
    $query = $conn->query("UPDATE `tokens` SET `UID` = '".$data["cs"]."' WHERE `Username` = '".$uname."' AND `Password` = '".$pass."'");
} else if($res["UID"] != $data["cs"]) {
    $ackdata = array(
        "Status" => "Failed",
        "MessageString" => "Your Device is Changed",
        "SubscriptionLeft" => "0",
        "CurrVersion" => $latestver
    );
    PlainDie(tokenResponse($ackdata));
}

if(intval($res["Expiry"]) == 0){
    $ackdata = array(
        "Status" => "Failed",
        "MessageString" => "Your Token is Expired",
        "SubscriptionLeft" => "0",
        "CurrVersion" => $latestver
    );
    PlainDie(tokenResponse($ackdata));
}

$ackdata = array(
    "Status" => "Success",
    "MessageString" => "",
    "SubscriptionLeft" => $res["Expiry"],
    "CurrVersion" => $latestver
);
if($data["load"] == 1) {
    $loaderdata = readFileData("Loaders/PUBG.kmods");
    $ackdata = array(
        "Status" => "Success",
        "MessageString" => "",
        "Loader" => toBase64($loaderdata),
        "SubscriptionLeft" => $res["Expiry"],
        "CurrVersion" => $latestver
    );
}
echo tokenResponse($ackdata);