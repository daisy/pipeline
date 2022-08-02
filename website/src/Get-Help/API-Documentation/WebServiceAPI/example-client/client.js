window.onload = function() {
	let ws = document.getElementById("ws-url");
	let clientkey = document.getElementById("clientkey");
	let clientsecret = document.getElementById("clientsecret");
	let jobsList = document.getElementById("jobs");
	let messagesList = document.getElementById("messages");
	let parseXml = function(text) {
		if (window.DOMParser) {
			return new DOMParser().parseFromString(text, "text/xml");
		} else if (window.ActiveXObject) {
			let xml = new ActiveXObject("Microsoft.XMLDOM");
			xml.async = false;
			if (!xml.loadXML(text))
				throw xml.parseError.reason + " " + xml.parseError.srcText;
			return xml;
		}
	};
	let evaluateXPath = function(expression, context) {
		return new XPathEvaluator().evaluate(expression,
											 context,
											 null,
											 XPathResult.ANY_TYPE,
											 null);
	};
	let asyncGET = function(url, callback) {
		let xmlHttpReq = new XMLHttpRequest();
		xmlHttpReq.onreadystatechange = function() {
			if (xmlHttpReq.readyState == 4 && xmlHttpReq.status == 200) {
				callback(xmlHttpReq.responseText);
			}
		}
		xmlHttpReq.open("GET", url, true);
		xmlHttpReq.send(null);
	};
	let withAuthentication = function(callback) {
		asyncGET(ws.value + "/alive", function(responseText) {
			callback(evaluateXPath("/*/@authentication='true'", parseXml(responseText)).booleanValue);
		});
	};
	document.getElementById("refresh-button").onclick = function() {
		jobsList.innerHTML = "";
		messagesList.innerHTML = "";
		withAuthentication(function(authentication) {
			asyncGET(ws.value + "/jobs", function(responseText) {
				let jobs = parseXml(responseText).documentElement.children;
				for (let i = 0; i < jobs.length; i++) {
					let job = document.createElement("li");
					let id = document.createElement("div");
					id.innerHTML = "ID: <code>" + evaluateXPath("string(@id)", jobs[i]).stringValue + "</code>";
					job.appendChild(id);
					let status = document.createElement("div");
					status.innerHTML = "Status: <code>" + evaluateXPath("string(@status)", jobs[i]).stringValue + "</code>";
					job.appendChild(status);
					let showMessages = document.createElement("div");
					let button = document.createElement("button");
					button.innerHTML = "Show messages";
					let notificationsURL = evaluateXPath("string(@notifications)", jobs[i]).stringValue;
					button.onclick = function() {
						messagesList.innerHTML = "";
						let socket = new WebSocket(notificationsURL);
						socket.onmessage = function(event) {
							let message = document.createElement("li");
							message.textContent = event.data;
							messagesList.append(message);
						};
					};
					showMessages.appendChild(button);
					job.appendChild(showMessages);
					jobsList.appendChild(job);
				}				
			});
		});
	};
};
