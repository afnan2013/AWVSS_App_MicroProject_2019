<?php

$server = "localhost";
$user = "root";
$pass = "";
$db = "mapdb";

$con = mysqli_connect($server,$user,$pass, $db);

if($con){
	echo "";
}
else{
	echo "Connection failed";
}

?>