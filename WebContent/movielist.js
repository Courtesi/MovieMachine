var page = 1;

document.addEventListener('DOMContentLoaded', function () {
    console.log("cum");
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

        console.log("WHYY");

        page -= 1;

        updateForm();
    });

    document.getElementById('next').addEventListener('click', function () {

        console.log("cumnext");

        page += 1;

        updateForm();
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

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
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

    if (resultData.length <= parseInt(num_movies.value)) {
        document.querySelector("#next").disabled = true;
    }

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
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

function setting_parameters() {
    const parameters_array = [];

// Get parameters from URL
    let results = getParameterByName("results");
    if (results !== null && results.length !== 0) {
        console.log("api/movies?results=true")
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/movies?results=true", // Setting request url, which is mapped by StarsServlet in Stars.java
            success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
        return;
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

    let page_number = getParameterByName("page");
    if (page_number !== null && page_number.length !== 0) {
        page = parseInt(page_number);
        parameters_array.push("page=" + page_number);
    } else {
        parameters_array.push("page=" + page);
    }

    let temp_results = getParameterByName("results");
    if (temp_results !== null && temp_results.length !== 0) {
        parameters_array.push("results=" + temp_results);
    }

    let final_url = "api/movies";
    let flag = 0;
    for (let i = 0; i < parameters_array.length; i++) {
        if (flag) {
            final_url += "&" + parameters_array[i];
        } else {
            final_url += "?" + parameters_array[i];
            flag += 1;
        }
    }

    console.log(final_url)
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: final_url, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}