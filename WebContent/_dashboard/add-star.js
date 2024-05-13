let add_star = $("#add-star");

function handleAddStarResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    let starDetails = jQuery("#star_details");

    if (resultDataJson["status"] === "success") {
        starDetails.append(resultDataJson["message"]);
        $("#add_star_error_message").text("");
    } else {
        $("#add_star_error_message").text(resultDataJson["message"]);
    }


}
function addStarForm(formSubmitEvent) {
    console.log("submit employee login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/add-star", {
            method: "GET",
            // Serialize the login form to the data sent by POST request
            data: add_star.serialize(),
            success:  handleAddStarResult,
            error: function(data){
                console.log(data);
            }
        }
    );
}

add_star.submit(addStarForm);