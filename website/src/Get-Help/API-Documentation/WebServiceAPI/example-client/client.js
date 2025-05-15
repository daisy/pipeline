window.onload = function() {
	(function(doc, proto) {
		try { // check if browser supports :scope natively
			doc.querySelector(':scope body');
		} catch (err) { // polyfill native methods if it doesn't
			['querySelector', 'querySelectorAll'].forEach(function(method) {
				var nativ = proto[method];
				proto[method] = function(selectors) {
					if (/(^|,)\s*:scope/.test(selectors)) { // only if selectors contains :scope
						var id = this.id; // remember current element id
						this.id = 'ID_' + Date.now(); // assign new unique id
						selectors = selectors.replace(/((^|,)\s*):scope/g, '$1#' + this.id); // replace :scope with #ID
						var result = doc[method](selectors);
						this.id = id; // restore previous id
						return result;
					} else {
						return nativ.call(this, selectors); // use native code for other selectors
					}
				}
			});
		}
	})(window.document, Element.prototype);
	let ws = document.getElementById("ws-url");
	let clientkey = document.getElementById("clientkey");
	let clientsecret = document.getElementById("clientsecret");
	let nonce = function() {
		return crypto.getRandomValues(new Uint8Array(20)).map(x => x % 10).join("");
	}
	let addAuthenticationQuery = function(url) {
		if (url.includes("?"))
			url += "&";
		else
			url += "?";
		url += "authid=";
		url += clientkey.value;
		url += "&time=";
		url += new Date().toISOString();
		url += "&nonce=";
		url += nonce();
		let hmac; {
			const shaObj = new jsSHA("SHA-1", "TEXT", {
				hmacKey: { value: clientsecret.value, format: "TEXT" },
			});
			shaObj.update(url);
			hmac = shaObj.getHash("B64");
		}
		url += "&sign=";
		url += encodeURIComponent(hmac);
		return url;
	};
	let jobsList = document.getElementById("jobs");
	let messagesList = document.getElementById("messages");
	let messageElements = new Map();
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
			if (xmlHttpReq.readyState == 4) {
				if (xmlHttpReq.status == 200) {
					console.log("response from " + url + ":\n" + xmlHttpReq.responseText);
					callback(xmlHttpReq.responseText);
				} else {
					console.log("failed to get " + url + " (status " + xmlHttpReq.status + ": " + xmlHttpReq.statusText + ")");
				}
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
			let url = ws.value + "/jobs";
			if (authentication)
				url = addAuthenticationQuery(url);
			asyncGET(url, function(responseText) {
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
					if (authentication)
						notificationsURL = addAuthenticationQuery(notificationsURL);
					button.onclick = function() {
						messagesList.innerHTML = "";
						messageElements = new Map();
						let socket = new WebSocket(notificationsURL);
						socket.onerror = function(event) {
							console.log("web socket error:");
							console.log(event);
						}
						socket.onmessage = function(event) {
							let jobXML = new DOMParser().parseFromString(event.data, "application/xml").documentElement;
							let messagesXML = jobXML.getElementsByTagNameNS("http://www.daisy.org/ns/pipeline/data", "messages");
							if (messagesXML.length > 0) {
								messagesXML = messagesXML.item(0);
								let addMessagesRecursively = function(xmlElement, htmlElement) {
									let progress = xmlElement.getAttribute("progress");
									if (progress != null || xmlElement.children.length > 0) {
										let ul = htmlElement.localName == "ul"
											? htmlElement
											: htmlElement.querySelector(":scope > ul");
										if (ul == null)
											ul = htmlElement.appendChild(document.createElement("ul"));
										if (progress != null) {
											ul.setAttribute("data-progress", progress);
											progress = Number.parseFloat(progress);
											ul.style.setProperty("--progress", progress)
										}
										for (const m of xmlElement.children) {
											let id = m.getAttribute("sequence");
											id = "message-" + id;
											let messageElement = messageElements.get(id);
											if (messageElement == null) {
												messageElement = ul.appendChild(document.createElement("li"));
												let span = messageElement.appendChild(document.createElement("span"));
												span.classList.add("text");
												span.textContent = m.getAttribute("content")
												span.addEventListener("click", () => {
													if (messageElement.classList.contains("collapsed"))
														messageElement.classList.replace("collapsed", "expanded");
													else if (messageElement.classList.contains("expanded"))
														messageElement.classList.replace("expanded", "collapsed");
													else
														messageElement.classList.add("collapsed");
												});
												messageElements.set(id, messageElement);
											}
											addMessagesRecursively(m, messageElement);
										}
									}
								};
								addMessagesRecursively(messagesXML, messagesList);
							}
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
