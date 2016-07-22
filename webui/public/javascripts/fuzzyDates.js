// based on http://stackoverflow.com/questions/11/calculating-relative-time
function fuzzyDate(date) {
	var SECOND = 1000;
	var MINUTE = 60 * SECOND;
	var HOUR = 60 * MINUTE;
	var DAY = 24 * HOUR;
	var WEEK = 7 * DAY;
	var MONTH = 30 * DAY;
	var YEAR = 365.25 * DAY;

	var now = new Date();
	var delta = now.getTime() - date.getTime();

	if (delta < 0) {
	  return "not yet";
	}
	if (delta < 1 * MINUTE) {
	  var seconds = Math.round(delta / SECOND);
	  return seconds == 0 ? "just now" : seconds == 1 ? "one second ago" : seconds + " seconds ago";
	}
	if (delta < 2 * MINUTE) {
	  return "a minute ago";
	}
	if (delta < 45 * MINUTE) {
	  return Math.round(delta / MINUTE) + " minutes ago";
	}
	if (delta < 90 * MINUTE) {
	  return "an hour ago";
	}
	if (delta < 24 * HOUR) {
	  return Math.round(delta / HOUR) + " hours ago";
	}
	if (delta < 48 * HOUR) {
	  return "yesterday";
	}
	if (delta < 7 * DAY) {
	  return Math.round(delta / DAY) + " days ago";
	}
	if (delta < 30 * DAY) {
	  var weeks = Math.round(delta / WEEK);
	  return weeks <= 1 ? "one week ago" : weeks + " weeks ago";
	}
	if (delta < 12 * MONTH) {
	  var months = (now.getYear()-date.getYear())*12 + now.getMonth()-date.getMonth();
	  return months <= 1 ? "one month ago" : months + " months ago";
	}
	else {
	  var years = Math.round(delta / YEAR);
	  return years <= 1 ? "one year ago" : years + " years ago";
	}
}

/** Start a bunch of setTimeouts and setInterval that updates the timer text to something nice and readable. */
function updateFuzzy(id, date) {
	var delta, timeoutIDs, intervalIDs;
	
	// stop any previous timeouts
	clearFuzzyUpdates(id);
	
	// now
	date = date instanceof Date ? date.getTime() : typeof date === "number" ? date : 99999999999999;
	document.getElementById(id).innerHTML = fuzzyDate(new Date(date));
	
	// every second the first minute
	timeoutIDs = [];
	var delta = new Date().getTime() - date;
	if (0 <= delta && delta < 60000) {
		for (var t = 1000; t < 60000; t += 1000) {
			timeoutIDs.push(
				window.setTimeout(function(){
					var e = document.getElementById(id);
					if (e !== undefined && e !== null) {
						e.innerHTML = fuzzyDate(new Date(date));
					}
				}, t)
			);
		}
	}
	document.getElementById(id).setAttribute("data-timeout-ids", timeoutIDs.join(" "));
	
	// every minute
	intervalIDs = [];
	intervalIDs.push(
		window.setInterval(function(){
			var e = document.getElementById(id);
			if (e !== undefined && e !== null) {
				e.innerHTML = fuzzyDate(new Date(date));
			}
		}, 60000)
	);
	document.getElementById(id).setAttribute("data-interval-ids", intervalIDs.join(" "));
}

function clearFuzzyUpdates(id) {
	var timeoutIDsString, intervalIDsString, timeoutIDs, intervalIDs, i;
	
	timeoutIDsString = document.getElementById(id).getAttribute("data-timeout-ids");
	timeoutIDs = typeof timeoutIDsString === "string" ? timeoutIDsString.split(" ") : [];
	for (i = 0; i < timeoutIDs.length; i++) {
		window.clearTimeout(timeoutIDs[i]);
	}
	document.getElementById(id).setAttribute("data-timeout-ids", "");
	
	intervalIDsString = document.getElementById(id).getAttribute("data-interval-ids");
	intervalIDs = typeof intervalIDsString === "string" ? intervalIDsString.split(" ") : [];
	for (i = 0; i < intervalIDs.length; i++) {
		window.clearInterval(intervalIDs[i]);
	}
	document.getElementById(id).setAttribute("data-interval-ids", "");
}