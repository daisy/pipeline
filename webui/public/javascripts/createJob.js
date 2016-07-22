var Job = {
	// properties
	debug: false,
	uploads: [],
	filesetTypes: [],
	filesetTypeDescriptions: {},
    jobId: null,

	// listeners
	uploadListeners: [],
	validators: [],
	filesetTypeListeners: [],
	setFromTemplateHandlers: [],
	
	// private properties
	_filesetTypeDeterminator: undefined,
	
	onNewUpload: function(listener) {
		Job.uploadListeners.push(listener);
		for (var u = 0; u < Job.uploads.length; u++) {
			listener(Job.uploads[u].fileset);
		}
	},

	onFilesetTypeUpdate: function(listener) {
		Job.filesetTypeListeners.push(listener);
		var filesets = {};
		for (var i = 0; i < Job.filesetTypes.length; i++) {
			var id = Job.filesetTypes[i];
			filesets[id] = {
				name: Job.filesetTypeDescriptions[id].name
			};
		}
		listener(filesets);
	},
	
	onValidate: function(listener) {
		Job.validators.push(listener);
	},
	
	submit: function() {
		return true;
	},

	upload: function(fileset) {
		for (var u = 0; u < Job.uploadListeners.length; u++) {
			Job.uploadListeners[u](fileset);
		}
	},

	prettySize: function(bytes) {
		if (bytes < 1000) return bytes+" B";
		if (bytes < 1000000) return (Math.round(bytes/100)/10).toLocaleString()+" kB";
		if (bytes < 1000000000) return (Math.round(bytes/100000)/10).toLocaleString()+" MB";
		if (bytes < 1000000000000) return (Math.round(bytes/100000000)/10).toLocaleString()+" GB";
		return (Math.round(bytes/100000000000)/10).toLocaleString()+" TB";
	},

	determineFilesetType: function() {
		for (var id in Job.filesetTypeDescriptions) {
			var desc = Job.filesetTypeDescriptions[id];
			if (Job._determineFilesetType_both(desc.requirements)) {
				Job.filesetTypes.push(id);
			}
		}
		Job.filesetTypes = $.unique(Job.filesetTypes);

		var filesets = {};
		for (var i = 0; i < Job.filesetTypes.length; i++) {
			var id = Job.filesetTypes[i];
			filesets[id] = {
				name: Job.filesetTypeDescriptions[id].name
			};
		}
		for (var l = 0; l < Job.filesetTypeListeners.length; l++) {
			Job.filesetTypeListeners[l](filesets);
		}
	},
	_determineFilesetType_both: function(tests) {
		for (var t = 0; t < tests.length; t++) {
			var test = tests[t];
			if ('either' in test && Job._determineFilesetType_either(test.either) === false) return false;
			if ('both' in test && Job._determineFilesetType_both(test.both) === false) return false;
			if (Job._determineFilesetType_test(test) === false) return false;
		}
		return true;
	},
	_determineFilesetType_either: function(tests) {
		for (var t = 0; t < tests.length; t++) {
			var test = tests[t];
			if ('either' in test && Job._determineFilesetType_either(test.either) === true) return true;
			if ('both' in test && Job._determineFilesetType_both(test.both) === true) return true;
			if (Job._determineFilesetType_test(test) === true) return true;
		}
		return false;
	},
	_determineFilesetType_test: function(test) {
		for (var u = 0; u < Job.uploads.length; u++) {
			for (var f = 0; f < Job.uploads[u].fileset.length; f++) {
				var file = Job.uploads[u].fileset[f];
				var foundMatchingFile = true;
				for (var property in test) {
					var restriction = test[property];
					if (restriction instanceof RegExp) {
						foundMatchingFile = foundMatchingFile && restriction.test(file[property]);
					} else {
						foundMatchingFile = foundMatchingFile && (restriction === file[property]);
					}
				}
				if (foundMatchingFile) return true;
			}
		}
		return false;
	}
};

/* Update the Job object when push notifications of type "uploads" arrive */
Notifications.listen("uploads", function(notification) {
    if (notification.jobId !== Job.jobId) {
        return;
    }
	Job.uploads.push(notification);
	for (var u = 0; u < Job.uploadListeners.length; u++) {
		Job.uploadListeners[u](notification.fileset);
	}
	clearTimeout(Job._filesetTypeDeterminator);
	Job._filesetTypeDeterminator = setTimeout(Job.determineFilesetType,100);
});

Job.filesetTypeDescriptions["daisy202"] = {
	name: "DAISY 2.02",
	requirements: [
		{ fileName: new RegExp("(^|/)ncc\\.html$","i") }
	]
};

Job.filesetTypeDescriptions["daisy3"] = {
	name: "DAISY 3",
	requirements: [
		{ contentType: "application/x-dtbncx+xml" },
		{ fileName: new RegExp("(^|/).*\\.smil$","i") }
	]
};

Job.filesetTypeDescriptions["dtbook"] = {
	name: "DTBook",
	requirements: [
		{ contentType: "application/x-dtbook+xml" }
	]
};

Job.filesetTypeDescriptions["nimas"] = Job.filesetTypeDescriptions["dtbook"];

Job.filesetTypeDescriptions["zedai"] = {
	name: "ZedAI",
	requirements: [
		{ contentType: "application/z3998-auth+xml" }
	]
};

Job.filesetTypeDescriptions["epub"] = {
	name: "EPUB",
	requirements: [
		{ either: [
			{ contentType: "application/epub+zip" },
			{ both: [
				{ contentType: "application/oebps-package+xml" },
				{ either: [
					{ contentType: "text/html" },
					{ contentType: "application/xhtml+xml" }
				] }
			]}
		] }
	]
};

Job.filesetTypeDescriptions["epub3"] = Job.filesetTypeDescriptions["epub"];

Job.filesetTypeDescriptions["audio"] = {
	name: "Audio",
	requirements: [
		{ contentType: new RegExp("^audio/","i") }
	]
};

Job.filesetTypeDescriptions["mp3"] = {
	name: "Audio",
	requirements: [
	    { either: [
			{ contentType: "audio/mpeg" },
			{ contentType: "audio/mp3" }
		]}
	]
};

Job.filesetTypeDescriptions["html"] = {
	name: "HTML",
	requirements: [
	    { either: [
			{ contentType: "text/html" },
			{ contentType: "application/xhtml+xml" }
		]}
	]
};

Job.filesetTypeDescriptions["mathml"] = {
	name: "Math ML",
	requirements: [
	    { either: [
			{ contentType: "application/mathml+xml" },
	    	{ contentType: "application/mathml-presentation+xml" },
			{ contentType: "application/mathml-content+xml" }
		]}
	]
};

Job.filesetTypeDescriptions["odt"] = {
	name: "Math ML",
	requirements: [
	    { either: [
			{ contentType: "application/mathml+xml" },
	    	{ contentType: "application/mathml-presentation+xml" },
			{ contentType: "application/mathml-content+xml" }
		]}
	]
};

Job.filesetTypeDescriptions["odt"] = { name: "OpenDocument Text",            requirements: [ { contentType: "application/vnd.oasis.opendocument.text" } ] };
Job.filesetTypeDescriptions["ods"] = { name: "OpenDocument Spreadsheet",     requirements: [ { contentType: "application/vnd.oasis.opendocument.spreadsheet" } ] };
Job.filesetTypeDescriptions["odp"] = { name: "OpenDocument Presentation",    requirements: [ { contentType: "application/vnd.oasis.opendocument.presentation" } ] };
Job.filesetTypeDescriptions["odg"] = { name: "OpenDocument Drawing",         requirements: [ { contentType: "application/vnd.oasis.opendocument.graphics" } ] };
Job.filesetTypeDescriptions["odc"] = { name: "OpenDocument Chart",           requirements: [ { contentType: "application/vnd.oasis.opendocument.chart" } ] };
Job.filesetTypeDescriptions["odf"] = { name: "OpenDocument Formula",         requirements: [ { contentType: "application/vnd.oasis.opendocument.formula" } ] };
Job.filesetTypeDescriptions["odi"] = { name: "OpenDocument Image",           requirements: [ { contentType: "application/vnd.oasis.opendocument.image" } ] };
Job.filesetTypeDescriptions["odm"] = { name: "OpenDocument Master Document", requirements: [ { contentType: "application/vnd.oasis.opendocument.text-master" } ] };

Job.filesetTypeDescriptions["pef"] = {
	name: "Portable Embosser Format (PEF)",
	requirements: [
		{ contentType: "application/x-pef+xml" }
	]
};

Job.filesetTypeDescriptions["ssml"] = {
	name: "Speech Synthesis Markup Language (SSML)",
	requirements: [
		{ contentType: "application/ssml+xml" }
	]
};

Job.filesetTypeDescriptions["text"] = {
	name: "Plain Text",
	requirements: [
		{ contentType: "text/plain" }
	]
};
