---
---
window.addEventListener("load", function() {
	let links = document.getElementsByClassName("download");
	for (let i = 0; i < links.length; i++) {
		links[i].addEventListener("click", function(event) {
			setTimeout(function() {
				window.location.href = "{{site.baseurl}}/Support.html?post-download=true";
			}, 1000);
		});
	}
}
