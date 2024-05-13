let add_movie = $("#add-movie");

function handleAddMovieResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    let starDetails = jQuery("#movie_details");

    if (resultDataJson["status"] === "success") {
        starDetails.append(resultDataJson["message"]);
        $("#add_star_error_message").text("");
    } else {
        $("#add_star_error_message").text(resultDataJson["message"]);
    }


}
function addMovieForm(formSubmitEvent) {
    console.log("submit employee login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/add-movie", {
            method: "GET",
            // Serialize the login form to the data sent by POST request
            data: add_movie.serialize(),
            success:  handleAddMovieResult,
            error: function(data){
                console.log(data);
            }
        }
    );
}

add_movie.submit(addMovieForm);