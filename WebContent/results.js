document.getElementById("results").addEventListener("click", function(event) {
    jQuery.ajax({
        method: "GET",
        url: "api/results?result=true",
        success: (resultLink) => window.location.href = resultLink
    });
});