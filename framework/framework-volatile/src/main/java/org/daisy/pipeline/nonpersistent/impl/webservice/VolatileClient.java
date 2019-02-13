package org.daisy.pipeline.nonpersistent.impl.webservice;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;

public final class VolatileClient implements Client {


    private String id;
    private String secret;
    private Role role;

    private String contactInfo;

    private Priority priority;

    public VolatileClient(String id, String secret, Role role,
            String contactInfo, Priority priority) {
        this.id = id;
        this.secret = secret;
        this.role = role;
        this.contactInfo = contactInfo;
        this.priority = priority;
    }

    /**
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the secret
     */
    @Override
    public String getSecret() {
        return secret;
    }

    /**
     * @param secret the secret to set
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * @return the contactInfo
     */
    @Override
    public String getContactInfo() {
        return contactInfo;
    }

    /**
     * @param contactInfo the contactInfo to set
     */
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    /**
     * @return the priority
     */
    @Override
    public Priority getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public Role getRole() {
        return this.role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Client [id=" + getId() + "; role=" + getRole() + "]";
    }
}
