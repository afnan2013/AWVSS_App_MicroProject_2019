<?php

include_once 'initandroid.php';

if(isset($_POST['lat']) && isset($_POST['lon'])){
	$latitude = $_POST['lat'];
	$longitude = $_POST['lon'];

	$query_update = "UPDATE tbl_destintion_location SET status='0'";
	if(mysqli_query($con,$query_update)){
		echo "";
	}
	else{
		echo "Failed".mysqli_error($con);
	}

	$query_insert = "INSERT INTO tbl_destintion_location (lat, lon, status) VALUES ('$latitude', '$longitude', '1')";

	if(mysqli_query($con,$query_insert)){
		echo "Success";
	}
	else{
		echo "Failed".mysqli_error($con);
	}

}

else{
	$query = "SELECT * from tbl_current_location";

	$result = mysqli_query($con, $query);

	$response = array();

	while($row = mysqli_fetch_array($result)){
		array_push($response, array('id' => $row[0], 'lat' => $row[1], 'lon' => $row[2]));
	}

	echo json_encode(array('locations' => $response));
}

?>