<?PHP

error_reporting(0); // Don't show errors or warnings, to not pollute console debug
header('Access-Control-Allow-Methods: POST'); // Only accept POST requests
header('Content-type: text/html; charset=UTF-8'); // Always set up unicode, always!

$checkpass = ""; // Your password here
$hashAlgorithm = "sha512"; // Declare the hash type

$receivedHash = $_POST['hash']; // The hash data, will always named 'hash'
// Each argument received from plugin is named as arg<number>, so if you have 2 args, they will be arg0 and arg1
if (isset($_POST['arg0'])):
	$arg0 = $_POST['arg0'];
endif;
if (isset($_POST['arg1'])):
	$arg1 = $_POST['arg1'];
endif;

if($receivedHash != "") {
    if($receivedHash == hash($hashAlgorithm, $checkpass)) {
        print_r("Passwords match! Saving data.\n");
        
        //Put your code here.
        print_r("All saved.");

        //generate json with arg0 and arg1 on server
        $data = array('arg0' => $arg0, 'arg1' => $arg1);
        $json = json_encode($data);
        file_put_contents('data.json', $json);
        //Stop editing here.
    }
    else {
        print_r("Passwords don't match!");
    }
}
else {
    print_r("Password data null! Did you really configured one?");
}

?>
