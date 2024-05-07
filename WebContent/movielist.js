var page = 1;
var decreasing = false;
var increasing = false;
var update = false;
document.addEventListener('DOMContentLoaded', function () {
    var num_movies = document.getElementById('num_movies');
    if (localStorage['num_movies']) { // if job is set
        num_movies.value = localStorage['num_movies']; // set the value
    }
    num_movies.onchange = function () {
        localStorage['num_movies'] = this.value; // change localStorage on change
    };

    var sorting = document.getElementById('sorting');
    if (localStorage['sorting']) { // if job is set
        sorting.value = localStorage['sorting']; // set the value
    }
    sorting.onchange = function () {
        localStorage['sorting'] = this.value; // change localStorage on change
    }

    document.getElementById('previous').addEventListener('click', function () {
        // page -= 1;

        decreasing = true;
        updateForm();
        decreasing = false;
    });

    document.getElementById('next').addEventListener('click', function () {
        // page += 1;

        increasing = true;
        updateForm();
        increasing = false;
    });

    setting_parameters();
});

function updateForm() {
    let pageElement = jQuery("#page_number");
    pageElement.empty();
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();

    setting_parameters();
}

function diffLayout() {
    // var elements = document.getElementById("update").elements;
    // var obj ={};
    // for(var i = 0 ; i < elements.length ; i++){
    //     var item = elements.item(i);
    //     obj[item.name] = item.value;
    // }
    //
    // var str = window.location.search
    // str = replaceQueryParam('num_movies', obj["num_movies"], str);
    // str = replaceQueryParam('sort_method', obj["sort_method"], str);
    // window.history.pushState(null,"", "movielist.html" + str);
    update = true;
    page = 1;
    updateForm();
    update = false;
}

function handleCartInfo(movieId, movieTitle) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    // cartEvent.preventDefault();

    $.ajax("api/cart", {
        method: "POST",
        data: {"movie_id": movieId, "movie_title": movieTitle},
    });
    document.getElementById("overlay").style.display = "block";
    document.getElementById("popupDialog").style.display = "block";
}

function closeFn() {
    document.getElementById("overlay").style.display = "none";
    document.getElementById("popupDialog").style.display = "none";
}



function making_url() {
    const parameters_array = [];

    // Get parameters from URL
    let results = getParameterByName("results");
    if (results !== null && results.length !== 0) {

    }

    let title = getParameterByName('title');
    if (title !== null && title.length !== 0) {
        parameters_array.push("title=" + title);
    }

    let year = getParameterByName('year');
    if (year !== null && year.length !== 0) {
        parameters_array.push("year=" + year);
    }

    let director = getParameterByName('director');
    if (director !== null && director.length !== 0) {
        parameters_array.push("director=" + director);
    }

    let star = getParameterByName('star');
    if (star !== null && star.length !== 0) {
        parameters_array.push("star=" + star);
    }

    let char = getParameterByName('char');
    if (char !== null && char.length !== 0) {
        parameters_array.push("char=" + char);
    }

    let genre = getParameterByName('genre');
    if (genre !== null && genre.length !== 0) {
        parameters_array.push("genre=" + genre);
    }

    if (update) {
        parameters_array.push("sort_method=" + document.getElementById("sorting").value.toString());
        parameters_array.push("num_movies=" + document.getElementById("num_movies").value.toString());
    } else {
        let sort_method = getParameterByName("sort_method");
        if (sort_method !== null && sort_method.length !== 0) {
            parameters_array.push("sort_method=" + sort_method);
        } else {
            parameters_array.push("sort_method=" + document.getElementById("sorting").value.toString());
        }

        let num_movies = getParameterByName("num_movies");
        if (num_movies !== null && num_movies.length !== 0) {
            parameters_array.push("num_movies=" + num_movies);
        } else {
            parameters_array.push("num_movies=" + document.getElementById("num_movies").value.toString());
        }
    }

    if (increasing) {
        parameters_array.push("page=" + (page + 1).toString());
    } else if (decreasing) {
        parameters_array.push("page=" + (page - 1).toString());
    } else if (update) {
        parameters_array.push("page=1");
    } else {
        let page_number = getParameterByName("page");
        if (page_number !== null && page_number.length !== 0) {
            // page = parseInt(page_number);
            parameters_array.push("page=" + page_number);
        } else {
            parameters_array.push("page=" + page);
        }
    }

    let parameters = "";
    let flag = 0;
    for (let i = 0; i < parameters_array.length; i++) {
        if (flag) {
            parameters += "&" + parameters_array[i];
        } else {
            parameters += "?" + parameters_array[i];
            flag += 1;
        }
    }
    return parameters;
}
function setting_parameters() {

    let making = making_url();
    let final_url = "api/movies" + making;
    let push_url = "movielist.html" + making;
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: final_url, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
    window.history.pushState(null,"", push_url);
}

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // document.querySelector("#previous").disabled = false;
    // document.querySelector("#next").disabled = false;

    let num_movies = document.getElementById("num_movies");
    let pageElement = jQuery("#page_number");

    if (resultData[0]["page"] !== null && resultData[0]["page"].length !== 0) {
        pageElement.append(resultData[0]["page"].toString());
        page = parseInt(resultData[0]["page"]);
    } else {
        pageElement.append("1");
        page = 1;
    }

    document.querySelector("#previous").disabled = page === 1;

    console.log("next disabled?" + resultData.length <= parseInt(num_movies.value));
    console.log("rdlength: " + resultData.length + " parseint: " + parseInt(num_movies.value));
    document.querySelector("#next").disabled = resultData.length <= parseInt(num_movies.value);

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(num_movies.value, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' +
            resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        const list_of_genres = resultData[i]["movie_genres"].split(",");

        rowHTML += "<th>";
        for (let j = 0; j < Math.min(3, list_of_genres.length); j++) {
            rowHTML += '<a href="movielist.html?genre=' + list_of_genres[j] + '">'
            + list_of_genres[j] + '</a>';
            if (j !== (Math.min(3, list_of_genres.length) - 1)) {
                rowHTML += ", "
            }
        }
        rowHTML += "</th>";


        const list_of_stars = resultData[i]["movie_stars"].split(",");
        const list_of_stars_ids = resultData[i]["stars_ids"].split(",");

        rowHTML += "<th>"
        for (let j = 0; j < Math.min(3, list_of_stars.length); j++) {
            rowHTML += '<a href="single-star.html?id=' + list_of_stars_ids[j] + '">'
            + list_of_stars[j] + '</a>';
            if (j !== (Math.min(3, list_of_stars.length) - 1)) {
                rowHTML += ", "
            }
        }
        rowHTML += "</th>";

        let movie_rating = "N/A";
        if (resultData[i]["movie_rating"] !== null) {
            movie_rating = resultData[i]["movie_rating"]
        }
        rowHTML += "<th>" + movie_rating + "</th>";

        rowHTML += "<th>"+ "<input type='button' value='Add' onclick='handleCartInfo(\"" + resultData[i]["movie_id"] +
            "\", \""+  resultData[i]["movie_title"] + "\");' />" + "</th>"

        rowHTML += "</tr>";
        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}