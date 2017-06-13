package org.daisy.pipeline.client.models;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of the "/admin/clients" response from the Pipeline 2 Web Service.
 * 
 * {@code
 * GET /admin/clients
 * <clients href="http://localhost:8181/ws/admin/clients" xmlns="http://www.daisy.org/ns/pipeline/data">
 *     <client id="" href="" secret="" role="" contact=""/>
 * </clients>
 * 
 * GET /admin/clients/{clientId}
 * }
 * 
 * 
 */
public class Client {
	
    public enum Role { ADMIN, CLIENTAPP };
    
    private String id;
    private String href;
    private String secret;
    private Role role;
    private String contactInfo;
    // priority?
    
	private Node clientNode;
	private boolean lazyLoaded = false;
    
	public Client(Node clientNode) {
		this.clientNode = clientNode;
	}
	
	public void lazyLoad() {
		if (!lazyLoaded) {
			try {
				this.id = XPath.selectText("@id", clientNode, XPath.dp2ns);
				if (this.id == null)
					this.id = "";
				
				this.href = XPath.selectText("@href", clientNode, XPath.dp2ns);
				if (this.href == null)
					this.href = "";
				
				this.secret = XPath.selectText("@secret", clientNode, XPath.dp2ns);
				if (this.secret == null)
					this.secret = "";
				
				try {
					this.role = Role.valueOf(XPath.selectText("@role", clientNode, XPath.dp2ns));
				} catch (IllegalArgumentException e) {
					this.role = null;
				} catch (NullPointerException e) {
					this.role = null;
				}
				
				this.contactInfo = XPath.selectText("@contact", clientNode, XPath.dp2ns);
				if (this.contactInfo == null)
					this.contactInfo = "";

			} catch (Pipeline2Exception e) {
				Pipeline2Logger.logger().error("Failed to parse argument node", e);
			}
			lazyLoaded = true;
		}
	}

	public static List<Client> parseClientsXml(Node clientsNode) {
		try {
			// select root element if the node is a document node
			if (clientsNode instanceof Document)
				clientsNode = XPath.selectNode("/d:clients", clientsNode, XPath.dp2ns);

			List<Node> clientNodes;
			clientNodes = XPath.selectNodes("d:client", clientsNode, XPath.dp2ns);

			List<Client> clients = new ArrayList<Client>();
			for (Node clientNode : clientNodes) {
				clients.add(new Client(clientNode));
			}
			
			return clients;
			
		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Failed to parse clients XML", e);
		}
		
		return null;
	}
	
	// getters and setters to ensure lazy loading
	public String getId() { lazyLoad(); return id; }
	public String getHref() { lazyLoad(); return href; }
	public String getSecret() { lazyLoad(); return secret; }
	public Role getRole() { lazyLoad(); return role; }
	public String getContactInfo() { lazyLoad(); return contactInfo; }
	public void setId(String id) { lazyLoad(); this.id = id; }
	public void setHref(String href) { lazyLoad(); this.href = href; }
	public void setSecret(String secret) { lazyLoad(); this.secret = secret; }
	public void setRole(Role role) { lazyLoad(); this.role = role; }
	public void setContactInfo(String contactInfo) { lazyLoad(); this.contactInfo = contactInfo; }

}
