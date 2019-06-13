<?php

include_once 'initandroid.php';

if(isset($_POST['lat']) && isset($_POST['lon'])){
	$latitude = $_POST['lat'];
	$longitude = $_POST['lon'];

	$query_update = "UPDATE tbl_current_location SET lat='$latitude',lon='$longitude'";

	if(mysqli_query($con,$query_update)){
		echo "Success";
	}
	else{
		echo "Failed".mysqli_error($con);
	}

}

else{
	$query = "SELECT * FROM tbl_destintion_location WHERE status='1'";

	$result = mysqli_query($con, $query);

	if(mysqli_num_rows($result)>0){
		$response = array();

		while($row = mysqli_fetch_array($result)){
			array_push($response, array('id' => $row[0], 'lat' => $row[1], 'lon' => $row[2]));
		}

		echo json_encode(array('locations' => $response));
	}
	else{
		echo "";
	}

	
}
?>