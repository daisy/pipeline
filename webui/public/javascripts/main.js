
function toggleRadio(idPrefix, state, defaultState) {
	$('#'+idPrefix+'-value').val(state?'true':'false');
	$('#'+idPrefix+'-on').attr('aria-checked',state?'true':'false');
	$('#'+idPrefix+'-off').attr('aria-checked',state?'false':'true');
	if (state) {
		$('#'+idPrefix+'-on').addClass('active');
		$('#'+idPrefix+'-off').removeClass('active');
	} else {
		$('#'+idPrefix+'-on').removeClass('active');
		$('#'+idPrefix+'-off').addClass('active');
	}
	toggleChanged(idPrefix, state?'true':'false', defaultState);
}

function toggleMultiRadio(classPrefix, value, defaultValue) {
	$('#'+classPrefix+'-value').val(value);
	$('.'+classPrefix+'-btn').each(function(index, btn) {
		if ($(btn).val() === value) {
			$(btn).addClass('active');
			$(btn).attr('aria-checked','true');
		} else {
			$(btn).removeClass('active');
			$(btn).attr('aria-checked','false');
		}
	});
	toggleChanged(classPrefix, value, defaultValue);
}

function toggleChanged(idPrefix, value, defaultValue) {
	if (value === defaultValue)
		$('#'+idPrefix+'Group').removeClass("changed");
	else
		$('#'+idPrefix+'Group').addClass("changed");
}

function markdownToHtml(text) {
	var elem = document.createElement('textarea');
	elem.innerHTML = text;
	text = elem.value;
	return marked(text);
}

function argumentsToHtml(arguments) {
	var argumentGroup = null;
	var argumentsHTML = "";
	for (var a = 0; a < arguments.length; a++) {
		var arg = arguments[a];
		if (arg.nicename.split(":").length > 1 && arg.nicename.split(":")[0] !== argumentGroup) {
			if (a > 0) {
				argumentsHTML += "</tbody></table>\n";
			}
			argumentGroup = arg.nicename.split(":",2)[0];
			argumentsHTML += "<h3>"+argumentGroup+"</h3>\n";
			argumentsHTML += "<table class=\"table table-bordered\" style=\"width:auto;\"><tbody>\n";
		} else if (a === 0) {
			argumentsHTML += "<table class=\"table table-bordered\" style=\"width:auto;\"><tbody>\n";
		}
		argumentsHTML += "<tr><th>"+(arg.nicename.split(":",2).length > 1 ? arg.nicename.replace(/^[^:]*: */,'') : arg.nicename)+"</th><td>\n";
		var values = 'asList' in arg ? arg.asList : arg.values;
		if ($.isArray(values) && values.length > 0) {
			for (var v = 0; v < values.length; v++) {
				argumentsHTML += "<code>"+values[v]+"</code>";
				if (v !== values.length - 1) {
					argumentsHTML += "<br/>\n";
				}
			}
		} else {
			argumentsHTML += "<i>(empty)</i>\n";
		}
		argumentsHTML += "</td></tr>\n";
	}
	if (arguments.length > 0) {
		argumentsHTML += "</tbody></table>\n";
	}
	return argumentsHTML;
}
