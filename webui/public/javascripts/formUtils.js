DP2Forms = {
	debug: false,

	forms: [],
	submissionHandlers: {},
	validators: {},
	listeners: {},
	lastReport: {},

	onValidationReport: function(formName, listener, data) {
		if (DP2Forms.debug && window.console && console.log) console.log("adding event listener for validation of "+formName);
		if (!$.isArray(DP2Forms.listeners[formName])) DP2Forms.listeners[formName] = new Array();
		DP2Forms.listeners[formName].push({ fn: listener, data: data});
		if (typeof DP2Forms.lastReport[formName] !== "undefined") listener(lastReport[formName],data);
	},

	startValidation: function(formName, url, fields) {
		if (DP2Forms.debug && window.console && console.log) console.log("starting validation of "+formName);
		DP2Forms.forms.push(formName);
		DP2Forms.validators[formName] = {
			url: url,
			fields: fields,
			lastValidation: 0,
			lastValidationRequestTime: 0,
			interval: setInterval(DP2Forms.scheduleValidation, 5000, formName)
		};
		for (var f in fields) {
			var fieldName = fields[f];
			var field = $("#"+formName+"-"+fieldName);
			field.data("validationValue", field[0].value);
			field.data("lastValueChange", 0);
			field.data("messages", []);
			field.data("messagesText", "");
			field.data("initial", "");
			field.data("state", "");
			field.data("lastValidationPause", 0);


			var group = $("#"+formName+"-"+fieldName+"Group");
			if (group.hasClass("error")) {
				field.data("state", "error");
				field.data("initial",null);
			} else if (group.hasClass("success")) {
				field.data("state", "success");
				field.data("initial",null);
			}
		}
		setTimeout(DP2Forms.scheduleValidation, 500, formName);
	},

	stopValidation: function(formName) {
		if (DP2Forms.debug && window.console && console.log) console.log("stopping validation of "+formName);
		DP2Forms.forms.splice($.inArray(formName,DP2Forms.forms),1)
		clearInterval(DP2Forms.validators[formName].interval);
		delete DP2Forms.validators[formName];
	},

	/** No error will be reported when the value is the same as `initial`
	  * (used for forms that have not been filled yet). */
	validateAsTextField: function(formName, field, initial) {
		var data = {
			formName: formName,
			field: field,
			initial: initial
		};
		if ($("#"+formName+"-"+field).data("initial") !== null)
			$("#"+formName+"-"+field).data("initial",initial);
		$("#"+formName+"-"+field).on('change keyup', data, function(event){
			DP2Forms.scheduleValidation(formName);
		});
		$("#"+formName+"-"+field).on('focus', data, function(event){
			$(this).data("initial",null);
		});
		DP2Forms.onValidationReport(formName, function(form, data){
			// nothing special to do for text fields
		}, data);
	},

	disableButtonOnErrors: function(formName, field) {
		var data = {
			formName: formName,
			field: field
		};
		DP2Forms.onValidationReport(formName, function(form, data){
			var errors = false;
			for (var p in DP2Forms.validators[form.data.formName].fields) {
				var field = DP2Forms.validators[form.data.formName].fields[p];
				if (form.errors.hasOwnProperty(field)) {
					errors = true;
					break;
				}
			}
			if (errors) {
				$("#"+data.formName+"-"+data.field).attr("disabled","");
			} else {
				$("#"+data.formName+"-"+data.field).removeAttr("disabled");
			}
		}, data);
		$("#"+formName+"-"+field).attr("disabled","");
	},

	beforeSubmit: function(formName, handler, data) {
		if (DP2Forms.debug && window.console && console.log) console.log("adding form widget submission handler for "+formName);
		if (!$.isArray(DP2Forms.submissionHandlers[formName])) DP2Forms.submissionHandlers[formName] = new Array();
		DP2Forms.submissionHandlers[formName].push({ fn: handler, data: data});
		if (typeof DP2Forms.lastReport[formName] !== "undefined") handler(data);
	},

	prepareSubmission: function(formName) {
		if ($.isArray(DP2Forms.submissionHandlers[formName])) {
			for (var l in DP2Forms.submissionHandlers[formName]) {
				DP2Forms.submissionHandlers[formName][l].fn(DP2Forms.submissionHandlers[formName][l].data);
			}
		}
		return true;
	},

	scheduleValidation: function(formName) {
		if (typeof DP2Forms.validators[formName] === "undefined" || DP2Forms.validators[formName] === null) {
			if (DP2Forms.debug && window.console && console.log) console.log("can't validate "+formName+" - it's not created yet");
		} else if (typeof DP2Forms.validators[formName].lastValidation === "undefined" || DP2Forms.validators[formName].lastValidation === null || new Date().getTime() - DP2Forms.validators[formName].lastValidation >= 490) {
			DP2Forms._validate(formName);

		} else {
			setTimeout(function(formName){
				if (typeof DP2Forms.validators[formName] === "undefined" || DP2Forms.validators[formName] === null) {
					if (DP2Forms.debug && window.console && console.log) console.log("can't validate "+formName+" - it's not created yet");
				} else if (typeof DP2Forms.validators[formName].lastValidation === "undefined" || DP2Forms.validators[formName].lastValidation === null || new Date().getTime() - DP2Forms.validators[formName].lastValidation >= 490) {
					DP2Forms._validate(formName);
				}
			},510,formName);
		}
	},

	_validate: function(formName) {
		// prepare form fields
		DP2Forms.prepareSubmission(formName);

		// serialize form
		var form = $("#"+formName+"-form").serializeArray();
		var validationRequestTime = new Date().getTime();
		form.push({ name: '_validationRequestTime', value: validationRequestTime });

		for (var f in DP2Forms.validators[formName].fields) {
			var fieldName = DP2Forms.validators[formName].fields[f];
			var field = $("#"+formName+"-"+fieldName);
			if (field.data("validationValue") !== field[0].value) {
				field.data("lastValueChange", validationRequestTime);
				field.data("validationValue", field[0].value);
			}
		}
		DP2Forms.validators[formName].lastValidation = new Date().getTime();
		DP2Forms.validators[formName].validating = true;
		
		// post validation request
		$.post(
			DP2Forms.validators[formName].url,
			$.param(form),
			function(form, textStatus, jqXHR) {
				var now = new Date().getTime();
				if (DP2Forms.validators[form.data.formName].lastValidationRequestTime <= parseInt(form.data._validationRequestTime)) {
					DP2Forms.validators[form.data.formName].lastValidationRequestTime = parseInt(form.data._validationRequestTime);
					DP2Forms.validators[form.data.formName].lastValidation = now;
					DP2Forms.validators[form.data.formName].validating = false;

					// update form field data
					for (var f in DP2Forms.validators[form.data.formName].fields) {
						var fieldName = DP2Forms.validators[form.data.formName].fields[f];
						var field = $("#"+form.data.formName+"-"+fieldName);

						field.data("messages", typeof form.errors[fieldName] !== "undefined" ? form.errors[fieldName] : []);
						if (field[0].value === field.data("initial")) {
							field.data("state","initial");
						} else  if (form.errors.hasOwnProperty(fieldName)) {
							field.data("state","error");
						} else {
							field.data("state","success");
						}
					}

					// external listeners
					if ($.isArray(DP2Forms.listeners[form.data.formName])) {
						for (var l in DP2Forms.listeners[form.data.formName]) {
							DP2Forms.listeners[form.data.formName][l].fn(form, DP2Forms.listeners[form.data.formName][l].data);
						}
					}

					// update form field display
					for (var f in DP2Forms.validators[form.data.formName].fields) {
						var fieldName = DP2Forms.validators[form.data.formName].fields[f];
						var field = $("#"+form.data.formName+"-"+fieldName);

						DP2Forms._updateFieldDisplay(form.data.formName, fieldName);
					}
				}
			},
			"json"
		);
	},

	_updateFieldDisplay: function(formName, fieldName) {
		if (formName === undefined || fieldName === undefined || formName === null || fieldName === null)
			return;
		var field = $("#"+formName+"-"+fieldName);
		var now = new Date().getTime();

		// loading-animations
		if (now - field.data("lastValueChange") > 1000 && field.data("validationValue") === field.get(0).value) {
			field.data("lastValidationPause", now);
		}
		if (now - field.data("lastValidationPause") > 1000) {
			$("#"+formName+"-"+fieldName+"Group").removeClass("success error");
			$("#"+formName+"-"+fieldName+"HelpLoading").show();
			setTimeout(DP2Forms._updateFieldDisplay, 1000, formName, fieldName);
		} else {
			$("#"+formName+"-"+fieldName+"HelpLoading").hide();
		}
		
		// validation messages
		var text = $.isArray(field.data("messages")) ? field.data("messages").join("<br/>") : "";
		if (field.data("messagesText") !== text) {
			field.data("messagesText", text);
			$("#"+formName+"-"+fieldName+"Help").html(text);
		}

		// validation state
		if (field.data("state") === "success") {
			$("#"+formName+"-"+fieldName+"Group").removeClass("error").addClass("success");
		} else if (field.data("state") === "error") {
			$("#"+formName+"-"+fieldName+"Group").removeClass("success").addClass("error");
		} else {
			$("#"+formName+"-"+fieldName+"Group").removeClass("error success");
		}
	}

};
